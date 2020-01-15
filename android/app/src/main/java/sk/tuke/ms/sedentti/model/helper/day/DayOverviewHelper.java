package sk.tuke.ms.sedentti.model.helper.day;

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
        List<Session> sessions = sessionHelper.getLatest();
        ArrayList<DayOverview> dayOverviewsList = new ArrayList<>();

        for (Session session : sessions) {
            if (dayOverviewsList.isEmpty()) {
                // only first time running
                createNewDayOverview(dayOverviewsList, session);
                continue;
            }

            boolean added = false;
            for (DayOverview dayOverview : dayOverviewsList) {
                if (dayOverview.getDay().getDate().compareTo(session.getDate()) == 0) {
                    dayOverview.getSessionsOfDay().add(session);
                    added = true;
                    break;
                }
            }

            if (!added) {
                createNewDayOverview(dayOverviewsList, session);
            }
        }

        for (DayOverview dayOverview : dayOverviewsList) {
            Day day = DayHelper.getDay(dayOverview.getSessionsOfDay(), this.sessionHelper);
            dayOverview.setDay(day);
        }
        return dayOverviewsList;
    }

    private void createNewDayOverview(ArrayList<DayOverview> dayOverviewList, Session session) {
        DayOverview dayOverview = new DayOverview();
        Day day = new Day();
        day.setDate(session.getDate());
        dayOverview.setDay(day);
        dayOverview.getSessionsOfDay().add(session);

        dayOverviewList.add(dayOverview);
    }
}
