package com.oguzhandongul.floatinglogcat.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.oguzhandongul.floatinglogcat.ApplicationClass;
import com.oguzhandongul.floatinglogcat.R;
import com.oguzhandongul.floatinglogcat.events.PermissionEvent;
import com.oguzhandongul.floatinglogcat.events.StopServiceEvent;
import com.squareup.otto.Subscribe;

import static com.oguzhandongul.floatinglogcat.Utils.isMyServiceRunning;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button button2;
    private Switch startSwitch;
    private Intent mServiceIntent;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //register event bus
        ApplicationClass.getEventBus().register(this);

        //Initialize service intent
        mServiceIntent = new Intent(MainActivity.this, LogcatService.class);

        setupSwitch();

    }


    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister event bus
        ApplicationClass.getEventBus().unregister(this);
    }

    @Subscribe
    public void warnForPermission(PermissionEvent event) {
        button.performClick();
    }

    @Subscribe
    public void serviceStopped(StopServiceEvent event) {
        updateServiceStatus();
    }

    void setupSwitch() {
        button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TEST_LOGS", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
            }
        });


        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        // Show alert dialog to the user saying a separate permission is needed
                        // Launch the settings activity if the user prefers
                        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(myIntent);
                    }
                }
            }
        });


        startSwitch = findViewById(R.id.switch1);
        startSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isMyServiceRunning(MainActivity.this, LogcatService.class)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            MainActivity.this.startForegroundService(mServiceIntent);
                        } else {
                            MainActivity.this.startService(mServiceIntent);
                        }
                    }
                } else {
                    if (mServiceIntent != null && isMyServiceRunning(MainActivity.this, LogcatService.class)) {
                        stopService(mServiceIntent);
                    }
                }
            }
        });
    }


    private void updateServiceStatus() {
        if (isMyServiceRunning(MainActivity.this, LogcatService.class) && !startSwitch.isChecked()) {
            startSwitch.toggle();
        } else if (!isMyServiceRunning(MainActivity.this, LogcatService.class) && startSwitch.isChecked()) {
            startSwitch.toggle();
        }

    }


    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // You don't have permission
                checkPermission();
            } else {
                updateServiceStatus();
            }

        }

    }

}
