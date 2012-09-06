package com.example.httptest;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Xml;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	static String strUrl,title,content;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    Runnable inMainThread = new Runnable() {
		
		@Override
		public void run() {
			// TODO 自動生成されたメソッド・スタブ
			
			TextView tv = (TextView)findViewById(R.id.textView2);
			if(content == "")content=getResources().getString(R.string.hello_world);
			tv.setText(Html.fromHtml(content));
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			tv.setLinksClickable(true);
			setTitle(title);
			
		}
	};
	
	Runnable inReadingThread = new Runnable(){
		@Override
		public void run(){
			content=readHtml(false);
			runOnUiThread(inMainThread);
		}
	};
	
	public static String readHtml(boolean simple){
		String str = "";
		
		HttpURLConnection connection = null;
		try{
			URL url = new URL(strUrl);
			connection=(HttpURLConnection)url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			
			XmlPullParser xmlPP = Xml.newPullParser();
			xmlPP.setInput(new InputStreamReader(connection.getInputStream(),"UTF-8"));
			
			int eventType = xmlPP.getEventType();
			
			while(eventType != XmlPullParser.END_DOCUMENT){
				if(eventType == XmlPullParser.START_TAG){
					if(xmlPP.getName().equalsIgnoreCase("channel")){
						do{
							eventType = xmlPP.next();
							
							if(xmlPP.getName() != null &&
									xmlPP.getName().equalsIgnoreCase("title")){
								title = xmlPP.nextText();
								break;
							}

						}while(xmlPP.getName() != "item");
					}

					if(xmlPP.getName() != null&&xmlPP.getName().equalsIgnoreCase("item")){
						String itemtitle = "title";
						String linkurl = "";
						String pubdate="";
						do{
							eventType = xmlPP.next();
							if(eventType == XmlPullParser.START_TAG){
								String tagName = xmlPP.getName();
								if(tagName.equalsIgnoreCase("title")){
									itemtitle = xmlPP.nextText();										
								}else if(tagName.equalsIgnoreCase("link")){
									linkurl = xmlPP.nextText();										
								}else if (tagName.equalsIgnoreCase("pubDate")){
									pubdate = xmlPP.nextText();
								}
							}

						}while(!((eventType == XmlPullParser.END_TAG)&&
								(xmlPP.getName().equalsIgnoreCase("item"))));
						if(simple){
							str = str + Html.fromHtml(itemtitle).toString() + "\n";
						}else{
							str = str + "<a href=\" + linkurl + "\>"
									+itemtitle + "</a><br>"+ pubdate
									+"<br>";
						}
					}
				}
				eventType = xmlPP.next();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(connection != null){
				connection.disconnect();
			}
		}
		return str;
	}
}