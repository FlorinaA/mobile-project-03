package com.florina.activityrecognition;

import com.google.android.gms.maps.model.LatLng;
import java.util.HashMap;

final class Constants {


    private Constants() {
    }

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    static final float GEOFENCE_RADIUS_IN_METERS = 100; // 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
    static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<>();

    static {
        // San Francisco International Airport.
//        BAY_AREA_LANDMARKS.put("SFO", new LatLng(37.621313, -122.378955));

        // Googleplex.
//        BAY_AREA_LANDMARKS.put("GOOGLE", new LatLng(37.422611,-122.0840577));

        // 37 William Street
//        BAY_AREA_LANDMARKS.put("37-William", new LatLng(42.267241, -71.807722));

        // Fuller labs
//        BAY_AREA_LANDMARKS.put("Fuller", new LatLng(42.274997, -71.806383));

        // Gordon Library
//        BAY_AREA_LANDMARKS.put("Library", new LatLng(42.274215, -71.806393));

        // Gordon Library
//        BAY_AREA_LANDMARKS.put("Gordon Library", new LatLng(42.274209, -71.806752));
        BAY_AREA_LANDMARKS.put("Gordan Library", new LatLng(42.274172, -71.806513));

        // Fuller Labs
        BAY_AREA_LANDMARKS.put("Fuller Labs", new LatLng(42.274851, -71.806662));   // ACTUAL LOCATION OF FULLER
//        BAY_AREA_LANDMARKS.put("Fuller Labs", new LatLng(42.268627, -71.806034));   // FOR TESTING, 32 JOHN ST
    }
}
