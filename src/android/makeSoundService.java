package backgroundapprun;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.berger.app.cbservice.R;

import org.apache.cordova.CordovaInterface;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import backgroundapprun.backgroundAppRun;


public class makeSoundService extends Service {

  private static final int NOTIF_ID = 1;
  private static final String NOTIF_CHANNEL_ID = "Channel_Id";

  private backgroundAppRun pluginInstance;

  private CordovaInterface cordova;

  private PowerManager.WakeLock wakeLock;

  private Handler handler;

  private static final long SCREEN_ACTIVATION_INTERVAL = (long) ( 3000 ) ; // 30 seconds

  private AlarmManager alarmManager;
  private PendingIntent alarmIntent;

  private MediaPlayer mediaPlayer;
  private boolean isPlaying = false;

  private static final long MEDIA_PLAYER_INTERVAL = 30; // 30 seconds

  private ScheduledExecutorService executorService;

  private boolean isTaskScheduled = false;

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialize the Cordova plugin instance
    pluginInstance = backgroundAppRun.getInstance();

    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "PiPWakeLockTag");
    if (wakeLock != null) {
      wakeLock.acquire();
    } else {
      Log.e("Background App Run", "Wake lock is null");
    }

    handler = new Handler();

    // Your service initialization code here
  }


  private void activateScreen() {
    wakeLock.acquire();
    Log.d("Background APp Run ", "Screen activated by Wake Lock");
  }

  private Runnable screenActivationRunnable = new Runnable() {
    @Override
    public void run() {
      activateScreen();
      handler.postDelayed(this, SCREEN_ACTIVATION_INTERVAL);
    }
  };

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    // do your jobs here
    Log.d("YourPluginName", "Background service started.");
    System.out.println("Background Service Running : ");

    // Foreground-service boundary: create a valid notification and call startForeground immediately.
    ensureNotificationChannel();
    Notification notification = makeSoundNotification();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API level 29
      startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
    } else {
      startForeground(NOTIF_ID, notification);
    }

    // Keep existing repeating-sound behavior.
    ensureSoundSchedulerStarted();


    return START_STICKY;
  }

  private void ensureNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationManager notificationManager = getSystemService(NotificationManager.class);
      if (notificationManager == null) return;
      NotificationChannel channel = new NotificationChannel(
        NOTIF_CHANNEL_ID,
        "Your Channel Name",
        NotificationManager.IMPORTANCE_LOW
      );
      notificationManager.createNotificationChannel(channel);
    }
  }

  private void ensureSoundSchedulerStarted() {
    if (mediaPlayer == null) {
      mediaPlayer = MediaPlayer.create(this, R.raw.choix_de_vie);
    }
    if (executorService == null) {
      executorService = Executors.newScheduledThreadPool(1);
    }

    // Start the repeating playback of MediaPlayer if not already scheduled
    if (!isTaskScheduled) {
      executorService.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
          mediaPlayer.start();
          isPlaying = true;
        }
      }, 0, MEDIA_PLAYER_INTERVAL, TimeUnit.SECONDS);

      isTaskScheduled = true; // Set the flag to true once scheduled
    } else {
      System.out.println("is Task Scheduled False");
    }
  }

  private Notification makeSoundNotification() {
    // Implement code to create a foreground notification here.
    // Set a title, content, and icon for the notification.

    // Example:
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
      .setContentTitle("Your Background Service")
      .setContentText("Running in the background Sound #BPIL_FUSION")
      .setSmallIcon(R.mipmap.ic_launcher)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT);;

    return builder.build();
  }


  @Override
  public void onDestroy() {
    super.onDestroy();

    // Stop the repeating playback when the service is destroyed
    executorService.shutdown();

    isTaskScheduled = false; // Reset the flag when service is destroyed

  }
}
