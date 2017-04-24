package com.kaspars.mytranslator.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kaspars.mytranslator.App;
import com.kaspars.mytranslator.R;
import com.kaspars.mytranslator.models.TranslationDirection;
import com.kaspars.mytranslator.models.yandextranslator.DetectedLanguage;
import com.kaspars.mytranslator.models.yandextranslator.TranslateResult;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class FragmentTranslationResult extends Fragment {
    @Bind(R.id.result_to)
    protected TextView mResultTo;
    public static String mResultToText = "";
    public static String mLang = "";
    public static int mAddedToFavorites;

    @Bind(R.id.result_language_to)
    protected TextView mLanguageTo;

    @Bind(R.id.add_to_favorites)
    protected ImageButton mAddToFavorites;

    @Bind(R.id.translation_to_layout)
    protected LinearLayout mTranslationLayout;

    @Bind(R.id.container_language_to)
    protected CardView mContainerLanguageTo;


    private FragmentPlayerButton mPlayerFragment;
    private OnDetectedLanguageClickListener mListener;
    private OnAddToFavoritesClickListener mClickListener;
    private DetectedLanguage mCurrentDetectLanguage;

    public FragmentTranslationResult() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof OnDetectedLanguageClickListener) {
            mListener = (OnDetectedLanguageClickListener) getActivity();
            mClickListener = (OnAddToFavoritesClickListener) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_translation_result, container, false);
        ButterKnife.bind(this, view);
        mPlayerFragment = (FragmentPlayerButton) getChildFragmentManager().findFragmentById(R.id.player);
        return view;
    }

    @OnClick(R.id.copy_to_button)
    protected void copyResultToClipboard(View view) {
        if (!mResultTo.getText().toString().isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", mResultTo.getText().toString());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(getActivity(), R.string.text_copied, Toast.LENGTH_SHORT).show();
        }
    }
    @OnClick(R.id.add_to_favorites)
    protected void addResultToFavoritesClicked(View view) {
            mClickListener.onFavoritesClicked();
    }



    public Subscription subscribe(Observable<TranslateResult> observable) {

        return observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TranslateResult>() {

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(TranslateResult translateResult) {
                        showResultCard(translateResult);
                        if (translateResult.getDetectedLang().getLanguage() != null
                                && !translateResult.getDetectedLang().getLanguage().equals(App.getInstance()
                                .getTranslationDirection().getFrom())) {
                            mCurrentDetectLanguage = translateResult.getDetectedLang();
                        } else {
                            mCurrentDetectLanguage = null;
                        }
                        if (mListener != null && mCurrentDetectLanguage != null) {
                                mListener.onDetectedLanguageClicked(mCurrentDetectLanguage);}
                    }
                });
    }
    private void showResultCard(TranslateResult result) {

        mTranslationLayout.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        TranslationDirection translationDirection = App.getInstance().getTranslationDirection();

        mResultToText =  result.getMainTranslatedText();
        mLang =  result.getLang();

        List<TranslateResult.Definition> definition = result.getDefinition();
        result.getDefinition();
        if (definition != null) {
            String wordPrevious = ""; String word = "";
            for (int i = 0; i < definition.size(); i++) {
                View  defView = inflater.inflate(R.layout.def_result, mTranslationLayout, false);
                LinearLayout mTrLayout = (LinearLayout) defView.findViewById(R.id.tr_layout);

                TextView defText = (TextView) defView.findViewById(R.id.defText);
                TextView defPos = (TextView) defView.findViewById(R.id.defPos);
                TextView defGen = (TextView) defView.findViewById(R.id.defGen);
                LinearLayout defLayout = (LinearLayout) defView.findViewById(R.id.defLayout);
                if (i>0) wordPrevious = word;
                word = definition.get(i).word;
                if ((!word.equals(wordPrevious) && i>0) || i==0) {
                    defText.setText(word);
                    defGen.setText(word);
                } else {
                    defLayout.removeAllViews();}

                defPos.setText(definition.get(i).partOfSpeech);
                defGen.setText(definition.get(i).gender);
                mTranslationLayout.addView(defView);
                List<TranslateResult.Translation> translations = definition.get(i).translations;
                for (int j = 0; j < translations.size(); j++) {
                     View trView = inflater.inflate(R.layout.tr_result, mTrLayout, false);
                    LinearLayout trLayout = (LinearLayout) trView.findViewById(R.id.trLayout);
                    TextView meanText = (TextView) trView.findViewById(R.id.meanText);
                   TextView trText = (TextView) trView.findViewById(R.id.trText);
                    List<TranslateResult.Synonym> synonyms = translations.get(j).synonyms;
                    String mGenderText = "";
                    if  (translations.get(j).gender !=null) mGenderText = translations.get(j).gender;
                    String mSynWord = ""; String mSynText = "";
                    if (synonyms != null && !synonyms.isEmpty()) {
                        for (int k = 0; k < synonyms.size(); k++) {
                            mSynWord = ", " + synonyms.get(k).word;
                            mSynText = mSynText + mSynWord;
                    }}
                    String translationText;
                    if (translations.size() > 1){
                    translationText = String.valueOf(j+1)+"  "+translations.get(j).word;} else
                    {translationText = "   "+translations.get(j).word;}

                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(translationText);

                    if (!mGenderText.isEmpty()) {
                        SpannableString genderText = new SpannableString(" " + mGenderText);
                        genderText.setSpan(new ForegroundColorSpan(Color.parseColor("#a1abb5")), 0, genderText.length(), 0);
                        genderText.setSpan(new StyleSpan(Typeface.ITALIC), 0, genderText.length(), 0);
                        builder.append(genderText);
                    }

                    builder.append(mSynText);

                    trText.setText(builder, TextView.BufferType.SPANNABLE);

                    List<TranslateResult.TranslatedString> meanings = translations.get(j).meanings;
                    if (meanings != null && !meanings.isEmpty()) {
                    String mMeanWord = ""; String mMeanText = "";

                        for (int k = 0; k < meanings.size(); k++) {
                        if (meanings.size() > 1){
                        if (k==0) mMeanWord = "("+meanings.get(k).text + ", ";
                        if (k==(meanings.size()-1)) mMeanWord = meanings.get(k).text + ")";
                        if (k>0 && k<(meanings.size()-1)) mMeanWord = meanings.get(k).text + ", ";
                        } else
                            mMeanWord = "("+meanings.get(k).text + ")";
                            mMeanText = mMeanText + mMeanWord;
                        }
                        meanText.setText(mMeanText);
                } else trLayout.removeView(meanText);

                    mTrLayout.addView(trView);
                }
            }

        }
        mLanguageTo.setText(translationDirection.getTo().getName().toUpperCase());
        mResultTo.setText(result.getMainTranslatedText());
        mPlayerFragment.setAudioUrl(mResultTo.getText().toString(), App.getInstance().getTranslationDirection().getTo().getLanguageCode());
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void updateFavorites(int isAddedToFavorites) {
        mAddedToFavorites =  isAddedToFavorites;
        mAddToFavorites.setImageResource((isAddedToFavorites == 1 || isAddedToFavorites == 2) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }


    public interface OnDetectedLanguageClickListener {
        void onDetectedLanguageClicked(DetectedLanguage detectedLanguage);
    }

    public interface OnAddToFavoritesClickListener {

        void onFavoritesClicked();
    }
}
