package sk.tuke.ms.sedentti.model.exporter;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sk.tuke.ms.sedentti.config.Configuration;

public class DatabaseExporter {
    private static final String TAG = "DatabaseExporter";

    public static String getDatabaseAsFile(@NotNull SQLiteDatabase database) {
        String outFileName;
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDatabaseAsFile");

        try {
            File dbFile = new File(database.getPath());
            FileInputStream fis = new FileInputStream(dbFile);

            outFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                    Configuration.DATABASE_NAME + ".db";

            OutputStream output = new FileOutputStream(outFileName);

            Crashlytics.log(Log.DEBUG, TAG, "Copying database contents");

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            fis.close();
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Failed to create database as a file");
            return null;
        }
        Crashlytics.log(Log.DEBUG, TAG, "Database as file created");

        return outFileName;
    }
}
