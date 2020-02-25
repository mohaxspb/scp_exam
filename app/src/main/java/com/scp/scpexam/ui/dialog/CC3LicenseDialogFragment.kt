package com.scp.scpexam.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.scp.scpexam.R
import com.scp.scpexam.controller.manager.preference.MyPreferenceManager
import com.scp.scpexam.di.Di
import toothpick.Toothpick
import java.util.*
import javax.inject.Inject

class CC3LicenseDialogFragment : androidx.fragment.app.DialogFragment() {

    @Inject
    lateinit var mMyPreferenceManager: MyPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //icons from https://github.com/hjnilsson/country-flags/tree/master/svg

        val dialogTextSizeBuilder = MaterialDialog.Builder(activity!!)
        dialogTextSizeBuilder
                .customView(R.layout.dialog_cc3_license, false)
                .title(R.string.attention)
                .cancelable(false)
                .positiveText(R.string.i_accept)
                .negativeText(android.R.string.no)
                .onPositive { _, _ ->
                    mMyPreferenceManager.setPersonalDataAccepted(true)
                    dismissAllowingStateLoss()
                }
                .onNegative { _, _ ->
                    mMyPreferenceManager.setPersonalDataAccepted(false)
                    activity!!.finish()
                }

        val dialog = dialogTextSizeBuilder.build()

        dialog.customView?.let { customView ->
            val en = customView.findViewById<View>(R.id.en)
            val ru = customView.findViewById<View>(R.id.ru)
            val pl = customView.findViewById<View>(R.id.pl)
            val de = customView.findViewById<View>(R.id.de)
            val fr = customView.findViewById<View>(R.id.fr)
            val es = customView.findViewById<View>(R.id.es)
            val it = customView.findViewById<View>(R.id.it)
            val pt = customView.findViewById<View>(R.id.pt)
            val ch = customView.findViewById<View>(R.id.ch)

            val content = customView.findViewById<TextView>(R.id.content)
            content.movementMethod = LinkMovementMethod.getInstance()

            val siteUrl = "http://www.scp-wiki.net/"

            val langCode = Locale.getDefault().language

            when (langCode) {
                "ru" -> setContentText(
                        content,
                        getString(R.string.license_ru, siteUrl, siteUrl)
                )
                "en" -> setContentText(
                        content,
                        getString(R.string.license_en, siteUrl, siteUrl)
                )
                "pl" -> setContentText(
                        content,
                        getString(R.string.license_pl, siteUrl, siteUrl)
                )
                "de" -> setContentText(
                        content,
                        getString(R.string.license_de, siteUrl, siteUrl)
                )
                "fr" -> setContentText(
                        content,
                        getString(R.string.license_fr, siteUrl, siteUrl)
                )
                "es" -> setContentText(
                        content,
                        getString(R.string.license_es, siteUrl, siteUrl)
                )
                "it" -> setContentText(
                        content,
                        getString(R.string.license_it, siteUrl, siteUrl)
                )
                "pt" -> setContentText(
                        content,
                        getString(R.string.license_pt, siteUrl, siteUrl)
                )
                "ch" -> setContentText(
                        content,
                        getString(R.string.license_ch, siteUrl, siteUrl)
                )
                else -> setContentText(
                        content,
                        getString(R.string.license_en, siteUrl, siteUrl)
                )
            }

            en.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_en, siteUrl, siteUrl)
                )
            }
            pl.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_pl, siteUrl, siteUrl)
                )
            }
            ru.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_ru, siteUrl, siteUrl)
                )
            }
            de.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_de, siteUrl, siteUrl)
                )
            }
            fr.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_fr, siteUrl, siteUrl)
                )
            }
            es.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_es, siteUrl, siteUrl)
                )
            }
            it.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_it, siteUrl, siteUrl)
                )
            }
            pt.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_pt, siteUrl, siteUrl)
                )
            }
            ch.setOnClickListener {
                setContentText(
                        content,
                        getString(R.string.license_ch, siteUrl, siteUrl)
                )
            }
        }

        return dialog
    }

    companion object {

        val TAG: String = CC3LicenseDialogFragment::class.java.simpleName

        fun newInstance() = CC3LicenseDialogFragment()

        private fun setContentText(content: TextView, text: String) {
            content.text = Html.fromHtml(text)
        }
    }
}
