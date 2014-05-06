package com.rivalcode.automusic;

import java.util.ArrayList;
import java.util.Random;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

	protected MediaPlayer player;
	protected ArrayList<Song> songs;
	protected int songPosn;
	protected final IBinder musicBind = new MusicBinder();
	
	protected String songTitle = "";
	protected static final int NOTIFY_ID = 1;
	protected boolean shuffle = false;
	protected Random rand;
	
	protected MainActivity parent = null;
	
	public void onCreate() {
		super.onCreate();
		songPosn = 0;
		player = new MediaPlayer();
		rand = new Random();
		
		initMusicPlayer();
	}
	
	public void initMusicPlayer() {
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}
	
	public void setList(ArrayList<Song> songList, MainActivity parent){
		songs = songList;
		this.parent = parent;
	}
	
	public void playSong() {
		player.reset();
		
		Song playSong = getCurrentSong();
		songTitle = playSong.getTitle();
		long currSong = playSong.getId();
		Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
		
		try	{
			player.setDataSource(getApplicationContext(), trackUri);
		}
		catch(Exception e){
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
		
		player.prepareAsync();
	}
	
	public Song getCurrentSong() {
		if (songPosn >= 0 && songPosn < songs.size())
			return songs.get(songPosn);
		else
			return null;
	}
	
	public void setSong(int songIndex){
		songPosn = songIndex;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (player.getCurrentPosition() > 0) {
			mp.reset();
			playNext();
		}
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		mediaPlayer.start();
		
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		Notification.Builder builder = new Notification.Builder(this);
		 
		builder.setContentIntent(pendInt)
		  .setSmallIcon(R.drawable.notification)
		  .setTicker(songTitle)
		  .setOngoing(true)
		  .setContentTitle("Playing")
		  .setContentText(songTitle);
		Notification not = builder.build();
		 
		startForeground(NOTIFY_ID, not);
		
		parent.updateCurrentSong();
	}
	
	@Override
	public void onDestroy() {
		stopForeground(true);
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}
	
	public boolean setShuffle() {
		if (shuffle)
			shuffle = false;
		else
			shuffle = true;
		
		return shuffle;
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
	
	public void playPrev(){
		  songPosn--;
		  if (songPosn > 0)
			  songPosn = songs.size() - 1;
		  playSong();
	}
	
	public void playNext(){
		if (shuffle) {
			int newSong = songPosn;
			while (newSong == songPosn)
				newSong = rand.nextInt(songs.size());
			songPosn = newSong;
		}
		else {
			songPosn++;
		    if (songPosn >= songs.size())
		    	songPosn = 0;
		}
		playSong();
	}
	
	public class MusicBinder extends Binder {
		MusicService getService() {
			return MusicService.this;
		}
	}
}
