package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Activity;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.SessionType;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

import static com.google.android.gms.location.DetectedActivity.IN_VEHICLE;
import static com.google.android.gms.location.DetectedActivity.ON_BICYCLE;
import static com.google.android.gms.location.DetectedActivity.ON_FOOT;
import static com.google.android.gms.location.DetectedActivity.RUNNING;
import static com.google.android.gms.location.DetectedActivity.STILL;
import static com.google.android.gms.location.DetectedActivity.TILTING;
import static com.google.android.gms.location.DetectedActivity.UNKNOWN;
import static com.google.android.gms.location.DetectedActivity.WALKING;
import static sk.tuke.ms.sedentti.config.PredefinedValues.DETECTED_ACTIVITY_SIG_MOV;

public class SessionHelper {
    private final Long HOME_TIMELINE_SESSIONS_LIMIT = 3L;
    private final int HIGHEST_SUCCESS_RATE = 100;
    private final int LOWEST_SUCCESS_RATE = 0;

    private final static String TAG = "SessionHelper";

    public enum SessionsInterval {
        LAST_MONTH,
        LAST_WEEK,
    }

    private Dao<Session, Long> sessionDao;
    private ActivityHelper activityHelper;
    private QueryBuilder<Session, Long> sessionDaoQueryBuilder;

    private AppSPHelper appSPHelper;

    private Profile profile;

    public SessionHelper(Context context, Profile profile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
            this.sessionDao = databaseHelper.sessionDao();
            this.sessionDaoQueryBuilder = sessionDao.queryBuilder();
            this.activityHelper = new ActivityHelper(context);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.appSPHelper = new AppSPHelper(context);
        this.profile = profile;
    }

    /**
     * @param interval Specifies the interval in which the requested sessions are
     * @return List of sessions in specified interval
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getFromInterval(SessionsInterval interval) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getFromInterval");
        Crashlytics.log(Log.DEBUG, TAG, "@interval: " + interval);

        Date normalizedEndDate = DateHelper.getNormalized(
                new Date()
        );
        Date normalizedStartDate = DateHelper.getNormalized(
                getStartDate(interval)
        );

        Crashlytics.log(Log.DEBUG, TAG, "Start date: " + normalizedStartDate + "\n");
        Crashlytics.log(Log.DEBUG, TAG, "End date: " + normalizedEndDate);

        sessionDaoQueryBuilder.reset();

        return new ArrayList<>(
                sessionDaoQueryBuilder
                        .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                        .where()
                        .between(Session.COLUMN_DATE, normalizedStartDate, normalizedEndDate)
                        .and()
                        .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                        .query()
        );
    }

    /**
     * @return List of the last 3 sessions (potentioal pending session included)
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getHomeTimelineSessions() throws SQLException {
        return getLatest(HOME_TIMELINE_SESSIONS_LIMIT, true);
    }

    /**
     * @return List of all the sessions from the latest to the oldest (potential pending session included)
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getLatest() throws SQLException {
        return getLatest(0, true);
    }

    public ArrayList<Session> getLatest(boolean countSessionsInVehicle) throws SQLException {
        return getLatest(0, countSessionsInVehicle);
    }

    /**
     * @param limit Specifies the maximum number of sessions to retrieve
     * @return List of the latest sessions (potentioal pending session included)
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getLatest(long limit, boolean countSessionsInVehicle) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLatest");
        Crashlytics.log(Log.DEBUG, TAG, "@limit: " + limit);
        Crashlytics.log(Log.DEBUG, TAG, "@countSessionsInVehicle: " + countSessionsInVehicle);

        sessionDaoQueryBuilder.reset();

        Where<Session, Long> where = sessionDaoQueryBuilder
                .limit(limit == 0L ? null : limit)
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId());

        PreparedQuery<Session> preparedQuery = prepareQueryWithInVehicleConsidered(countSessionsInVehicle, where);

        return new ArrayList<>(sessionDao.query(preparedQuery));
    }

    private PreparedQuery<Session> prepareQueryWithInVehicleConsidered(boolean countSessionsInVehicle, Where<Session, Long> where) throws SQLException {
        if (!countSessionsInVehicle) {
            return where
                    .and()
                    .eq(Session.COLUMN_IN_VEHICLE, false)
                    .prepare();
        }
        return where.prepare();
    }

    /**
     * @return Number of sessions
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getFinishedCount() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getFinishedCount");

        sessionDaoQueryBuilder.reset();

        return (int) sessionDaoQueryBuilder
                .where()
                .ne(Session.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .countOf();
    }

    private Date getStartDate(@NotNull SessionsInterval interval) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getStartDate");
        Crashlytics.log(Log.DEBUG, TAG, "@interval: " + interval);

        switch (interval) {
            case LAST_MONTH:
                return DateHelper.getLastMonth();
            case LAST_WEEK:
                return DateHelper.getLastWeek();
            default:
                return DateHelper.getLastDay();
        }
    }

    /**
     * @return The number of consequent sessions which were successful
     */
    public int getStreak() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getStreak");

        Session lastUnsuccessful = getLastUnsuccessful();

        if (lastUnsuccessful == null) {
            Crashlytics.log(Log.DEBUG, TAG, "Last unsuccessful session not found");
            int latestSessionsCount = getLatest(false).size();

            if (pendingExists()) {
                Crashlytics.log(Log.DEBUG, TAG, "Returning the amount of all sessions minus pending session");
                return latestSessionsCount - 1;
            } else {
                Crashlytics.log(Log.DEBUG, TAG, "Returning the amount of all sessions");
                return latestSessionsCount;
            }
        }

        Crashlytics.log(Log.DEBUG, TAG, "Last unsuccessful session found successfully");
        return getConsequentSuccessfulCount(lastUnsuccessful, false);
    }

    private Session getLastUnsuccessful() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLastUnsuccessful");

        sessionDaoQueryBuilder.reset();

        return sessionDaoQueryBuilder
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_SUCCESSFUL, false)
                .and()
                .ne(Session.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    private int getConsequentSuccessfulCount(@NotNull Session lastUnsuccessful, boolean countSessionsInVehicle) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getConsequentSuccessfulCount");
        Crashlytics.log(Log.DEBUG, TAG, "@lastUnsuccessful ID: " + lastUnsuccessful.getId());
        Crashlytics.log(Log.DEBUG, TAG, "@countSessionsInVehicle: " + countSessionsInVehicle);

        sessionDaoQueryBuilder.reset();

        Where<Session, Long> where = sessionDaoQueryBuilder
                .where()
                .gt(Session.COLUMN_START_TIMESTAMP, lastUnsuccessful.getStartTimestamp())
                .and()
                .ne(Session.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId());

        PreparedQuery<Session> preparedQuery = prepareQueryWithInVehicleConsidered(countSessionsInVehicle, where);

        return sessionDao.query(preparedQuery).size();
    }

    /**
     * @return Integer value representing the sessions success ratio in the current day
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getSuccessRate(boolean countSessionsInVehicle) throws SQLException {
        return getSuccessRate(new Date(), countSessionsInVehicle);
    }

    /**
     * @param date                   Date object representing the day in which to calculate the success rate
     * @param countSessionsInVehicle
     * @return Integer value representing the sessions success ratio in the spectified day
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getSuccessRate(Date date, boolean countSessionsInVehicle) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getSuccessRate");
        Crashlytics.log(Log.DEBUG, TAG, "@date: " + date);
        Crashlytics.log(Log.DEBUG, TAG, "@countSessionsInVehicle: " + countSessionsInVehicle);

        Date normalizedDate = DateHelper.getNormalized(date);

        sessionDaoQueryBuilder.reset();

        Where<Session, Long> where = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .gt(Session.COLUMN_END_TIMESTAMP, 0)
                .and()
                .eq(Session.COLUMN_SUCCESSFUL, true)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId());

        PreparedQuery<Session> preparedQuery = prepareQueryWithInVehicleConsidered(countSessionsInVehicle, where);

        List<Session> successfulSessions = sessionDao.query(preparedQuery);

        sessionDaoQueryBuilder.reset();

        Where<Session, Long> where2 = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .gt(Session.COLUMN_END_TIMESTAMP, 0)
                .and()
                .eq(Session.COLUMN_SUCCESSFUL, false)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId());

        PreparedQuery<Session> preparedQuery2 = prepareQueryWithInVehicleConsidered(countSessionsInVehicle, where2);

        List<Session> unsuccessfulSessions = sessionDao.query(preparedQuery2);

        return getCalculatedSuccessRate(successfulSessions, unsuccessfulSessions);
    }

    private int getCalculatedSuccessRate(@NotNull List<Session> successfulSessions, @NotNull List<Session> unsuccessfulSessions) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getCalculatedSuccessRate");
        Crashlytics.log(Log.DEBUG, TAG, "@successfulSessions SIZE: " + successfulSessions.size());
        Crashlytics.log(Log.DEBUG, TAG, "@unsuccessfulSessions SIZE: " + unsuccessfulSessions.size());

        if (unsuccessfulSessions.size() == 0 && successfulSessions.size() != 0) {
            return HIGHEST_SUCCESS_RATE;
        } else if (successfulSessions.size() == 0) {
            return LOWEST_SUCCESS_RATE;
        } else {
            float totalSessions = successfulSessions.size() + unsuccessfulSessions.size();
            float successRate = successfulSessions.size() / totalSessions * 100F;
            return (int) successRate;
        }
    }

//    /**
//     * @param activityType Google Activity Recognition value determining the activity
//     * @return Whether the user is sedentary or not
//     */
//    // TODO context-involved determination
//    @Contract(pure = true)
//    private boolean isSedentary(int activityType) {
//        Crashlytics.log(Log.DEBUG, TAG, "Executing isSedentary");
//        Crashlytics.log(Log.DEBUG, TAG, "@activityType: " + activityType);
//
//        return activityType == DetectedActivity.STILL;
//    }

    public SessionType getSessionType(@NotNull Activity activity) {
        return getSessionType(activity.getType());
    }

    public SessionType getSessionType(int detectedActivity) {
        switch (detectedActivity) {
            case IN_VEHICLE:
            case ON_BICYCLE:
                return SessionType.IN_VEHICLE;
            case ON_FOOT:
            case WALKING:
            case RUNNING:
            case UNKNOWN:
            case TILTING:
            case DETECTED_ACTIVITY_SIG_MOV:
                return SessionType.ACTIVE;
            case STILL:
            default:
                return SessionType.SEDENTARY;
        }
    }

    /**
     * @param session Session to update as the ended one
     * @throws SQLException In case that communication with DB was not successful
     */
    public void end(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing end");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID:" + session.getId());

        long endTimestamp = new Date().getTime();

        session.setDuration(
                getDuration(session.getStartTimestamp(), endTimestamp)
        );
        session.setEndTimestamp(endTimestamp);
        session.setSuccessful(isSuccessful(session));

        update(session);
    }

    /**
     * @throws SQLException
     */
    public void endPending() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing endPending");
        Session pendingSession = null;
        try {
            pendingSession = getPending();
        } catch (NullPointerException e) {
            Crashlytics.log(Log.DEBUG, TAG, "No pending session to be ended");
            e.printStackTrace();
        }
        if (pendingSession != null) {
            end(pendingSession);
        }
    }

    private long getDuration(long startTimestamp, long endTimestamp) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@startTimestamp: " + startTimestamp);
        Crashlytics.log(Log.DEBUG, TAG, "@endTimestamp: " + endTimestamp);

        return endTimestamp - startTimestamp;
    }

    private boolean isSuccessful(@NotNull Session session) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing isSuccessful");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        if (session.isSedentary()) {
            Crashlytics.log(Log.DEBUG, TAG, "Session is sedentary");
            return session.getDuration() <= appSPHelper.getSedentaryLimit();
        } else {
            if (session.isInVehicle()) {
                Crashlytics.log(Log.DEBUG, TAG, "Session is successful due to being in a vehicle");
                return true;
            }
            Crashlytics.log(Log.DEBUG, TAG, "Session is active");
            return session.getDuration() >= appSPHelper.getActiveLimit();
        }
    }

    public boolean pendingExists() {
        Crashlytics.log(Log.DEBUG, TAG, "Executing pendingExists");
        try {
            getPending();
            Crashlytics.log(Log.DEBUG, TAG, "Session exists");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Crashlytics.log(Log.DEBUG, TAG, "Session does not exist");
        }
        return false;
    }

    /**
     * @return Pending session if any
     * @throws SQLException In case that communication with DB was not successful
     */
    public Session getPending() throws SQLException, NullPointerException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getPending");

        sessionDaoQueryBuilder.reset();

        Session pendingSession = sessionDaoQueryBuilder
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();

        if (pendingSession == null) {
            throw new NullPointerException();
        } else {
            return getPendingWithDuration(pendingSession);
        }
    }

    /**
     * @param pendingSession Pending session to set duration for
     * @return Updated session object
     */
    @Contract("_ -> param1")
    private Session getPendingWithDuration(Session pendingSession) {
        try {
            pendingSession.setDuration(getDuration(pendingSession));
        } catch (NullPointerException e) {
            pendingSession.setDuration(0L);
        }

        return pendingSession;
    }

    /**
     * @param session Session to update
     * @throws SQLException In case that communication with DB was not successful
     */
    public void update(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing update");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        sessionDao.update(session);
    }

    /**
     * @param activityType Type of activity to create session for
     * @return New session object
     * @throws SQLException In case that communication with DB was not successful
     */
    public Session create(int activityType) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing create");

        SessionType newSessionType = getSessionType(activityType);

        boolean sedentary = newSessionType == SessionType.SEDENTARY;
        boolean inVehicle = newSessionType == SessionType.IN_VEHICLE;

        Session newSession = new Session(
                sedentary,
                inVehicle,
                new Date().getTime(),
                profile
        );

        sessionDao.create(newSession);

        return getPending();
    }

    /**
     * @param sedentary Type of the new session
     * @throws SQLException In case that communication with DB was not successful
     */
    public Session createAndReplacePending(boolean sedentary) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing createAndReplacePending");
        Crashlytics.log(Log.DEBUG, TAG, "@sedentary: " + sedentary);


        try {
            Session pendingSession = getPending();
            end(pendingSession);
        } catch (NullPointerException e) {
            Crashlytics.log(Log.DEBUG, TAG, "Pending session does not exist");
        }

        Session newSession = new Session(
                sedentary,
                false,
                new Date().getTime(),
                profile
        );

        sessionDao.create(newSession);

        return getPending();
    }

    /**
     * @return Duration in milliseconds for todays sedentary time
     * @throws SQLException In case that communication with DB was not successful
     */
    public long getDailySedentaryDuration() throws SQLException {
        return getDailySedentaryDuration(new Date());
    }

    /**
     * @param date Date for which to return sedentary duration
     * @return Duration in milliseconds for sedentary time at specified date
     * @throws SQLException In case that communication with DB was not successful
     */
    public long getDailySedentaryDuration(Date date) throws SQLException {
        return getDailyDuration(date, true, false);
    }

    /**
     * @return Duration in milliseconds for todays active time
     * @throws SQLException In case that communication with DB was not successful
     */
    public long getDailyActiveDuration() throws SQLException {
        return getDailyActiveDuration(new Date());
    }

    /**
     * @param date Date for which to return active duration
     * @return Duration in milliseconds for active time at specified date
     * @throws SQLException In case that communication with DB was not successful
     */
    public long getDailyActiveDuration(Date date) throws SQLException {
        return getDailyDuration(date, false, false);
    }

    /**
     * @return
     * @throws SQLException
     */
    public long getDailyInVehicleDuration() throws SQLException {
        return getDailyInVehicleDuration(new Date());
    }

    /**
     * @param date
     * @return
     * @throws SQLException
     */
    public long getDailyInVehicleDuration(Date date) throws SQLException {
        return getDailyDuration(date, false, true);
    }

    private long getDailyDuration(Date date, boolean sedentary, boolean countSessionsInVehicle) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDailyDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@date: " + date);
        Crashlytics.log(Log.DEBUG, TAG, "@sedentary: " + sedentary);
        Crashlytics.log(Log.DEBUG, TAG, "@countSessionsInVehicle: " + countSessionsInVehicle);

        Date normalizedDate = DateHelper.getNormalized(date);

        sessionDaoQueryBuilder.reset();

        Where<Session, Long> where = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .eq(Session.COLUMN_SEDENTARY, sedentary)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId());

        PreparedQuery<Session> preparedQuery = prepareQueryWithInVehicleConsidered(countSessionsInVehicle, where);

        List<Session> sessions = sessionDao.query(preparedQuery);

        return getTotalDuration(sessions);
    }

    private long getTotalDuration(@NotNull List<Session> sessions) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getTotalDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions SIZE: " + sessions.size());

        long totalDuration = 0L;

        for (Session session : sessions) {
            if (session.getDuration() != 0L) {
                totalDuration += session.getDuration();
            }
        }

        return totalDuration;
    }

    /**
     * @param sessionsOfDay List of sessions to get streak for
     * @return The number of consequent sessions which were successful
     */
    public int getDayStreak(@NotNull ArrayList<Session> sessionsOfDay) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDayStreak");

        int streak = 0;

        for (Session session : sessionsOfDay) {
            if (session.isSuccessful()) {
                streak += 1;
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * @param sessionsOfDay List of all the sessions on certain date
     * @return Integer value representing the sessions success ratio in the spectified day
     */
    public int getDaySuccessRate(ArrayList<Session> sessionsOfDay) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDaySuccessRate");

        ArrayList<Session> successfulSessions = getSuccessful(sessionsOfDay, false);

        ArrayList<Session> unsuccessfulSessions = getUnsuccessful(sessionsOfDay, false);

        return getCalculatedSuccessRate(successfulSessions, unsuccessfulSessions);
    }

    @Contract("_, _ -> param1")
    private ArrayList<Session> getSuccessful(@NotNull ArrayList<Session> sessionsOfDay, boolean countSessionsInVehicle) {
        ArrayList<Session> successfulSessions = new ArrayList<>();
        for (Session session : sessionsOfDay) {
            if (countSessionsInVehicle) {
                if (session.isSuccessful()) {
                    successfulSessions.add(session);
                }
            } else {
                if (session.isSuccessful() && !session.isInVehicle()) {
                    successfulSessions.add(session);
                }
            }
        }
        return successfulSessions;
    }

    private ArrayList<Session> getUnsuccessful(@NotNull ArrayList<Session> sessionsOfDay, boolean countSessionsInVehicle) {
        ArrayList<Session> unsuccessfulSessions = new ArrayList<>();
        for (Session session : sessionsOfDay) {
            if (countSessionsInVehicle) {
                if (!session.isSuccessful()) {
                    unsuccessfulSessions.add(session);
                }
            } else {
                if (!session.isSuccessful() && !session.isInVehicle()) {
                    unsuccessfulSessions.add(session);
                }
            }
        }
        return unsuccessfulSessions;
    }

    /**
     * @param sessionsOfDay
     * @return
     */
    public long getDaySedentaryTime(ArrayList<Session> sessionsOfDay) {
        return getDayTotalDuration(sessionsOfDay, true, false);
    }

    /**
     * @param sessionsOfDay
     * @return
     */
    public long getDayActiveTime(ArrayList<Session> sessionsOfDay) {
        return getDayTotalDuration(sessionsOfDay, false, false);
    }

    public long getDayInVehicleTime(ArrayList<Session> sessionsOfDay) {
        return getDayTotalDuration(sessionsOfDay, false, true);
    }

    private long getDayTotalDuration(@NotNull List<Session> sessionsOfDay, boolean sedentary, boolean countInVehicleTimeOnly) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDayTotalDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions SIZE: " + sessionsOfDay.size());
        Crashlytics.log(Log.DEBUG, TAG, "@sedentary: " + sedentary);
        Crashlytics.log(Log.DEBUG, TAG, "@countInVehicleTimeOnly: " + countInVehicleTimeOnly);

        long totalDuration = 0L;

        for (Session session : sessionsOfDay) {
            if (session.getDuration() != 0L && session.isSedentary() == sedentary && session.isInVehicle() == countInVehicleTimeOnly) {
                totalDuration += session.getDuration();
            }
        }

        return totalDuration;
    }

    /**
     * @param sessions
     * @throws SQLException
     */
    public void setExported(@NotNull List<Session> sessions) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing setExported");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions SIZE: " + sessions.size());

        for (Session session : sessions) {
            session.setExported(true);
            update(session);
        }
    }

    /**
     * @throws SQLException
     */
    public void setExportedToUploaded() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing setExportedToUploaded");

        sessionDaoQueryBuilder.reset();

        ArrayList<Session> sessions = getExportedNotUploaded();

        for (Session session : sessions) {
            session.setUploaded(true);
            update(session);
        }
    }

    /**
     * @throws SQLException
     */
    public void revertExported() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing revertExported");

        sessionDaoQueryBuilder.reset();

        ArrayList<Session> sessions = getExportedNotUploaded();

        for (Session session : sessions) {
            session.setExported(false);
            update(session);
        }
    }

    @NotNull
    @Contract(" -> new")
    public ArrayList<Session> getExportedNotUploaded() throws SQLException {
        return new ArrayList<>(
                sessionDaoQueryBuilder
                        .where()
                        .eq(Session.COLUMN_EXPORTED, true)
                        .and()
                        .eq(Session.COLUMN_UPLOADED, false)
                        .and()
                        .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                        .query()
        );
    }

    /**
     * @return
     * @throws SQLException
     */
    public int getNotExportedFinishedCount() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getNotExportedFinishedCount");

        sessionDaoQueryBuilder.reset();

        return (int) sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_EXPORTED, false)
                .and()
                .ne(Session.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .countOf();
    }

    /**
     * @return
     * @throws SQLException
     */
    public ArrayList<Session> getNotExportedFinished() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getNotExportedFinished");

        sessionDaoQueryBuilder.reset();

        return new ArrayList<>(
                sessionDaoQueryBuilder
                        .where()
                        .eq(Session.COLUMN_EXPORTED, false)
                        .and()
                        .ne(Session.COLUMN_END_TIMESTAMP, 0L)
                        .and()
                        .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                        .query()
        );
    }

    /**
     * @throws SQLException
     */
    public void discardPending() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing discard");
        Session pendingSession = null;
        try {
            pendingSession = getPending();
        } catch (NullPointerException e) {
            Crashlytics.log(Log.DEBUG, TAG, "No pending session to be ended");
            e.printStackTrace();
        }
        if (pendingSession != null) {
            activityHelper.discardCorresponding(pendingSession);
            sessionDao.delete(pendingSession);
        }
    }

    /**
     * @throws SQLException
     */
    public void discardPendingAndUndoPrevious() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing discard");
        discardPending();
        Session latestSession = getLatest(1, false).get(0);
        if (latestSession != null) {
            latestSession.setEndTimestamp(0L);
            update(latestSession);
        }
    }

    /**
     * @param sessions List of sessions to stringify
     * @return String containing IDs of the sessions
     */
    @NotNull
    public static String getStringifiedSessions(@NotNull ArrayList<Session> sessions) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getStringifiedSessions");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions SIZE: " + sessions.size());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sessions.size(); i++) {
            Session session = sessions.get(i);
            sb.append(session.getId());
            if (i < sessions.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    /**
     * @return
     * @throws SQLException
     */
    public boolean isPendingReal() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing isPendingReal");

        Session pendingSession = getPending();
        return pendingSession != null && isReal(pendingSession);
    }

    /**
     * @param session
     * @return
     * @throws SQLException
     */
    public boolean isReal(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing isReal");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        ArrayList<Activity> activities = activityHelper.getCorresponding(session);
        return activities.size() != 1 || activities.get(0).getType() != DETECTED_ACTIVITY_SIG_MOV;
    }

    /**
     * @param session
     * @return
     */
    public long getDuration(@NotNull Session session) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        return session.getEndTimestamp() == 0L ? new Date().getTime() - session.getStartTimestamp() : session.getDuration();
    }
}