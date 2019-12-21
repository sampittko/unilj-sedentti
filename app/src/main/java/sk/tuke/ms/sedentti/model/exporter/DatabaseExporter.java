package sk.tuke.ms.sedentti.model.exporter;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
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
    public String generateFile(boolean regenerate) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing generateFile");
        Crashlytics.log(Log.DEBUG, TAG, "@regenerate: " + regenerate);

        String dbFilePath;
        ArrayList<Session> sessions;

        if (!regenerate) {
            sessions = sessionHelper.getNotExportedFinishedForUploadWorker();
        }
        else {
            sessions = sessionHelper.getExportedNotUploaded();
        }

        dbFilePath = generateFile(sessions);

        if (regenerate) {
            sessionHelper.setExported(sessions);
        }

        Crashlytics.log(Log.DEBUG, TAG, "File generated");
        return dbFilePath;
    }

    private String generateFile(@NotNull ArrayList<Session> sessions) {
        Crashlytics.log(Log.DEBUG, TAG, "generateFile");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions: " + sessions.size());

        File file;
        PrintWriter printWriter = null;
        String outFilePath = getOutFilePath();

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
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_1 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_2 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_3 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_4 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_5 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_6 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_7 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_8 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_9 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_10 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_11 + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                Configuration.DB_EXPORTER_CSV_HEADER_COLUMN_12;

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
                        profile.getFirebaseAuthUid() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        getGloballyUniqueIdentifierOf(session.getId()) + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        session.isSedentary() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        session.isInVehicle() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        session.getStartTimestamp() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        session.getEndTimestamp() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        session.getDuration() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        session.isSuccessful() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        df.format(session.getDate()) + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        getGloballyUniqueIdentifierOf(activity.getId()) + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        activity.getType() + PredefinedValues.DB_EXPORTER_CSV_DATA_SEPARATOR +
                        activity.getTimestamp();
                    printWriter.println(record);
                    Crashlytics.log(Log.DEBUG, TAG, "Printed record: " + record);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    private String getGloballyUniqueIdentifierOf(long id) {
        return id + PredefinedValues.DB_EXPORTER_CSV_INNER_DATA_SEPARATOR + profile.getFirebaseAuthUid();
    }

    @NotNull
    @Contract(" -> !null")
    public static String getExistingFilePath() throws FileNotFoundException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getExistingFilePath");

        String existingFilePath = getOutFilePath();
        if (isValid(existingFilePath)) {
            return existingFilePath;
        }
        throw new FileNotFoundException();
    }

    private static boolean isValid(String existingFilePath) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing isValid");
        Crashlytics.log(Log.DEBUG, TAG, "@existingFilePath: " + existingFilePath);

        File file = new File(existingFilePath);
        return file.exists() && !file.isDirectory();
    }

    @NotNull
    @Contract(" -> !null")
    private static String getOutFilePath() {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getOutFilePath");

        return getExportDir() + File.separator + Configuration.DB_EXPORTER_FILENAME;
    }

    private static File getExportDir() {
        Crashlytics.log(Log.DEBUG, TAG, "getExportDir");

        File exportDir = Configuration.DB_EXPORTER_EXPORT_DIR;
        if (!exportDir.exists()) {
            Crashlytics.log(Log.DEBUG, TAG, "Creating new directory");
            exportDir.mkdirs();
        }

        Crashlytics.log(Log.DEBUG, TAG, "Export dir is " + exportDir);

        return exportDir;
    }
}
