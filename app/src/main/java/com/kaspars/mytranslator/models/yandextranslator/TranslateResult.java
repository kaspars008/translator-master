package com.kaspars.mytranslator.models.yandextranslator;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class TranslateResult {

    @SerializedName("code")
    private final int mCode; // from translate api

    @SerializedName("lang")  // from translate api
    private String mLang;

    @SerializedName("text")
    private  List<String> mTexts; // from translate api

    @SerializedName("detected")
    private DetectedLanguage detectedLang; // from translate api

    @SerializedName("def")
    private  List<Definition> definition;  // from dictionary api

    @SerializedName("head")
    Object head;  // from dictionary api

    private String mainTranslationText;

    public static class Definition {
        @SerializedName("text")
        public String word;

        @SerializedName("pos")
        public String partOfSpeech;

        @SerializedName("gen")
        public String gender;

        @SerializedName("tr")
        public List<Translation> translations;
    }

    public static class Translation {
        @SerializedName("text")
        public String word;

        @SerializedName("pos")
        public String partOfSpeech;

        @SerializedName("gen")
        public String gender;

        @SerializedName("syn")
        public List<Synonym> synonyms;

        @SerializedName("mean")
        public List<TranslatedString> meanings;

        @SerializedName("ex")
        public List<Example> examples;
    }

    public static class Synonym {
        @SerializedName("text")
        public String word;
        @SerializedName("pos")
        public String partOfSpeech;
        @SerializedName("gen")
        public String gender;
    }

    public static class Example {
        @SerializedName("text")
        public String word;

        @SerializedName("tr")
        public List<TranslatedString> exampleTranslations;
    }

    public static class TranslatedString {
        @SerializedName("text")
        public String text;
    }

    public TranslateResult(int code, String lang, List<String> texts) {
        super();

        mCode = code;
        mLang = lang;
        mTexts = texts;
    }

    public int getCode() {
        return mCode;
    }

    public String getLang() {
        return mLang;
    }

    public List<String> getTexts() {
        return mTexts;
    }

    public void setLang(String lang) {
        this.mLang = lang;
    }

        public String getMainTranslatedText() {
        String result = "";

        if ( mainTranslationText != null) result = mainTranslationText;

            return result;}

    public String getTextFromTranslateApi() {
        String result = "";

        if ( mTexts != null) {

            if (mTexts.size() > 0) {
                result =  mTexts.get(0);
            } else {
                result =  "";
            }


        }
        return result;
           }


    public List<Definition> getDefinition() {

        return definition;
    }

public String getText() {
    String result = "";
    if (definition != null) {
        if (definition.size() > 0) {
            result = definition.get(0).translations.get(0).word;
        } else {
            result = "";
        }}
 return result;
}

    public DetectedLanguage getDetectedLang() {
        return detectedLang;
    }

    public void setDetectedLang(DetectedLanguage detectedLang) {
        this.detectedLang = detectedLang;
    }

    public void setText(String text) {
        this.mainTranslationText = text;
    }
}
