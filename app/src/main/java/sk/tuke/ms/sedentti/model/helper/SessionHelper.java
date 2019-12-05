package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.DetectedActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.tuke.ms.sedentti.helper.shared_preferences.AppSPHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class SessionHelper {
    private final Long HOME_TIMELINE_SESSIONS_LIMIT = 3L;
    private final int HIGHEST_SUCCESS_RATE = 100;
    private final int LOWEST_SUCCESS_RATE = 0;

    private final String TAG = "SessionHelper";

    public enum SessionsInterval {
        LAST_MONTH,
        LAST_WEEK,
    }

    private Dao<Session, Long> sessionDao;
    private QueryBuilder<Session, Long> sessionDaoQueryBuilder;

    private AppSPHelper appSPHelper;

    private Profile profile;

    public SessionHelper(Context context, Profile profile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
            this.sessionDao = databaseHelper.sessionDao();
            this.sessionDaoQueryBuilder = sessionDao.queryBuilder();
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
    public ArrayList<Session> getSessionsInInterval(SessionsInterval interval) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getSessionsInInterval");
        Crashlytics.log(Log.DEBUG, TAG, "@interval: " + interval);

        Date normalizedEndDate = DateHelper.getNormalizedDate(
                new Date()
        );
        Date normalizedStartDate = DateHelper.getNormalizedDate(
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
        return getLatestSessions(HOME_TIMELINE_SESSIONS_LIMIT);
    }

    /**
     * @return List of all the sessions from the latest to the oldest (potential pending session included)
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getLatestSessions() throws SQLException {
        return getLatestSessions(0);
    }

    /**
     * @param limit Specifies the maximum number of sessions to retrieve
     * @return List of the latest sessions (potentioal pending session included)
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getLatestSessions(long limit) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLatestSessions");
        Crashlytics.log(Log.DEBUG, TAG, "@limit: " + limit);

        sessionDaoQueryBuilder.reset();

        return new ArrayList<>(
                sessionDaoQueryBuilder
                        .limit(limit == 0L ? null : limit)
                        .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                        .where()
                        .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                        .query()
        );
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
            int latestSessionsCount = getLatestSessions().size();

            if (pendingSessionExists()) {
                Crashlytics.log(Log.DEBUG, TAG, "Returning the amount of all sessions minus pending session");
                return latestSessionsCount - 1;
            }
            else {
                Crashlytics.log(Log.DEBUG, TAG, "Returning the amount of all sessions");
                return latestSessionsCount;
            }
        }

        Crashlytics.log(Log.DEBUG, TAG, "Last unsuccessful session found successfully");
        return getConsequentSuccessfulCount(lastUnsuccessful);
    }

    private Session getLastUnsuccessful() throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getLastUnsuccessful");

        sessionDaoQueryBuilder.reset();

        return sessionDaoQueryBuilder
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_SUCCESSFUL, false)
                .and()
                .gt(Session.COLUMN_DURATION, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    private int getConsequentSuccessfulCount(@NotNull Session lastUnsuccessful) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getConsequentSuccessfulCount");
        Crashlytics.log(Log.DEBUG, TAG, "@lastUnsuccessful ID: " + lastUnsuccessful.getId());

        sessionDaoQueryBuilder.reset();

        return (int) sessionDaoQueryBuilder
                .where()
                .gt(Session.COLUMN_START_TIMESTAMP, lastUnsuccessful.getStartTimestamp())
                .and()
                .gt(Session.COLUMN_DURATION, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .countOf();
    }

    /**
     * @return Integer value representing the sessions success ratio in the current day
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getSuccessRate() throws SQLException {
        return getSuccessRate(new Date());
    }

    /**
     * @param date Date object representing the day in which to calculate the success rate
     * @return Integer value representing the sessions success ratio in the spectified day
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getSuccessRate(Date date) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getSuccessRate");
        Crashlytics.log(Log.DEBUG, TAG, "@date: " + date);

        Date normalizedDate = DateHelper.getNormalizedDate(date);

        sessionDaoQueryBuilder.reset();

        List<Session> successfulSessions = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .gt(Session.COLUMN_END_TIMESTAMP, 0)
                .and()
                .eq(Session.COLUMN_SUCCESSFUL, true)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .query();

        sessionDaoQueryBuilder.reset();

        List<Session> unsuccessfulSessions = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .gt(Session.COLUMN_END_TIMESTAMP, 0)
                .and()
                .eq(Session.COLUMN_SUCCESSFUL, false)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .query();

        return getCalculatedSuccessRate(successfulSessions, unsuccessfulSessions);
    }

    private int getCalculatedSuccessRate(@NotNull List<Session> successfulSessions, @NotNull List<Session> unsuccessfulSessions) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getCalculatedSuccessRate");
        Crashlytics.log(Log.DEBUG, TAG, "@successfulSessions SIZE: " + successfulSessions.size());
        Crashlytics.log(Log.DEBUG, TAG, "@unsuccessfulSessions SIZE: " + unsuccessfulSessions.size());

        if (unsuccessfulSessions.size() == 0 && successfulSessions.size() != 0) {
            return HIGHEST_SUCCESS_RATE;
        }
        else if (successfulSessions.size() == 0) {
            return LOWEST_SUCCESS_RATE;
        }
        else {
            float totalSessions = successfulSessions.size() + unsuccessfulSessions.size();
            float successRate = successfulSessions.size() / totalSessions * 100F;
            return (int) successRate;
        }
    }

    /**
     * @param activityType Google Activity Recognition value determining the activity
     * @return Whether the user is sedentary or not
     */
    // TODO context-involved determination
    @Contract(pure = true)
    private boolean isSedentary(int activityType) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing isSedentary");
        Crashlytics.log(Log.DEBUG, TAG, "@activityType: " + activityType);

        return activityType == DetectedActivity.STILL;
    }

    /**
     * @param session Session to update as the ended one
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateAsEndedSession(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing updateAsEndedSession");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID:" + session.getId());

        long endTimestamp = new Date().getTime();

        session.setDuration(
                getSessionDuration(session.getStartTimestamp(), endTimestamp)
        );
        session.setEndTimestamp(endTimestamp);
        session.setSuccessful(isSuccessful(session));

        updateSession(session);
    }

    private long getSessionDuration(long startTimestamp, long endTimestamp) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getSessionDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@startTimestamp: " + startTimestamp);
        Crashlytics.log(Log.DEBUG, TAG, "@endTimestamp: " + endTimestamp);

        return endTimestamp - startTimestamp;
    }

    private boolean isSuccessful(@NotNull Session session) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing isSuccessful");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        if (session.isSedentary()) {
            Crashlytics.log(Log.DEBUG, TAG, "Session is sedentary");
            return session.getDuration() <= appSPHelper.getSedentarySecondsLimit();
        }
        else {
            Crashlytics.log(Log.DEBUG, TAG, "Session is not sedentary");
            return session.getDuration() >= appSPHelper.getActiveSecondsLimit();
        }
    }

    public boolean pendingSessionExists() {
        Crashlytics.log(Log.DEBUG, TAG, "Executing pendingSessionExists");
        try {
            getPendingSession();
            Crashlytics.log(Log.DEBUG, TAG, "Session exists");
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            Crashlytics.log(Log.DEBUG, TAG, "Session does not exist");
        }
        return false;
    }

    /**
     * @return Pending session if any
     * @throws SQLException In case that communication with DB was not successful
     */
    public Session getPendingSession() throws SQLException, NullPointerException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getPendingSession");

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
        }
        else {
            return getPendingSessionWithDuration(pendingSession);
        }
    }

    /**
     * @param pendingSession Pending session to set duration for
     * @return Updated session object
     */
    @Contract("_ -> param1")
    private Session getPendingSessionWithDuration(Session pendingSession) {
        try {
            pendingSession.setDuration(new Date().getTime() - pendingSession.getStartTimestamp());
        }
        catch (NullPointerException e) {
            pendingSession.setDuration(0L);
        }

        return pendingSession;
    }

    /**
     * @param session Session to update
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateSession(@NotNull Session session) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing updateSession");
        Crashlytics.log(Log.DEBUG, TAG, "@session ID: " + session.getId());

        sessionDao.update(session);
    }

    /**
     * @param activityType Type of activity to create session for
     * @throws SQLException In case that communication with DB was not successful
     */
    public void createSession(int activityType) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing createSession");

        Session newSession = new Session(
                isSedentary(activityType),
                new Date().getTime(),
                profile
        );

        sessionDao.create(newSession);
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
        return getDailyDuration(date, true);
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
        return getDailyDuration(date, false);
    }

    private long getDailyDuration(Date date, boolean sedentary) throws SQLException {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDailyDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@date: " + date);
        Crashlytics.log(Log.DEBUG, TAG, "@sedentary: " + sedentary);

        Date normalizedDate = DateHelper.getNormalizedDate(date);

        sessionDaoQueryBuilder.reset();

        List<Session> sessions = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .eq(Session.COLUMN_SEDENTARY, sedentary)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .query();

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
            }
            else {
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

        ArrayList<Session> successfulSessions = getSuccessfulSessions(sessionsOfDay);

        ArrayList<Session> unsuccessfulSessions = getUnsuccessfulSessions(sessionsOfDay);

        return getCalculatedSuccessRate(successfulSessions, unsuccessfulSessions);
    }

    @Contract("_ -> param1")
    private ArrayList<Session> getSuccessfulSessions(@NotNull ArrayList<Session> sessionsOfDay) {
        ArrayList<Session> successfulSessions = new ArrayList<>();
        for (Session session : sessionsOfDay) {
            if (session.isSuccessful()) {
                successfulSessions.add(session);
            }
        }
        return successfulSessions;
    }

    private ArrayList<Session> getUnsuccessfulSessions(@NotNull ArrayList<Session> sessionsOfDay) {
        ArrayList<Session> unsuccessfulSessions = new ArrayList<>();
        for (Session session : sessionsOfDay) {
            if (!session.isSuccessful()) {
                unsuccessfulSessions.add(session);
            }
        }
        return unsuccessfulSessions;
    }

    /**
     * @param sessionsOfDay
     * @return
     */
    public long getDaySedentaryTime(ArrayList<Session> sessionsOfDay) {
        return getDayTotalDuration(sessionsOfDay, true);
    }

    /**
     * @param sessionsOfDay
     * @return
     */
    public long getDayActiveTime(ArrayList<Session> sessionsOfDay) {
        return getDayTotalDuration(sessionsOfDay, false);
    }

    private long getDayTotalDuration(@NotNull List<Session> sessionsOfDay, boolean sedentary) {
        Crashlytics.log(Log.DEBUG, TAG, "Executing getDayTotalDuration");
        Crashlytics.log(Log.DEBUG, TAG, "@sessions SIZE: " + sessionsOfDay.size());
        Crashlytics.log(Log.DEBUG, TAG, "@sedentary: " + sedentary);

        long totalDuration = 0L;

        for (Session session : sessionsOfDay) {
            if (session.getDuration() != 0L && session.isSedentary() == sedentary) {
                totalDuration += session.getDuration();
            }
        }

        return totalDuration;
    }
}