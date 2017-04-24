package com.kaspars.mytranslator.models.yandextranslator;

import com.google.gson.annotations.SerializedName;
import com.kaspars.mytranslator.models.Language;



public class DetectedLanguage {
    @SerializedName("lang")
    private final String code;

    public DetectedLanguage(String lang) {
        this.code = lang;
    }

    public String getCode() {
        return code;
    }

    public Language getLanguage() {
        if (code != null && code.length() > 0) {
            return new Language(code);
        } else {
            return null;
        }
    }
}
