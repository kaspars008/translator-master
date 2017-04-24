package com.kaspars.mytranslator.api;

import com.kaspars.mytranslator.api.request.CloudSight;
import com.kaspars.mytranslator.exception.network.AuthException;
import com.kaspars.mytranslator.exception.network.ConnectionException;
import com.kaspars.mytranslator.models.cloudsight.CSCheckResultResponse;
import com.kaspars.mytranslator.models.cloudsight.CSSendImageResponse;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.mime.TypedFile;
import rx.Observable;


public class ApiCloudSight {
    private static ApiCloudSight instance;

    private ApiCloudSight() {
        initRestAdapter();
        initRequests();
    }

    public static ApiCloudSight getInstance() {
        if (instance == null) {
            instance = new ApiCloudSight();
        }

        return instance;
    }

    private static final String BASE_URL = "http://api.cloudsightapi.com";

    private static final String API_KEY = "8Ykx4fYMwwoZw8MGCaqu5g";

    private static final String DEFAULT_LOCATION = "en-US";


    private RestAdapter mRestAdapter;
    private CloudSight mCloudSightRecognizer;

    private void initRestAdapter() {
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .setRequestInterceptor(request -> request.addHeader("Authorization", "CloudSight " + API_KEY))
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
        mCloudSightRecognizer = mRestAdapter.create(CloudSight.class);
    }

    public Observable<CSSendImageResponse> recognize(TypedFile file, String language) {
        return mCloudSightRecognizer.recognize(file, DEFAULT_LOCATION, language);
    }

    public CSCheckResultResponse checkResponse(String token) {
        return mCloudSightRecognizer.checkResponse(token);
    }
}
