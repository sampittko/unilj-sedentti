package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.field.types.DateStringSQLiteType;

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
    @DatabaseField(canBeNull = false)
    private int openness;
    @DatabaseField(canBeNull = false)
    private int conscientiousness;
    @DatabaseField(canBeNull = false)
    private int extraversion;
    @DatabaseField(canBeNull = false)
    private int agreeableness;
    @DatabaseField(canBeNull = false)
    private int neuroticism;
    // TMP solution
//    @DatabaseField(canBeNull = false, persisterClass = DateStringSQLiteType.class)
//    private Date answeredDate;
    @DatabaseField(canBeNull = false)
    private Date answeredDate;

    public PersonalityTest() {

    }

    public PersonalityTest(int openness, int conscientiousness, int extraversion, int agreeableness, int neuroticism) {
        this.openness = openness;
        this.conscientiousness = conscientiousness;
        this.extraversion = extraversion;
        this.agreeableness = agreeableness;
        this.neuroticism = neuroticism;
        this.answeredDate = new Date();
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
