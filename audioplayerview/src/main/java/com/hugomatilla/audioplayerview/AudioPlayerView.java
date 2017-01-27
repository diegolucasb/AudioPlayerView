package com.hugomatilla.audioplayerview;

/**
 * Created by hugomatilla on 10/02/16.
 *
 * Changed by Lucas Diego on 25/01/17
 * Add reference attributes to seekbar and textview to show current audio time
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioPlayerView extends TextView {

    private static final long AUDIO_PROGRESS_UPDATE_TIME = 100;
    public static final String TIME_FORMAT = "%02d:%02d";

    private static final String NULL_PARAMETER_ERROR = "`pauseText`, `playText` and `loadingText`" +
            " must have some value, if `useIcons` is set to false. Set `useIcons` to true, or add strings to stopText`, " +
            "`playText` and `loadingText` in the AudioPlayerView.xml";
    private Context context;
    private MediaPlayer mediaPlayer;
    private String playText;
    private String pauseText;
    private String loadingText;
    private String url;

    private SeekBar seekBar;
    private Handler progressUpdateHandler;

    //to see audio current time progress
    private TextView runTimeTextView;

    private boolean useIcons;
    private boolean audioReady;
    private boolean usesCustomIcons;

    private int resourceSeekBar;
    private int resourceRunTimeViewId;

    private Runnable updateProgressBar = new Runnable() {
        @Override
        public void run() {
            //if it gets here, it means either seekbar or textviewRunTime is not null
            if (progressUpdateHandler != null && mediaPlayer != null && mediaPlayer.isPlaying()){

                int curreTime = mediaPlayer.getCurrentPosition();
                if(seekBar != null){
                    seekBar.setProgress(curreTime);
                }

                updateAudioRunTime(curreTime);

                //call it again
                progressUpdateHandler.postDelayed(this, AUDIO_PROGRESS_UPDATE_TIME);
            }

        }
    };

    //Callbacks
    public interface OnAudioPlayerViewListener {
        void onAudioPreparing();

        void onAudioReady();

        void onAudioFinished();

        void onError(Exception e);
    }

    private OnAudioPlayerViewListener listener;

    public void setOnAudioPlayerViewListener(OnAudioPlayerViewListener listener) {
        this.listener = listener;
    }

    private void sendCallbackError(Exception e){
        if(listener != null){
            listener.onError(e);
        }
    }

    private void sendCallbackAudioFinished() {
        if (listener != null)
            listener.onAudioFinished();
    }

    private void sendCallbackAudioReady() {
        if (listener != null)
            listener.onAudioReady();
    }

    private void sendCallbackAudioPreparing() {
        if (listener != null)
            listener.onAudioPreparing();
    }

    //Constructors
    public AudioPlayerView(Context context) {
        super(context);
        this.context = context;
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        getAttributes(attrs);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        getAttributes(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        getAttributes(attrs);
    }

    public void getAttributes(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AudioPlayerTextView, 0, 0);

        try {
            pauseText = a.getString(R.styleable.AudioPlayerTextView_pauseText);
            playText = a.getString(R.styleable.AudioPlayerTextView_playText);
            loadingText = a.getString(R.styleable.AudioPlayerTextView_loadingText);
            useIcons = a.getBoolean(R.styleable.AudioPlayerTextView_useIcons, true);

            resourceSeekBar = a.getResourceId(R.styleable.AudioPlayerTextView_seekBar, 0);
            resourceRunTimeViewId = a.getResourceId(R.styleable.AudioPlayerTextView_runTimeView, 0);

            if ((pauseText != null && playText != null && loadingText != null) && useIcons)
                usesCustomIcons = true;
            else if ((pauseText == null || playText == null || loadingText == null) && !useIcons)
                throw new UnsupportedOperationException(NULL_PARAMETER_ERROR);

        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //if user has referred to a seekbar
        if(resourceSeekBar != 0){
            seekBar = (SeekBar) getRootView().findViewById(resourceSeekBar);
        }

        if(resourceRunTimeViewId != 0){
            runTimeTextView = (TextView) getRootView().findViewById(resourceRunTimeViewId);
        }


    }

    //Implementation
    public void withUrl(String url) {
        this.url = url;
        setUpMediaPlayer();
    }

    private void setUpMediaPlayer() {
        if (useIcons) {
            setUpFont();
        }
        setText(playText);
        this.setOnClickListener(onViewClickListener);
    }

    private void setUpFont() {
        if (!usesCustomIcons) {
//            Typeface iconFont = Typeface.createFromAsset(context.getAssets(), "audio-player-view-font.ttf");
            Typeface iconFont = Typeface.createFromAsset(context.getAssets(), "audio-player-view-font_opt1.ttf");
            setTypeface(iconFont);
            playText = getResources().getString(R.string.playIcon1);
            pauseText = getResources().getString(R.string.pauseIcon1);
            loadingText = getResources().getString(R.string.loadingIcon1);
        }
    }


    private OnClickListener onViewClickListener = new OnClickListener() {

        public void onClick(View v) {
            try {
                toggleAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void toggleAudio() throws IOException {

        if (mediaPlayer != null && mediaPlayer.isPlaying())
            pause();
        else
            play();
    }

    private void play() throws IOException {
        if (!audioReady) {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(url);

            progressUpdateHandler = new Handler();
            updateAudioRunTime(0);

            if(seekBar != null){
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mediaPlayer.seekTo(seekBar.getProgress());
                        updateAudioRunTime(seekBar.getProgress());
                    }
                });
            }

            prepareAsync();

            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnErrorListener(onErrorListener);

        } else
            playAudio();
    }

    private void prepareAsync() {
        mediaPlayer.prepareAsync();
        setTextLoading();
        sendCallbackAudioPreparing();
    }

    private void playAudio() {
        progressUpdateHandler.postDelayed(updateProgressBar, AUDIO_PROGRESS_UPDATE_TIME);
        mediaPlayer.start();
        setText(pauseText);
    }

    private MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

            String messageError = "";
            switch (what){
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    messageError = context.getString(R.string.media_error_unknown);
                    break;
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    messageError = context.getString(R.string.media_error_server_died);
                    break;
            }

            switch (extra){
                case MediaPlayer.MEDIA_ERROR_IO:
                    messageError += "\n" + context.getString(R.string.media_error_io);
                    break;
                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    messageError += "\n" + context.getString(R.string.media_error_malformed);
                    break;
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    messageError += "\n" + context.getString(R.string.media_error_unsupported);
                    break;
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                    messageError += "\n" + context.getString(R.string.media_error_timed_out);
                    break;
            }

            sendCallbackError(new Exception(messageError));
            //True if the method handled the error, false if it didn't. Returning false, or not having an OnErrorListener at all, will cause the OnCompletionListener to be called.
            return true;
        }
    };

    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {

            if(seekBar != null){
                long finalTime = mediaPlayer.getDuration();
                seekBar.setMax((int) finalTime);
                seekBar.setProgress(0);
            }

            playAudio();
            audioReady = true;
            clearAnimation();
            sendCallbackAudioReady();
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {

            if(seekBar != null){
                seekBar.setProgress(0);
                mediaPlayer.seekTo(0);
            }

            setText(playText);
            sendCallbackAudioFinished();
        }
    };


    private void setTextLoading() {
        setText(loadingText);
        if (useIcons)
            startAnimation();
    }

    private void startAnimation() {
        final Animation rotation = AnimationUtils.loadAnimation(context, R.anim.rotate_indefinitely);
        this.startAnimation(rotation);
    }

    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
//            mediaPlayer.seekTo(0);
            setText(playText);
        }
    }

    public void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            audioReady = false;
        }
    }

    private void updateAudioRunTime(int currentTime){
        if(runTimeTextView == null || currentTime < 0){
            return;
        }

        StringBuilder playbackStr = new StringBuilder();
        // set the current time
        // its ok to show 00:00 in the UI
        playbackStr.append(
                String.format(
                        TIME_FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes((long) currentTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) currentTime) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes((long) currentTime))));

        runTimeTextView.setText(playbackStr);
    }


}