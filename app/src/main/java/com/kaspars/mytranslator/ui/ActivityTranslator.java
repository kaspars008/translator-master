package com.kaspars.mytranslator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.kaspars.mytranslator.App;
import com.kaspars.mytranslator.R;
import com.kaspars.mytranslator.api.ApiTranslator;
import com.kaspars.mytranslator.data.History;
import com.kaspars.mytranslator.data.HistoryItem;
import com.kaspars.mytranslator.models.TranslationDirection;
import com.kaspars.mytranslator.models.TranslationTask;
import com.kaspars.mytranslator.models.yandextranslator.DetectedLanguage;
import com.kaspars.mytranslator.models.yandextranslator.TranslateResult;
import com.kaspars.mytranslator.ui.fragment.FragmentLanguagePicker;
import com.kaspars.mytranslator.ui.fragment.FragmentTranslationResult;
import com.kaspars.mytranslator.ui.widget.EditTextBackEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ActivityTranslator extends AppCompatActivity implements FragmentTranslationResult.OnDetectedLanguageClickListener,
        FragmentTranslationResult.OnAddToFavoritesClickListener
{
    public static final String ORIGINAL_TEXT_EXTRA = "text";
    private static final int REQUEST_CODE_EDIT_TEXT_ACTIVITY = 0;
    private static final int REQUEST_CODE_TEXT_RECOGNIZE_ACTIVITY= 1;
    private static final int REQUEST_CODE_ANALYZE_PHOTO_ACTIVITY = 2;
    private boolean isKeyboardOpen = false;
    TranslationDirection  direction;

    @Bind(R.id.original_text_input_container)
    protected LinearLayout mLanguageInputContainer;

    @Bind(R.id.original_text_input)
    protected EditTextBackEvent mOriginalTextInput;

    @Bind(R.id.activity_translator_close_button)
    protected ImageView mClearButton;

    @Bind(R.id.loading_progress_bar)
    protected ProgressBar mLoadingProgressBar;

    @Bind(R.id.result_container)
    protected View mResultContainer;

    @Bind(R.id.scroll)
    protected ScrollView mScrollView;

    @Bind(R.id.toolbar)
    protected View mToolbarContainer;

    private FragmentLanguagePicker mLanguagePicker;
    private Subscription mSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_translator);

        ButterKnife.bind(this);
        mOriginalTextInput.setOnClickListener(view -> {isKeyboardOpen = true;});

        mLanguagePicker = (FragmentLanguagePicker) getFragmentManager().findFragmentById(R.id.language_picker);

        Observable<TranslateResult> resultObservable1 = getTranslationTaskObservable()
                .debounce(350, TimeUnit.MILLISECONDS)
                .filter(translationTask -> translationTask.getTextToTranslate().length() > 0)
                .doOnNext(translationTask -> {setProgress(true); direction = translationTask.getTranslationDirection();} )
                .switchMap(task -> ApiTranslator.getInstance().lookup(task))
                .doOnNext(result -> {setProgress(false); result.setLang(direction.toString());})
                .share();
        Observable<TranslateResult> resultObservable2 = getTranslationTaskObservable()
                .debounce(350, TimeUnit.MILLISECONDS)
                .filter(translationTask -> translationTask.getTextToTranslate().length() > 0)
                .doOnNext(translationTask -> {setProgress(true); direction = translationTask.getTranslationDirection();} )
                .switchMap(task -> ApiTranslator.getInstance().translate(task))
                .doOnNext(result -> {setProgress(false); result.setLang(direction.toString());})
                .share();

        Observable<TranslateResult> resultObservable = Observable.combineLatest(
                resultObservable1,
                resultObservable2,
                (rezult1, rezult2) -> {
                    TranslateResult rezult = null;
                    if (rezult1.getText().isEmpty()) {
                        rezult = rezult2;
                        rezult.setText(rezult2.getTextFromTranslateApi());
                        }
                    else {rezult = rezult1;
                        rezult.setDetectedLang(rezult2.getDetectedLang());
                        rezult.setText(rezult2.getTextFromTranslateApi());
                    }
                     return rezult;
                }
        ).share();

        History.whereStatement = "is_added_to_favorites = 1 OR is_added_to_favorites = 2";

        History.orderByStatement = "date_favorites DESC";
        Observable<HistoryItem> favoritesObservable = getFavoritesTaskObservable(resultObservable, History.getLastItems(this));
        FragmentTranslationResult translationCard = (FragmentTranslationResult) getSupportFragmentManager().findFragmentById(R.id.translation_result_card);
        Subscription translationCardSubscription = translationCard.subscribe(resultObservable);
        Subscription favoritesSubscription = favoritesObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(item -> {
        translationCard.updateFavorites(item.getIsAddedToFavorites());});
        Subscription saveToHistorySubscription = subscribeHistorySave(resultObservable);
        Subscription hideResultSubscription = subscribeHideResult();


        mOriginalTextInput.setOnEditTextImeBackListener((ctrl, text) -> {
            boolean areTextFieldsEmpty = (FragmentTranslationResult.mResultToText.length() > 0 && text.length() > 0);
            if (isKeyboardOpen && areTextFieldsEmpty) {
            saveFavorites(text, FragmentTranslationResult.mResultToText.toString(), FragmentTranslationResult.mLang, FragmentTranslationResult.mAddedToFavorites);}
            isKeyboardOpen = false;
        });

        mSubscription = new CompositeSubscription(
                translationCardSubscription,
                favoritesSubscription,
                hideResultSubscription,
                saveToHistorySubscription
        );
    }
   private Subscription  subscribeHistorySave(Observable<TranslateResult> resultObservable) {
   return    resultObservable
                .debounce(1000, TimeUnit.MILLISECONDS)
                .map(result -> new HistoryItem(
                        mOriginalTextInput.getText().toString(),
                        result.getMainTranslatedText(),
                        result.getLang(),
                        FragmentTranslationResult.mAddedToFavorites
                ))
                .filter(item -> item.getOriginal().length() > 0 && item.getTranslate().length() > 0)
                .filter(item -> !isKeyboardOpen)
                .subscribe(historyItem -> {
                    History.putObject(ActivityTranslator.this, historyItem);
                });
    }
    private Subscription subscribeHideResult() {
        return getTranslationTaskObservable()
                .filter(translationTask -> translationTask.getTextToTranslate().length() == 0)
                .subscribe(translationTask -> {
                    mResultContainer.setVisibility(View.GONE);
                });
    }
    @OnClick(R.id.main_bottom_button_image)
    protected void captureImage() {
        Intent intent = new Intent(this, ActivityCloudSightAnalyze.class);
        startActivityForResult(intent, REQUEST_CODE_ANALYZE_PHOTO_ACTIVITY);
    }
    @OnClick(R.id.main_bottom_button_mic)
    protected void speechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, App.getInstance().getTranslationDirection().getFrom().getLanguageCode());
        try {
            startActivityForResult(intent, REQUEST_CODE_TEXT_RECOGNIZE_ACTIVITY );
        } catch (Exception ignored) {}
    }
    @OnClick(R.id.translator_history)
         protected void goToHistory() {
        Intent intent = new Intent(getApplicationContext(), ActivityHistoryFavofites.class);
        startActivityForResult(intent, REQUEST_CODE_EDIT_TEXT_ACTIVITY);
    }

    @OnClick(R.id.activity_translator_close_button)
    protected void onCloseClicked(View view) {
        mOriginalTextInput.setText("");
    }

    public Observable<TranslationTask> getTranslationTaskObservable() {
        Observable<String> originalTextObservable = RxTextView.textChanges(mOriginalTextInput).map(CharSequence::toString);
        Observable<TranslationDirection> languageDirectionObservable = mLanguagePicker.getObservable();

        return Observable.combineLatest(
                languageDirectionObservable,
                originalTextObservable,
                (direction, text) -> new TranslationTask(text, direction)
        );
    }

    public Observable<HistoryItem> getFavoritesTaskObservable(Observable<TranslateResult> resultObservable, Observable<List<HistoryItem>> itemsObservable) {

        return Observable.combineLatest(
                resultObservable,
                itemsObservable,
                (translateResult, itemList) -> new HistoryItem(
                        mOriginalTextInput.getText().toString(),
                        translateResult.getMainTranslatedText(),
                        translateResult.getLang(), isListContainId(itemList, translateResult.
                        getLang() + mOriginalTextInput.getText().toString())
                ));
    }
    private void setProgress(boolean progress) {
        runOnUiThread(() -> {
            if (progress) {
                mLoadingProgressBar.setVisibility(View.VISIBLE);
                mResultContainer.setVisibility(View.GONE);
            } else {
                mLoadingProgressBar.setVisibility(View.GONE);
                mResultContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOriginalTextInput.setSelection(mOriginalTextInput.getText().length());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSubscription.unsubscribe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_EDIT_TEXT_ACTIVITY:
                    mOriginalTextInput.setText(data.getStringExtra(ORIGINAL_TEXT_EXTRA));
                    break;

                case REQUEST_CODE_ANALYZE_PHOTO_ACTIVITY:
                    mOriginalTextInput.setText(data.getStringExtra(ActivityImageAnalyze.ARG_ANALYZE_RESULT));
                    break;

                case REQUEST_CODE_TEXT_RECOGNIZE_ACTIVITY:
                    ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (thingsYouSaid != null && !thingsYouSaid.isEmpty()) {
                        mOriginalTextInput.setText(thingsYouSaid.get(0));
                    }
                    break;
            }
        }
    }
    @Override
    public void onDetectedLanguageClicked(DetectedLanguage detectedLanguage) {
        TranslationDirection currentDirection = App.getInstance().getTranslationDirection();
        if (currentDirection.getTo().equals(detectedLanguage.getLanguage())) {
            currentDirection.swap();
            App.getInstance().setTranslationDirection(currentDirection);
        } else {
            App.getInstance().setTranslationDirection(new TranslationDirection(detectedLanguage.getLanguage(), currentDirection.getTo()));
        }
        mLanguagePicker.updateDirection();
    }
    @Override
    public void onFavoritesClicked() {
        int add = 1;
        if (FragmentTranslationResult.mAddedToFavorites == 1) add = 0;
        saveFavorites(mOriginalTextInput
                .getText().toString(), FragmentTranslationResult.mResultToText.toString(), FragmentTranslationResult.mLang, add);
    }
    public void saveFavorites(String textInput, String mResultToText, String mLang, int isAdded) {
    new Thread(() -> History.putObject(ActivityTranslator.this, new HistoryItem(textInput, mResultToText, mLang, isAdded))).start();
    }
    public static int isListContainId(List<HistoryItem> arraylist, String text) {
        int result = 0;
        if (arraylist !=null){
        for (HistoryItem arr : arraylist) {
            if (arr.getId().toLowerCase().equals(text) && (arr.getIsAddedToFavorites() == 1 || arr.getIsAddedToFavorites() == 2)) {
               result = 1;
                break;
           }
        }}
        return result;
    }
    @Override
    protected void onPause() {
        super.onPause();
        isKeyboardOpen = false;
    }
}
