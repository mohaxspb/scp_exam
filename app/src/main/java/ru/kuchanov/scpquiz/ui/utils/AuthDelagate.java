package ru.kuchanov.scpquiz.ui.utils;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.kuchanov.scpquiz.Constants;
import ru.kuchanov.scpquiz.model.CommonUserData;
import timber.log.Timber;

public class AuthDelagate {

    public AuthDelagate() {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                CommonUserData commonUserData = new CommonUserData();
                commonUserData.getEmail() = vkAccessToken.email;
                commonUserData.getId() = vkAccessToken.userId;
                VKRequest request = VKApi.users().get();
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        VKApiUserFull user = ((VKList<VKApiUserFull>) response.parsedModel).get(0);
                        commonUserData.firstName = user.first_name;
                        commonUserData.lastName = user.last_name;
                        commonUserData.avatarUrl = user.photo_200;
                        commonUserData.fullName = user.first_name + "" + user.last_name;
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        compositeDisposable.add(apiClient.loginSocial(Constants.Social.VK, gson.toJson(commonUserData))
                                .doOnSuccess(tokenResponse -> {
                                    preferences.setAccessToken(tokenResponse.accessToken);
                                    preferences.setRefreshToken(tokenResponse.refreshToken);

                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(tokenResponse -> router.navigateTo(Constants.ALL_QUIZ_SCREEN),
                                        error -> getViewState().showError(error.toString()))
                        );
                    }

                    @Override
                    public void onError(VKError error) {
                        getViewState().showError(error.toString());
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {

                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Timber.d("Error ; %s", error.toString());
                getViewState().showError(error.toString());
            }
        })) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_GOOGLE:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    compositeDisposable.add(apiClient.loginSocial(Constants.GOOGLE, result.getSignInAccount().getIdToken())
                            .doOnSuccess(tokenResponse -> {
                                preferences.setAccessToken(tokenResponse.accessToken);
                                preferences.setRefreshToken(tokenResponse.refreshToken);
                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(tokenResponse -> router.navigateTo(Constants.ALL_QUIZ_SCREEN)));
                } else Timber.d("ERROR : %s", result.getStatus());
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

}
