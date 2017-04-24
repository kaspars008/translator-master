package com.kaspars.mytranslator.api.request;

import com.kaspars.mytranslator.models.TranslationDirection;
import com.kaspars.mytranslator.models.yandextranslator.TranslateResult;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;



public interface YandexTranslator {

    @GET("/translate")
    Observable<TranslateResult> translate(@Query("text") String text, @Query("lang") TranslationDirection lang, @Query("options") int options);
    @GET("/lookup")
    Observable<TranslateResult> lookup(@Query("text") String text, @Query("lang") TranslationDirection lang, @Query("flags") int flag);

}
