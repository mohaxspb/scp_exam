package ru.kuchanov.scpquiz.ui.utils

import ru.kuchanov.scpquiz.App
import ru.kuchanov.scpquiz.model.db.Quiz

fun Quiz.getImageUrl(): String {
    val imagesFiles = App.INSTANCE.resources.assets.list("quizImages").toList()
    val imageFileExtension = when {
        imagesFiles.contains("$scpNumber.jpg") -> ".jpg"
        imagesFiles.contains("$scpNumber.jpeg") -> ".jpeg"
        imagesFiles.contains("$scpNumber.png") -> ".png"
        imagesFiles.contains("$scpNumber.gif") -> ".gif"
        else -> null
    }
    return if (imageFileExtension != null) {
        scpNumber + imageFileExtension
    } else {
        imageUrl
    }
}