package sk.tuke.ms.sedentti.model.field.types;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateStringType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;

public class DateStringSQLiteType extends DateStringType {
    private static final DateStringFormatConfig dateFormatConfig = new DateStringFormatConfig("yyyy-M-d H:m:s");
    private static final DateStringSQLiteType singleTon = new DateStringSQLiteType();

    public static DateStringSQLiteType getSingleton() {
        return singleTon;
    }

    private DateStringSQLiteType() {
        super(SqlType.STRING, new Class<?>[0]);
    }

    /**
     * Convert a default string object and return the appropriate argument to a
     * SQL insert or update statement.
     */
    @Override
    public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
        DateStringFormatConfig formatConfig = convertDateStringConfig(
                fieldType, dateFormatConfig);
        try {
            // we parse to make sure it works and then format it again
            return normalizeDateString(formatConfig, defaultStr);
        } catch (ParseException e) {
            throw SqlExceptionUtil.create("Problems with field " + fieldType
                    + " parsing default date-string '" + defaultStr
                    + "' using '" + formatConfig + "'", e);
        }
    }

    /**
     * Return the SQL argument object extracted from the results associated with
     * column in position columnPos. For example, if the type is a date-long
     * then this will return a long value or null.
     *
     * @throws SQLException
     *             If there is a problem accessing the results data.
     * @param fieldType
     *            Associated FieldType which may be null.
     */
    @Override
    public Object resultToSqlArg(FieldType fieldType, DatabaseResults results,
                                 int columnPos) throws SQLException {
        return results.getString(columnPos);
    }

    /**
     * Return the object converted from the SQL arg to java. This takes the
     * database representation and converts it into a Java object. For example,
     * if the type is a date-long then this will take a long which is stored in
     * the database and return a Date.
     *
     * @param fieldType
     *            Associated FieldType which may be null.
     * @param sqlArg
     *            SQL argument converted with
     *            {@link #resultToSqlArg(FieldType, DatabaseResults, int)} which
     *            will not be null.
     */
    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos)
            throws SQLException {
        String value = (String) sqlArg;
        DateStringFormatConfig formatConfig = convertDateStringConfig(
                fieldType, dateFormatConfig);
        try {
            return parseDateString(formatConfig, value);
        } catch (ParseException e) {
            throw SqlExceptionUtil.create("Problems with column " + columnPos
                    + " parsing date-string '" + value + "' using '"
                    + formatConfig + "'", e);
        }
    }

    /**
     * Convert a Java object and return the appropriate argument to a SQL insert
     * or update statement.
     */
    @Override
    public Object javaToSqlArg(FieldType fieldType, Object obj) {
        DateFormat dateFormat = convertDateStringConfig(fieldType,
                dateFormatConfig).getDateFormat();
        return dateFormat.format((Date) obj);
    }

    /**
     * @throws SQLException
     *             If there are problems creating the config object. Needed for
     *             subclasses.
     */
    @Override
    public Object makeConfigObject(FieldType fieldType) {
        String format = fieldType.getFormat();
        if (format == null) {
            return dateFormatConfig;
        } else {
            return new DateStringFormatConfig(format);
        }
    }
}