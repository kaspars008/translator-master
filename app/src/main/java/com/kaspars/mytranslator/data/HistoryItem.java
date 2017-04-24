package com.kaspars.mytranslator.data;

import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

@StorIOSQLiteType(table = "history")
public  class HistoryItem {
    @StorIOSQLiteColumn(name = "id", key = true)
    String id;

    @StorIOSQLiteColumn(name = "date_history")
    long dateHistory;

    @StorIOSQLiteColumn(name = "date_favorites")
    long dateFavorites;

    @StorIOSQLiteColumn(name = "original")
    String original;

    @StorIOSQLiteColumn(name = "translate")
    String translate;


    @StorIOSQLiteColumn(name = "lang")
    String lang;

    @StorIOSQLiteColumn(name = "is_added_to_favorites")
    int isAddedToFavorites;


     HistoryItem() {}
    public HistoryItem(int isAddedToFavorites) {
        this.dateFavorites = System.currentTimeMillis();
        this.isAddedToFavorites = isAddedToFavorites;


    }
    public HistoryItem(String original, String lang, int isAddedToFavorites) {
        this.dateFavorites = System.currentTimeMillis();
        this.dateHistory = System.currentTimeMillis();
        this.isAddedToFavorites = isAddedToFavorites;
        this.original = original;

        this.lang = lang;

        this.id = lang + original;

    }

    public HistoryItem(String original, String translate, String lang, int isAddedToFavorites) {
        this.dateHistory = System.currentTimeMillis();
        this.original = original;
        this.translate = translate;
        this.lang = lang;
        this.isAddedToFavorites = isAddedToFavorites;
        this.id = lang + original;

    }

    public String getOriginal() {
        return original;
    }

    public String getTranslate() {
        return translate;
    }

    public int getIsAddedToFavorites() {
        return isAddedToFavorites;
    }

    public String getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }


}