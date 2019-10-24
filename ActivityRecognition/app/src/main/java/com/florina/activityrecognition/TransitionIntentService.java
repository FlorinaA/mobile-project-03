package com.florina.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.net.MailTo;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
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
        switch(activity) {
            case 0:
                return "IN_VEHICLE";
            case 8:
                return "RUNNING";
            case 3:
                return "STILL";
            case 7:
                return "WALKING";
            default:
                return "UNKNOWN ACTIVITY";
        }
    }
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent: HERE" + intent);

        if (intent != null) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                        for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                            Integer activityType = event.getActivityType();

                            Log.d(TAG, "onHandleIntent: TOAST"+ event.getTransitionType() + activityType);
                            showToast("MyService is handling intent." + activityType+ " " +event.getTransitionType());
                            //7 for walking and 8 for running
                            Log.i(TAG, "Activity Type " +activityType);
                            // 0 for enter, 1 for exit
                            Log.i(TAG, "Transition Type " + event.getTransitionType());

                            // processing done here….
                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
                            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                            broadcastIntent.putExtra(PARAM_OUT_MSG, mapToActivity(activityType));
                            Long hasEntered = null;
                            Long hasLeft = null;

                            if(event.getTransitionType() == 0) {
                                hasEntered = System.currentTimeMillis();
                                Log.d(TAG, "onHandleIntent: HASENTERED" + hasEntered);

                            }
                            else {
                                hasLeft = System.currentTimeMillis();
                            }
                            Log.d(TAG, "HAS: " + hasEntered);
                            broadcastIntent.putExtra(ENTER_TIME, hasEntered);
                            broadcastIntent.putExtra(LEAVE_TIME, hasLeft);
                            sendBroadcast(broadcastIntent);
                        }



            }
        }
    }
}