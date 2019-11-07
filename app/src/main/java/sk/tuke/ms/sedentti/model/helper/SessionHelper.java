package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import sk.tuke.ms.sedentti.model.Profile;
import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.config.DatabaseHelper;

public class SessionHelper {
    public enum SessionsInterval {
        LAST_MONTH,
        LAST_WEEK,
        LAST_DAY
    }

    private Dao<Session, Long> sessionDao;
    private QueryBuilder<Session, Long> sessionDaoQueryBuilder;

    private Profile profile;

    public SessionHelper(Context context, Profile profile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            sessionDao = databaseHelper.sessionDao();
            sessionDaoQueryBuilder = sessionDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.profile = profile;
    }

    /**
     * @param interval Specifies the interval in which the requested sessions are
     * @return List of sessions in specified interval
     * @throws SQLException In case that communication with DB was not successful
     */
    public List<Session> getSessionsInInterval(SessionsInterval interval) throws SQLException {
        Date end = new Date();
        Date start = getStartDate(interval);

        return sessionDaoQueryBuilder
                .where()
                .between(Session.COLUMN_DATE, start, end)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .query();
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
        PreparedQuery<Session> preparedQuery = sessionDaoQueryBuilder
                .orderBy(Session.COLUMN_START_TIMESTAMP, false)
                .where()
                .eq(Session.COLUMN_SUCCESSFUL, false)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .prepare();

        return sessionDao.queryForFirst(preparedQuery);
    }

    private int getConsequentSuccessfulCount(@NotNull Session lastUnsuccessful) throws SQLException {
        PreparedQuery<Session> preparedQuery = sessionDaoQueryBuilder
                .where()
                .gt(Session.COLUMN_START_TIMESTAMP, lastUnsuccessful.getStartTimestamp())
                .and()
                .eq(Session.COLUMN_PROFILE_ID, profile.getId())
                .prepare();

        return (int) sessionDao.countOf(preparedQuery);
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

        PreparedQuery<Session> preparedQuery = sessionDaoQueryBuilder
                .where()
                .eq(Session.COLUMN_DATE, normalizedDate)
                .and()
                .gt(Session.COLUMN_END_TIMESTAMP, 0)
                .prepare();

        List<Session> sessions = sessionDao.query(preparedQuery);

        List<Session> successfulSessions = getSuccessfulSessions(sessions);
        List<Session> unsuccessfulSessions = getUnsuccessfulSessions(sessions, successfulSessions);

        return getSuccessRate(successfulSessions, unsuccessfulSessions);
    }

    @Contract("_ -> param1")
    private List<Session> getSuccessfulSessions(@NotNull List<Session> sessions) {
        for (Session session : sessions) {
            if (!session.isSuccessful()) {
                sessions.remove(session);
            }
        }
        return sessions;
    }

    @Contract("_, _ -> param1")
    private List<Session> getUnsuccessfulSessions(@NotNull List<Session> sessions, List<Session> successfulSessions) {
        sessions.removeAll(successfulSessions);
        return sessions;
    }

    private int getSuccessRate(@NotNull List<Session> successfulSessions, @NotNull List<Session> unsuccessfulSessions) {
        return (int) Math.ceil(successfulSessions.size() / unsuccessfulSessions.size());
    }
}