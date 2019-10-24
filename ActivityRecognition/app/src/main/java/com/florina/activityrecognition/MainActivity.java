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
    ArrayList<String> activities= new ArrayList<>();
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
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
        Task<Void> task = activityRecognitionClient.requestActivityUpdates(0, transitionPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }


    public void unregisterHandler() {
        Task<Void> task = activityRecognitionClient.removeActivityTransitionUpdates(transitionPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                transitionPendingIntent.cancel();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
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
            case "Driving":
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                activityImage.setImageResource(R.drawable.in_vehicle);
                break;
            case "Running":
                playMusic();
                activityImage.setImageResource(R.drawable.running);
                break;
            case "Walking":
                playMusic();
                activityImage.setImageResource(R.drawable.walking);
                break;
            default:
                if(mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                activityImage.setImageResource(R.drawable.still);
                break;
        }

    }


    public class ResponseReceiver extends BroadcastReceiver {
        public static final String ACTION_RESP =
                "com.florina.activityrecognition.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            //activity transition is legit. Do stuff here..
            TextView activityText = findViewById(R.id.activityText);
            String text = intent.getStringExtra(TransitionIntentService.PARAM_OUT_MSG);
            activities.add(text);
//            startTime.add(intent.getLongExtra(TransitionIntentService.ENTER_TIME, 0));
            String timeTaken = "";
            if(activities.size() == 1) {
                startTime.add(System.currentTimeMillis());
            }

            if(text != activities.get(activities.size() - 1)) {
                Log.d(TAG, "onReceiveTEXT: "+ activities.get(activities.size() - 1));
                Log.d(TAG, "onReceive: "+ text);
                startTime.add(System.currentTimeMillis());
                if(startTime.size() > 2) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date(startTime.get(startTime.size() - 1)  + startTime.get(startTime.size() - 2));
                    timeTaken = sdf.format(date);
                    Log.d(TAG, "BLA: " + startTime.get(startTime.size() - 1)  + startTime.get(startTime.size() - 2));
                    Toast.makeText(mContext, "You have been" + text + "for "  + timeTaken, Toast.LENGTH_LONG).show();
                }
            }
            displayImageAndAudio(text);
            Log.d(TAG, "STARTIME: " + startTime);
            activityText.setText("You are " + text);
        }
    }


}

