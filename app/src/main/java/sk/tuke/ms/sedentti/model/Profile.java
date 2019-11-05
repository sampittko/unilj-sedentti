package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable
public class Profile {
    public static String COLUMN_ID = "id";
    public static String COLUMN_NAME = "name";
    public static String COLUMN_REGISTERED_DATE = "registeredDate";
    public static String COLUMN_PERSONALITY_TEST_ID = "personalityTestId";

    @DatabaseField(generatedId = true, unique = true)
    private long id;
    @DatabaseField
    private String name;
    @DatabaseField
    private Date registeredDate;
    @DatabaseField(foreign = true)
    private PersonalityTest personalityTest;

    public Profile() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(Date registeredDate) {
        this.registeredDate = registeredDate;
    }

    public PersonalityTest getPersonalityTest() {
        return personalityTest;
    }

    public void setPersonalityTest(PersonalityTest personalityTest) {
        this.personalityTest = personalityTest;
    }
}