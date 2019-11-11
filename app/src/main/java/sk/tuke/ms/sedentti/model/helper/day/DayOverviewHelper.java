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

        for (Session session : sessions) {
            if (dayOverviews.isEmpty()) {
                // only first time running
                createNewDayOverview(dayOverviews, session);
                continue;
            }

            boolean added = false;
            for (DayOverview dayOverview : dayOverviews) {
                if (dayOverview.getDay().getDate().compareTo(session.getDate()) == 0) {
                    dayOverview.getSessionsOfDay().add(session);
                    added = true;
                    break;
                }
            }

            if (!added) {
                createNewDayOverview(dayOverviews, session);
            }
        }

        return dayOverviews;
    }

    private void createNewDayOverview(ArrayList<DayOverview> dayOverviews, Session session) {
        DayOverview dayOverview = new DayOverview();
        Day day = new Day();
        day.setDate(session.getDate());
        dayOverview.setDay(day);
        dayOverview.getSessionsOfDay().add(session);

        dayOverviews.add(dayOverview);
    }

    @NotNull
    private DayOverview getDayOverview(ArrayList<Session> sessionsOfDay) {
        Day day = DayHelper.getDay(sessionsOfDay, sessionHelper);
        return new DayOverview(day, sessionsOfDay);
    }
}
