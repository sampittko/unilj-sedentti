package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;
import android.util.Log;

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

import sk.tuke.ms.sedentti.helper.SharedPreferencesHelper;
import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class SessionHelper {
    private static final Long HOME_TIMELINE_SESSIONS_LIMIT = 3L;
    private static final int HIGHEST_SUCCESS_RATE = 100;
    private static final int LOWEST_SUCCESS_RATE = 0;

    private static final String TAG = "SessionHelper";

    public enum SessionsInterval {
        LAST_MONTH,
        LAST_WEEK,
        LAST_DAY
    }

    private Dao<Session, Long> sessionDao;
    private QueryBuilder<Session, Long> sessionDaoQueryBuilder;

    private SharedPreferencesHelper sharedPreferencesHelper;

    private Profile profile;

    public SessionHelper(Context context, Profile profile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);

        try {
            this.sessionDao = databaseHelper.sessionDao();
            this.sessionDaoQueryBuilder = sessionDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.sharedPreferencesHelper = new SharedPreferencesHelper(context);
        this.profile = profile;
    }

    /**
     * @param interval Specifies the interval in which the requested sessions are
     * @return List of sessions in specified interval
     * @throws SQLException In case that communication with DB was not successful
     */
    public ArrayList<Session> getSessionsInInterval(SessionsInterval interval) throws SQLException {
        Log.d(TAG, "Executing getSessionsInInterval");
        Log.d(TAG, "@interval: " + interval);

        Date normalizedEndDate = DateHelper.getNormalizedDate(
                new Date()
        );
        Date normalizedStartDate = DateHelper.getNormalizedDate(
                getStartDate(interval)
        );

        Log.d(TAG, "Start date: " + normalizedStartDate + "\n");
        Log.d(TAG, "End date: " + normalizedEndDate);

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
        Log.d(TAG, "Executing getLatestSessions");
        Log.d(TAG, "@limit: " + limit);

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
        Log.d(TAG, "Executing getStartDate");
        Log.d(TAG, "@interval: " + interval);

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
        Log.d(TAG, "Executing getStreak");

        Session lastUnsuccessful = getLastUnsuccessful();

        if (lastUnsuccessful == null) {
            Log.d(TAG, "Last unsuccessful session not found");
            int latestSessionsCount = getLatestSessions().size();

            if (pendingSessionExists()) {
                Log.d(TAG, "Returning the amount of all sessions minus pending session");
                return latestSessionsCount - 1;
            }
            else {
                Log.d(TAG, "Returning the amount of all sessions");
                return latestSessionsCount;
            }
        }

        Log.d(TAG, "Last unsuccessful session found successfully");
        return getConsequentSuccessfulCount(lastUnsuccessful);
    }

    private Session getLastUnsuccessful() throws SQLException {
        Log.d(TAG, "Executing getLastUnsuccessful");

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
        Log.d(TAG, "Executing getConsequentSuccessfulCount");
        Log.d(TAG, "@lastUnsuccessful ID: " + lastUnsuccessful.getId());

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
        Log.d(TAG, "Executing getSuccessRate");
        Log.d(TAG, "@date: " + date);

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
        Log.d(TAG, "Executing getCalculatedSuccessRate");
        Log.d(TAG, "@successfulSessions SIZE: " + successfulSessions.size());
        Log.d(TAG, "@unsuccessfulSessions SIZE: " + unsuccessfulSessions.size());

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
        Log.d(TAG, "Executing isSedentary");
        Log.d(TAG, "@activityType: " + activityType);

        return activityType == DetectedActivity.STILL;
    }

    /**
     * @param session Session to update as the ended one
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateAsEndedSession(@NotNull Session session) throws SQLException {
        Log.d(TAG, "Executing updateAsEndedSession");
        Log.d(TAG, "@session ID:" + session.getId());

        long endTimestamp = new Date().getTime();

        session.setDuration(
                getSessionDuration(session.getStartTimestamp(), endTimestamp)
        );
        session.setEndTimestamp(endTimestamp);
        session.setSuccessful(isSuccessful(session));

        updateSession(session);
    }

    private long getSessionDuration(long startTimestamp, long endTimestamp) {
        Log.d(TAG, "Executing getSessionDuration");
        Log.d(TAG, "@startTimestamp: " + startTimestamp);
        Log.d(TAG, "@endTimestamp: " + endTimestamp);

        return endTimestamp - startTimestamp;
    }

    private boolean isSuccessful(@NotNull Session session) {
        Log.d(TAG, "Executing isSuccessful");
        Log.d(TAG, "@session ID: " + session.getId());

        if (session.isSedentary()) {
            Log.d(TAG, "Session is sedentary");
            return session.getDuration() <= sharedPreferencesHelper.getSedentarySecondsLimit();
        }
        else {
            Log.d(TAG, "Session is not sedentary");
            return session.getDuration() >= sharedPreferencesHelper.getActiveSecondsLimit();
        }
    }

    public boolean pendingSessionExists() {
        Log.d(TAG, "Executing pendingSessionExists");
        try {
            getPendingSession();
            Log.d(TAG, "Session exists");
            return true;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            Log.d(TAG, "Session does not exist");
        }
        return false;
    }

    /**
     * @return Pending session if any
     * @throws SQLException In case that communication with DB was not successful
     */
    public Session getPendingSession() throws SQLException, NullPointerException {
        Log.d(TAG, "Executing getPendingSession");

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
        Log.d(TAG, "Executing updateSession");
        Log.d(TAG, "@session ID: " + session.getId());

        sessionDao.update(session);
    }

    /**
     * @param activityType Type of activity to create session for
     * @throws SQLException In case that communication with DB was not successful
     */
    public void createSession(int activityType) throws SQLException {
        Log.d(TAG, "Executing createSession");

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
        Log.d(TAG, "Executing getDailyDuration");
        Log.d(TAG, "@date: " + date);
        Log.d(TAG, "@sedentary: " + sedentary);

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
        Log.d(TAG, "Executing getTotalDuration");
        Log.d(TAG, "@sessions SIZE: " + sessions.size());

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
        Log.d(TAG, "Executing getDayStreak");

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
        Log.d(TAG, "Executing getDaySuccessRate");

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
        Log.d(TAG, "Executing getDayTotalDuration");
        Log.d(TAG, "@sessions SIZE: " + sessionsOfDay.size());
        Log.d(TAG, "@sedentary: " + sedentary);

        long totalDuration = 0L;

        for (Session session : sessionsOfDay) {
            if (session.getDuration() != 0L && session.isSedentary() == sedentary) {
                totalDuration += session.getDuration();
            }
        }

        return totalDuration;
    }
}