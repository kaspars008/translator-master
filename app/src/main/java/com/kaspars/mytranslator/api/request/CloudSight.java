package com.kaspars.mytranslator.api.request;

import com.kaspars.mytranslator.models.cloudsight.CSCheckResultResponse;
import com.kaspars.mytranslator.models.cloudsight.CSSendImageResponse;

import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import rx.Observable;

public interface CloudSight {

    @Multipart
    @POST("/image_requests")
    Observable<CSSendImageResponse> recognize(@Part("image_request[image]") TypedFile photo,
                                          @Part("image_request[locale]") String locale,
                                          @Part("image_request[language]") String language);

    @GET("/image_responses/{token}")
    CSCheckResultResponse checkResponse(@Path("token") String token);
}
