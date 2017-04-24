package com.kaspars.mytranslator.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kaspars.mytranslator.R;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.yandex.speechkit.SpeechKit;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;

public class FragmentPlayerButton extends Fragment implements VocalizerListener {
    private static final String API_KEY = "5b2c00bf-0db0-4807-9ae5-e3b6e22415b3";
    private Vocalizer vocalizer;
    private final String[] acceptedLanguages = {"en", "ru", "tr", "uk"};
    @Bind(R.id.button)
    protected ImageButton mButton;

    private String mText;
    private String mLangCode;

    private State mState = State.DISABLED;

    private enum State {
        DISABLED,
        NORMAL,
        PLAYING
    }

    @Override
    public void onSynthesisBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis) {

    }

    @Override
    public void onPlayingBegin(Vocalizer vocalizer) {
        mState = State.PLAYING;
        updateUi();

    }

    @Override
    public void onPlayingDone(Vocalizer vocalizer) {
        mState = State.NORMAL;
        updateUi();

    }

    @Override
    public void onVocalizerError(Vocalizer vocalizer, ru.yandex.speechkit.Error error) {
        resetVocalizer();
    }

    public FragmentPlayerButton() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechKit.getInstance().configure(getContext(), API_KEY);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_button, container, false);
        ButterKnife.bind(this, view);
        updateUi();


        return view;
    }
    public void setAudioUrl(String text, String langCode) {

        if (text != null && Arrays.asList(acceptedLanguages).contains(langCode)) {
            mText = text;
            mLangCode = langCode;
            mState = State.NORMAL;
        } else  {
            mState = State.DISABLED;
        }

        updateUi();

    }

    @OnClick(R.id.button)
    protected void onButtonClick() {
        boolean isStarted = false;
        Log.d("state11=", String.valueOf(mState));
        if (mState == State.NORMAL) {
        if (TextUtils.isEmpty(mText)) {
            Toast.makeText(getContext(), "Write smth to be vocalized!", Toast.LENGTH_SHORT).show();
        } else {
            // Reset the current vocalizer.
            resetVocalizer();
            // To create a new vocalizer, specify the language, the text to be vocalized, the auto play parameter
            // and the voice.
            vocalizer = Vocalizer.createVocalizer(mLangCode, mText, true, Vocalizer.Voice.ERMIL);
            // Set the listener.
            vocalizer.setListener(FragmentPlayerButton.this);
            // Don't forget to call start.
            vocalizer.start();
            //mState = State.PLAYING;
           // updateUi();
            isStarted = true;

        }}
        if (!isStarted)
        if (mState == State.PLAYING) {
                resetVocalizer();
                mState = State.NORMAL;
                updateUi();
        }

}
    @Override
    public void onPause() {
        super.onPause();
        resetVocalizer();
    }

    private void resetVocalizer() {
        if (vocalizer != null) {
            vocalizer.cancel();
            vocalizer = null;
        }
    }
    private void updateUi() {
        int buttonIcon = R.drawable.ic_volume_up_black_24dp;
        switch (mState) {
            case DISABLED:
                buttonIcon = R.drawable.ic_volume_down_black_24dp;
                break;
            case NORMAL:
                buttonIcon = R.drawable.ic_volume_up_black_24dp;
                break;
            case PLAYING:
                buttonIcon = R.drawable.ic_stop_black_24dp;
                break;
        }

        mButton.setImageResource(buttonIcon);
    }
}