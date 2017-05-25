package tam.musicplayerver2;


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
import android.widget.MediaController;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Tam on 5/25/2017.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    private final IBinder iBinder = new MediaBinder();
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private ArrayList<Track> tracks;
    private int trackPosition;
    private boolean isStarted = true;

    ///when create service
   /* @Override
    public void onCreate()
    {
        super.onCreate();
        trackPosition = 0;
        mediaPlayer = new MediaPlayer();

        //initial music player
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setWakeMode(getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.reset();

        //////////////

    }*/

    @Override
    public IBinder onBind(Intent intent) {
        //return iBinder;
        return iBinder;
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
    ////get tracks list from main
 /*   public void getTracksListFromMain(ArrayList<Track> getTracks)
    {
        tracks = getTracks;
    }


    @Override
    public boolean onUnbind(Intent intent)
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }*/

//////////////////////////////////
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    //////////////////////////////////////

   /* public void playTrack()
    {
        mediaPlayer.reset();
        Track playingTrack = tracks.get(trackPosition);
        long currentTrack = playingTrack.getId();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentTrack);

        try{
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mediaPlayer.prepareAsync();
    }*/

    /////////////////////// playback control
 /*   public int getPosn(){
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



*/

}
