package net.yihabits.english;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.yihabits.english.R;
import net.yihabits.english.db.EnglishModel;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.Toast;

public class DownloadUtil {

	private static String english_url = "http://www.bbc.co.uk/worldservice/learningenglish/language/wordsinthenews";
	
	private static String english_url2 = "http://www.eslpod.com/website/show_all.php";

	private DailyEnglishActivity activity;
	public static MyAudioPlayer player;

	public DownloadUtil(DailyEnglishActivity activity) {
		this.activity = activity;
	}

	public void saveNPlayUrl(final String url, final String audioUrl) {
		final String dir = initBaseDir() + "/";
		if ("NA".equals(audioUrl)) {
			this.activity.toastMsg(R.string.noAudio);
			return;
		}

		Runnable saveUrl = new Runnable() {
			public void run() {
				saveUrl(audioUrl, dir, url);

				// notify the activity refresh progress and button

			}
		};
		new Thread(saveUrl).start();
	}

	private void saveUrl(String audiourl, String dir, String url) {
		if(audiourl == null){
			return;
		}
		HttpEntity resEntity = null;

		String path = dir + parseFileNameByUrl(audiourl);
		File ftmp = new File(path);
		if (!ftmp.exists()) {

			try {

				this.activity.toastMsg(R.string.startDownload);

				ApplicationEx app = (ApplicationEx) this.activity
						.getApplication();

				HttpClient httpclient = app.getHttpClient();

				HttpGet httpget = new HttpGet(audiourl);

				HttpResponse response = httpclient.execute(httpget);

				int status = response.getStatusLine().getStatusCode();

				if (status == HttpStatus.SC_OK) {

					resEntity = response.getEntity();

					// 1. save to sdcard
					save2card(resEntity, path);
					
					this.activity.toastMsg(R.string.stopDownload);

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {

				if (resEntity != null) {
					try {
						resEntity.consumeContent();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
			// 3.deal music by menu item
			if (player != null) {
				player.end();
			}
			player = new MyAudioPlayer(this.activity, new File(path));
			player.go();
			((DailyEnglishActivity) this.activity).enableBtn();
	}

	public void updateEnglishFromWeb(int enSource) {
		// get the list page
		HttpEntity resEntity = null;

		try {

			ApplicationEx app = (ApplicationEx) this.activity.getApplication();

			HttpClient httpclient = app.getHttpClient();

			HttpGet httpget = new HttpGet(enSource==1?english_url:english_url2);

			HttpResponse response = httpclient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {

				resEntity = response.getEntity();
				String content = EntityUtils.toString(resEntity);
				

				if(enSource == 1){
					Source source = new Source(content);
				// parse list
				Element divEle = source.getElementById("plain1");
				processDiv(divEle, "video");
				
				divEle = source.getElementById("plain2");
				processDiv(divEle, "news");

				divEle = source.getElementById("plain3");
				processDiv(divEle, "business");

				divEle = source.getElementById("plain4");
				processDiv(divEle, "others");
				}else{
					//parse esl list
					int start = content.indexOf("<TD width=\"99%\"");
					content = content.substring(start);
					int end = content.indexOf("background=\"images/square_2_06.gif");
					content = content.substring(0, end);
					Source source = new Source(content);
					
					List<Element> tList = source.getAllElements(HTMLElementName.TABLE);
					int i = 0;
					for(Element tEle : tList){
						i++;
						if(i < 9){
							continue;
						}
						List<Element> aList = tEle.getAllElements(HTMLElementName.A);
						//get date
						String date = tEle.getFirstElement(HTMLElementName.SPAN).getTextExtractor().toString();
						//get title
						String title = aList.get(0).getTextExtractor().toString();
						//get url
						String url = "http://www.eslpod.com/website/" + aList.get(0).getAttributeValue("href");
						//get mp3 url
						String mp3Url = aList.get(3).getAttributeValue("href");
						
						//get brief content
						String econtent = "";
						Element spanEle = tEle.getAllElementsByClass("pod_body").get(1);
						if(spanEle != null){
							econtent = spanEle.getTextExtractor().toString().trim().substring(16);
						}else{
							econtent = "N/A";
						}
						
						EnglishModel em = new EnglishModel();
						em.setUrl(url);
						em.setName(title);
						em.setReportUrl(mp3Url);
						em.setPublishedAt(date);
						em.setSource(2);
						em.setContent(econtent);
						
						// try to insert em into db, if failed, then break the loop
						boolean flag = this.activity.insert(em);

						if (!flag) {
							break;
						}
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (resEntity != null) {
				try {
					resEntity.consumeContent();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void processDiv(Element divEle, String category) {
		if (divEle != null) {
			List<Element> tlist = divEle
					.getAllElementsByClass("teaser ts-headline");
			for (Element ele : tlist) {
				Element titleEle = ele.getFirstElement(HTMLElementName.A);
				String name = titleEle.getTextExtractor().toString();
				String url = titleEle.getAttributeValue("href");
				List<Element> dateList = ele.getAllElements(HTMLElementName.DIV);
				String date = "";
				if(dateList.size() > 1){
				 date = dateList.get(1)
						.getTextExtractor().toString();
				}else{
					date = dateList.get(0)
					.getTextExtractor().toString();
				}

				EnglishModel em = new EnglishModel();
				em.setName(name);
				em.setUrl("http://www.bbc.co.uk" + url);
				em.setPublishedAt(date);
				em.setCategory(category);
				em.setSource(1);

				// try to insert em into db, if failed, then break the loop
				boolean flag = this.activity.insert(em);

				if (!flag) {
					break;
				}
			}
		}
	}

	public String updateEnglishContent(int position, String url) {
		// get the content of report
		HttpEntity resEntity = null;

		try {

//			this.activity.toastMsg(R.string.startUpdateContent);
			
			ApplicationEx app = (ApplicationEx) this.activity.getApplication();

			HttpClient httpclient = app.getHttpClient();

			HttpGet httpget = new HttpGet(url);

			HttpResponse response = httpclient.execute(httpget);

			int status = response.getStatusLine().getStatusCode();

			if (status == HttpStatus.SC_OK) {

				resEntity = response.getEntity();
				String content = EntityUtils.toString(resEntity);
				Source source = new Source(content);

				// parse main content
				content = "";
				Element cele = source.getFirstElement(HTMLElementName.H1);
				content += addTrTd(cele.toString());

				cele = source.getElementById("story");
				if (cele != null) {
					content += addTrTd(cele.toString());
				}

				// parse audio url
				List<Element> aele = source
						.getAllElementsByClass("audio-link file-link");
				if (aele != null && aele.size() > 0) {
					// update content in db
					this.activity.updateContent(position, content, aele.get(0)
							.getFirstElement(HTMLElementName.A)
							.getAttributeValue("href"), (aele.size() > 1)?aele.get(1)
							.getFirstElement(HTMLElementName.A)
							.getAttributeValue("href"):"NA");
				} else {

					// update content in db
					this.activity.updateContent(position, content, "NA", "NA");
				}
				
//				this.activity.toastMsg(R.string.stopUpdateContent);
				
				return content;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (resEntity != null) {
				try {
					resEntity.consumeContent();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;

	}

	public String convertUrl2Path(String url) {
		int start = url.indexOf("p=");
		if (start > 0) {
			String tmp = url.substring(start + 2);
			tmp = tmp.replace("?", "_");
			tmp = tmp.replace("/", "_");
			if (!tmp.endsWith(".html")) {
				tmp += ".html";
			}
			return tmp;
		} else {
			return null;
		}
	}

	private String parseFileNameByUrl(String url) {
		if(url == null){
			return null;
		}
		String res = "";
		int start = url.lastIndexOf("/") + 1;
		res = url.substring(start+1);
		res = res.replace("&", "_");
		res = res.replace("=", "_");
		res = res.replace("%", "_");
		return res;

	}

	private void save2card(HttpEntity resEntity, String path) {
		try {
			// save to sdcard
			FileOutputStream fos = new FileOutputStream(new File(path));
			resEntity.writeTo(fos);

			// release all instances
			fos.flush();
			fos.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public String initBaseDir() {
		File sdDir = Environment.getExternalStorageDirectory();
		File uadDir = null;
		if (sdDir.exists() && sdDir.canWrite()) {

		} else {
			sdDir = Environment.getDataDirectory();

		}
		uadDir = new File(sdDir.getAbsolutePath() + "/english/");
		if (!uadDir.exists()) {
			uadDir.mkdirs();
		}
		return uadDir.getAbsolutePath();
	}

	public static String getIndexPath() {
		File sdDir = Environment.getExternalStorageDirectory();
		if (sdDir.exists() && sdDir.canWrite()) {

		} else {
			sdDir = Environment.getDataDirectory();

		}
		File index = new File(sdDir.getAbsolutePath() + "/download/index.html");
		return index.getAbsolutePath();
	}

	public void toastMsg(final String msg) {
		this.activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(
						DownloadUtil.this.activity.getApplicationContext(),
						msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	private String addTrTd(String content) {
		return "<TR><TD>" + content + "</TD></TR>";
	}

}
