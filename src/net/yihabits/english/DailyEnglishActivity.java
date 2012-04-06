package net.yihabits.english;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import net.yihabits.english.R;
import net.yihabits.english.customview.SegmentButton;
import net.yihabits.english.customview.SegmentButton.OnSegmentChangedListener;
import net.yihabits.english.db.EnglishDAO;
import net.yihabits.english.db.EnglishDBOpenHelper;
import net.yihabits.english.db.EnglishModel;

public class DailyEnglishActivity extends ListActivity {
	private String uid;
	private String LOGTAG = "DailyEnglishActivity";

	private int selected_item = -1;
	
	private int source = 1; // 1 - bbc ; 2 - esl
	private boolean isPlayAll = false; 

	private long lastUpdateTime;

	private EnglishDAO dba;

	private ArrayList<EnglishModel> englishList;

	private DownloadUtil util;

	private LinearLayout btnLayout;

	private ToggleButton playSelectBtn;
	
	private WebView contentLbl;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// initialize DownloadUtil
		if (util == null) {
			util = new DownloadUtil(this);
			util.initBaseDir();
		}

		if (englishList == null) {
			englishList = new ArrayList<EnglishModel>();
		}

		dba = EnglishDAO.getInstance(this);

		// ad initialization
		// Create the adView
		AdView adView = new AdView(this, AdSize.BANNER, "a14dedd35cc1632");
		// Lookup your LinearLayout assuming it��s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.ad_layout);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());

		// listen phone state
		MyPhoneStateListener phoneListener = new MyPhoneStateListener();
		phoneListener.setContext(this);

		TelephonyManager telephony = (TelephonyManager)

		this.getSystemService(Context.TELEPHONY_SERVICE);

		telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		btnLayout = (LinearLayout) findViewById(R.id.btn_layout);

		contentLbl = (WebView) findViewById(R.id.contentLbl);
		contentLbl.getSettings().setJavaScriptEnabled(true);
		showContent(false);
		
	}

	public class MyPhoneStateListener extends PhoneStateListener {

		private Context context;

		public Context getContext() {
			return context;
		}

		public void setContext(Context context) {
			this.context = context;
		}

		public void onCallStateChanged(int state, String incomingNumber) {

			switch (state) {

			case TelephonyManager.CALL_STATE_IDLE: {

				Log.d("DEBUG", "IDLE");

				if (DownloadUtil.player != null
						&& DownloadUtil.player.isInterrupted()) {
					DownloadUtil.player.playatpause();
				}
				break;
			}

			case TelephonyManager.CALL_STATE_OFFHOOK:

				Log.d("DEBUG", "OFFHOOK");

			case TelephonyManager.CALL_STATE_RINGING:

				Log.d("DEBUG", "RINGING");

				// pause player
				if (DownloadUtil.player != null) {
					DownloadUtil.player.pause();
				}

				break;

			}

		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		// initialize download folders
		util.initBaseDir();

		// get last updated time
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String key = source + "lastUpdateTime";
		lastUpdateTime = prefs.getLong(key, 0);

		getListView().setChoiceMode(ListView.CHOICE_MODE_NONE);

		initList();
		
		initBtn();
	}

	@Override
	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (contentLbl.getVisibility() == View.VISIBLE) {
				showContent(false);
			} else {
				this.finish();
			}
			return true;
		default:
			return false;
		}
	}

	private void initList() {

		englishList.clear();
		
		englishList.addAll(getAllEnglish());

		if (englishList.size() > 0) {
			// set adapter
			refreshList();

			// check if the last update time exceeds 1 day
			if (new Date().getTime() - lastUpdateTime > 86400000) {
				Runnable saveUrl = new Runnable() {

					public void run() {

						// get list from the web and insert new report into db
						util.updateEnglishFromWeb(source);

						// save the new update time
						savePrefRelease(new Date().getTime());

						// set adapter
						refreshList();

					}

				};
				new Thread(saveUrl).start();
			}
		} else {

			// show waiting dialog
			final ProgressDialog dialog = ProgressDialog.show(
					DailyEnglishActivity.this, getString(R.string.waitTitle),
					getString(R.string.wait), true);

			Runnable saveUrl = new Runnable() {

				public void run() {

					// get list from the web and insert new report into db
					util.updateEnglishFromWeb(source);
					englishList.addAll(getAllEnglish());

					// save the new update time
					savePrefRelease(new Date().getTime());

					// set adapter
					refreshList();

					// hide waiting dialog
					dialog.dismiss();
				}

			};
			new Thread(saveUrl).start();
		}
	}

	private ArrayList<EnglishModel> getAllEnglish() {
		ArrayList<EnglishModel> list = new ArrayList<EnglishModel>();

		// get the list from db
		dba.open();

		Cursor c = dba.getAllEnglishBySource(source);
		if (c.moveToFirst()) {
			do {
				long id = c.getLong(0);
				String name = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.NAME));
				String category = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.CATEGORY));
				String content = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.CONTENT));
				String published = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.PUBLISHED_AT));
				String reportLoc = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.REPORT_LOCATION));
				String reportUrl = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.REPORT_URL));
				String url = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.URL));
				String wordsLoc = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.WORDS_LOCATION));
				String wordsUrl = c.getString(c
						.getColumnIndex(EnglishDBOpenHelper.WORDS_URL));
				int source1 = c.getInt(c
						.getColumnIndex(EnglishDBOpenHelper.SOURCE));

				EnglishModel temp = new EnglishModel();
				temp.setId(id);
				temp.setName(name);
				temp.setSource(source1);
				temp.setCategory(category);
				temp.setContent(content);
				temp.setPublishedAt(published);
				temp.setReportLocation(reportLoc);
				temp.setReportUrl(reportUrl);
				temp.setUrl(url);
				temp.setWordsLocation(wordsLoc);
				temp.setWordsUrl(wordsUrl);

				list.add(temp);
			} while (c.moveToNext());
		}

		c.close();
		dba.close();

		return list;
	}

	public void refreshList() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				// selected_item = -1;
				EnglishAdapter adapter = new EnglishAdapter(
						DailyEnglishActivity.this);
				setListAdapter(adapter);
				getListView().setSelectionAfterHeaderView();
			}
		});

	}

	@Override
	protected void onListItemClick(final ListView list, View v,
			final int position, long id) {
		super.onListItemClick(list, v, position, id);

		selected_item = position;
		((BaseAdapter) getListAdapter()).notifyDataSetChanged();

		// check content
		final String content = englishList.get(position).getContent();
		if (content == null) {

			final ProgressDialog dialog = ProgressDialog.show(
					DailyEnglishActivity.this, getString(R.string.waitTitle),
					getString(R.string.startUpdateContent), true);
			Runnable updateContent = new Runnable() {
				@Override
				public void run() {

							// play the music
					if(source == 1){
						String newContent = util.updateEnglishContent(position, englishList.get(position)
								.getUrl());
						
						// set content for the report
						englishList.get(position).setContent(newContent);
						contentLbl.loadData(newContent, "text/html", "utf-8");
						
							if (playSelectBtn.isChecked()) {
								util.saveNPlayUrl(englishList.get(selected_item).getUrl(), englishList.get(selected_item)
										.getReportUrl());
							} else {
								util.saveNPlayUrl(englishList.get(selected_item).getUrl(), englishList.get(selected_item)
										.getWordsUrl());
							}
					}else{
						contentLbl.loadData(content, "text/html", "utf-8");
						
						util.saveNPlayUrl(englishList.get(selected_item).getUrl(), englishList.get(selected_item)
								.getReportUrl());
					}
							showContent(true);
							dialog.dismiss();

				}
			};
			new Thread(updateContent).start();
			
		}else{
		// set content for the report
			contentLbl.loadData(content, "text/html", "utf-8");
		
		showContent(true);

		// set selected
		list.post(new Runnable() {
			@Override
			public void run() {

				if(source == 1){
					if (playSelectBtn.isChecked()) {
						util.saveNPlayUrl(englishList.get(selected_item).getUrl(), englishList.get(selected_item)
								.getReportUrl());
					} else {
						util.saveNPlayUrl(englishList.get(selected_item).getUrl(), englishList.get(selected_item)
								.getWordsUrl());
					}
			}else{
				util.saveNPlayUrl(englishList.get(selected_item).getUrl(), englishList.get(selected_item)
						.getReportUrl());
			}

			}
		});
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_help:
			// popup the about window
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setClass(DailyEnglishActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void savePrefRelease(long time) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		String key = source + "lastUpdateTime";
		editor.putLong(key, time);
		editor.commit();

		// update field
		lastUpdateTime = time;
	}

	public void toastMsg(int resId) {
		final String msg = this.getString(resId);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public String getUid() {
		if (this.uid == null) {
			this.uid = Settings.Secure.getString(getContentResolver(),
					Settings.Secure.ANDROID_ID);
		}
		return this.uid;
	}

	private void initBtn() {
		final Button playBtn = (Button) findViewById(R.id.playBtn);

		final Button stopBtn = (Button) findViewById(R.id.stopBtn);

		final TextView displayLbl = (TextView) findViewById(R.id.displayLbl);
		displayLbl.setText(R.string.noMusic);

		final LinearLayout btnLayout = (LinearLayout) findViewById(R.id.btn_layout);
		btnLayout.setVisibility(View.INVISIBLE);

		playBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// play or pause
				if (DownloadUtil.player != null
						&& DownloadUtil.player.isPlayed()
						&& !DownloadUtil.player.isPaused()) {
					// player is playing, so pause it and change the icon
					DownloadUtil.player.pause();
					v.setBackgroundResource(R.drawable.media_playback_start2);
					displayLbl.setText(R.string.paused);
				} else if (DownloadUtil.player != null
						&& DownloadUtil.player.isPlayed()
						&& DownloadUtil.player.isPaused()) {
					// player is paused, so resume the music and change the icon
					DownloadUtil.player.playatpause();
					v.setBackgroundResource(R.drawable.media_playback_pause2);
					displayLbl.setText(R.string.playing);
				} else {
					displayLbl.setText(R.string.noMusic);
				}
			}
		});

		stopBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				displayLbl.setText(R.string.noMusic);
				if (DownloadUtil.player != null) {
					// stop to play the music
					DownloadUtil.player.end();
				}
				playBtn.setBackgroundResource(R.drawable.media_playback_start2);
				displayToolbar(false);
			}
		});

		playSelectBtn = (ToggleButton) findViewById(R.id.playSelectBtn);

		playSelectBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// change the playing music
				if (selected_item >= 0
						&& btnLayout.getVisibility() == View.VISIBLE) {
					if (playSelectBtn.isChecked()) {
						util.saveNPlayUrl(englishList.get(selected_item).getUrl(),
								englishList.get(selected_item).getReportUrl());
					} else {
						util.saveNPlayUrl(englishList.get(selected_item).getUrl(),
								englishList.get(selected_item).getWordsUrl());
					}
				}
			}
		});
		
		final ToggleButton playAllBtn = (ToggleButton) findViewById(R.id.playAllBtn);

		playAllBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isPlayAll = playAllBtn.isChecked(); 
			}
		});
		
		SegmentButton sourceSegBtn = (SegmentButton) findViewById(R.id.sourceSegBtn);
		sourceSegBtn.newButton("BBC", 0);
		sourceSegBtn.newButton("ESL", 1);
		sourceSegBtn.setSelectedIndex(0);
		
		sourceSegBtn.setOnSegmentChangedListener(new OnSegmentChangedListener() {
			
			@Override
			public void onSegmentChanged(int index) {
				source = index+1;
				selected_item = -1;
				
				showContent(false);
				initList();
				if(source == 1){
					playSelectBtn.setVisibility(View.VISIBLE);
				}else{
					playSelectBtn.setVisibility(View.GONE);
				}
				
			}
		});

	}

	public void enableBtn() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Button playBtn = (Button) findViewById(R.id.playBtn);
				playBtn.setBackgroundResource(R.drawable.media_playback_pause2);
				TextView displayLbl = (TextView) findViewById(R.id.displayLbl);
				displayLbl.setText(R.string.playing);
				displayToolbar(true);
			}
		});

	}

	public void showContent(final boolean flag) {
		runOnUiThread(new Thread() {
			public void run() {
		if (!flag) {

			// hide textview and show listview
			contentLbl.setVisibility(View.GONE);
			getListView().setVisibility(View.VISIBLE);

		} else {

			// show textview and hide listview
			getListView().setVisibility(View.GONE);
			contentLbl.setVisibility(View.VISIBLE);

			displayToolbar(true);
		}
			}
		});
	}

	void displayToolbar(final boolean display) {
		runOnUiThread(new Thread() {
			public void run() {
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (display) {
					TextView displayLbl = (TextView) findViewById(R.id.displayLbl);
					displayLbl.setText(R.string.playing);

					btnLayout.setVisibility(View.VISIBLE);
				} else {
					btnLayout.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	public boolean insert(EnglishModel em) {
		long flag = -1;
		dba.open();

		Cursor c = dba.getEnglishByUrl(em.getUrl());
		if (!c.moveToFirst()) {
			flag = dba.insert(em);
		}
		c.close();

		dba.close();

		return flag > 0;
	}

	public void updateContent(final int position, final String content,
			final String reportUrl, final String wordsUrl) {
				dba.open();

				// 1.update englishList
				englishList.get(position).setContent(content);
				englishList.get(position).setReportUrl(reportUrl);
				englishList.get(position).setWordsUrl(wordsUrl);

				// 2.update content and urls
				dba.updateContent(englishList.get(position).getUrl(), content,
						reportUrl, wordsUrl);
				dba.close();

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
				// 3.refresh the list
				((BaseAdapter) DailyEnglishActivity.this.getListAdapter())
						.notifyDataSetChanged();
					}
				});


	}

	public void updateLocation(final String url, final String location) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dba.open();

				// update report location
				if (location.contains("_report_audio_")) {
					// 1.update list
//					englishList.get(position).setReportLocation(location);
					// 2.update db

					dba.updateReportLocation(
							url, location);

				} else {
					// update words location
					// 1.update list
//					englishList.get(position).setWordsLocation(location);
					// 2.update db

					dba.updateWordsLocation(url,
							location);
				}

				// 3.refresh the list
				((BaseAdapter) DailyEnglishActivity.this.getListAdapter())
						.notifyDataSetChanged();

				dba.close();
			}
		});

	}

	private class EnglishAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public EnglishAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return englishList.size();
		}

		public EnglishModel getItem(int i) {
			return englishList.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		public View getView(final int position, View convertView, ViewGroup vg) {
			if (englishList == null || position < 0
					|| position > englishList.size())
				return null;

			final View row;
			if (convertView == null) {
				row = mInflater.inflate(R.layout.list_item, null);
			} else {
				row = convertView;
			}

			ViewHolder holder = (ViewHolder) row.getTag();
			if (holder == null) {
				holder = new ViewHolder(row);
				row.setTag(holder);
			}

			// other normal row
			final EnglishModel rm = englishList.get(position);

			// set name to label
			holder.title.setText(position + 1 + "." + rm.getName());

			holder.icon.setVisibility(View.VISIBLE);
			// set tringle for the icon
			if (position == selected_item) {
				holder.icon.setImageResource(R.drawable.right_selected_24);
			} else {
				holder.icon.setImageResource(R.drawable.right_24);
			}

			return (row);
		}

	}

	class ViewHolder {
		TextView title = null;
		ImageView icon = null;

		ViewHolder(View base) {
			this.title = (TextView) base.findViewById(R.id.row_title);
			this.icon = (ImageView) base.findViewById(R.id.icon);
		}
	}

	public int getSource() {
		return source;
	}
	
}