package com.chinamobile.gdwy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Created by liangzhongtai on 2017/6/29.
 */

public class DataService extends Service{
    private final static String CHANNEL_ID_DATA      = "566789";
    private final static int NOTIFYCATION_ID_DATA    = 38621;
    private NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // startForeground(1,new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(NOTIFYCATION_ID_DATA, new Notification());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getApplicationContext(), GuardService.class));
            NotificationManager notificationManager = (NotificationManager) getApplication().getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID_DATA, "后台保活",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_DATA).build();
            startForeground(NOTIFYCATION_ID_DATA, notification);
        } else {
            Intent guardIntent = new Intent(this, GuardService.class);
            startService(guardIntent);
            startForeground(NOTIFYCATION_ID_DATA, new Notification());
        }
        if (notificationManager==null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
