package jp.co.abs.calender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class AlarmNotification extends BroadcastReceiver {
    private static final String TAG = AlarmNotification.class.getSimpleName();

    private static final String INTENT_NAME = "RequestCode";
    private static final String INTENT_NAME_SCHEDULE_TEXT = "ScheduleText";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receive Success");
        int requestCode = intent.getIntExtra(INTENT_NAME, 0);
        String scheduleText = intent.getStringExtra(INTENT_NAME_SCHEDULE_TEXT);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "default";
        // app name
        String title = context.getString(R.string.app_name);

        String message = "スケジュール: " + scheduleText;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Notification　Channel 設定
        NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(message);
        channel.enableVibration(true);
        channel.canShowBadge();
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setSound(defaultSoundUri, null);
        channel.setShowBadge(true);

        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(context, channelId)
                    .setContentTitle(title)
                    // android標準アイコンから
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .build();
            // 通知
            notificationManager.notify(R.string.app_name, notification);

        }
    }
}
