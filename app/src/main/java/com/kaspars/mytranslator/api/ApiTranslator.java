package com.kaspars.mytranslator.api;

import com.kaspars.mytranslator.api.request.YandexTranslator;
import com.kaspars.mytranslator.exception.network.AuthException;
import com.kaspars.mytranslator.exception.network.ConnectionException;
import com.kaspars.mytranslator.models.TranslationTask;
import com.kaspars.mytranslator.models.yandextranslator.TranslateResult;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import rx.Observable;



public class ApiTranslator {
    public static final int ENABLE_LANG_DETECTION = 1;
    private static ApiTranslator instance;

    private ApiTranslator() {
        initRestAdapter();
        initRequests();
        initRestAdapterTranslate();
        initRequestsTranslate();
    }

    public static ApiTranslator getInstance() {
        if (instance == null) {
            instance = new ApiTranslator();
        }

        return instance;
    }

    private static final String BASE_URL_TRANSLATE = "https://translate.yandex.net/api/v1.5/tr.json";
    private static final String BASE_URL = "https://dictionary.yandex.net/api/v1/dicservice.json";
    private static final String API_KEY_TRANSLATE = "trnsl.1.1.20170316T151001Z.5f14562719941be3.e11bb639cfa210afc277aa7de3abfa6daa7c8dd8";
   private static final String API_KEY = "dict.1.1.20170415T215724Z.b558d79056d58450.ac01766056544bb270dcd903694780a0f7725917";
    private RestAdapter mRestAdapter;
    private RestAdapter mRestAdapterTranslate;
    private YandexTranslator mYandexTranslator;
    private YandexTranslator mYandexTranslatorTranslate;

    private void initRestAdapter() {
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .setRequestInterceptor(request -> request.addQueryParam("key", API_KEY))
                .setErrorHandler(cause -> {
                    switch(cause.getKind()) {
                        case NETWORK:
                            return new ConnectionException();
                        case HTTP:
                            return new AuthException();
                        default:
                            return new RuntimeException();
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("RETROFIT"))
                .build();
    }

    private void initRequests() {
        mYandexTranslator = mRestAdapter.create(YandexTranslator.class);
    }
    private void initRestAdapterTranslate() {
        mRestAdapterTranslate = new RestAdapter.Builder()
                .setEndpoint(BASE_URL_TRANSLATE)
                .setRequestInterceptor(request -> request.addQueryParam("key", API_KEY_TRANSLATE))
                .setErrorHandler(cause -> {
                    switch(cause.getKind()) {
                        case NETWORK:
                            return new ConnectionException();
                        case HTTP:
                            return new AuthException();
                        default:
                            return new RuntimeException();
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("RETROFIT"))
                .build();
    }

    private void initRequestsTranslate() {
        mYandexTranslatorTranslate = mRestAdapterTranslate.create(YandexTranslator.class);
    }
    public Observable<TranslateResult> lookup(TranslationTask task) {

        return mYandexTranslator.lookup(task.getTextToTranslate(), task.getTranslationDirection(), 2);
    }
    public Observable<TranslateResult> translate(TranslationTask task) {

         return mYandexTranslatorTranslate.translate(task.getTextToTranslate(), task.getTranslationDirection(), ENABLE_LANG_DETECTION);

    }

}
