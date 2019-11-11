package sk.tuke.ms.sedentti.model.helper.day;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.day.Day;
import sk.tuke.ms.sedentti.model.day.DayOverview;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class DayOverviewHelper {
    private SessionHelper sessionHelper;

    public DayOverviewHelper(SessionHelper sessionHelper) {
        this.sessionHelper = sessionHelper;
    }

    public ArrayList<DayOverview> getDayOverviews() throws SQLException {
        List<Session> sessions = sessionHelper.getLatestSessions();
        return getDayOverviews(sessions);
    }

    private ArrayList<DayOverview> getDayOverviews(@NotNull List<Session> sessions) {
        ArrayList<DayOverview> dayOverviews = new ArrayList<>();
        ArrayList<Session> sessionsOfDay = new ArrayList<>();
        Session previousSession = null;

        for (Session session : sessions) {
            if (previousSession != null) {
                if (session.getDate() != previousSession.getDate()) {
                    dayOverviews.add(
                            getDayOverview(sessionsOfDay)
                    );
                    sessionsOfDay = new ArrayList<>();
                }
            }
            sessionsOfDay.add(session);
            previousSession = session;
        }

        return dayOverviews;
    }

    @NotNull
    private DayOverview getDayOverview(ArrayList<Session> sessionsOfDay) {
        Day day = DayHelper.getDay(sessionsOfDay, sessionHelper);
        return new DayOverview(day, sessionsOfDay);
    }
}
