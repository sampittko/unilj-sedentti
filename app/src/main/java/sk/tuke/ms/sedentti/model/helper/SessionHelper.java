package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

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
        Date end = DateHelper.getNormalizedDate(new Date());
        Date start = getStartDate(interval);

        return new ArrayList<>(
                sessionDaoQueryBuilder
                        .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                        .where()
                        .between(Session.COLUMN_DATE, start, end)
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
     * @return List of all the sessions from the latest to the oldest (potentioal pending session included)
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
        return new ArrayList<>(
                sessionDaoQueryBuilder
                        .limit(limit == 0 ? null : limit)
                        .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                        .where()
                        .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                        .query()
        );
    }

    private Date getStartDate(@NotNull SessionsInterval interval) {
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
     * @throws SQLException In case that communication with DB was not successful
     */
    public int getStreak() throws SQLException {
        Session lastUnsuccessful = getLastUnsuccessful();
        return getConsequentSuccessfulCount(lastUnsuccessful);
    }

    private Session getLastUnsuccessful() throws SQLException {
        return sessionDaoQueryBuilder
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_SUCCESSFUL, false)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    private int getConsequentSuccessfulCount(@NotNull Session lastUnsuccessful) throws SQLException {
        return (int) sessionDaoQueryBuilder
                .where()
                .gt(Session.COLUMN_START_TIMESTAMP, lastUnsuccessful.getStartTimestamp())
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
        Date normalizedDate = DateHelper.getNormalizedDate(date);

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

        return getSuccessRate(successfulSessions, unsuccessfulSessions);
    }

    private int getSuccessRate(@NotNull List<Session> successfulSessions, @NotNull List<Session> unsuccessfulSessions) {
        return (int) Math.ceil(successfulSessions.size() / unsuccessfulSessions.size());
    }

    /**
     * @param activityType Google Activity Recognition value determining the activity
     * @return Whether the user is sedentary or not
     */
    // TODO context-involved determination
    @Contract(pure = true)
    public static boolean isSedentary(int activityType) {
        return activityType == DetectedActivity.STILL;
    }

    /**
     * @param session Session to update as the ended one
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateAsEndedSession(@NotNull Session session) throws SQLException {
        long endTimestamp = new Date().getTime();

        session.setDuration(
                getSessionDuration(session.getStartTimestamp(), endTimestamp)
        );
        session.setEndTimestamp(endTimestamp);
        session.setSuccessful(isSuccessful(session));

        updateSession(session);
    }

    @Contract(pure = true)
    private static long getSessionDuration(long startTimestamp, long endTimestamp) {
        return endTimestamp - startTimestamp;
    }

    private boolean isSuccessful(@NotNull Session session) {
        if (session.isSedentary()) {
            return session.getDuration() <= sharedPreferencesHelper.getSedentarySecondsLimit();
        }
        else {
            return session.getDuration() >= sharedPreferencesHelper.getActiveSecondsLimit();
        }
    }

    /**
     * @return Pending session if any
     * @throws SQLException In case that communication with DB was not successful
     */
    public Session getPendingSession() throws SQLException {
        return sessionDaoQueryBuilder
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_END_TIMESTAMP, 0L)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .queryForFirst();
    }

    /**
     * @param session Session to update
     * @throws SQLException In case that communication with DB was not successful
     */
    public void updateSession(Session session) throws SQLException {
        sessionDao.update(session);
    }

    /**
     * @param session Session to create
     * @throws SQLException In case that communication with DB was not successful
     */
    public void createSession(Session session) throws SQLException {
        sessionDao.create(session);
    }

    /**
     * @return Duration of pending session
     * @throws SQLException In case that communication with DB was not successful
     */
    public long getPendingSessionDuration() throws SQLException {
        return System.currentTimeMillis() - getPendingSession().getStartTimestamp();
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
        List<Session> sessions = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, date)
                .and()
                .eq(Session.COLUMN_SEDENTARY, sedentary)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .query();

        return getTotalDuration(sessions);
    }

    private long getTotalDuration(List<Session> sessions) {
        if (sessions.size() == 0) {
            return 0L;
        }

        long totalDuration = 0L;

        for (Session session : sessions) {
            if (session.getEndTimestamp() != 0L) {
                totalDuration += session.getDuration();
            }
            else {
                totalDuration += new Date().getTime() - session.getStartTimestamp();
            }
        }

        return totalDuration;
    }
}