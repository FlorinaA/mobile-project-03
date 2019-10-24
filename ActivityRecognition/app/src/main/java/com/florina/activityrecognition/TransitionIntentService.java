package com.florina.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.net.MailTo;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TransitionIntentService extends IntentService {

    private static final String TAG = TransitionIntentService.class.getSimpleName();
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static final String ENTER_TIME = "enter_time";
    public static final String LEAVE_TIME = "leave_time";

    public TransitionIntentService() {
        super("TransitionIntentService");
    }

    public void showToast(String message) {
        final String msg = message;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public String mapToActivity(Integer activity) {
        switch (activity) {
            case 0:
                return "Driving";
            case 8:
                return "Running";
            case 3:
                return "Still";
            case 7:
                return "Walking";
            default:
                return "";
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent: HERE" + intent);

        //Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            Integer detectedActivity = 3;
            Long hasEntered = null;
            for (DetectedActivity activity : detectedActivities) {
                Log.d(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
                if( activity.getConfidence() >= 55 ) {
                    detectedActivity = activity.getType();
//                    hasEntered = System.currentTimeMillis();
                }

            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(PARAM_OUT_MSG, mapToActivity(detectedActivity));
//            broadcastIntent.putExtra(ENTER_TIME, hasEntered);
            sendBroadcast(broadcastIntent);
        }
    }
}

