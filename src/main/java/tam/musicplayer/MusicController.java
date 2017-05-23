package tam.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.MediaController;
import java.util.Random;

/**
 * Created by Equinnox Zhetena on 5/23/2017.
 */

public class MusicController extends MediaController {

    private MediaPlayerControl mPlayer;
    private boolean shuffle=false;

    private int songPosn;
    private int trackSize;
    private Random rand;
    rand = new Random();


    public MusicController(Context c){
        super(c);
    }

    public MusicController(Context c,int size){
        super(c);
        trackSize = size;
    }

    public void hide(){}



    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong == songPosn){
                newSong = rand.nextInt(trackSize); //need to get size
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn >= trackSize) //need to get size
                songPosn=0;
        }
        go();
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0)
            songPosn = trackSize-1;
        go();
    }

    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public int getPosn(){
        return mPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mPlayer.getDuration();
    }

    public boolean isPng(){
        return mPlayer.isPlaying();
    }

    public void pausePlayer(){
        mPlayer.pause();
    }

    public void seek(int posn){
        mPlayer.seekTo(posn);
    }

    public void go(){
        mPlayer.start();
    }

    public void onCompletion(MediaPlayer mp) {
        if(mPlayer.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

}
