package sk.tuke.ms.sedentti.model.helper.session;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;

import sk.tuke.ms.sedentti.model.Session;
import sk.tuke.ms.sedentti.model.day.Day;
import sk.tuke.ms.sedentti.model.helper.SessionHelper;

public class DayHelper {
    public static Day getDay(@NotNull ArrayList<Session> sessionsOfDay, SessionHelper sessionHelper) {
        Date date = sessionsOfDay.get(0).getDate();
        int streak = sessionHelper.getDayStreak(sessionsOfDay);
        int successRate = sessionHelper.getDaySuccessRate(sessionsOfDay);
        int numberOfSessions = sessionsOfDay.size();
        long sedentaryTime = sessionHelper.getDaySedentaryTime(sessionsOfDay);
        long activeTime = sessionHelper.getDayActiveTime(sessionsOfDay);

        return new Day(date, streak, successRate, numberOfSessions, sedentaryTime, activeTime);
    }
}
