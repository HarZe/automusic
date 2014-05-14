package com.rivalcode.automusic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.rivalcode.automusic.MusicService.MusicBinder;

public class MusicActivity extends Activity {

	protected ArrayList<Song> songList;
	protected ListView songView;
	
	protected MusicService musicSrv;
	protected Intent playIntent;
	protected boolean musicBound = false;
	
	protected Menu menu;
	protected TextView currentSongTitle;
	protected TextView currentSongInfo;
	protected ImageButton loopButton;
	protected ImageButton backButton;
	protected ImageButton playButton;
	protected ImageButton nextButton;
	protected ImageButton randButton;
	
	protected MusicActivity thisAux = null;
	
	protected boolean paused = false;
	protected boolean looping = false;
	protected boolean loopSong = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);
		
		thisAux = this;
		
		// Getting the songs from the device
		songView = (ListView)findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		getSongList();
		
		// Sort songs by title
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});
		
		// Setting the songs on the layout
		SongAdapter songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
		
		// Gets the text view for "now playing"
		currentSongTitle = (TextView) findViewById(R.id.songCurrentTitle);
		currentSongInfo = (TextView) findViewById(R.id.songCurrentInfo);
		loopButton = (ImageButton) findViewById(R.id.loop_button);
		backButton = (ImageButton) findViewById(R.id.back_button);
		playButton = (ImageButton) findViewById(R.id.play_button);
		nextButton = (ImageButton) findViewById(R.id.next_button);
		randButton = (ImageButton) findViewById(R.id.rand_button);
		
		// Button listeners
		buttonListenersSetup();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent == null){
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}
	
	// Connecting to the service
	private ServiceConnection musicConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder) service;
			musicSrv = binder.getService();
			musicSrv.setList(songList, thisAux);
			musicBound = true;
		}
	 
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_end:
			stopService(playIntent);
			musicSrv = null;
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		stopService(playIntent);
		musicSrv = null;
		super.onDestroy();
	}

	/**
	 * Get the songs from the device
	 */
	public void getSongList() {
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		
		if (musicCursor != null && musicCursor.moveToFirst()) {
			
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			int albumColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
			int durationColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION);
			int songIdColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE_KEY);

			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				String thisAlbum = musicCursor.getString(albumColumn);
				long thisDuration = musicCursor.getLong(durationColumn);
				String thisSongId = musicCursor.getString(songIdColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum, thisDuration, thisSongId));
			}
			while (musicCursor.moveToNext());
		}
	}
	
	public void songPicked(View view) {
		musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
		musicSrv.playSong();
		paused = false;
		playButton.setImageResource(R.drawable.pause);
	}
	
	public void updateCurrentSong() {
		currentSongTitle.setText(musicSrv.getCurrentSong().getTitle());
		currentSongInfo.setText(musicSrv.getCurrentSong().getArtist() + " - " + musicSrv.getCurrentSong().getAlbum());
	}
	
	protected void buttonListenersSetup() {
		
		loopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                
            	// TODO: implement real looping
            	
            	if (!looping) {
            		looping = true;
            		loopButton.setImageResource(R.drawable.loop_on);
            	}
            	else
            		if (!loopSong) {
            			loopSong = true;
            			loopButton.setImageResource(R.drawable.loop_1);
            		}
            		else {
            			looping = loopSong = false;
            			loopButton.setImageResource(R.drawable.loop);
            		}
            	
            }
        });
		
		backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	musicSrv.playPrev();
            }
        });
		
		playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (paused) {
            		musicSrv.go();
            		paused = false;
            		playButton.setImageResource(R.drawable.pause);
            	}
            	else {
            		musicSrv.pausePlayer();
            		paused = true;
            		playButton.setImageResource(R.drawable.play);
            	}
            		
            }
        });
		
		nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	musicSrv.playNext();
            }
        });
		
		randButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (musicSrv.setShuffle())
            		randButton.setImageResource(R.drawable.rand_on);
        		else
        			randButton.setImageResource(R.drawable.rand);
            }
        });
		
	}

	/* MEDIA PLAYER CONTROLS:

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if (musicSrv != null && musicBound && musicSrv.isPng())
		    return musicSrv.getPosn();
		else
			return 0;
	}

	@Override
	public int getDuration() {
		if (musicSrv != null && musicBound && musicSrv.isPng())
		    return musicSrv.getDur();
		else
			return 0;
	}

	@Override
	public boolean isPlaying() {
		if(musicSrv != null && musicBound)
		    return musicSrv.isPng();
		return
			false;
	}

	@Override
	public void seekTo(int pos) {
		musicSrv.seek(pos);
	}

	@Override
	public int getAudioSessionId() {
		// TODO Auto-generated method stub
		return 0;
	} */
}
