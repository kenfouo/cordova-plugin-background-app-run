package backgroundapprun;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;

public class DeleteFileWorker extends Worker {

  public DeleteFileWorker(@NonNull Context context, @NonNull WorkerParameters params) {
    super(context, params);
  }

  @NonNull
  @Override
  public Result doWork() {
    String filePath = getInputData().getString("decrypted_file_path");

    if (filePath == null || filePath.isEmpty()) {
      Log.e("DeleteFileWorker", "File path is null or empty.");
      return Result.failure();
    }

    File file = new File(filePath);
    if (!file.exists()) {
      Log.w("DeleteFileWorker", "File not found: " + filePath);
      return Result.success(); // Nothing to do
    }

    try {
      FileOutputStream fos = new FileOutputStream(file);
      byte[] junk = "RANDOM_INVALID_DATA".getBytes();
      fos.write(junk);
      fos.close();

      Log.i("DeleteFileWorker", "File successfully overwritten.");
      return Result.success();
    } catch (Exception e) {
      Log.e("DeleteFileWorker", "Error overwriting file", e);
      return Result.failure();
    }
  }
}
