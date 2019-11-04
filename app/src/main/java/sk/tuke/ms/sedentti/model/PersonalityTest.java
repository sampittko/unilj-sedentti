package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class PersonalityTest {
    public static String COLUMN_ID = "id";
    public static String COLUMN_OPENNESS = "openness";
    public static String COLUMN_CONSCIENTIOUSNESS = "conscientiousness";
    public static String COLUMN_EXTRAVERSION = "extraversion";
    public static String COLUMN_AGREEABLENESS = "agreeableness";
    public static String COLUMN_NEUROTICISM = "neuroticism";
    public static String COLUMN_ANSWERED_DATE = "answeredDate";

    @DatabaseField(generatedId = true, unique = true)
    private long id;
    @DatabaseField
    private int openness;
    @DatabaseField
    private int conscientiousness;
    @DatabaseField
    private int extraversion;
    @DatabaseField
    private int agreeableness;
    @DatabaseField
    private int neuroticism;
    @DatabaseField
    private Date answeredDate;

    public PersonalityTest() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOpenness() {
        return openness;
    }

    public void setOpenness(int openness) {
        this.openness = openness;
    }

    public int getConscientiousness() {
        return conscientiousness;
    }

    public void setConscientiousness(int conscientiousness) {
        this.conscientiousness = conscientiousness;
    }

    public int getExtraversion() {
        return extraversion;
    }

    public void setExtraversion(int extraversion) {
        this.extraversion = extraversion;
    }

    public int getAgreeableness() {
        return agreeableness;
    }

    public void setAgreeableness(int agreeableness) {
        this.agreeableness = agreeableness;
    }

    public int getNeuroticism() {
        return neuroticism;
    }

    public void setNeuroticism(int neuroticism) {
        this.neuroticism = neuroticism;
    }

    public Date getAnsweredDate() {
        return answeredDate;
    }

    public void setAnsweredDate(Date answeredDate) {
        this.answeredDate = answeredDate;
    }
}
