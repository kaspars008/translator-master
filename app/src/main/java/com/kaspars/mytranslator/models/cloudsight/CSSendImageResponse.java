package com.kaspars.mytranslator.models.cloudsight;

public class CSSendImageResponse {

    private String url;
    private String token;

    public CSSendImageResponse(String url, String token) {
        this.url = url;
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
