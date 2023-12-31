package com.scp.scpexam.controller.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import javax.inject.Inject

class SettingsRepository @Inject constructor(
        private val preferenceManager: MyPreferenceManager
) {
    private val langTextRelay = BehaviorRelay.createDefault(preferenceManager.getLang())

    fun setLanguage(langText: String) {
        preferenceManager.setLang(langText)
        langTextRelay.accept(langText)
    }

    fun observeLanguage(): Flowable<String> = langTextRelay.toFlowable(BackpressureStrategy.BUFFER)
}