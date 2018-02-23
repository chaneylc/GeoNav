package edu.ksu.wheatgenetics.geonav;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import edu.ksu.wheatgenetics.geonav.R;

public class GeoNavServiceTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_field_mapping_service_test);

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 100);


        /*
        //first method
        //create bundle of flattened lat/lng values
        // s.a LatLng1= -103.4, -96.5; LatLng2= -105.4, -96.3 is defined as
        //new double[] {-103.4, -96.5, -105.4, -96.3}
        final Intent fieldMappingIntent = new Intent(this, GeoNavService.class);

        //sending a flattened array of coordinates
        fieldMappingIntent.putExtra("array", new double[] {1.0, 2.0, 3.0, 4.0});

        //updating accuracy in meters
        fieldMappingIntent.putExtra("Accuracy", Float.MAX_VALUE);

        //updating time in ms
        fieldMappingIntent.putExtra("Time", Long.MIN_VALUE);

        //updating distance in meters
        fieldMappingIntent.putExtra("Distance", Float.MIN_VALUE);

        //second method
        //create map plot id, and value is lat/lng coordinates
        //following map contains the 8 cardinal boundary points for East Stadium Manhattan, KS
        //Q1 - Q4 are approximate cartesian quadrant midpoints
        final HashMap<String, double[]> idMap = new HashMap<>();
        idMap.put("N", new double[] {39.187959, -96.584348});
        idMap.put("NW", new double[] {39.187988, -96.584680});
        idMap.put("W", new double[] {39.187500, -96.584705});
        idMap.put("SW", new double[] {39.187006, -96.584697});
        idMap.put("S", new double[] {39.187006, -96.584297});
        idMap.put("SE", new double[] {39.187024, -96.583954});
        idMap.put("E", new double[] {39.187492, -96.583929});
        idMap.put("NE", new double[] {39.187968, -96.583946});
        idMap.put("Q1", new double[] {39.187791, -96.584093});
        idMap.put("Q2", new double[] {39.187706, -96.584554});
        idMap.put("Q3", new double[] {39.187268, -96.584509});
        idMap.put("Q4", new double[] {39.187268, -96.584093});
        fieldMappingIntent.putExtra("map", idMap);

        startService(fieldMappingIntent); */

    }

    @Override
    public void onRequestPermissionsResult(int resultCode, String[] permissions, int[] granted) {

        boolean permissionGranted = true;
        if (resultCode == 100) {
            for (int i = 0; i < granted.length; i++) {
                if (granted[i] != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                }
            }
        }
        if(permissionGranted) {

            final Intent sharedFilePicker = new Intent();
            sharedFilePicker.setType("text/*");
            sharedFilePicker.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(sharedFilePicker, "Select file."), 0);

            final IntentFilter filter = new IntentFilter();
            filter.addAction(GeoNavConstants.BROADCAST_LOCATION);
            filter.addAction(GeoNavConstants.BROADCAST_ACCURACY);
            filter.addAction(GeoNavConstants.BROADCAST_PLOT_ID);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    new ResponseReceiver(),
                    filter
            );
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {

        if (resultCode == RESULT_OK) {
            final Uri returnUri = returnIntent.getData();
            try {
                final ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(returnUri, "r");
                final FileDescriptor fd = pfd.getFileDescriptor();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(GeoNavConstants.LOCATION)) {
                final String plot_id = ((Location) intent.getExtras()
                        .get(GeoNavConstants.LOCATION))
                        .toString();
                if (plot_id != null)
                    ((TextView) findViewById(R.id.locationText)).setText(plot_id);
            }

            if (intent.hasExtra(GeoNavConstants.PLOT_ID)) {
                ((TextView) findViewById(R.id.idText)).setText(
                        (String) intent.getExtras()
                                .get(GeoNavConstants.PLOT_ID)
                );
            }

            if (intent.hasExtra(GeoNavConstants.ACCURACY)) {
                ((TextView) findViewById(R.id.accuracyText)).setText(
                        String.valueOf(intent.getExtras()
                            .get(GeoNavConstants.ACCURACY))
                );
            }
        }
    }
}
