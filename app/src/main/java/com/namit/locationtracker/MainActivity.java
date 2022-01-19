package com.namit.locationtracker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button b1;
    TextView t3;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    boolean isGPS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = findViewById(R.id.b1);
        t3 = findViewById(R.id.t3);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        locationRequest = LocationRequest.create()
                .setInterval(500).setFastestInterval(1000 * 2).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).setWaitForAccurateLocation(true);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // save the location
                updateLocation(locationResult.getLastLocation());
            }
        };
    }

    public void turnOnGPS() {
        new GpsUtils(this).turnGPSOn(isGPSEnable -> {
            // turn on GPS
            isGPS = isGPSEnable;
        });
        t3.setText("Press the Track Button");
    }

    public void trackLocation(View view) {

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (isGPS) {
                t3.setText("Please wait.....");
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);

                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location == null) {
                        Log.d("Location Error: can ignore also", "Unable to fetch location");
                    } else {
                        updateLocation(location);
                    }

                });
            } else {
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
                turnOnGPS();
            }
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 99);
        }
    }

    public void updateLocation(Location location) {
        if (location != null) {
            Geocoder geocoder = new Geocoder(MainActivity.this);
            try {
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                t3.setText(addressList.get(0).getAddressLine(0));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to track location", Toast.LENGTH_LONG).show();
                Log.e("Location Error: ", "Unable to track location");
            }
        }
    }

//    public boolean isGPSEnabled() {
//
//        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            buildAlertMessageNoGPS();
//            return false;
//        }
//        return true;
//    }

//    public void buildAlertMessageNoGPS() {
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("This app requires GPS to work properly, do you want to enable it?").setCancelable(false).setPositiveButton("Yes", (dialog, which) -> {
//            Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivity(enableGPSIntent);
//        });
//        final AlertDialog alert = builder.create();
//        alert.show();
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 99) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                b1.performClick();
                turnOnGPS();
            } else {
                Toast.makeText(this, "This app requires location permission to be granted to work properly", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                isGPS = true; // flag maintain before get location
            }
        }
    }
}