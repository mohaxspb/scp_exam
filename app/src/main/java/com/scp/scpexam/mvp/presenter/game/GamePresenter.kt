package com.scp.scpexam.mvp.presenter.game

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import com.scp.scpexam.Constants
import com.scp.scpexam.R
import com.scp.scpexam.controller.api.ApiClient
import com.scp.scpexam.controller.db.AppDatabase
import com.scp.scpexam.controller.interactor.GameInteractor
import com.scp.scpexam.controller.interactor.TransactionInteractor
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.model.db.QuizTranslationPhrase
import com.scp.scpexam.model.db.TransactionType
import com.scp.scpexam.model.ui.ChatAction
import com.scp.scpexam.model.ui.ChatActionsGroupType
import com.scp.scpexam.model.ui.QuizLevelInfo
import com.scp.scpexam.model.ui.QuizScreenLaunchData
import com.scp.scpexam.mvp.AuthPresenter
import com.scp.scpexam.mvp.presenter.BasePresenter
import com.scp.scpexam.mvp.view.game.GameView
import com.scp.scpexam.ui.fragment.game.GameFragment
import com.scp.scpexam.ui.utils.AuthDelegate
import com.scp.scpexam.ui.view.KeyboardView
import com.scp.scpexam.utils.BitmapUtils
import com.scp.scpexam.utils.addTo
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
import ru.terrakok.cicerone.Router
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.properties.Delegates

@InjectViewState
class GamePresenter @Inject constructor(
        override var appContext: Application,
        override var preferences: MyPreferenceManager,
        override var router: Router,
        override var appDatabase: AppDatabase,
        private var gameInteractor: GameInteractor,
        override var transactionInteractor: TransactionInteractor,
        public override var apiClient: ApiClient
) : BasePresenter<GameView>(appContext, preferences, router, appDatabase, apiClient, transactionInteractor), AuthPresenter<GameFragment> {

    override fun getAuthView(): GameView = viewState

    override fun onAuthSuccess() {
        preferences.setIntroDialogShown(true)
        viewState.showMessage(R.string.settings_success_auth)
    }

    override fun onAuthCanceled() {
        viewState.showMessage(R.string.canceled_auth)
    }

    override fun onAuthError() {
        viewState.showMessage(appContext.getString(R.string.auth_retry))
        viewState.showChatActions(generateAuthActions(), ChatActionsGroupType.AUTH)
    }

    override lateinit var authDelegate: AuthDelegate<GameFragment>

    enum class EnterType {
        NAME, NUMBER, NOT_CHOOSED
    }

    private var currentLang: String = preferences.getLang()

    private var isLevelShown: Boolean = false

    var quizId: Long by Delegates.notNull()

    lateinit var quizLevelInfo: QuizLevelInfo

    private val enteredName = mutableListOf<Char>()

    private val enteredNumber = mutableListOf<Char>()

    private val usedPhrases = mutableListOf<QuizTranslationPhrase>()

    private var levelDataDisposable: Disposable? = null

    private var periodicMessagesCompositeDisposable: CompositeDisposable = CompositeDisposable()

    var currentEnterType = EnterType.NOT_CHOOSED

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        loadLevel()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        authDelegate.onActivityResult(requestCode, resultCode, data)
    }

    private fun generateAuthActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageAuthFacebook = appContext.getString(R.string.chat_action_auth_facebook)
        chatActions += ChatAction(
                messageAuthFacebook,
                onActionClicked(messageAuthFacebook) { onFacebookLoginClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageAuthGoogle = appContext.getString(R.string.chat_action_auth_google)
        chatActions += ChatAction(
                messageAuthGoogle,
                onActionClicked(messageAuthGoogle) { onGoogleLoginClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageAuthVk = appContext.getString(R.string.chat_action_auth_vk)
        chatActions += ChatAction(
                messageAuthVk,
                onActionClicked(messageAuthVk) { onVkLoginClicked() },
                R.drawable.selector_chat_action_accent
        )
        val messageSkipAuthAndDontShowAgain = appContext.getString(R.string.chat_action_skip_auth_and_dont_show)
        chatActions += ChatAction(
                messageSkipAuthAndDontShowAgain,
                onActionClicked(messageSkipAuthAndDontShowAgain) { onSkipAuthAndNeverShowClicked() },
                R.drawable.selector_chat_action_accent
        )

        return chatActions
    }

    private fun generateGoToAdminAppActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val messageAdminAppLink = appContext.getString(R.string.chat_action_admin_app_download)
        chatActions += ChatAction(
                messageAdminAppLink,
                onActionClicked(messageAdminAppLink) { onGoToAdminAppClicked() },
                R.drawable.selector_chat_action_accent
        )

        val skipSuggestionOfAdminApp = appContext.getString(R.string.chat_action_skip_suggestion_of_admin_app)
        chatActions += ChatAction(
                skipSuggestionOfAdminApp,
                onActionClicked(skipSuggestionOfAdminApp) { onSkipSuggestionOfAdminAppClicked() },
                R.drawable.selector_chat_action_accent
        )

        val messageDontWantToDownloadAppAndNeverShow = appContext.getString(R.string.chat_action_dont_want_to_download_app_and_never_show)
        chatActions += ChatAction(
                messageDontWantToDownloadAppAndNeverShow,
                onActionClicked(messageDontWantToDownloadAppAndNeverShow) { onSkipDownloadAdminAppAndNeverShowSuggestionClicked() },
                R.drawable.selector_chat_action_accent
        )

        return chatActions
    }

    private fun showAuthChatActions() {
        viewState.showChatMessage(appContext.getString(R.string.message_auth_suggestion_game), quizLevelInfo.doctor)
        viewState.showChatActions(generateAuthActions(), ChatActionsGroupType.AUTH)
    }

    private fun showGoToAdminAppChatActions() {
        viewState.showChatMessage(appContext.getString(R.string.suggestion_go_to_admin_app), quizLevelInfo.doctor)
        viewState.showChatActions(generateGoToAdminAppActions(), ChatActionsGroupType.GO_TO_ADMIN_APP)
    }

    private fun onSkipAuthAndNeverShowClicked() {
        preferences.setNeverShowAuth(true)
    }

    private fun onGoToAdminAppClicked() {
        router.navigateTo(Constants.Screens.PlayMarketScreen)
    }

    private fun onSkipDownloadAdminAppAndNeverShowSuggestionClicked() {
        preferences.setNeverShowAdminForQuizAppAds(true)
    }

    private fun onSkipSuggestionOfAdminAppClicked() {}

    private fun onActionClicked(text: String, onCompleteAction: () -> Unit): (Int) -> Unit =
            { index ->
                viewState.removeChatAction(index)
                viewState.showChatMessage(text, quizLevelInfo.player)

                onCompleteAction.invoke()
            }

    private fun sendPeriodicMessages() {
        if (periodicMessagesCompositeDisposable.isDisposed) {
            periodicMessagesCompositeDisposable = CompositeDisposable()
        }
        periodicMessagesCompositeDisposable.add(Flowable.interval(
                PERIODIC_MESSAGES_INITIAL_DELAY,
                PERIODIC_MESSAGES_PERIOD,
                TimeUnit.SECONDS
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    val phrases = quizLevelInfo.quiz.quizTranslations?.first()?.quizTranslationPhrases
                    if (phrases?.isNotEmpty() == true) {
                        val notUsedPhrases = phrases.toMutableList()
                        notUsedPhrases.removeAll(usedPhrases)
                        if (notUsedPhrases.isNotEmpty()) {
                            val randomPhrase = notUsedPhrases[Random().nextInt(notUsedPhrases.size)]
                            usedPhrases += randomPhrase
                            viewState.showChatMessage(randomPhrase.translation, quizLevelInfo.doctor)
                        }
                    }
                }
        )
    }

    private fun sendPeriodicSuggestionsMessages() {
        if (periodicMessagesCompositeDisposable.isDisposed) {
            periodicMessagesCompositeDisposable = CompositeDisposable()
        }
        Flowable.interval(
                PERIODIC_SUGGESTIONS_INITIAL_DELAY,
                PERIODIC_SUGGESTIONS_PERIOD,
                TimeUnit.SECONDS
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestions)
                    viewState.showChatMessage(
                            suggestionsMessages[Random().nextInt(suggestionsMessages.size)],
                            quizLevelInfo.doctor
                    )
                    viewState.showChatActions(generateSuggestions(), ChatActionsGroupType.SUGGESTIONS)
                }
                .addTo(periodicMessagesCompositeDisposable)

        if (!preferences.getNeverShowAuth() && preferences.getTrueAccessToken() == null) {
            Flowable.interval(
                    PERIODIC_GAME_AUTH_INITIAL_DELAY,
                    PERIODIC_GAME_AUTH_PERIOD,
                    TimeUnit.SECONDS
            )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy {
                        if (!preferences.getNeverShowAuth() && preferences.getTrueAccessToken() == null) {
                            showAuthChatActions()
                        }
                    }
                    .addTo(periodicMessagesCompositeDisposable)
        }

        Maybe.fromCallable {
            if (preferences.getTrueAccessToken() != null && !preferences.getNeverShowAdminForQuizAppAds() && appDatabase.finishedLevelsDao().getCountOfFullyFinishedLevels() > 4) {
                true
            } else {
                null
            }
        }
                .flatMapObservable {
                    Flowable.interval(
                            PERIODIC_GO_TO_ADMIN_APP_INITIAL_DELAY,
                            PERIODIC_GO_TO_ADMIN_APP_PERIOD,
                            TimeUnit.SECONDS
                    )
                            .toObservable()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    if (preferences.getTrueAccessToken() != null && !preferences.getNeverShowAdminForQuizAppAds()) {
                        showGoToAdminAppChatActions()
                    }
                }
                .addTo(periodicMessagesCompositeDisposable)
    }

    private fun generateSuggestions(): List<ChatAction> {
        val actions = mutableListOf<ChatAction>()

        //todo move to function
        val checkCoins: (Int, Int) -> Boolean = { price, indexOfChatAction ->
            val hasEnoughCoins = quizLevelInfo.player.score >= price
            if (!hasEnoughCoins) {
                viewState.removeChatAction(indexOfChatAction)
                viewState.showChatMessage(
                        appContext.getString(R.string.message_not_enough_coins),
                        quizLevelInfo.doctor
                )
                viewState.showChatActions(generateGainCoinsActions(), ChatActionsGroupType.GAIN_COINS)
            }
            hasEnoughCoins
        }

        val nameCharsRemoved = quizLevelInfo.finishedLevel.nameRedundantCharsRemoved
        val numberCharsRemoved = quizLevelInfo.finishedLevel.numberRedundantCharsRemoved
        val nameFilled = quizLevelInfo.finishedLevel.scpNameFilled
        val numberFilled = quizLevelInfo.finishedLevel.scpNumberFilled

        if (!nameCharsRemoved && !nameFilled && currentEnterType == EnterType.NAME
                || !numberCharsRemoved && !numberFilled && currentEnterType == EnterType.NUMBER) {
            val removeCharsActionText = appContext.getString(R.string.suggestion_remove_redundant_chars)
            actions += ChatAction(
                    removeCharsActionText,
                    { indexOfChatActionsViewInChatLayout: Int ->
                        viewState.removeChatAction(indexOfChatActionsViewInChatLayout)
                        viewState.showChatMessage(removeCharsActionText, quizLevelInfo.player)
                        val price = Constants.SUGGESTION_PRICE_REMOVE_CHARS
                        if (checkCoins.invoke(price, indexOfChatActionsViewInChatLayout)) {
                            val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestion_remove_chars)
                            viewState.showChatMessage(
                                    suggestionsMessages[Random().nextInt(suggestionsMessages.size)],
                                    quizLevelInfo.doctor
                            )
                            var nameRedundantCharsRemoved: Boolean? = null
                            var numberRedundantCharsRemoved: Boolean? = null
                            val chars: List<Char>?
                            val transactionType: TransactionType
                            val coinsAmount: Int = -Constants.SUGGESTION_PRICE_REMOVE_CHARS
                            if (currentEnterType == EnterType.NAME) {
                                nameRedundantCharsRemoved = true
                                transactionType = TransactionType.NAME_CHARS_REMOVED
                                chars = quizLevelInfo.quiz.quizTranslations?.first()?.translation?.toList()
                                        ?: throw IllegalStateException("no chars for keyboard")
                            } else {
                                numberRedundantCharsRemoved = true
                                transactionType = TransactionType.NUMBER_CHARS_REMOVED
                                chars = quizLevelInfo.quiz.scpNumber.toList()
                            }
                            chars.let { viewState.setKeyboardChars(it) }
                            compositeDisposable.add(gameInteractor.updateFinishedLevel(
                                    quizLevelInfo.quiz.id,
                                    nameRedundantCharsRemoved = nameRedundantCharsRemoved,
                                    numberRedundantCharsRemoved = numberRedundantCharsRemoved
                            )
                                    .observeOn(Schedulers.io())
                                    .flatMap { gameInteractor.increaseScore(-price).toSingleDefault(-price) }
                                    .flatMapCompletable { transactionInteractor.makeTransaction(quizLevelInfo.quiz.id, transactionType, coinsAmount) }
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeBy(
                                            onError = {
                                                Timber.e(it)
                                                viewState.showMessage(it.message
                                                        ?: "Unexpected error")
                                            },
                                            onComplete = {}
                                    ))
                        }
                    },
                    R.drawable.selector_chat_action_green,
                    Constants.SUGGESTION_PRICE_REMOVE_CHARS
            )
        }

        if (!nameFilled) {
            val enterNameActionText = appContext.getString(R.string.suggestion_enter_name)
            actions += ChatAction(
                    enterNameActionText,
                    {
                        with(viewState) {
                            removeChatAction(it)
                            showChatMessage(enterNameActionText, quizLevelInfo.player)
                            if (checkCoins.invoke(Constants.SUGGESTION_PRICE_NAME, it)) {
                                onNameEntered(false)
                            }
                        }
                    },
                    R.drawable.selector_chat_action_green,
                    Constants.SUGGESTION_PRICE_NAME
            )
        }

        if (!numberFilled) {
            val enterNumberActionText = appContext.getString(R.string.suggestion_enter_number)
            actions += ChatAction(
                    enterNumberActionText,
                    {
                        with(viewState) {
                            removeChatAction(it)
                            showChatMessage(enterNumberActionText, quizLevelInfo.player)
                            if (checkCoins.invoke(Constants.SUGGESTION_PRICE_NUMBER, it)) {
                                onNumberEntered(false)
                            }
                        }
                    },
                    R.drawable.selector_chat_action_green,
                    Constants.SUGGESTION_PRICE_NUMBER
            )
        }

        val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestion_no)
        val noActionText = suggestionsMessages[Random().nextInt(suggestionsMessages.size)]
        actions += ChatAction(
                noActionText,
                {
                    viewState.removeChatAction(it)
                    viewState.showChatMessage(
                            noActionText,
                            quizLevelInfo.player
                    )
                },
                R.drawable.selector_chat_action_red
        )

        return actions
    }

    private fun generateGainCoinsActions(): List<ChatAction> {
        val actions = mutableListOf<ChatAction>()

        val watchVideoActionText = appContext.getString(R.string.chat_action_watch_video, Constants.REWARD_VIDEO_ADS)
        actions += ChatAction(
                watchVideoActionText,
                {
                    viewState.removeChatAction(it)
                    viewState.showChatMessage(
                            watchVideoActionText,
                            quizLevelInfo.player
                    )
                    onNeedToShowVideoAds()
                },
                R.drawable.selector_chat_action_red
        )

        val buyCoinsActionText0 = appContext.getString(R.string.chat_action_buy_coins, Constants.COINS_FOR_SKU_INAPP_0)
        actions += ChatAction(
                buyCoinsActionText0,
                onActionClicked(buyCoinsActionText0) { onBuyCoinsActionClicked(Constants.SKU_INAPP_BUY_COINS_0) },
                R.drawable.selector_chat_action_red
        )

        val buyCoinsActionText1 = appContext.getString(R.string.chat_action_buy_coins, Constants.COINS_FOR_SKU_INAPP_1)
        actions += ChatAction(
                buyCoinsActionText1,
                onActionClicked(buyCoinsActionText1) { onBuyCoinsActionClicked(Constants.SKU_INAPP_BUY_COINS_1) },
                R.drawable.selector_chat_action_red
        )

        val buyCoinsActionText2 = appContext.getString(R.string.chat_action_buy_coins, Constants.COINS_FOR_SKU_INAPP_2)
        actions += ChatAction(
                buyCoinsActionText2,
                onActionClicked(buyCoinsActionText2) { onBuyCoinsActionClicked(Constants.SKU_INAPP_BUY_COINS_2) },
                R.drawable.selector_chat_action_red
        )

        val buyCoinsActionText3 = appContext.getString(R.string.chat_action_buy_coins, Constants.COINS_FOR_SKU_INAPP_3)
        actions += ChatAction(
                buyCoinsActionText3,
                onActionClicked(buyCoinsActionText3) { onBuyCoinsActionClicked(Constants.SKU_INAPP_BUY_COINS_3) },
                R.drawable.selector_chat_action_red
        )

        val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestion_no)
        val noActionText = suggestionsMessages[Random().nextInt(suggestionsMessages.size)]
        actions += ChatAction(
                noActionText,
                {
                    viewState.removeChatAction(it)
                    viewState.showChatMessage(
                            noActionText,
                            quizLevelInfo.player
                    )
                },
                R.drawable.selector_chat_action_red
        )

        return actions
    }

    private fun onBuyCoinsActionClicked(skuInapp: String) {

        Timber.d("onBuyCoinsActionClicked : $skuInapp")
        viewState.onNeedToBuyCoins(skuInapp)
    }

    private fun onNeedToShowVideoAds() {
        viewState.onNeedToShowRewardedVideo()
    }

    private fun loadLevel() {
        viewState.showProgress(true)
        levelDataDisposable = gameInteractor
                .getLevelInfo(quizId)
                .subscribeBy(
                        onNext = { levelInfo ->
                            //                            Timber.d("quiz:${levelInfo.quiz.scpNumber}\ntranslationTexts:${levelInfo.randomTranslations.map { it.translation }}")

                            quizLevelInfo = levelInfo

                            viewState.showProgress(false)
                            if (!isLevelShown) {
                                isLevelShown = true

                                //todo param for number
//                            viewState.showLevelNumber(-1)

                                quizLevelInfo.finishedLevel.apply {
                                    viewState.showImage(quizLevelInfo.quiz)
                                    when {
                                        scpNumberFilled && scpNameFilled -> {
                                            with(viewState) {
                                                showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
                                                showNumber(quizLevelInfo.quiz.scpNumber.toList())
                                            }
                                            onLevelCompletelyFinished()
                                        }
                                        !scpNumberFilled && scpNameFilled -> {
//                                            Timber.d("!scpNumberFilled && scpNameFilled")
                                            currentEnterType = EnterType.NUMBER

                                            with(viewState) {
                                                showToolbar(true)
                                                showHelpButton(true)

                                                val scpNumberChars = quizLevelInfo.quiz.scpNumber.toMutableList()
                                                setKeyboardChars(
                                                        if (numberRedundantCharsRemoved) {
                                                            scpNumberChars
                                                        } else {
                                                            KeyboardView.fillCharsList(
                                                                    scpNumberChars,
                                                                    Constants.DIGITS_CHAR_LIST
                                                            )
                                                        }
                                                )
                                                showChatMessage(
                                                        appContext.getString(
                                                                R.string.message_enter_number_description,
                                                                Constants.COINS_FOR_NUMBER
                                                        ),
                                                        quizLevelInfo.doctor
                                                )
                                                showKeyboard(true)
                                                showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
                                            }

                                            sendPeriodicMessages()
                                            sendPeriodicSuggestionsMessages()
                                        }
                                        else -> {
                                            with(viewState) {
                                                showToolbar(true)

                                                if (scpNumberFilled && !scpNameFilled) {
                                                    currentEnterType = EnterType.NAME
                                                    showHelpButton(true)

                                                    showNumber(quizLevelInfo.quiz.scpNumber.toList())

                                                    val startLevelMessages = appContext
                                                            .resources
                                                            .getStringArray(R.array.messages_level_start)
                                                    showChatMessage(
                                                            startLevelMessages[Random().nextInt(startLevelMessages.size)],
                                                            quizLevelInfo.doctor
                                                    )

                                                    val chars = quizLevelInfo.quiz.quizTranslations?.first()?.translation?.toMutableList()
                                                            ?: throw IllegalStateException("translations is null")
                                                    val availableChars = quizLevelInfo.randomTranslations
                                                            .joinToString(separator = "") { it.translation }
                                                            .toList()
                                                    setKeyboardChars(
                                                            if (nameRedundantCharsRemoved) {
                                                                chars
                                                            } else {
                                                                KeyboardView.fillCharsList(
                                                                        chars,
                                                                        availableChars
                                                                )
                                                            }
                                                    )
                                                    animateKeyboard()

                                                    sendPeriodicSuggestionsMessages()
                                                } else {
                                                    showChatMessage(
                                                            appContext.getString(R.string.message_choose_name_or_number),
                                                            quizLevelInfo.doctor
                                                    )

                                                    showChatActions(
                                                            generateChooseNameOrNumberActions(),
                                                            ChatActionsGroupType.CHOOSE_ENTER_TYPE
                                                    )
                                                    showKeyboard(false)
                                                }
                                            }
                                            sendPeriodicMessages()
                                        }
                                    }
                                }
                            }

                            viewState.showCoins(quizLevelInfo.player.score)
                        },
                        onError = {
                            Timber.e(it)
                            viewState.showProgress(false)
                            viewState.showError(it)
                        }
                )
    }

    fun onLevelsClicked() = router.newRootScreen(Constants.Screens.LevelsScreen)

    fun onCoinsClicked() = viewState.onNeedToOpenCoins()

    fun openCoins(bitmap: Bitmap) {
        Completable.fromAction {
            BitmapUtils.persistImage(
                    appContext,
                    bitmap,
                    Constants.SETTINGS_BACKGROUND_FILE_NAME)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = { router.navigateTo(Constants.Screens.MonetizationScreen) }
                )
    }

    fun onHamburgerMenuClicked() = viewState.onNeedToOpenSettings()

    fun openSettings(bitmap: Bitmap) {
        Completable.fromAction {
            BitmapUtils.persistImage(
                    appContext,
                    bitmap,
                    Constants.SETTINGS_BACKGROUND_FILE_NAME)
        }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onComplete = { router.navigateTo(Constants.Screens.SettingsScreen) }
                )
    }

    fun onCharClicked(char: Char, charId: Int) {
//        Timber.d("char pressed: $char")

        val charEnteredForName = {
            //            Timber.d("charEnteredForName invoked")
            enteredName += char.toLowerCase()

            viewState.addCharToNameInput(char, charId)

            viewState.showBackspaceButton(true)
            checkEnteredScpName()
        }

        val charEnteredForNumber = {
            //            Timber.d("charEnteredForNumber invoked")
            enteredNumber += char.toLowerCase()

            viewState.addCharToNumberInput(char, charId)

            viewState.showBackspaceButton(true)
            checkEnteredScpNumber()
        }

        if (currentEnterType == EnterType.NUMBER) {
            charEnteredForNumber.invoke()
        } else if (currentEnterType == EnterType.NAME) {
            charEnteredForName.invoke()
        }
    }

    fun onCharRemovedFromName(charId: Int, indexOfChild: Int) {
//        Timber.d("onCharRemoved: $charId, $indexOfChild")
//        Timber.d("enteredName: $enteredName, enteredNumber: $enteredNumber")
        if (!quizLevelInfo.finishedLevel.scpNameFilled) {
            enteredName.removeAt(indexOfChild)
            if (enteredName.isEmpty()) {
                viewState.showBackspaceButton(false)
            }
        } else {
            enteredNumber.removeAt(indexOfChild)
            if (enteredNumber.isEmpty()) {
                viewState.showBackspaceButton(false)
            }
        }
        viewState.removeCharFromNameInput(charId, indexOfChild)
    }

    fun onCharRemovedFromNumber(charId: Int, indexOfChild: Int) {
//        Timber.d("onCharRemoved: $charId, $indexOfChild")
//        Timber.d("enteredName: $enteredName, enteredNumber: $enteredNumber")
        if (!quizLevelInfo.finishedLevel.scpNameFilled && currentEnterType == EnterType.NAME) {
            enteredName.removeAt(indexOfChild)
            if (enteredName.isEmpty()) {
                viewState.showBackspaceButton(false)
            }
        } else {
            enteredNumber.removeAt(indexOfChild)
            if (enteredNumber.isEmpty()) {
                viewState.showBackspaceButton(false)
            }
        }
        viewState.removeCharFromNumberInput(charId, indexOfChild)
    }

    private fun checkEnteredScpNumber() {
        if (quizLevelInfo.quiz.scpNumber.toLowerCase() == enteredNumber.joinToString("").toLowerCase()) {
//            Timber.d("number is correct!")
            currentEnterType = EnterType.NAME
            onNumberEntered(true)
        } else {
//            Timber.d("number is not correct")
        }
    }

    private fun checkEnteredScpName() {
        quizLevelInfo.quiz.quizTranslations?.first()?.let { quizTranslation ->
            if (enteredName.joinToString("").toLowerCase() == quizTranslation.translation.toLowerCase()) {
//                Timber.d("name is correct!")
                currentEnterType = EnterType.NUMBER
                onNameEntered(true)
            } else {
//                Timber.d("name is not correct")
            }
        }
    }

    private fun generateLevelCompletedActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        generateNextLevelAction()?.let { chatActions += it }

        val enterNumberAction = ChatAction(
                appContext.getString(R.string.chat_action_levels_list),
                {
                    router.newRootScreen(Constants.Screens.LevelsScreen)
                },
                R.drawable.selector_chat_action_accent
        )
        chatActions += enterNumberAction
        return chatActions
    }

    private fun generateNameEnteredChatActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        generateNextLevelAction()?.let { chatActions += it }

        val message = appContext.getString(R.string.chat_action_enter_number)
        val enterNumberAction = ChatAction(
                message,
                {
                    val availableChars = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
                    val scpNumberChars = quizLevelInfo.quiz.scpNumber.toMutableList()
                    with(viewState) {
                        setKeyboardChars(
                                if (quizLevelInfo.finishedLevel.numberRedundantCharsRemoved) {
                                    scpNumberChars
                                } else {
                                    KeyboardView.fillCharsList(scpNumberChars, availableChars)
                                }
                        )
                        showKeyboard(true)
                        showChatMessage(message, quizLevelInfo.player)
                        removeChatAction(it)
                    }
                },
                R.drawable.selector_chat_action_green
        )
        chatActions += enterNumberAction
        return chatActions
    }

    private fun generateNextLevelAction() = if (quizLevelInfo.nextQuizIdAndFinishedLevel.first != null) {
        val nextLevelMessageText = appContext.getString(R.string.chat_action_next_level)
        ChatAction(
                nextLevelMessageText,
                { index ->
                    val showAds = !preferences.isAdsDisabled()
                            && preferences.isNeedToShowInterstitial()
                            && (!quizLevelInfo.nextQuizIdAndFinishedLevel.second!!.scpNameFilled
                            || !quizLevelInfo.nextQuizIdAndFinishedLevel.second!!.scpNumberFilled)
//                    Timber.d(
//                            "!preferences.isAdsDisabled()\npreferences.isNeedToShowInterstitial()\n(!levelViewModel.scpNameFilled || !levelViewModel.scpNumberFilled): %s/%s/%s",
//                            !preferences.isAdsDisabled(),
//                            preferences.isNeedToShowInterstitial(),
//                            !quizLevelInfo.nextQuizIdAndFinishedLevel.second!!.scpNameFilled
//                                    || !quizLevelInfo.nextQuizIdAndFinishedLevel.second!!.scpNumberFilled
//                    )
//                    Timber.d("showAds: $showAds")
                    if (quizLevelInfo.nextQuizIdAndFinishedLevel.second!!.isLevelAvailable) {
                        router.replaceScreen(
                                Constants.Screens.GameScreen(QuizScreenLaunchData(quizLevelInfo.nextQuizIdAndFinishedLevel.first!!, !showAds))
                        )
                    } else {
                        //todo move to function
                        val checkCoins: (Int, Int) -> Boolean = { price, _ ->
                            val hasEnoughCoins = quizLevelInfo.player.score >= price
//                            Timber.d("hasEnoughCoins: $hasEnoughCoins (coins: ${quizLevelInfo.player.score})")
                            if (!hasEnoughCoins) {
                                viewState.showChatMessage(nextLevelMessageText, quizLevelInfo.player)
                                viewState.showChatMessage(
                                        appContext.getString(R.string.message_not_enough_coins),
                                        quizLevelInfo.doctor
                                )
                                viewState.showChatActions(generateGainCoinsActions(), ChatActionsGroupType.GAIN_COINS)
                            }
                            hasEnoughCoins
                        }

                        if (checkCoins.invoke(Constants.COINS_FOR_LEVEL_UNLOCK, index)) {
                            viewState.removeChatAction(index)
                            viewState.showChatMessage(nextLevelMessageText, quizLevelInfo.player)
                            compositeDisposable.add(gameInteractor
                                    .increaseScore(-Constants.COINS_FOR_LEVEL_UNLOCK)
                                    .andThen(transactionInteractor.makeTransaction(quizLevelInfo.quiz.id, TransactionType.LEVEL_ENABLE_FOR_COINS, -Constants.COINS_FOR_LEVEL_UNLOCK))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeBy(
                                            onError = {
                                                Timber.e(it)
                                                viewState.showMessage(it.message
                                                        ?: "Unexpected error")
                                            },
                                            onComplete = {
                                                router.replaceScreen(
                                                        Constants.Screens.GameScreen(QuizScreenLaunchData(quizLevelInfo.nextQuizIdAndFinishedLevel.first!!, !showAds))
                                                )
                                            }
                                    ))
                        }
                    }
                },
                R.drawable.selector_chat_action_accent,
                if (quizLevelInfo.nextQuizIdAndFinishedLevel.second!!.isLevelAvailable) 0 else Constants.COINS_FOR_LEVEL_UNLOCK
        )
    } else {
        null
    }

    private fun generateChooseNameOrNumberActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        val message = appContext.getString(R.string.chat_action_want_to_enter_number)
        val enterNumberAction = ChatAction(
                message,
                {
                    val availableChars = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
                    val scpNumberChars = quizLevelInfo.quiz.scpNumber.toMutableList()
                    with(viewState) {
                        setKeyboardChars(
                                if (quizLevelInfo.finishedLevel.scpNumberFilled) {
                                    scpNumberChars
                                } else {
                                    KeyboardView.fillCharsList(scpNumberChars, availableChars)
                                }
                        )
                        showKeyboard(true)
                        animateKeyboard()
                        showChatMessage(message, quizLevelInfo.player)
                        removeChatAction(it)
                        showHelpButton(true)
                    }

                    currentEnterType = EnterType.NUMBER
                    sendPeriodicSuggestionsMessages()
                },
                R.drawable.selector_chat_action_green
        )
        chatActions += enterNumberAction

        val messageEnterName = appContext.getString(R.string.chat_action_want_to_enter_name)
        val enterNameAction = ChatAction(
                messageEnterName,
                { index ->
                    with(viewState) {
                        val chars = quizLevelInfo.quiz.quizTranslations?.first()?.translation?.toMutableList()
                                ?: throw IllegalStateException("translations is null")
                        val availableChars = quizLevelInfo.randomTranslations
                                .joinToString(separator = "") { it.translation }
                                .toList()
                        setKeyboardChars(
                                if (quizLevelInfo.finishedLevel.nameRedundantCharsRemoved) {
                                    chars
                                } else {
                                    KeyboardView.fillCharsList(
                                            chars,
                                            availableChars
                                    )
                                }
                        )
                        showKeyboard(true)
                        animateKeyboard()
                        showChatMessage(messageEnterName, quizLevelInfo.player)
                        removeChatAction(index)
                        showHelpButton(true)
                    }

                    currentEnterType = EnterType.NAME
                    sendPeriodicSuggestionsMessages()
                },
                R.drawable.selector_chat_action_accent
        )
        chatActions += enterNameAction

        return chatActions
    }

    private fun onLevelCompleted() {
        //mark level as completed
        compositeDisposable.add(gameInteractor.updateFinishedLevel(
                quizId,
                quizLevelInfo.finishedLevel.scpNameFilled,
                quizLevelInfo.finishedLevel.scpNumberFilled
        )
                .flatMap { gameInteractor.getNumberOfPartiallyAndFullyFinishedLevels() }
                .subscribeBy(
                        onSuccess = {
                            //                            Timber.d("finished levels updated!")
                            if (it.first == Constants.FINISHED_LEVEL_BEFORE_ASK_RATE_APP && preferences.isAlreadySuggestRateUs()) {
                                preferences.setAlreadySuggestRateUs(true)
                                viewState.askForRateApp()
                            }
                            if (it.first > 0
                                    && it.first % Constants.NUM_OF_FINISHED_LEVEL_BEFORE_SHOW_ADS == 0L
                                    && it.first != preferences.getLastFinishedLevelsNum()
                            ) {
                                preferences.setLastFinishedLevelsNum(it.first)
                                preferences.setNeedToShowInterstitial(true)
                            }
                        },
                        onError = {
                            Timber.e(it)
                            viewState.showError(it)
                        }
                ))
    }

    fun checkLang() {
        val langInPrefs = preferences.getLang()
        if (currentLang != langInPrefs) {
            isLevelShown = false
            viewState.clearChatMessages()
            loadLevel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        periodicMessagesCompositeDisposable.dispose()
        levelDataDisposable?.dispose()
    }

    private fun onNameEntered(receiveReward: Boolean) {
//        Timber.d("onNameEntered")
        val quizTranslation = quizLevelInfo.quiz.quizTranslations?.first()
                ?: throw IllegalStateException("quizTranslation is NULL!")

        with(viewState) {
            quizLevelInfo.finishedLevel.scpNameFilled = true
            onLevelCompleted()

            if (!receiveReward) {
                val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestion_enter_name)
                showChatMessage(
                        suggestionsMessages[Random().nextInt(suggestionsMessages.size)],
                        quizLevelInfo.doctor
                )

                showChatMessage(
                        appContext.getString(R.string.message_after_suggestion_of_name),
                        quizLevelInfo.player
                )
                compositeDisposable.add(gameInteractor
                        .increaseScore(-Constants.SUGGESTION_PRICE_NAME)
                        .andThen(transactionInteractor.makeTransaction(quizLevelInfo.quiz.id, TransactionType.NAME_NO_PRICE, -Constants.SUGGESTION_PRICE_NAME))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onError = {
                                    Timber.e(it)
                                    viewState.showMessage(it.message
                                            ?: "Unexpected error")
                                },
                                onComplete = {
                                }
                        ))
            } else {
                showChatMessage(
                        quizTranslation.translation,
                        quizLevelInfo.player
                )
                showChatMessage(
                        appContext.getString(R.string.message_correct_give_coins, Constants.COINS_FOR_NAME),
                        quizLevelInfo.doctor
                )
                compositeDisposable.add(gameInteractor
                        .increaseScore(Constants.COINS_FOR_NAME)
                        .andThen(transactionInteractor.makeTransaction(quizLevelInfo.quiz.id, TransactionType.NAME_WITH_PRICE, Constants.COINS_FOR_NAME))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onError = {
                                    Timber.e(it)
                                    viewState.showMessage(it.message
                                            ?: "Unexpected error")
                                },
                                onComplete = {
                                }
                        ))
            }
            showName(quizLevelInfo.quiz.quizTranslations!!.first().translation.toList())
            showBackspaceButton(false)

            if (quizLevelInfo.finishedLevel.scpNameFilled && quizLevelInfo.finishedLevel.scpNumberFilled) {
                onLevelCompletelyFinished()
            } else {
                showChatMessage(
                        appContext.getString(R.string.message_suggest_scp_number),
                        quizLevelInfo.doctor
                )

                showChatActions(generateNameEnteredChatActions(), ChatActionsGroupType.NAME_ENTERED)
                showKeyboard(false)
            }
        }
    }

    private fun onNumberEntered(receiveReward: Boolean) {
//        Timber.d("onNumberEntered")
        quizLevelInfo.finishedLevel.scpNumberFilled = true
        onLevelCompleted()

        with(viewState) {
            if (!receiveReward) {
                val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestion_enter_number)
                showChatMessage(
                        suggestionsMessages[Random().nextInt(suggestionsMessages.size)],
                        quizLevelInfo.doctor
                )

                showChatMessage(
                        appContext.getString(R.string.message_after_suggestion_of_number),
                        quizLevelInfo.player
                )

                compositeDisposable.add(gameInteractor
                        .increaseScore(-Constants.SUGGESTION_PRICE_NUMBER)
                        .andThen(transactionInteractor.makeTransaction(quizLevelInfo.quiz.id, TransactionType.NUMBER_NO_PRICE, -Constants.SUGGESTION_PRICE_NUMBER))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onError = {
                                    Timber.e(it)
                                    viewState.showMessage(it.message
                                            ?: "Unexpected error")
                                },
                                onComplete = {
                                }
                        ))
            } else {
                showChatMessage(
                        appContext.getString(R.string.message_correct_give_coins, Constants.COINS_FOR_NUMBER),
                        quizLevelInfo.doctor
                )
                showChatMessage(
                        appContext.getString(R.string.message_level_comleted, quizLevelInfo.player.name),
                        quizLevelInfo.doctor
                )
                compositeDisposable.add(gameInteractor
                        .increaseScore(Constants.COINS_FOR_NUMBER)
                        .andThen(transactionInteractor.makeTransaction(quizLevelInfo.quiz.id, TransactionType.NUMBER_WITH_PRICE, Constants.COINS_FOR_NUMBER))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onError = {
                                    Timber.e(it)
                                    viewState.showMessage(it.message
                                            ?: "Unexpected error")
                                },
                                onComplete = {
                                }
                        ))
            }

            showNumber(quizLevelInfo.quiz.scpNumber.toList())
            showBackspaceButton(false)

            if (quizLevelInfo.finishedLevel.scpNameFilled && quizLevelInfo.finishedLevel.scpNumberFilled) {
                onLevelCompletelyFinished()
            } else {
                showChatMessage(
                        appContext.getString(R.string.message_suggest_scp_name),
                        quizLevelInfo.doctor
                )

                showChatActions(generateNumberEnteredChatActions(), ChatActionsGroupType.NUMBER_ENTERED)
                showKeyboard(false)
            }
        }
    }

    private fun generateNumberEnteredChatActions(): List<ChatAction> {
        val chatActions = mutableListOf<ChatAction>()

        generateNextLevelAction()?.let { chatActions += it }

        val message = appContext.getString(R.string.chat_action_enter_name)
        chatActions += ChatAction(
                message,
                { index ->
                    with(viewState) {
                        val chars = quizLevelInfo.quiz.quizTranslations?.first()?.translation?.toMutableList()
                                ?: throw IllegalStateException("translations is null")
                        val availableChars = quizLevelInfo.randomTranslations
                                .joinToString(separator = "") { it.translation }
                                .toList()
                        setKeyboardChars(
                                if (quizLevelInfo.finishedLevel.nameRedundantCharsRemoved) {
                                    chars
                                } else {
                                    KeyboardView.fillCharsList(
                                            chars,
                                            availableChars
                                    )
                                }
                        )
                        showKeyboard(true)
                        animateKeyboard()
                        showChatMessage(message, quizLevelInfo.player)
                        removeChatAction(index)
                    }
                },
                R.drawable.selector_chat_action_green
        )

        return chatActions
    }

    private fun onLevelCompletelyFinished() {
        with(viewState) {
            showKeyboard(false)
            showToolbar(false)
            showHelpButton(false)
            setBackgroundDark(true)

            showChatMessage(
                    quizLevelInfo.quiz.quizTranslations!!.first().description,
                    quizLevelInfo.player
            )
            showChatMessage(
                    appContext.getString(R.string.message_level_comleted, quizLevelInfo.player.name),
                    quizLevelInfo.doctor
            )

            showChatActions(generateLevelCompletedActions(), ChatActionsGroupType.LEVEL_FINISHED)
        }

        periodicMessagesCompositeDisposable.dispose()
    }

    fun onHelpClicked() {
        val suggestionsMessages = appContext.resources.getStringArray(R.array.messages_suggestions)
        viewState.showChatMessage(
                suggestionsMessages[Random().nextInt(suggestionsMessages.size)],
                quizLevelInfo.doctor
        )
        viewState.showChatActions(generateSuggestions(), ChatActionsGroupType.SUGGESTIONS)
    }

    companion object {
        const val PERIODIC_MESSAGES_INITIAL_DELAY = 30L
        const val PERIODIC_MESSAGES_PERIOD = 60L
        const val PERIODIC_SUGGESTIONS_INITIAL_DELAY = 90L
        const val PERIODIC_SUGGESTIONS_PERIOD = 180L
        const val PERIODIC_GAME_AUTH_INITIAL_DELAY = 180L
        const val PERIODIC_GAME_AUTH_PERIOD = 600L
        const val PERIODIC_GO_TO_ADMIN_APP_INITIAL_DELAY = 60L
        const val PERIODIC_GO_TO_ADMIN_APP_PERIOD = 120L
    }
}