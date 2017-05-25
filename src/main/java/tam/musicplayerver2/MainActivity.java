package tam.musicplayerver2;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends ListActivity {

    private static final int UPDATE_FREQUENCY = 500;
    private static final int STEP_VALUE = 4000;

    private MediaCursorAdapter mediaAdapter = null;
    private TextView selelctedFile = null;
    private SeekBar seekbar = null;
    private MediaPlayer mediaplayer = null;
    private ImageButton playButton = null;
    private ImageButton prevButton = null;
    private ImageButton nextButton = null;
    private ImageButton repeatButton = null;
    private ImageButton stopButton = null;
    private ArrayList<Track> tracks;
    private int n_t = 0;
    private Random rand;


    private boolean isStarted = true;
    private String currentFile = "";
    private boolean isMoveingSeekBar = false;

    private final Handler handler = new Handler();

    private final Runnable updatePositionRunnable = new Runnable() {
        public void run() {
            updatePosition();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selelctedFile = (TextView) findViewById(R.id.selectedfile);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        playButton = (ImageButton) findViewById(R.id.play);
        prevButton = (ImageButton) findViewById(R.id.prev);
        nextButton = (ImageButton) findViewById(R.id.next);
        repeatButton = (ImageButton) findViewById(R.id.repeat);
        stopButton = (ImageButton) findViewById(R.id.stop);

        mediaplayer = new MediaPlayer();

        mediaplayer.setOnCompletionListener(onCompletion);
        mediaplayer.setOnErrorListener(onError);
        seekbar.setOnSeekBarChangeListener(seekBarChanged);

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);

        if (null != cursor) {

           // tracks = new ArrayList<Track>();
            cursor.moveToFirst();
           /* while(cursor.moveToNext()){
                long tid = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String ttitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String tartist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String talbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String tdescription = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                n_t++;

                tracks.add(new Track(tid, ttitle, tartist, talbum, tdescription));
            }*/
           // cursor.moveToFirst();
            mediaAdapter = new MediaCursorAdapter(this, R.layout.listitem, cursor);

            setListAdapter(mediaAdapter);

            playButton.setOnClickListener(onButtonClick);
            nextButton.setOnClickListener(onButtonClick);
            prevButton.setOnClickListener(onButtonClick);
            repeatButton.setOnClickListener(onButtonClick);
            stopButton.setOnClickListener(onButtonClick);
        }

    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        currentFile = (String) view.getTag();

        startPlay(currentFile);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(updatePositionRunnable);
        mediaplayer.stop();
        mediaplayer.reset();
        mediaplayer.release();

        mediaplayer = null;
    }

    private void startPlay(String file) {
        Log.i("Selected: ", file);

        selelctedFile.setText(file);
        seekbar.setProgress(0);

        mediaplayer.stop();
        mediaplayer.reset();

        try {
            mediaplayer.setDataSource(file);
            mediaplayer.prepare();
            mediaplayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekbar.setMax(mediaplayer.getDuration());
        playButton.setImageResource(android.R.drawable.ic_media_pause);
        updatePosition();
        isStarted = true;
    }

    private void stopPlay() {
        mediaplayer.stop();
        mediaplayer.reset();
        playButton.setImageResource(android.R.drawable.ic_media_play);
        handler.removeCallbacks(updatePositionRunnable);
        seekbar.setProgress(0);

        isStarted = false;
    }

    private void updatePosition() {
        handler.removeCallbacks(updatePositionRunnable);

        seekbar.setProgress(mediaplayer.getCurrentPosition());

        handler.postDelayed(updatePositionRunnable, UPDATE_FREQUENCY);
    }

    private class MediaCursorAdapter extends SimpleCursorAdapter {

        public MediaCursorAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c,
                    new String[]{MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.DURATION},
                    new int[]{R.id.displayname, R.id.title, R.id.duration});
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView name = (TextView) view.findViewById(R.id.displayname);
            TextView duration = (TextView) view.findViewById(R.id.duration);

            name.setText(cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));

            title.setText(cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.TITLE)));

            long durationInMs = Long.parseLong(cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)));

            double durationInMin = ((double) durationInMs / 1000.0) / 60.0;

            durationInMin = new BigDecimal(Double.toString(durationInMin)).setScale(2, BigDecimal.ROUND_UP).doubleValue();

            duration.setText("" + durationInMin);

            view.setTag(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.listitem, parent, false);

            bindView(v, context, cursor);

            return v;
        }
    }

    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play: {
                    if (mediaplayer.isPlaying()) {
                        handler.removeCallbacks(updatePositionRunnable);
                        mediaplayer.pause();
                        playButton.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        if (isStarted) {
                            mediaplayer.start();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);

                            updatePosition();
                        } else {
                            startPlay(currentFile);
                        }
                    }

                    break;
                }
                case R.id.next: {
                    int seekto = mediaplayer.getCurrentPosition() + STEP_VALUE;

                    if (seekto > mediaplayer.getDuration())
                        seekto = mediaplayer.getDuration();

                    mediaplayer.pause();
                    mediaplayer.seekTo(seekto);
                    mediaplayer.start();


                    break;
                }
                case R.id.prev: {
                    int seekto = mediaplayer.getCurrentPosition() - STEP_VALUE;

                    if (seekto < 0)
                        seekto = 0;

                    mediaplayer.pause();
                    mediaplayer.seekTo(seekto);
                    mediaplayer.start();

                    break;
                }
                case R.id.repeat:{
                    int seekto = 0;
                    mediaplayer.pause();
                    mediaplayer.seekTo(seekto);
                    mediaplayer.start();

                    break;
                }
                case R.id.stop:{
                    //mediaplayer.stop();
                    stopPlay();
                    playButton.setImageResource(android.R.drawable.ic_media_play);

                    /*rand = new Random();
                    int i = rand.nextInt(tracks.size() + 1);
                    mediaplayer.start();*/
                    break;

                }
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletion = new MediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlay();
        }
    };

    private MediaPlayer.OnErrorListener onError = new MediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {

            return false;
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isMoveingSeekBar = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isMoveingSeekBar = true;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (isMoveingSeekBar) {
                mediaplayer.seekTo(progress);

                Log.i("OnSeekBarChangeListener", "onProgressChanged");
            }
        }
    };
}




