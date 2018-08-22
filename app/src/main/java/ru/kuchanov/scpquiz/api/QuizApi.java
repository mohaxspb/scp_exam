package ru.kuchanov.scpquiz.api;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import ru.kuchanov.scpquiz.api.response.TokenResponse;
import ru.kuchanov.scpquiz.model.api.NwQuiz;

public interface QuizApi {
    @FormUrlEncoded
    @POST("scp-quiz/oauth/token")
    Observable<TokenResponse> getAccessToken(
            @Header("Authorization") String authorization,
            @Field("grant_type") String grantType
    );


    @GET("scp-quiz/quiz/all")
    Observable<List<NwQuiz>> getNwQuizList(
            @Header("Authorization") String authorization
    );
}

