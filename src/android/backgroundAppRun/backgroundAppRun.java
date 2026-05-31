package backgroundapprun;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.util.Rational;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.VideoView;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;


public class backgroundAppRun extends CordovaPlugin {

  // Static reference to the plugin instance
  private static backgroundAppRun instance;

  public static backgroundAppRun getInstance() {
    return instance;
  }

  private static final String TAG = "PictureInPicturePlugin";
  private static final int REQUEST_ENTER_PIP = 1;
  private static final int REQUEST_SYSTEM_ALERT_WINDOW = 123;

  private VideoView videoView;
  private ViewGroup parent;
  private int videoPosition;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
//    if (action.equals("coolMethod")) {
//      this.coolMethod("Plugin Works", callbackContext);
//      return true;
//    }
    String fileName = "";
    if ("startBackgroundService".equals(action)) {
      // Set the plugin instance on the instance of PluginInstanceHolder
//      PluginInstanceHolder.setInstance(this);

      // Start the background service using an Android Intent.
      Intent serviceIntent = new Intent(cordova.getActivity().getApplicationContext(), runService.class);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.cordova.getActivity().startForegroundService(serviceIntent);
      }
      else {
        this.cordova.getActivity().startService(serviceIntent);
      }
      callbackContext.success("Background service started.");

      enterPictureInPicture(callbackContext);

      return true;
    } else if ("startMakeSoundBackgroundService".equals(action)) {
      // Stop the background service using an Android Intent.
      Intent serviceIntent = new Intent(cordova.getActivity().getApplicationContext(), makeSoundService.class);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.cordova.getActivity().startForegroundService(serviceIntent);
      }
      else {
        this.cordova.getActivity().startService(serviceIntent);
      }

      callbackContext.success("Background service sound started.");
      return true;
    } else if ("stopMakeSoundBackgroundService".equals(action)) {
      // Stop the background service using an Android Intent.
      Intent serviceIntent = new Intent(cordova.getActivity().getApplicationContext(), makeSoundService.class);
      this.cordova.getActivity().stopService(serviceIntent);

      callbackContext.success("Background service sound stopped.");
      return true;
    } else if ("backgroundDeleteService".equals(action)) {
      // Stop the background service using an Android Intent.
      System.out.println("JSON args : " + args);
      if (args.length() > 0) {
        String filePath = args.getString(0);
        System.out.println("Received filePath: " + filePath);

        if (filePath == null || filePath.isEmpty()) {
          callbackContext.error("Error: File path is null or empty.");
          return false;
        }

        // ✅ Call the new method using WorkManager

        try {
          CordovaResourceApi resourceApi = webView.getResourceApi();
          Uri fileUri = resourceApi.remapUri(Uri.parse(filePath));
          fileName = fileUri.getPath();
        } catch (Exception e) {
          fileName = filePath;
        }

        scheduleFileDelete(fileName);

        callbackContext.success("Scheduled delete task for file: " + filePath);
      } else {
        callbackContext.error("Error: Missing file path argument.");
      }

      return true;
    } else if (action.equals("fireEvent")) {
      try {
        String eventData = args.getString(0);

        // Create an intent to broadcast the event
        Intent intent = new Intent("your_custom_event");
        intent.putExtra("data", eventData);

        // Send the broadcast
//        cordova.getActivity().sendBroadcast(intent);

        callbackContext.success("Event fired successfully");
        System.out.println("EVENT FIRED SUCCESSFULLY AFTER CALLING PLUIGIN");
        return true;
      } catch (JSONException e) {
        callbackContext.error("JSON Exception: " + e.getMessage());
        return false;
      }
    }

    return false;
  }

  private void scheduleFileDelete(String filePath) {
    Context context = cordova.getActivity().getApplicationContext();

    Data inputData = new Data.Builder()
      .putString("decrypted_file_path", filePath)
      .build();

    OneTimeWorkRequest deleteRequest = new OneTimeWorkRequest.Builder(DeleteFileWorker.class)
      .setInitialDelay(60, TimeUnit.SECONDS) // 20 seconds delay
      .setInputData(inputData)
      .build();

    WorkManager.getInstance(context).enqueue(deleteRequest);

    Log.i("backgroundAppRun", "Scheduled file delete task for: " + filePath);
  }

  private void enterPictureInPicture(final CallbackContext callbackContext) {
    System.out.println("enterPictureInPicture Called: ");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && cordova.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
      System.out.println("FEATURE_PICTURE_IN_PICTURE Supported: ");
//      if (cordova.hasPermission("android.permission.SYSTEM_ALERT_WINDOW")) {
        // Request PiP mode
        System.out.println("SYSTEM_ALERT_WINDOW IF REQUEST Called: ");
        PictureInPictureParams params = new PictureInPictureParams.Builder()
          .setAspectRatio(new Rational(16, 9))
          .build();

        cordova.getActivity().enterPictureInPictureMode(params);

        PluginResult result = new PluginResult(PluginResult.Status.OK, "PiP mode entered successfully");
        callbackContext.sendPluginResult(result);

        // Request PiP mode
//        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//          Uri.parse("package:" + cordova.getActivity().getPackageName()));
//        cordova.startActivityForResult(this, intent, REQUEST_SYSTEM_ALERT_WINDOW);

//      } else {
//        // Request the SYSTEM_ALERT_WINDOW permission
//        System.out.println("SYSTEM_ALERT_WINDOW ELSE REQUEST Called: ");
//        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//          Uri.parse("package:" + cordova.getActivity().getPackageName()));
//        cordova.startActivityForResult(this, intent, REQUEST_SYSTEM_ALERT_WINDOW);
//      }
    } else {
      callbackContext.error("Picture-in-picture mode not supported on this device or Android version.");
    }
  }

  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults, CallbackContext callbackContext) throws JSONException {
    for (int grantResult : grantResults) {
      if (grantResult == android.content.pm.PackageManager.PERMISSION_DENIED) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, "Permission denied");
        callbackContext.sendPluginResult(result);
        return;
      }
    }

    if (requestCode == 0) {
      // SYSTEM_ALERT_WINDOW permission granted, enter PiP mode
      enterPictureInPicture(callbackContext);
    }
  }
//  private void coolMethod(String message, CallbackContext callbackContext) {
//    if (message != null && message.length() > 0) {
//      callbackContext.success(message);
//    } else {
//      callbackContext.error("Expected one non-empty string argument.");
//    }
//  }

//  @Override
//  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
//    super.initialize(cordova, webView);
//    // Initialize your plugin here
//    instance = this;
//    // Set the plugin instance in the PluginInstanceHolder
//    PluginInstanceHolder.setInstance(this);
//
//    Log.d("backgroundAppRun", "Cordova plugin initialized.");
//  }

//  public void triggerCustomEvent(String eventData) {
//    // Trigger a JavaScript event
//    String javascriptCode = "var event = new Event('customEvent');"
//      + "event.data = '" + eventData + "';"
//      + "document.dispatchEvent(event);";
//    System.out.println("triggerCustomEvent TESTING TRiggered"+ eventData);
//    System.out.println("triggerCustomEvent TESTING TRiggered javascriptCode"+ javascriptCode);
//
//    cordova.getActivity().runOnUiThread(new Runnable() {
//      public void run() {
//        webView.getEngine().evaluateJavascript(javascriptCode, null);
//      }
//    });
//  }

//  @Override
//  public void onDestroy() {
//    //stopService(mServiceIntent);
//    Intent broadcastIntent = new Intent();
//    broadcastIntent.setAction("restartservice");
//    broadcastIntent.setClass(cordova.getActivity(), Restarter.class);
//    cordova.getActivity().sendBroadcast(broadcastIntent);
//    super.onDestroy();
//  }
}
