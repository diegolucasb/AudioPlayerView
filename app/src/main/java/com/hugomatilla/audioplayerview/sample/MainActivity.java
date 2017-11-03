package com.hugomatilla.audioplayerview.sample;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hugomatilla.audioplayerview.AudioPlayerView;

public class MainActivity extends AppCompatActivity {

    String url = "http://www.freesound.org/data/previews/137/137227_1735491-lq.mp3";
    private View spinner;
    private AudioPlayerView
            audioPlayerView,
            audioPlayProgress,
            audioPlayProgressProgramatically,
            audioPlayerViewText,
            audioPlayerViewCustomFont;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.loading_spinner);

        audioPlayerView = (AudioPlayerView) findViewById(R.id.play);
        audioPlayerView.withUrl(url);
        audioPlayerView.setOnAudioPlayerViewListener(new AudioPlayerView.OnAudioPlayerViewListener() {
            @Override
            public void onAudioPreparing() {
                Toast.makeText(getBaseContext(), "Audio is loading callback", Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAudioReady() {
                Toast.makeText(getBaseContext(), "Audio is ready callback", Toast.LENGTH_SHORT).show();
                spinner.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAudioFinished() {
                Toast.makeText(getBaseContext(), "Audio finished callback", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAudioPause() {

            }

            @Override
            public void onAudioPlay() {

            }
        });

        audioPlayProgress = (AudioPlayerView) findViewById(R.id.playProgress);
        audioPlayProgress.withUrl(url);

        audioPlayProgressProgramatically = (AudioPlayerView) findViewById(R.id.playProgressProg);
        SeekBar seekBar = (SeekBar) findViewById(R.id.progressBar1);
        audioPlayProgressProgramatically.setSeekBar(seekBar);

        TextView textViewRunTime = (TextView) findViewById(R.id.textViewRunTime1);
        audioPlayProgressProgramatically.setRunTimeTextView(textViewRunTime);
        audioPlayProgressProgramatically.withUrl(url);

        audioPlayerViewText = (AudioPlayerView) findViewById(R.id.playText);
        audioPlayerViewText.withUrl(url);


        audioPlayerViewCustomFont = (AudioPlayerView) findViewById(R.id.playCustomFonts);
        Typeface iconFont = Typeface.createFromAsset(getAssets(), "audio-player-view-font-custom.ttf");
        audioPlayerViewCustomFont.setTypeface(iconFont);
        audioPlayerViewCustomFont.withUrl(url);



    }

    @Override
    protected void onDestroy() {
        audioPlayerView.destroy();
        audioPlayProgress.destroy();
        audioPlayerViewText.destroy();
        audioPlayerViewCustomFont.destroy();
        super.onDestroy();
    }
}
