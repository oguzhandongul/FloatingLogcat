package com.oguzhandongul.floatinglogcat.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oguzhandongul.floatinglogcat.ApplicationClass;
import com.oguzhandongul.floatinglogcat.R;
import com.oguzhandongul.floatinglogcat.events.PermissionEvent;
import com.oguzhandongul.floatinglogcat.events.StopServiceEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.oguzhandongul.floatinglogcat.Utils.convertDpiToPixel;

public class LogcatService extends Service {
    private WindowManager windowManager;
    private ImageView menuButton;
    private TextView tvContent;
    FrameLayout mTopView;
    ScrollView scrollView;

    boolean isAutoScroll = true;

    public static boolean isRunning = false;
    String line = "";

    Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_app";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "MyApp", NotificationManager.IMPORTANCE_DEFAULT);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Logcat is active")
                    .setSmallIcon(R.drawable.ic_adb_black_24dp)
                    .setContentText("").build();
            startForeground(1, notification);

        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                900,
                1200,
                LAYOUT_FLAG,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;


        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mTopView = (FrameLayout) li.inflate(R.layout.service_content, null);

        menuButton = mTopView.findViewById(R.id.handler);
        tvContent = mTopView.findViewById(R.id.tvContent);
        scrollView = mTopView.findViewById(R.id.scrollView);

        try {
            windowManager.addView(mTopView, params);
        } catch (Exception ex) {
            ex.printStackTrace();
            LogcatService.this.stopSelf();
            ApplicationClass.getEventBus().post(new PermissionEvent());
            return;
        }

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoOptions();
            }
        });

        try {
            mTopView.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(mTopView, paramsF);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        handler.postDelayed(runnable, 1000);


    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            writeLog(tvContent);
            handler.postDelayed(runnable, 1000);
        }
    };

    private void writeLog(TextView tvContent) {
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                    log.append("\n\n" + line);
            }
            tvContent.setText(log.toString());
            if (isAutoScroll) {
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTopView != null) {
            windowManager.removeView(mTopView);
        }

        //remove handler callbacks
        if (handler != null) {
            try {
                handler.removeCallbacks(runnable);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showPhotoOptions() {
        PopupMenu popup = new PopupMenu(this, menuButton);
        popup.getMenuInflater().inflate(R.menu.menu_popup,
                popup.getMenu());
        popup.show();
        popup.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {
                case R.id.resize:
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mTopView.getLayoutParams();
                    params.height = params.height == convertDpiToPixel(getApplicationContext(), 40) ? convertDpiToPixel(getApplicationContext(), 400) : convertDpiToPixel(getApplicationContext(), 40);
                    params.width = params.width == convertDpiToPixel(getApplicationContext(), 92) ? convertDpiToPixel(getApplicationContext(), 300) : convertDpiToPixel(getApplicationContext(), 92);
                    mTopView.setLayoutParams(params);
                    windowManager.updateViewLayout(mTopView, params);
                    break;
                case R.id.autoscroll:
                    isAutoScroll = !isAutoScroll;
                    break;
                case R.id.clear:
                    try {
                        Runtime.getRuntime().exec("logcat -c");
                    }catch (Exception ex){
                         ex.printStackTrace();
                    }
                    tvContent.setText("");
                    break;
                case R.id.close:
                    LogcatService.this.stopSelf();
                    ApplicationClass.getEventBus().post(new StopServiceEvent());
                    break;

                default:
                    break;
            }

            return true;
        });
    }


}