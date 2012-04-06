package net.yihabits.english;

import java.io.File;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MyAudioPlayer extends Thread {
	private Context c;
	private Thread blinker;
	private File file;
	private MediaPlayer ap;
	private long pauseTime;
	private boolean paused = false;

	public MyAudioPlayer (Context c, File file) {
	    this.c = c;
	    this.file = file;
	}

	public void go () {
	    blinker = this;
	    if(!blinker.isAlive()) {
	        blinker.start();
	    }
	}

	public void end () {
	    Thread waiter = blinker;
	    blinker = null;
	    if (waiter != null)
	        waiter.interrupt ();
	}
	
	public void pause(){
		if(ap != null){
			ap.pause();
			pauseTime = System.currentTimeMillis();
			paused = true;
		}
	}
	
	public void playatpause(){
		if(ap != null && pauseTime != 0){
			ap.start();
			pauseTime = System.currentTimeMillis() - pauseTime;
			paused = false;
		}
	}

	public void run () {
	    ap = MediaPlayer.create(c, Uri.fromFile(file));
	    if(ap == null){
	    	return;
	    }
	    int duration = ap.getDuration();
	    long startTime = System.currentTimeMillis();
	    ap.start();
	    try {
	        Thread thisThread = Thread.currentThread();
	        while ((this.blinker == thisThread && System.currentTimeMillis() - pauseTime - startTime < duration) || paused) {           
	            Thread.sleep (500);  // interval between checks (in ms)
	        }
	        ap.stop ();
	        ap.release ();
	        ap = null;
	        
	        //callback the activity
	        ((DailyEnglishActivity)this.c).displayToolbar(false);
	        
	    } catch (InterruptedException e) {
	        Log.d("AUDIO-PLAYER", "INTERRUPTED EXCEPTION");
	        ap.stop ();
	        ap.release();
	        ap = null;
	    }
	    }

	public boolean isPaused() {
		return paused;
	}
	
	public boolean isPlayed() {
		return ap != null;
	}
	}
