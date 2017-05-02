package fesb.papac.marin.augmented_reality_poi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ViewMain Content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showSettingAlert();
        }

        FrameLayout ViewPane2 = (FrameLayout) findViewById(R.id.ar_view_pane_2);
        FrameLayout ViewPane3 = (FrameLayout) findViewById(R.id.ar_view_pane_3);

/**
        WindowManager wm = (WindowManager) this.getSystemService(Activity.WINDOW_SERVICE);
        int screenRotation = wm.getDefaultDisplay().getRotation();
        FrameLayout.LayoutParams frameParams = (FrameLayout.LayoutParams) ViewPane2.getLayoutParams();

        switch (screenRotation) {
            case Surface.ROTATION_0:
                frameParams.setMargins(0,0,0,0);
                break;
            case Surface.ROTATION_90: // rotation to left
                frameParams.setMargins(512,0,512,0);
                break;
            case Surface.ROTATION_270: // rotation to right
                frameParams.setMargins(512,0,512,0);
                break;

        }

**/

        DisplayView displayView = new DisplayView(getApplicationContext(), this);
        ViewPane3.addView(displayView);

        Content = new ViewMain(getApplicationContext());
        ViewPane2.addView(Content);


    }
    @Override
    protected void onPause() {
        Content.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Content.onResume();
        super.onResume();
    }

    public void showSettingAlert(){


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle("GPS Not Enabled");

        alertDialog.setMessage("Do you wants to turn On GPS");


        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert11 = alertDialog.create();
        alert11.show();

    }

}
