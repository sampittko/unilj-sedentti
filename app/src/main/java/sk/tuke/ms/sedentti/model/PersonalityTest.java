package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.helper.DateHelper;

@DatabaseTable
public class PersonalityTest {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_OPENNESS = "openness";
    public final static String COLUMN_CONSCIENTIOUSNESS = "conscientiousness";
    public final static String COLUMN_EXTRAVERSION = "extraversion";
    public final static String COLUMN_AGREEABLENESS = "agreeableness";
    public final static String COLUMN_NEUROTICISM = "neuroticism";
    public final static String COLUMN_ANSWERED_DATE = "answeredDate";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_OPENNESS)
    private int openness;
    @DatabaseField(canBeNull = false, columnName = COLUMN_CONSCIENTIOUSNESS)
    private int conscientiousness;
    @DatabaseField(canBeNull = false, columnName = COLUMN_EXTRAVERSION)
    private int extraversion;
    @DatabaseField(canBeNull = false, columnName = COLUMN_AGREEABLENESS)
    private int agreeableness;
    @DatabaseField(canBeNull = false, columnName = COLUMN_NEUROTICISM)
    private int neuroticism;
    @DatabaseField(canBeNull = false, columnName = COLUMN_ANSWERED_DATE)
    private Date answeredDate;

    public PersonalityTest() {

    }

    public PersonalityTest(int openness, int conscientiousness, int extraversion, int agreeableness, int neuroticism) {
        this.openness = openness;
        this.conscientiousness = conscientiousness;
        this.extraversion = extraversion;
        this.agreeableness = agreeableness;
        this.neuroticism = neuroticism;
        this.answeredDate = DateHelper.getNormalizedDate(new Date());
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
