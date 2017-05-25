package tam.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import android.os.Bundle;
import java.util.ArrayList;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ImageButton;
import android.widget.ListView;
import android.view.View;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;



public class MainActivity extends Activity{

    private ArrayList<Track> tracksList;
    private MusicPlayerService musicService;
    private Intent intent;
    private boolean bound=false;
    private MusicController controller;
    private ListView trackView;
    private ImageButton playButton = null;
    private ImageButton prevButton = null;
    private ImageButton nextButton = null;
    private ImageButton randomButton = null;
    private ImageButton repeatButton = null;
    private ImageButton stopButton = null;
    private TextView theTitile = null;
    private TextView theArtist = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //trackView = (ListView)findViewById(R.id.track_list);


        playButton = (ImageButton)findViewById(R.id.playButton);
        nextButton = (ImageButton)findViewById(R.id.forward);
        prevButton = (ImageButton)findViewById(R.id.backward);
        randomButton = (ImageButton)findViewById(R.id.shuffle);
        repeatButton = (ImageButton)findViewById(R.id.replay);
        stopButton = (ImageButton)findViewById(R.id.stop);

        tracksList = new ArrayList<Track>();
        loadTracks();

        theTitile = (TextView)findViewById(R.id.track_title);
        theArtist = (TextView)findViewById(R.id.track_artist);

        musicService.playTracks();

       /*Collections.sort(tracksList, new Comparator<Track>(){
            public int compare(Track a, Track b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });*/


       // trackView.setAdapter(adapter);
        //setController();
    }

    public void loadTracks()
    {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri,null,null,null,null);

        if(cursor != null && cursor.moveToFirst())
        {
            while(cursor.moveToNext())
            {
                int i =0;
                long tid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String ttitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String tartist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String talbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String tdescription = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                tracksList.add(new Track(tid, ttitle, tartist, talbum, tdescription));
            }

        }
    }

    /////////service binder
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MediaBinder binder = (MusicPlayerService.MediaBinder) service;
            musicService = binder.getService();
            musicService.getTracksListFromMain(tracksList);     //pass track to service
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
    /////////


    @Override
    protected void onDestroy()
    {
        stopService(intent);
        musicService = null;
        super.onDestroy();
    }

    private View.OnClickListener onClick = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.playButton:
                {
                    if(musicService.isPng())
                    {
                        musicService.pausePlayer();
                        playButton.setImageResource(R.drawable.play);
                        int i = musicService.getPosition();
                        theTitile.setText(tracksList.get(i).getTitle());
                        theArtist.setText(tracksList.get(i).getArtist());
                    }
                    else
                    {
                        musicService.playMedia();
                        playButton.setImageResource(R.drawable.pause);
                        int i = musicService.getPosition();
                        theTitile.setText(tracksList.get(i).getTitle());
                        theArtist.setText(tracksList.get(i).getArtist());
                    }
                    break;
                }
                case R.id.forward:
                {
                    musicService.playNext();
                    int i = musicService.getPosition();
                    theTitile.setText(tracksList.get(i).getTitle());
                    theArtist.setText(tracksList.get(i).getArtist());
                    break;
                }
                case R.id.backward:
                {
                    musicService.playPrev();
                    int i = musicService.getPosition();
                    theTitile.setText(tracksList.get(i).getTitle());
                    theArtist.setText(tracksList.get(i).getArtist());
                    break;
                }
                case R.id.stop:
                {
                    musicService.stopMedia();
                    break;
                }
                case R.id.replay:
                {
                    musicService.replay();
                    break;
                }
                case R.id.shuffle:
                {
                    musicService.shuffle();
                    int i = musicService.getPosition();
                    theTitile.setText(tracksList.get(i).getTitle());
                    theArtist.setText(tracksList.get(i).getArtist());
                    break;
                }
            }

        }
    };

   /*private void setController()
    {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setEnabled(true);
    }*/

    /*public void trackPicked(View view)
    {
        musicService.setTracks(Integer.parseInt(view.getTag().toString()));
        musicService.playTracks();
    }*/

  /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                stopService(intent);
                musicService=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onStart()
    {
        super.onStart();
        if(intent == null)
        {
            intent = new Intent(this, MusicPlayerService.class);
            bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }



}
