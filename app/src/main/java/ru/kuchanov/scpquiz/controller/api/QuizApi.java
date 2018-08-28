package ru.kuchanov.scpquiz.controller.api;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ru.kuchanov.scpquiz.controller.api.response.TokenResponse;
import ru.kuchanov.scpquiz.model.api.NwQuiz;

public interface QuizApi {

    @FormUrlEncoded
    @POST("scp-quiz/oauth/token")
    Single<TokenResponse> getAccessToken(
            @Header("Authorization") String authorization,
            @Field("grant_type") String grantType
    );


    @GET("scp-quiz/quiz/all")
    Single<List<NwQuiz>> getNwQuizList(
            @Header("Authorization") String authorization
    );
}

