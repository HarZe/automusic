package com.rivalcode.automusic;

import android.content.Context;
import android.widget.MediaController;

public class MusicController extends MediaController {

	public MusicController(Context context) {
		super(context);
	}

	// override for avoiding hidding
	public void hide() {
		
	}
}
