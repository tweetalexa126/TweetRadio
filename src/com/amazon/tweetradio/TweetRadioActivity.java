package com.amazon.tweetradio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ListActivity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nuance.nmdp.speechkit.SpeechError;
import com.nuance.nmdp.speechkit.SpeechKit;
import com.nuance.nmdp.speechkit.Vocalizer;
import com.nuance.nmdp.speechkit.Vocalizer.Listener;


public class TweetRadioActivity extends ListActivity {

    private static final String LOG_TAG = "TR_Activity";
    
    private static final String     BROWSER_CONSUMER_KEY = "ie7dINtXBOzDYESMKimM6Ee0s";
    private static final String     BROWSER_CONSUMER_SECRET = "XMZWUJyOSn6hLQiBsQAF0qdg1HESH4P9PQxeUaPteKQcxZUcZA";
    private static final String     ACCESS_TOKEN = "26122273-aWRUdzbQW3YL4KwYOtrJ3UMSVBe3WeblBe9zCbpEq";
    private static final String     ACCESS_TOKEN_SECRET = "JCuWEysi6D1AtZBX4z0h82iq6BB9ITUFw6cDdPNNJw3K4";

    
    private SpeechKit               speechKit;
    private List<Handler>           handlerList;
    private int                     handlerCount;
    private Map<String, Vocalizer>  vocalizerByLangMap;
    private Twitter                 twitter;
    private String                  userId;
    
    private InputMethodManager      inputMethodManager;
    private EditText                userIdEditText;
    private LinearLayout            goLinearLayout;
    private Button                  goButton;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_radio);

        handlerList = new ArrayList<Handler>();
        for (int i = 0; i < 12; i++) {
            Handler handler = new Handler();
            handlerList.add(handler);
        }
        speechKit = SpeechKit.initialize(this, AppInfo.SpeechKitAppId, AppInfo.SpeechKitServer, AppInfo.SpeechKitPort, AppInfo.SpeechKitSsl, AppInfo.SpeechKitApplicationKey);
        speechKit.connect();
        
        initTwitter();
        vocalizerByLangMap = new HashMap<String, Vocalizer>();
        
        inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        userIdEditText = (EditText) findViewById(R.id.userIdEditText);
        goLinearLayout = (LinearLayout) findViewById(R.id.goLinearLayout);
        goButton = (Button) findViewById(R.id.goButton);
    }

    
    private void initTwitter() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.
            setDebugEnabled(true).
            setOAuthConsumerKey(BROWSER_CONSUMER_KEY).
            setOAuthConsumerSecret(BROWSER_CONSUMER_SECRET).
            setOAuthAccessToken(ACCESS_TOKEN).
            setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
        twitter = new TwitterFactory(builder.build()).getInstance();
        Log.e(LOG_TAG, "initTwitter()");
    }
    
    
    public Vocalizer getVocalizerForLang(String lang) {
        Vocalizer vocalizer = vocalizerByLangMap.get(lang);
        if (vocalizer != null) {
            return vocalizer;
        }
        
        vocalizer = speechKit.createVocalizerWithLanguage(lang, new Listener() {            
            @Override
            public void onSpeakingDone(Vocalizer vocalizer, String text, SpeechError error, Object context) {
            }
            
            @Override
            public void onSpeakingBegin(Vocalizer vocalizer, String text, Object context) {
                Log.e(LOG_TAG, "onSpeakingBegin(): " + text);
            }
        }, handlerList.get(handlerCount++));
        vocalizerByLangMap.put(lang, vocalizer);
        return vocalizer;
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tweet_radio, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void goButtonClicked(View view) {
        userId = userIdEditText.getText().toString();
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        Log.e(LOG_TAG, "goButtonClicked(): " + userId);
        
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... void0) {
                UserTimelineProcessor processor = new UserTimelineProcessor(TweetRadioActivity.this, userId, R.layout.tweet);
                try {
                    processor.fetchUserTimeline();
                } 
                catch (TwitterException e) {
                    Log.e(LOG_TAG, "Exception in fetchUserTimeline()", e);
                }
                return null;
            }
        }.execute();
    }
    
    
    public Twitter getTwitter() {
        return twitter;
    }
 }
