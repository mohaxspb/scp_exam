package ru.kuchanov.scpquiz.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import javax.inject.Inject;

import ru.kuchanov.scpquiz.R;
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager;
import ru.kuchanov.scpquiz.di.Di;
import toothpick.Toothpick;

public class CC3LicenseDialogFragment extends DialogFragment {

    public static final String TAG = CC3LicenseDialogFragment.class.getSimpleName();

    @Inject
    MyPreferenceManager mMyPreferenceManager;

    public static DialogFragment newInstance() {
        return new CC3LicenseDialogFragment();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toothpick.inject(this, Toothpick.openScope(Di.Scope.APP));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        //icons from https://github.com/hjnilsson/country-flags/tree/master/svg

        final MaterialDialog.Builder dialogTextSizeBuilder = new MaterialDialog.Builder(getActivity());
        dialogTextSizeBuilder
                .customView(R.layout.dialog_cc3_license, false)
                .title(R.string.attention)
                .cancelable(false)
                .positiveText(R.string.i_accept)
                .negativeText(android.R.string.no)
                .onPositive((dialog1, which) -> {
                    mMyPreferenceManager.setPersonalDataAccepted(true);
                    dismissAllowingStateLoss();
                })
                .onNegative((dialog1, which) -> {
                    mMyPreferenceManager.setPersonalDataAccepted(false);
                    getActivity().finish();
                });

        final MaterialDialog dialog = dialogTextSizeBuilder.build();

        if (dialog.getCustomView() != null) {
            final View en = dialog.getCustomView().findViewById(R.id.en);
            final View ru = dialog.getCustomView().findViewById(R.id.ru);
            final View pl = dialog.getCustomView().findViewById(R.id.pl);
            final View de = dialog.getCustomView().findViewById(R.id.de);
            final View fr = dialog.getCustomView().findViewById(R.id.fr);
            final View es = dialog.getCustomView().findViewById(R.id.es);
            final View it = dialog.getCustomView().findViewById(R.id.it);
            final View pt = dialog.getCustomView().findViewById(R.id.pt);
            final View ch = dialog.getCustomView().findViewById(R.id.ch);

            final TextView content = dialog.getCustomView().findViewById(R.id.content);
            content.setMovementMethod(LinkMovementMethod.getInstance());

            final String siteUrl = "http://www.scp-wiki.net/";

            final String langCode = Locale.getDefault().getLanguage();

            switch (langCode) {
                case "ru":
                    setContentText(content, getString(R.string.license_ru, siteUrl, siteUrl));
                    break;
                case "en":
                    setContentText(content, getString(R.string.license_en, siteUrl, siteUrl));
                    break;
                case "pl":
                    setContentText(content, getString(R.string.license_pl, siteUrl, siteUrl));
                    break;
                case "de":
                    setContentText(content, getString(R.string.license_de, siteUrl, siteUrl));
                    break;
                case "fr":
                    setContentText(content, getString(R.string.license_fr, siteUrl, siteUrl));
                    break;
                case "es":
                    setContentText(content, getString(R.string.license_es, siteUrl, siteUrl));
                    break;
                case "it":
                    setContentText(content, getString(R.string.license_it, siteUrl, siteUrl));
                    break;
                case "pt":
                    setContentText(content, getString(R.string.license_pt, siteUrl, siteUrl));
                    break;
                case "ch":
                    setContentText(content, getString(R.string.license_ch, siteUrl, siteUrl));
                    break;
                default:
                    throw new IllegalArgumentException("unexpected lang: " + langCode);
            }

            en.setOnClickListener(view -> setContentText(content, getString(R.string.license_en, siteUrl, siteUrl)));
            pl.setOnClickListener(view -> setContentText(content, getString(R.string.license_pl, siteUrl, siteUrl)));
            ru.setOnClickListener(view -> setContentText(content, getString(R.string.license_ru, siteUrl, siteUrl)));
            de.setOnClickListener(view -> setContentText(content, getString(R.string.license_de, siteUrl, siteUrl)));
            fr.setOnClickListener(view -> setContentText(content, getString(R.string.license_fr, siteUrl, siteUrl)));
            es.setOnClickListener(view -> setContentText(content, getString(R.string.license_es, siteUrl, siteUrl)));
            it.setOnClickListener(view -> setContentText(content, getString(R.string.license_it, siteUrl, siteUrl)));
            pt.setOnClickListener(view -> setContentText(content, getString(R.string.license_pt, siteUrl, siteUrl)));
            ch.setOnClickListener(view -> setContentText(content, getString(R.string.license_ch, siteUrl, siteUrl)));
        }
        return dialog;
    }

    private static void setContentText(final TextView content, final String text) {
        content.setText(Html.fromHtml(text));
    }
}
