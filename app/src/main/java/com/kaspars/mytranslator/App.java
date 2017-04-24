package com.kaspars.mytranslator;

import com.kaspars.mytranslator.models.Language;
import com.kaspars.mytranslator.models.TranslationDirection;

import java.util.List;



public class App {
    private final static App ourInstance = new App();
    private TranslationDirection mTranslationDirection;

    public static App getInstance() {
         return ourInstance;
    }

    private App() {
        List<Language> languages = Languages.getInstance().getLanguages();
        mTranslationDirection = new TranslationDirection(
                languages.get(0),
                languages.get(1)
        );
    }

    public void setTranslationDirection(TranslationDirection direction) {
        mTranslationDirection = direction;
    }

    public TranslationDirection getTranslationDirection() {
        return mTranslationDirection;
    }
}
