package sk.tuke.ms.sedentti.model.exporter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Locale;

import sk.tuke.ms.sedentti.config.Configuration;
import sk.tuke.ms.sedentti.config.PredefinedValues;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.helper.ActivityHelper;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class DatabaseExporter {
    private static final String TAG = "DatabaseExporter";

    private SessionHelper sessionHelper;
    private ActivityHelper activityHelper;

    private Profile profile;

    public DatabaseExporter(Context context, Profile profile) {
        this.sessionHelper = new SessionHelper(context, profile);
        this.activityHelper = new ActivityHelper(context);
        this.profile = profile;
    }

    /**
     * @return
     * @throws SQLException
     */
    public String generateFile() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing generateFile");
        ArrayList<Session> sessions = sessionHelper.getNotExportedFinished();
        String dbFilePath = generateFile(sessions);
        sessionHelper.setExported(sessions);
        Crashlytics.log(Log.DEBUG, TAG, "File generated");
        return dbFilePath;
    }

    // TODO set valid database file export location
    private String generateFile(@NotNull ArrayList<Session> sessions) {
        Crashlytics.log(Log.DEBUG, TAG, "generateFile");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions: " + sessions.size());

        File exportDir = getExportDir();
        File file;
        PrintWriter printWriter = null;
        String outFilePath = exportDir + File.separator + Configuration.CSV_EXPORT_FILENAME;

        try {
            file = new File(outFilePath);
            file.createNewFile();
            printWriter = new PrintWriter(new FileWriter(file));
            printCSVHeader(printWriter);
            printCSVData(printWriter, sessions);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }

        return outFilePath;
    }

    private void printCSVHeader(@NotNull PrintWriter printWriter) {
        Crashlytics.log(Log.DEBUG, TAG, "printCSVHeader");

        String CSVHeader =
                Configuration.CSV_HEADER_COLUMN_1 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_2 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_3 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_4 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_5 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_6 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_7 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_8 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_9 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_10 + PredefinedValues.CSV_DATA_SEPARATOR +
                Configuration.CSV_HEADER_COLUMN_11;

        printWriter.println(CSVHeader);
        Crashlytics.log(Log.DEBUG, TAG, "CSV header: " + CSVHeader);
    }

    private void printCSVData(@NotNull PrintWriter printWriter, @NotNull ArrayList<Session> sessions) {
        Crashlytics.log(Log.DEBUG, TAG, "printCSVData");
        Crashlytics.log(Log.DEBUG, TAG, "@printWriter");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions: " + sessions.size());

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        ArrayList<Activity> activities;
        String record;

        for (Session session : sessions) {
            try {
                activities = activityHelper.getCorresponding(session);
                for (Activity activity : activities) {
                    record =
                        profile.getEmail() + PredefinedValues.CSV_DATA_SEPARATOR +
                        session.getId() + PredefinedValues.CSV_DATA_SEPARATOR +
                        session.isSuccessful() + PredefinedValues.CSV_DATA_SEPARATOR +
                        session.getStartTimestamp() + PredefinedValues.CSV_DATA_SEPARATOR +
                        session.getEndTimestamp() + PredefinedValues.CSV_DATA_SEPARATOR +
                        session.getDuration() + PredefinedValues.CSV_DATA_SEPARATOR +
                        session.isSuccessful() + PredefinedValues.CSV_DATA_SEPARATOR +
                        df.format(session.getDate()) + PredefinedValues.CSV_DATA_SEPARATOR +
                        activity.getId() + PredefinedValues.CSV_DATA_SEPARATOR +
                        activity.getType() + PredefinedValues.CSV_DATA_SEPARATOR +
                        activity.getTimestamp();
                    printWriter.println(record);
                    Crashlytics.log(Log.DEBUG, TAG, "Printed record: " + record);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private File getExportDir() {
        Crashlytics.log(Log.DEBUG, TAG, "getExportDir");

        File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        Crashlytics.log(Log.DEBUG, TAG, "Export dir: " + exportDir);

        return exportDir;
    }
}
