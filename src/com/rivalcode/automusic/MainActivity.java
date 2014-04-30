package com.rivalcode.automusic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.rivalcode.automusic.MusicService.MusicBinder;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

public class MainActivity extends Activity implements MediaPlayerControl {

	protected ArrayList<Song> songList;
	protected ListView songView;
	
	protected MusicService musicSrv;
	protected Intent playIntent;
	protected boolean musicBound = false;
	
	protected MusicController controller;
	protected boolean paused = false;
	protected boolean playbackPaused = false;
	
	protected Menu menu;
	protected TextView currentSongTitle;
	protected TextView currentSongInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		
		// Sets the controller
		setController();
		
		// Gets the text view for "now playing"
		currentSongTitle = (TextView) findViewById(R.id.songCurrentTitle);
		currentSongInfo = (TextView) findViewById(R.id.songCurrentInfo);
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
	
	// connect to the service
	private ServiceConnection musicConnection = new ServiceConnection() {
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder) service;
			musicSrv = binder.getService();
			musicSrv.setList(songList);
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
		case R.id.action_shuffle:
			MenuItem button = menu.getItem(0);
			if (musicSrv.setShuffle())
				button.setIcon(R.drawable.rand_on);
			else
				button.setIcon(R.drawable.rand);
			break;
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
	
	@Override
	protected void onPause(){
		super.onPause();
		paused = true;
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if (paused) {
			setController();
			paused = false;
		}
	}
	
	@Override
	protected void onStop() {
		controller.hide();
		super.onStop();
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
		updateCurrentSong();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}
	
	protected void setController() {
		controller = new MusicController(this);
		controller.setPrevNextListeners(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				playNext();
			}
		},
		
		new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playPrev();
			}
		});
		
		controller.setBackgroundColor(android.graphics.Color.GRAY);
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
	}
	
	private void playNext() {
		musicSrv.playNext();
		updateCurrentSong();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}

	private void playPrev() {
		musicSrv.playPrev();
		updateCurrentSong();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}
	
	private void updateCurrentSong() {
		currentSongTitle.setText(musicSrv.getCurrentSong().getTitle());
		currentSongInfo.setText(musicSrv.getCurrentSong().getArtist() + " - " + musicSrv.getCurrentSong().getAlbum());
	}

	// MEDIA PLAYER CONTROLS:
	
	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

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
	public void pause() {
		playbackPaused = true;
		musicSrv.pausePlayer();
	}

	@Override
	public void seekTo(int pos) {
		musicSrv.seek(pos);
	}

	@Override
	public void start() {
		musicSrv.go();
	}
}
