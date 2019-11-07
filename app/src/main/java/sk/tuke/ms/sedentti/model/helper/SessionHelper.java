package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

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

    private Profile activeProfile;

    public SessionHelper(Context context, Profile activeProfile) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            sessionDao = databaseHelper.sessionDao();
            sessionDaoQueryBuilder = sessionDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.activeProfile = activeProfile;
    }

    public List<Session> getSessionsInInterval(SessionsInterval interval) throws SQLException {
        Date end = new Date();
        Date start = getStartDate(interval);

        return sessionDaoQueryBuilder
                .where()
                .between(Session.COLUMN_DATE, start, end)
                .and()
                .eq(Session.COLUMN_PROFILE_ID, activeProfile.getId())
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
                .eq(Session.COLUMN_PROFILE_ID, activeProfile.getId())
                .prepare();

        return sessionDao.queryForFirst(preparedQuery);
    }

    private int getConsequentSuccessfulCount(Session lastUnsuccessful) throws SQLException {
        PreparedQuery<Session> preparedQuery = sessionDaoQueryBuilder
                .where()
                .gt(Session.COLUMN_START_TIMESTAMP, lastUnsuccessful.getStartTimestamp())
                .and()
                .eq(Session.COLUMN_PROFILE_ID, activeProfile.getId())
                .prepare();

        return (int) sessionDao.countOf(preparedQuery);
    }

    // TODO Success Rate
    public int getSuccessRate() {
        throw new UnsupportedOperationException();
    }
}
