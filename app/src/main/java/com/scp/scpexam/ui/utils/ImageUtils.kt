package com.scp.scpexam.ui.utils

import com.scp.scpexam.App
import com.scp.scpexam.model.db.Quiz

fun Quiz.getImageUrl(): String {
    val imagesFiles = App.INSTANCE.resources.assets.list("quizImages")!!.toList()
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