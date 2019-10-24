package com.florina.activityrecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Receiver for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        // Enqueues a JobIntentService passing the context and intent as parameters
//        GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
        Log.d("TAG", "RECIEVED!");
        handle(context, intent);

    }

    private void handle(Context context, Intent intent){

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(context,
                    geofencingEvent.getErrorCode());
            Log.e("TAG", errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d("TAG", Integer.toString(geofenceTransition));

//        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
//                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);

            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails);

            String Region = geofenceTransitionDetails.substring(geofenceTransitionDetails.indexOf(":") + 2);

            Log.i("TAG", "Region: " + Region);

            // Send notification and log the transition details.
            if(Region.equals("Fuller Labs")) {
                Toast.makeText(context, "You have been inside the Fuller Labs Geofence for 15 seconds, incrementing counter\"", Toast.LENGTH_SHORT).show();
                (MainActivity.counterF)++;
            }
            else if (Region.equals("Gordan Library")){
                Toast.makeText(context, "You have been inside the Gordon Library Geofence for 15 seconds, incrementing counter\"", Toast.LENGTH_SHORT).show();
                (MainActivity.counterG)++;
            }

//            Toast.makeText(context,"Where's my reward, huh???",Toast.LENGTH_SHORT).show();
            Log.i("TAG", "Geofence: " + geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e("TAG", "Invalid transition");
        }

        MainActivity.displayCount();

    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exited";
            default:
                return "Unknown";
        }
    }


}