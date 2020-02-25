package com.scp.scpexam.model.ui

data class ProgressPhrasesJson(
    val en: List<ProgressPhrase>,
    val ru: List<ProgressPhrase>
)

data class ProgressPhrase(
    val id: Int,
    val translation: String
)