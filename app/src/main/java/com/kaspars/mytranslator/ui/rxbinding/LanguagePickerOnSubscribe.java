package com.kaspars.mytranslator.ui.rxbinding;

import com.jakewharton.rxbinding.internal.MainThreadSubscription;
import com.kaspars.mytranslator.models.TranslationDirection;
import com.kaspars.mytranslator.ui.fragment.FragmentLanguagePicker;

import rx.Observable;
import rx.Subscriber;

public class LanguagePickerOnSubscribe implements Observable.OnSubscribe<TranslationDirection> {
    private final FragmentLanguagePicker mFragmentLanguagePicker;

    public LanguagePickerOnSubscribe(FragmentLanguagePicker fragmentLanguagePicker) {
        mFragmentLanguagePicker = fragmentLanguagePicker;
    }

    @Override
    public void call(Subscriber<? super TranslationDirection> subscriber) {
       FragmentLanguagePicker.DirectionChangedListener listener = subscriber::onNext;
                mFragmentLanguagePicker.addDirectionChangedListener(listener);

        subscriber.add(new MainThreadSubscription() {
            @Override
            protected void onUnsubscribe() {
                if (!subscriber.isUnsubscribed()) {
                    mFragmentLanguagePicker.removeDirectionChangedListener(listener);
                }
            }
        });


    }
}
