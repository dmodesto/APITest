package com.test.apitest.apitest;

/**
 * Created by danmo_000 on 3/7/2015.
 */
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class PhoneLocation {

    private static final int FREQUENCY = 1000 * 60 * 3; // 3 minutes
    Context mContext;
    Location location;

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            setLocation();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    public PhoneLocation(Context mContext){
        this.mContext = mContext;
        this.location = null;
    }

    private void setLocation(){
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager)mContext.getSystemService(context);

        String provider = LocationManager.GPS_PROVIDER;
        Location newLocation = locationManager.getLastKnownLocation(provider);

        if(isBetterLocation(newLocation, this.location)) {
            this.location = newLocation;
        }
    }

    public Location getLocation(){
        LocationManager locationManager;
        String context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager)mContext.getSystemService(context);

        String provider = LocationManager.GPS_PROVIDER;
        Location newLocation = locationManager.getLastKnownLocation(provider);

        if(isBetterLocation(newLocation, this.location)) {
            this.location = newLocation;
        }

        return newLocation;
    }

    public String updateWithNewLocation(Location location) {
        String latLongString;

        if (location != null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latLongString = "Lat:" + lat + "\nLong:" + lng;
        }else{
            latLongString = "No Location";
        }

        return latLongString;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > FREQUENCY;
        boolean isSignificantlyOlder = timeDelta < -FREQUENCY;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
