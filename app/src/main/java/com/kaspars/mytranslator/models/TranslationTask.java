package com.kaspars.mytranslator.models;



public class TranslationTask {
    private final String mTextToTranslate;
    private final TranslationDirection mTranslationDirection;

    public TranslationTask(String textToTranslate, TranslationDirection translationDirection) {
        mTextToTranslate = textToTranslate;
        mTranslationDirection = translationDirection;
    }

    public String getTextToTranslate() {
        return mTextToTranslate;
    }

    public TranslationDirection getTranslationDirection() {
        return mTranslationDirection;
    }
}
