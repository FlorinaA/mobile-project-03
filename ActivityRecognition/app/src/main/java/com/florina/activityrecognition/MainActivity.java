package com.florina.activityrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView textView;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps;
    private TextView TvSteps;

    //    Activity Recognition
    private static final String TAG = MainActivity.class.getSimpleName();
    private List<ActivityTransition> transitions;
    private ActivityRecognitionClient activityRecognitionClient;
    private PendingIntent transitionPendingIntent;
    private Context mContext;
    private ResponseReceiver receiver;
    private ImageView activityImage;
    ArrayList<Long> startTime = new ArrayList<>();
    ArrayList<Long> endTime = new ArrayList<>();

    private static MediaPlayer mediaPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        numSteps = -1;
        sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
//        ACTIVITY RECOGNITION
        activityImage = findViewById(R.id.activityImage);
        textView = findViewById(R.id.activityText);
        mContext = this;
        activityRecognitionClient = ActivityRecognition.getClient(mContext);

        Intent intent = new Intent(this, TransitionIntentService.class);
        transitionPendingIntent = PendingIntent.getService(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);
//        registerHandler();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHandler();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText("Steps taken since app started: " + numSteps);
    }

    public void registerHandler() {
        transitions = new ArrayList<>();

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());


        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());


        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());


        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());


        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());

        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());


        ActivityTransitionRequest activityTransitionRequest = new ActivityTransitionRequest(transitions);

        Task<Void> task = activityRecognitionClient.requestActivityTransitionUpdates(activityTransitionRequest, transitionPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                Toast.makeText(mContext, "Transition update set up", Toast.LENGTH_LONG).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(mContext, "Transition update Failed to set up", Toast.LENGTH_LONG).show();
//                e.printStackTrace();
            }
        });

    }


    public void unregisterHandler() {
        Task<Void> task = activityRecognitionClient.removeActivityTransitionUpdates(transitionPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                transitionPendingIntent.cancel();
//                Toast.makeText(mContext, "Remove Activity Transition Successfully", Toast.LENGTH_LONG).show();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(mContext, "Remove Activity Transition Failed", Toast.LENGTH_LONG).show();
            }
        });
    }


    public void playMusic(){
        mediaPlayer = MediaPlayer.create(this, R.raw.beat_02);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void displayImageAndAudio(String text){

        switch(text){
            case "IN_VEHICLE":
                mediaPlayer.stop();
                activityImage.setImageResource(R.drawable.in_vehicle);
                break;
            case "RUNNING":
                playMusic();
                activityImage.setImageResource(R.drawable.running);
                break;
            case "STILL":
                mediaPlayer.stop();
                activityImage.setImageResource(R.drawable.still);
                break;
            case "WALKING":
                playMusic();
                activityImage.setImageResource(R.drawable.walking);
                break;
            default:
                mediaPlayer.stop();
                activityImage.setImageResource(R.drawable.still);
                break;
        }


    }


    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.mamlambo.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            //activity transition is legit. Do stuff here..
            TextView activityText = findViewById(R.id.activityText);
            String text = intent.getStringExtra(TransitionIntentService.PARAM_OUT_MSG);
            startTime.add(intent.getLongExtra(TransitionIntentService.ENTER_TIME, 0));
            endTime.add(intent.getLongExtra(TransitionIntentService.LEAVE_TIME, 0));
            displayImageAndAudio(text);
            String timeTaken = "";
            Log.d(TAG, "STARTIME: " + startTime);
            Log.d(TAG, "ENDTIME: " + endTime);
            Log.d(TAG, "ENDTIME SIZE: " + endTime.size());
            Log.d(TAG, "BLA: " + startTime.get(startTime.size() - 1)  + endTime.get(endTime.size() - 1));
            if (endTime.size() != 0) {
                if (startTime.get(startTime.size() - 1) != 0 && endTime.get(endTime.size() - 1) != 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date(endTime.get(endTime.size() - 1) - startTime.get(startTime.size() - 1));
                    timeTaken = sdf.format(date);
                    Toast.makeText(mContext, "You have been" + text + "for" + timeTaken, Toast.LENGTH_LONG).show();

                }


            }
            Log.d(TAG, "onReceive: START" + timeTaken);
            activityText.setText("You are " + text);
        }
    }


}

