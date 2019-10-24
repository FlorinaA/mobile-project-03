package com.florina.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.net.MailTo;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class TransitionIntentService extends IntentService {

    private static final String TAG = TransitionIntentService.class.getSimpleName();
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
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
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent: HERE" + intent);

        if (intent != null) {
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    Log.d(TAG, "onHandleIntent: TOAST"+ event.getTransitionType() + event.getActivityType());
//                    Toast.makeText(this, event.getTransitionType() + "-" + event.getActivityType(), Toast.LENGTH_LONG).show();
                    showToast("MyService is handling intent." + event.getActivityType()+ " " +event.getTransitionType());
                    //7 for walking and 8 for running
                    Log.i(TAG, "Activity Type " + event.getActivityType());

                    // 0 for enter, 1 for exit
                    Log.i(TAG, "Transition Type " + event.getTransitionType());

                    // processing done here….
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra(PARAM_OUT_MSG, event.getActivityType());
                    sendBroadcast(broadcastIntent);
                }
            }
        }
    }
}