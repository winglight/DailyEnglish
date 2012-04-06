package net.yihabits.english;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;

public class AboutActivity extends Activity {
	
	private String LOGTAG = "AboutActivity";

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.history);
		
		// ad initialization
		// Create the adView
		AdView adView = new AdView(this, AdSize.BANNER, "a14dd47e50f3b70");
		// Lookup your LinearLayout assuming it��s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad_layout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());
		
		//set text of about content
		WebView about = (WebView)findViewById(R.id.about_content);
//		registerForContextMenu(about);
//		about.getSettings().setJavaScriptEnabled(true);
		
			about.loadUrl("file:///android_asset/help.html");
		
	}
	
}
