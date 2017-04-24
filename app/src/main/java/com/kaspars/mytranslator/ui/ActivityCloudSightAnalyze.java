package com.kaspars.mytranslator.ui;

import android.widget.ImageView;

import com.kaspars.mytranslator.App;
import com.kaspars.mytranslator.api.ApiCloudSight;
import com.kaspars.mytranslator.models.cloudsight.CSCheckResultResponse;

import java.io.File;
import java.util.concurrent.TimeUnit;

import retrofit.mime.TypedFile;
import rx.Observable;


public class ActivityCloudSightAnalyze extends ActivityImageAnalyze {

    public static final int MAX_RETRY_COUNT = 50;
    public static final int RETRY_DELAY_SECONDS = 3;
    @Override
    protected Observable<String> analyzeImage(String imagePath, ImageView imageView) {
        TypedFile imageFile = new TypedFile("image/jpeg", new File(imagePath));

        return ApiCloudSight.getInstance().recognize(imageFile,
                App.getInstance().getTranslationDirection().getFrom().getLanguageCode())
                .delay(RETRY_DELAY_SECONDS, TimeUnit.SECONDS)
                .map(csSendImageResponse -> {
                    CSCheckResultResponse checkResultResponse =
                            ApiCloudSight.getInstance().checkResponse(csSendImageResponse.getToken());

                    if (checkResultResponse.getStatus().equals(CSCheckResultResponse.STATUS_NOT_COMPLETED)) {
                        throw new RuntimeException("Analyze not completed");
                    }

                    return checkResultResponse;
                })
                .map(CSCheckResultResponse::getName)
                .retry(MAX_RETRY_COUNT);
    }
}
