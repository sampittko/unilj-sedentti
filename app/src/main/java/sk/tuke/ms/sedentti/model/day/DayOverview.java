package sk.tuke.ms.sedentti.model.day;

import java.util.List;

import sk.tuke.ms.sedentti.model.Session;

public class DayOverview {

    private Day day;
    private List<Session> sessionsOfDay;

    public DayOverview() {
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public List<Session> getSessionsOfDay() {
        return sessionsOfDay;
    }

    public void setSessionsOfDay(List<Session> sessionsOfDay) {
        this.sessionsOfDay = sessionsOfDay;
    }
}
