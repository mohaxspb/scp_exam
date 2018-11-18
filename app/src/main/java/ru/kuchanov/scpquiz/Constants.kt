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
    const val PRIVACY_POLICY_URL = "https://scpfoundation.app/scpQuiz/privacyPolicy.html"
    const val SETTINGS_BACKGROUND_FILE_NAME = "bgSettings"
    const val INTRO_DIALOG_BACKGROUND_FILE_NAME = "bgIntoDialog"
    const val FINISHED_LEVEL_BEFORE_ASK_RATE_APP = 5L

    //monetization
    const val SUGGESTION_PRICE_REMOVE_CHARS = 10
    const val SUGGESTION_PRICE_NAME = 40
    const val SUGGESTION_PRICE_NUMBER = 20
    const val REWARD_VIDEO_ADS = 5
    const val NUM_OF_FINISHED_LEVEL_BEFORE_SHOW_ADS = 3
    const val SKU_INAPP_DISABLE_ADS = "disable_ads_0718"
    const val COINS_FOR_LEVEL_UNLOCK = 5


    object Screens {
        const val ENTER = "ENTER"
        const val SETTINGS = "SETTINGS"
        const val QUIZ_LIST = "QUIZ_LIST"
        const val QUIZ = "QUIZ"
        const val APP_INFO = "APP_INFO"
        const val INTRO_DIALOG = "INTRO_DIALOG"
        const val MONETIZATION = "MONETIZATION"
    }

    object Api {
        const val GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials"
        const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"

        const val HEADER_AUTHORIZATION = "Authorization"
        const val HEADER_PART_BEARER = "Bearer"
    }

    object Auth {
        private const val FACEBOOK_SCOPE_EMAIL = "email"
        private const val FACEBOOK_SCOPE_PUBLIC_PROFILE = "public_profile"
        val FACEBOOK_SCOPES = listOf(FACEBOOK_SCOPE_EMAIL, FACEBOOK_SCOPE_PUBLIC_PROFILE)
    }

    object Social {
        const val VK = "VK"
        const val FACEBOOK = "FACEBOOK"
        const val GOOGLE = "GOOGLE"
        const val GAME = "GAME"
    }
}