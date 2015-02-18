package com.amazon.tweetradio;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.database.MatrixCursor;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class UserTimelineProcessor {
  
    public static final String      LOG_TAG = "TR_UserTimeline";
    
    public static final String[]    CURSOR_COLUMN_NAMES = new String[] {"_id", TweetRadioConstants.SCREEN_NAME, TweetRadioConstants.NAME, TweetRadioConstants.TEXT, TweetRadioConstants.LANG};
    public static final String[]    UI_COLUMN_NAMES = new String[] {TweetRadioConstants.SCREEN_NAME, TweetRadioConstants.NAME, TweetRadioConstants.TEXT};
    public static final int[]       TO_RESOURCE_IDS = new int[] {R.id.screenNameTextView, R.id.nameTextView, R.id.textTextView};
    
    private TweetRadioActivity  activity;
    private String              userId;
    private int                 rowLayout;
    private MatrixCursor        cursor;
    

    public UserTimelineProcessor(TweetRadioActivity activity, String userId, int rowLayout) {
        this.activity = activity;
        this.userId = userId;
        this.rowLayout = rowLayout;
    }
    
    
    public void fetchUserTimeline() throws TwitterException {
        if (cursor != null) {
            cursor.close();
        }
        cursor = new MatrixCursor(CURSOR_COLUMN_NAMES);

        Twitter twitter = activity.getTwitter();
        ResponseList<Status> statusList = twitter.getUserTimeline(userId);
        int count = 1;
        for (Status status : statusList) {
            Log.e(LOG_TAG, "Status: " + status.getLang() + "  " + status.getText());
            activity.getVocalizerForLang("hin-IND").speakString(status.getText(), activity);
            cursor.addRow(new Object[] {count, status.getUser().getScreenName(), status.getUser().getName(), status.getText(), status.getLang()});
            count++;
        }
        
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity, rowLayout, cursor, UI_COLUMN_NAMES, TO_RESOURCE_IDS, 0);
        activity.runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                activity.setListAdapter(adapter);
            }
        });
    }
}
