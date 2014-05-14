package com.rivalcode.automusic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends Activity {

	protected ImageButton localButton;
	protected ImageButton youtubeButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		localButton = (ImageButton) findViewById(R.id.local_button);
		youtubeButton = (ImageButton) findViewById(R.id.youtube_button);
		
		localButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(MainActivity.this, MusicActivity.class);
				startActivity(i);
            }
        });
		
		youtubeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent i = new Intent(MainActivity.this, YoutubeActivity.class);
				startActivity(i);
            }
        });
	}
	
}
