package tam.musicplayer;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.app.Service;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.PowerManager;
import android.os.Binder;

import java.io.IOException;
import java.util.Random;


/**
 * Created by Tam on 5/22/2017.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener{

    private final IBinder iBinder = new MediaBinder();
    private MediaPlayer mediaPlayer;
    private String mediaFile;
    private AudioManager audioManager;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private int resumePosition;
    private ArrayList<Track> tracks;
    private int trackPosition;
    private Track playingTrack;
    private boolean shuffle=false;
    private Random rand;

    /*private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            //pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }*/

    //handle phone call ->
   /* private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }*/


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return iBinder;
        return iBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
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
        /*int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
       */ return false;
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

        stopMedia();
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
       initMediaPlayer();



       return super.onStartCommand(intent,flags,startID);

    }

    public void getTracksListFromMain(ArrayList<Track> getTracks)
    {
        tracks = getTracks;
    }

    ////////////service binder
    public class MediaBinder extends Binder
    {
        public MusicPlayerService getService()
        {
            return MusicPlayerService.this;
        }
    }

    ////////////////////////

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


        /*mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try
        {
            // Set the data source to the mediaFile location
            mediaPlayer.setDataSource(mediaFile);

        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }*/
        mediaPlayer.prepareAsync();
    }



    /*@Override
    public void onCreate()
    {
        super.onCreate();
        trackPosition = 0;
        initMediaPlayer();

    }*/

    @Override
    public boolean onUnbind(Intent intent)
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mediaPlayer != null)
        {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
    }

    //after this-> play pause, stop, ... for basic -> controller handle much more interaction


    public void playTracks()
    {
        mediaPlayer.reset();
        playingTrack = tracks.get(trackPosition);
        long currSong = playingTrack.getId();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }


        mediaPlayer.prepareAsync();
    }


    /////after this , actually will be in controller class, this is just a test

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }

    public int getPosn(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaPlayer.getDuration();
    }

    public boolean isPng(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
    }

    public void seek(int posn){
        mediaPlayer.seekTo(posn);
    }

    public void go(){
        mediaPlayer.start();
    }

    public void playNext()
    {
        trackPosition++;
        if(trackPosition > tracks.size())
        {
            trackPosition = 0;
        }
        playTracks();
    }

    public void playPrev()
    {
        trackPosition--;
        if(trackPosition < 0){
            trackPosition = tracks.size() - 1;
        }

        playTracks();
    }

    public void replay()
    {
        stopMedia();
        playMedia();
    }

    public void shuffle()
    {
        rand = new Random();
        trackPosition = rand.nextInt(tracks.size());
        playTracks();
    }

    public int getPosition()
    {
        return trackPosition;
    }
}

