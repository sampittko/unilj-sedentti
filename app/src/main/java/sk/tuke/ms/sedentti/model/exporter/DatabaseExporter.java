package sk.tuke.ms.sedentti.model.exporter;

import android.content.Context;
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
import java.sql.SQLException;
import java.util.List;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class DatabaseExporter {
    private static final String TAG = "DatabaseExporter";

    private SessionHelper sessionHelper;

    public DatabaseExporter(Context context, Profile profile) {
        this.sessionHelper = new SessionHelper(context, profile);
    }

    public String getDatabaseAsFile(@NotNull SQLiteDatabase database) throws IOException, SQLException {
        String outFileName;
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDatabaseAsFile");
        try {
            Crashlytics.log(Log.DEBUG, TAG, "Getting sessions to export");
            List<Session> sessions = sessionHelper.getNotExportedFinishedSessions();
            outFileName = exportDatabase(database, sessions);
            Crashlytics.log(Log.DEBUG, TAG, "Updating exported sessions");
            assert sessions != null;
            sessionHelper.setExported(sessions);
        } catch (IOException e) {
            Crashlytics.log(Log.ERROR, TAG, "Failed to create database as a file");
            throw new IOException();
        }
        return outFileName;
    }

    // TODO incorporate only sessions specified in the method parameter
    // TODO set valid database file export location
    private String exportDatabase(@NotNull SQLiteDatabase database, List<Session> sessions) throws IOException {
        Crashlytics.log(Log.DEBUG, TAG, "Exporting database");

        File dbFile = new File(database.getPath());
        FileInputStream fis = new FileInputStream(dbFile);
        String outFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator +
                Configuration.DATABASE_NAME + ".db";
        OutputStream output = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;

        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        fis.close();

        Crashlytics.log(Log.DEBUG, TAG, "Database as file created");

        return outFileName;
    }
}
