package ru.kuchanov.scpquiz.utils

object LocaleUtils {

    fun countryCodeFromLocale(langCode: String) =
            when (langCode) {
                "en" -> "gb"
                "zh" -> "cn"
                else -> langCode
            }
}
