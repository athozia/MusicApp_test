package tam.musicplayer;

import android.content.Context;
import android.widget.MediaController;

import java.util.Random;

/**
 * Created by Equinnox Zhetena on 5/23/2017.
 */

public class MusicController extends MediaController {

    private boolean shuffle=false;
    private Random rand;
    rand = new Random();

    public MusicController(Context c){
        super(c);
    }

    public void hide(){}

    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong == songPosn){
                newSong=rand.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn >= songs.size())
                songPosn=0;
        }
        playSong();
    }

    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public void playPrev(){
        songPosn--;
        if(songPosn < 0)
            songPosn = songs.size()-1;
        playSong();
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

}
