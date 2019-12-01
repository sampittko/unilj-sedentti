package sk.tuke.ms.sedentti.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import sk.tuke.ms.sedentti.model.helper.DateHelper;

@DatabaseTable
public class Profile {
    public final static String COLUMN_ID = "id";
    public final static String COLUMN_NAME = "name";
    public final static String COLUMN_EMAIL = "email";
    public final static String COLUMN_PHOTO_URL = "photoUrl";
    public final static String COLUMN_FIREBASE_AUTH_UID = "firebaseAuthUid";
    public final static String COLUMN_REGISTERED_DATE = "registeredDate";
    public final static String COLUMN_PERSONALITY_TEST_ID = "personalityTest_id";

    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id;
    @DatabaseField(canBeNull = false, columnName = COLUMN_NAME)
    private String name;
    @DatabaseField(canBeNull = false, columnName = COLUMN_EMAIL)
    private String email;
    @DatabaseField(canBeNull = false, columnName = COLUMN_PHOTO_URL)
    private String photoUrl;
    @DatabaseField(canBeNull = false, columnName = COLUMN_FIREBASE_AUTH_UID)
    private String firebaseAuthUid;
    @DatabaseField(canBeNull = false, unique = true, columnName = COLUMN_REGISTERED_DATE)
    private Date registeredDate;
    @DatabaseField(foreign = true, columnName = COLUMN_PERSONALITY_TEST_ID)
    private PersonalityTest personalityTest;

    public Profile() {

    }

    public Profile(String name, String email, String photoUrl, String firebaseAuthUid) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.firebaseAuthUid = firebaseAuthUid;
        this.registeredDate = DateHelper.getNormalizedDate(new Date());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getFirebaseAuthUid() {
        return firebaseAuthUid;
    }

    public void setFirebaseAuthUid(String firebaseAuthUid) {
        this.firebaseAuthUid = firebaseAuthUid;
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
