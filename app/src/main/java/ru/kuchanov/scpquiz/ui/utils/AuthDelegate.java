package ru.kuchanov.scpquiz.ui.utils;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ru.kuchanov.scpquiz.Constants;
import ru.kuchanov.scpquiz.R;
import ru.kuchanov.scpquiz.controller.api.ApiClient;
import ru.kuchanov.scpquiz.controller.manager.preference.MyPreferenceManager;
import ru.kuchanov.scpquiz.model.CommonUserData;
import ru.kuchanov.scpquiz.mvp.AuthPresenter;
import ru.kuchanov.scpquiz.mvp.AuthView;
import ru.kuchanov.scpquiz.mvp.presenter.BasePresenter;
import ru.kuchanov.scpquiz.ui.BaseFragment;
import timber.log.Timber;

public class AuthDelegate<T extends BaseFragment<? extends AuthView, ? extends BasePresenter<? extends AuthView>>> {

    private static final int REQUEST_CODE_GOOGLE = 11;
    ApiClient apiClient;
    MyPreferenceManager preferences;
    private AuthPresenter authPresenter;
    private T fragment;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CallbackManager callbackManager = CallbackManager.Factory.create();
    private GoogleApiClient googleApiClient;

    public AuthDelegate(
            T fragment,
            AuthPresenter authPresenter,
            ApiClient apiClient,
            MyPreferenceManager preferences
    ) {
        this.fragment = fragment;
        this.authPresenter = authPresenter;
        this.apiClient = apiClient;
        this.preferences = preferences;
    }

    public BaseFragment<? extends AuthView, ? extends BasePresenter<? extends AuthView>> getFragment() {
        return fragment;
    }

    public void onViewCreated(FragmentActivity fragmentActivity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(fragmentActivity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(fragmentActivity)
                .enableAutoManage(fragmentActivity, connectionResult -> Timber.d("ERROR"))
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        fbRegisterCallback();
    }

    public void startGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        ((AuthView) fragment).startGoogleLogin(signInIntent, fragment);
    }

    private void fbRegisterCallback() {
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        socialLogin(Constants.Social.FACEBOOK, loginResult.getAccessToken().getToken());
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Timber.d("ON ERROR FB :%s", exception.toString());
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                CommonUserData commonUserData = new CommonUserData();
                commonUserData.setEmail(vkAccessToken.email);
                commonUserData.setId(vkAccessToken.userId);
                VKRequest request = VKApi.users().get();
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        //noinspection unchecked
                        VKApiUserFull user = ((VKList<VKApiUserFull>) response.parsedModel).get(0);
                        commonUserData.setFirstName(user.first_name);
                        commonUserData.setLastName(user.last_name);
                        commonUserData.setAvatarUrl(user.photo_200);
                        commonUserData.setFullName(user.first_name + "" + user.last_name);
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        socialLogin(Constants.Social.VK, gson.toJson(commonUserData));
                    }

                    @Override
                    public void onError(VKError error) {
                        Timber.e(error.errorMessage);
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {

                    }
                });
            }

            @Override
            public void onError(VKError error) {
                Timber.d("Error ; %s", error.toString());
            }
        })) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_GOOGLE:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    socialLogin(Constants.Social.GOOGLE, result.getSignInAccount().getIdToken());
                } else {
                    Timber.e("ERROR : %s", result.getStatus());
                }
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void socialLogin(Constants.Social socialName, String data) {
        compositeDisposable.add(apiClient.socialLogin(socialName, data)
                .doOnSuccess(tokenResponse -> {
                    preferences.setAccessToken(tokenResponse.getAccessToken());
                    preferences.setRefreshToken(tokenResponse.getRefreshToken());
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tokenResponse -> authPresenter.onAuthSuccess(),
                        error -> {
                            Timber.e(error);
                            fragment.showMessage(error.toString());
                        }));
    }

    public void onPause() {
        if (googleApiClient != null) {
            //noinspection ConstantConditions
            googleApiClient.stopAutoManage(fragment.getActivity());
            googleApiClient.disconnect();
        }
    }
}