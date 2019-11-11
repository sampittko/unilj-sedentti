package sk.tuke.ms.sedentti.model.helper;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.day.Day;
import sk.tuke.ms.sedentti.model.day.DayOverview;

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
//        ArrayList<Session> sessionsOfDay = new ArrayList<>();
//        Session previousSession = null;
//
//        for (Session session : sessions) {
//            if (previousSession != null) {
//                if (session.getDate() != previousSession.getDate()) {
//                    dayOverviews.add(
//                            getDayOverview(sessionsOfDay)
//                    );
//                    sessionsOfDay = new ArrayList<>();
//                }
//            }
//            sessionsOfDay.add(session);
//            previousSession = session;
//        }

        for (Session session : sessions) {
            if (dayOverviews.isEmpty()) {
//                only first time running
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

            if (added == false) {
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
        Day day = getDay(sessionsOfDay);
        return new DayOverview(day, sessionsOfDay);
    }

    private Day getDay(@NotNull ArrayList<Session> sessionsOfDay) {
        Date date = sessionsOfDay.get(0).getDate();
        int streak = sessionHelper.getDayStreak(sessionsOfDay);
        int successRate = sessionHelper.getDaySuccessRate(sessionsOfDay);
        int numberOfSessions = sessionsOfDay.size();
        long sedentaryTime = sessionHelper.getDaySedentaryTime(sessionsOfDay);
        long activeTime = sessionHelper.getDayActiveTime(sessionsOfDay);

        return new Day(date, streak, successRate, numberOfSessions, sedentaryTime, activeTime);
    }
}
