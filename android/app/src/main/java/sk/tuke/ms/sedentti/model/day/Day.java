package sk.tuke.ms.sedentti.model.day;

import java.util.Date;

public class Day extends DayModel {
    private Date date;
    private int streak;
    private int successRate;
    private int numberOfSessions;
    private long sedentaryTime;
    private long activeTime;
    private long inVehicleTime;

    public Day(Date date, int streak, int successRate, int numberOfSessions, long sedentaryTime, long activeTime, long inVehicleTime) {
        this.date = date;
        this.streak = streak;
        this.successRate = successRate;
        this.numberOfSessions = numberOfSessions;
        this.sedentaryTime = sedentaryTime;
        this.activeTime = activeTime;
        this.inVehicleTime = inVehicleTime;
    }

    public Day() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(int successRate) {
        this.successRate = successRate;
    }

    public int getNumberOfSessions() {
        return numberOfSessions;
    }

    public void setNumberOfSessions(int numberOfSessions) {
        this.numberOfSessions = numberOfSessions;
    }

    public long getSedentaryTime() {
        return sedentaryTime;
    }

    public void setSedentaryTime(long sedentaryTime) {
        this.sedentaryTime = sedentaryTime;
    }

    public long getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(long activeTime) {
        this.activeTime = activeTime;
    }

    public long getInVehicleTime() {
        return inVehicleTime;
    }

    public void setInVehicleTime(long inVehicleTime) {
        this.inVehicleTime = inVehicleTime;
    }

    @Override
    public String toString() {
        return "Day{" +
                "date=" + date +
                '}';
    }
}
