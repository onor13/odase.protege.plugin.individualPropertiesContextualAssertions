package odase.tools;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import java.util.HashMap;

/**
 * Created by vblagodarov on 20-07-17.
 */
public class OWLDataTypeFormatInfo {

    private OWLDataFactory df;
    private HashMap<OWLDatatype, String> datatypeFormatInfo;

    public OWLDataTypeFormatInfo(OWLDataFactory dataFactory) {
        df = dataFactory;
        datatypeFormatInfo = new HashMap<OWLDatatype, String>() {{
            put(df.getOWLDatatype(XSDVocabulary.DATE.getIRI()), dateFormat);
            put(df.getOWLDatatype(XSDVocabulary.TIME.getIRI()), timeFormat);
            put(df.getOWLDatatype(XSDVocabulary.DATE_TIME.getIRI()), dataTimeFormat);
            put(df.getOWLDatatype(XSDVocabulary.DURATION.getIRI()), durationFormat);
        }};
    }

    private static final String unknownFormat = "No information available";
    private static final String dateFormat = "<html>The date is specified in the following form \"YYYY-MM-DD\" where:" +
            "<ul>" +
            "<li>YYYY indicates the year </li>" +
            "<li>MM indicates the month </li>" +
            "<li>DD indicates the day </li>" +
            " </ul>" +
            "To specify a time zone, you can either enter a date in UTC time by adding a \"Z\" behind the date - like this: <BR>" +
            "<b>2002-09-24Z</b> <BR> or you can specify an offset from the UTC time by adding a positive or negative time behind the date: <BR> " +
            "<b>2002-09-24-06:00</b> or <b>2002-09-24+06:00</b> </html>";

    private static final String timeFormat = "<html>The time is specified in the following form \"hh:mm:ss\" where:" +
            "<ul>" +
            "<li>hh indicates the hour </li>" +
            "<li>mm indicates the minute </li>" +
            "<li>ss indicates the second </li>" +
            " </ul>" +
            "To specify a time zone, you can either enter a date in UTC time by adding a \"Z\" behind the time - like this: " +
            " <b>09:30:10Z</b> <BR> or you can specify an offset from the UTC time by adding a positive or negative time behind the time: <BR> " +
            " <b>2002-09-24-06:00</b> or <b>09:30:10+06:00</b> </html>";

    private static final String dataTimeFormat = "<html>The dateTime is specified in the following form \"YYYY-MM-DDThh:mm:ss\" where:" +
            "<ul>" +
            "<li>YYYY indicates the year </li>" +
            "<li>MM indicates the month </li>" +
            "<li>DD indicates the day </li>" +
            "<li>T indicates the start of the required time section </li>" +
            "<li>hh indicates the hour </li>" +
            "<li>mm indicates the minute </li>" +
            "<li>ss indicates the second </li>" +
            " </ul>" +
            "Example: <b>2002-05-30T09:00:00</b> <BR>" +
            "To specify a time zone, you can either enter a date in UTC time by adding a \"Z\" behind the time - like this:" +
            " <b>2002-05-30T09:30:10Z</b> <BR> or you can specify an offset from the UTC time by adding a positive or negative time behind the time: <BR> " +
            "<b>2002-05-30T09:30:10-06:00</b> or <b>2002-05-30T09:30:10+06:00</b> </html>";

    private static final String durationFormat = "<html>The time interval is specified in the following form \"PnYnMnDTnHnMnS\" where:" +
            "<ul>" +
            "<li>P indicates the period (required)</li>" +
            "<li>nM indicates the number of months</li>" +
            "<li>nD indicates the number of days</li>" +
            "<li>T indicates the start of a time section (required if you are going to specify hours, minutes, or seconds)</li>" +
            "<li>nH indicates the number of hours</li>" +
            "<li>nM indicates the number of minutes</li>" +
            "<li>nS indicates the number of seconds</li>" +
            " </ul>" +
            "This example indicates a period of five years, two months, and 10 days:  <BR>" +
            "<b>P5Y2M10D</b> <BR>" +
            "This example  indicates a period of a period of 15 hours: <BR>" +
            "<b>PT15H</b> <BR> </html>";

    public String getDatatypeFormatInfo(OWLDatatype datatype) {
        String result = datatypeFormatInfo.get(datatype);
        return result == null ? unknownFormat : result;
    }

}
