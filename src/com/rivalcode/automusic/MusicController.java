package com.rivalcode.automusic;

import android.content.Context;
import android.widget.MediaController;

public class MusicController extends MediaController {

	public MusicController(Context context) {
		super(context);
	}

	public void hide() {
		super.show();
	}
	
	public void hideAux() {
		super.hide();
	}
}
