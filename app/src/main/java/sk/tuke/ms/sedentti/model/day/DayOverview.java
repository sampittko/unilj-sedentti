package sk.tuke.ms.sedentti.model.day;

import java.util.ArrayList;

import sk.tuke.ms.sedentti.model.Session;

public class DayOverview {
    private Day day;
    private ArrayList<Session> sessionsOfDay = new ArrayList<>();

    public DayOverview(Day day, ArrayList<Session> sessionsOfDay) {
        this.day = day;
        this.sessionsOfDay = sessionsOfDay;
    }

    public DayOverview() {
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public ArrayList<Session> getSessionsOfDay() {
        return sessionsOfDay;
    }

    public void setSessionsOfDay(ArrayList<Session> sessionsOfDay) {
        this.sessionsOfDay = sessionsOfDay;
    }

    @Override
    public String toString() {
        return "DayOverview{" +
                "day=" + day +
                ", sessionsOfDay=" + sessionsOfDay +
                '}';
    }
}
