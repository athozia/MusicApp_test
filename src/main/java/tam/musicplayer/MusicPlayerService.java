package tam.musicplayer;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.app.Service;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.PowerManager;
import android.os.Binder;

import java.io.IOException;


/**
 * Created by Tam on 5/22/2017.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener{

   // private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private String mediaFile;
    private AudioManager audioManager;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return iBinder;
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //playMedia(); MusicController c = new ...
        // c.playMedia();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange)
        {
            //get gain in audio -> resume playing
            case AudioManager.AUDIOFOCUS_GAIN:
                if(mediaPlayer == null)
                    initMediaPlayer();
                else if(!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                mediaPlayer.setVolume(1.0f,1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // lost focus -> stop playback and release media player
                if (mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // lost focus only for short time , so puase only , not relesase media player
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing at an attenuated level (because of notification, etc.)
                if (mediaPlayer.isPlaying())
                    mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }

    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //stopMedia();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch(what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer error", "not valid for progressive playback" + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer error","server ded" + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer error","error occur with unknown reason" + extra);
        }

        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){

        try{
            mediaFile = intent.getExtras().getString("media");

        }catch (NullPointerException e)
        {
            stopSelf();
        }

        if(requestAudioFocus() == false)
        {
            stopSelf();
        }

        if(mediaFile != null && mediaFile != "")
        {
            initMediaPlayer();
        }

        return super.onStartCommand(intent,flags,startID);

    }

    private void initMediaPlayer()
    {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.reset();


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try
        {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(mediaFile);

        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        //mediaPlayer = new MediaPlayer();
        initMediaPlayer();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            //stopMedia -> media controller
            mediaPlayer.release();
        }
        removeAudioFocus();
    }

    //after this-> play pause, stop, ...


}
