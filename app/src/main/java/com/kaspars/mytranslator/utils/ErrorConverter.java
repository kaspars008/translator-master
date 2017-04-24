package com.kaspars.mytranslator.utils;

import android.content.Context;

import com.kaspars.mytranslator.R;
import com.kaspars.mytranslator.exception.network.AuthException;
import com.kaspars.mytranslator.exception.network.ConnectionException;

public class ErrorConverter {
    public static String convertError(Context context, Throwable throwable) {
        String errorMessage = null;
        if (throwable instanceof AuthException) {
            errorMessage = context.getResources().getString(R.string.error_auth);
        } else if (throwable instanceof ConnectionException) {
            errorMessage = context.getResources().getString(R.string.error_network);
        }
        return errorMessage;
    }
}
