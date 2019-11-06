package sk.tuke.ms.sedentti.model.helper;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

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

    public SessionHelper(Context context) {
        DatabaseHelper databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            sessionDao = databaseHelper.sessionDao();
            sessionDaoQueryBuilder = sessionDao.queryBuilder();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Session> getSessionsInInterval(SessionsInterval interval) throws SQLException {
        Date end = new Date();
        Date start = getStartDate(interval);

        return sessionDaoQueryBuilder
                .where()
                .between(Session.COLUMN_DATE, start, end)
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

    // TODO Streaks
    public int getStreak() {
        throw new UnsupportedOperationException();
    }

    // TODO Success Rate
    public int getSuccessRate() {
        throw new UnsupportedOperationException();
    }
}
