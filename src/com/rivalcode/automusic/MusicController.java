package com.rivalcode.automusic;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.widget.MediaController;

public class MusicController extends MediaController {

	public MusicController(Context context) {
		super(new ContextThemeWrapper(context, R.style.MusicPlayer));
	}

	/*public void hide() {
		super.show();
	}*/
	
	public void hideAux() {
		super.hide();
	}
}
