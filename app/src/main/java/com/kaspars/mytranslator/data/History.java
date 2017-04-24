package com.kaspars.mytranslator.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.impl.DefaultStorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.DeleteQuery;
import com.pushtorefresh.storio.sqlite.queries.Query;
import com.pushtorefresh.storio.sqlite.queries.UpdateQuery;

import java.util.List;

import rx.Observable;


public class History {
    public static final String DB_NAME = "history.db";
    private static StorIOSQLite mStorIOSQLite;

    public static String whereStatement;
    public static String orderByStatement;




    public static StorIOSQLite getStorIO(Context context) {
        if (mStorIOSQLite != null) {
            return mStorIOSQLite;
        }

        mStorIOSQLite = DefaultStorIOSQLite.builder()
                .sqliteOpenHelper(new OpenHelper(context))
                .addTypeMapping(HistoryItem.class, SQLiteTypeMapping.<HistoryItem>builder()
                        .putResolver(new HistoryItemStorIOSQLitePutResolver() {
                        })
                        .getResolver(new HistoryItemStorIOSQLiteGetResolver())
                        .deleteResolver(new HistoryItemStorIOSQLiteDeleteResolver())
                        .build())

                .build();
        return mStorIOSQLite;
    }


    public static Observable<List<HistoryItem>> getLastItems(Context context) {
        return getStorIO(context)
                .get()
                .listOfObjects(HistoryItem.class)
                .withQuery(Query.builder()
                        .table("history")

                        .orderBy(orderByStatement)

                        .where(whereStatement)

                        .build())
                .prepare()
                .asRxObservable();//createObservable();
    }

    public static List<HistoryItem> getItems(Context context, String whereStatement) {
        return getStorIO(context)
                .get()
                .listOfObjects(HistoryItem.class)
                .withQuery(Query.builder()
                        .table("history")

                        .where(whereStatement)

                        .build())
                .prepare()
                .executeAsBlocking();
    }
    public static void putObject(Context context, HistoryItem historyItem) {
        getStorIO(context)
                .put()
                .object(historyItem)
                .prepare()
                .executeAsBlocking();
    }
    public static void updateItems(Context context, HistoryItem historyItem, String whereStatement) {

        getStorIO(context)
        .put()
        .object(historyItem)
        .withPutResolver(new HistoryItemStorIOSQLitePutResolver() {
            @Override
            @NonNull
            protected UpdateQuery mapToUpdateQuery(@NonNull HistoryItem object) {

                return UpdateQuery.builder()
                        .table("history")

                        .where(whereStatement)

                        .build();
            }

            @Override
            @NonNull
            public ContentValues mapToContentValues(@NonNull HistoryItem object) {
                ContentValues contentValues;
                contentValues = new ContentValues();
                contentValues.put("is_added_to_favorites", object.isAddedToFavorites);
                if (object.isAddedToFavorites == 1) contentValues.put("date_favorites", object.dateFavorites);
                return contentValues;
            }
        })
                .prepare()
                .executeAsBlocking();
    }
    public static void deleteObject(Context context, HistoryItem historyItem) {
        getStorIO(context)
                .delete().object(historyItem)
                .prepare()
                .executeAsBlocking();
    }

    public static void deleteItems(Context context, String whereStatement) {
        getStorIO(context)
                .delete()
                .byQuery(DeleteQuery.builder()
                        .table("history")
                        .where(whereStatement)

                        .build())
                .prepare()
                .executeAsBlocking();
    }
    private static class OpenHelper extends SQLiteOpenHelper {
        public OpenHelper(Context context) {
            super(context, DB_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table history ("
                    + "id text primary key,"
                    + "date_history integer,"
                    + "date_favorites integer,"
                    + "original text,"
                    + "translate text,"
                    + "lang text,"
                    + "is_added_to_favorites integer"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
