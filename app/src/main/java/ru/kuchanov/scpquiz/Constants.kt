package ru.kuchanov.scpquiz

object Constants {
    const val COINS_FOR_NUMBER = 5
    const val COINS_FOR_NAME = 10
    val DIGITS_CHAR_LIST = listOf(
        '1',
        '2',
        '3',
        '4',
        '5',
        '6',
        '7',
        '8',
        '9',
        '0'
    )
    const val DEFAULT_LANG = "en"
    //todo create policy and set url
    const val PRIVACY_POLICY_URL = "http://kuchanov.ru"
    const val SETTINGS_BACKGROUND_FILE_NAME = "bgSettings"
    const val INTRO_DIALOG_BACKGROUND_FILE_NAME = "bgIntoDialog"
    const val FINISHED_LEVEL_BEFORE_ASK_RATE_APP = 5L

    //monetization
    const val SUGGESTION_PRICE_REMOVE_CHARS = 10
    const val SUGGESTION_PRICE_NAME = 40
    const val SUGGESTION_PRICE_NUMBER = 20
    const val REWARD_VIDEO_ADS = 5

    object Screens {
        const val ENTER = "ENTER"
        const val SETTINGS = "SETTINGS"
        const val QUIZ_LIST = "QUIZ_LIST"
        const val QUIZ = "QUIZ"
        const val APP_INFO = "APP_INFO"
        const val INTRO_DIALOG = "INTRO_DIALOG"
        const val MONETIZATION = "MONETIZATION"
    }
}