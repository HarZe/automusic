package com.rivalcode.automusic;

public class Song {
	protected long id;
	protected String title;
	protected String artist;
	protected String album;
	protected long duration;
	protected String durationString;
	protected String song_id;

	public Song(long id, String title, String artist, String album, long duration, String song_id) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.album = album;
		this.duration = duration;
		this.durationString = (duration / 1000) / 60 + ":" + ((duration / 1000) % 60 > 9 ? "" + (duration / 1000) % 60 : "0" + (duration / 1000) % 60);
		this.song_id = song_id;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public long getDuration() {
		return duration;
	}
	
	public String getDurationString() {
		return durationString;
	}

	public String getSong_id() {
		return song_id;
	}
	
}
