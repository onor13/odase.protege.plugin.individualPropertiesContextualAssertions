package odase;

import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.util.SAXParsers;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Pattern;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 1 Jun 16
 * improved by vblagodarov 20/07/17
 */
public class LiteralChecker {

    //The patterns come from official source: https://www.w3.org/TR/xmlschema11-2/
    //Except for duration, the regexp from the site has a bug: missing parentheses around [0-9]+Y?
    private static Pattern datePattern =
            Pattern.compile("-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?");

    private static Pattern timePattern =
            Pattern.compile("(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\\.[0-9]+)?|(24:00:00(\\.0+)?))(Z|(\\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?");

    private static Pattern durationPattern = Pattern.compile("-?P([0-9]+Y)?([0-9]+M)?([0-9]+D)?(T([0-9]+H)?([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?)?");

    public static boolean isLiteralIsInLexicalSpace(OWLLiteral literal) {
        OWLDatatype d = literal.getDatatype();
        if (d.isRDFPlainLiteral() || d.isString()) {
            return true;
        }
        if (d.isBuiltIn()) {
            OWL2Datatype builtIn = d.getBuiltInDatatype();
            if (builtIn.equals(OWL2Datatype.RDF_XML_LITERAL)) {
                return checkXMLLiteral(literal);
            } else {
                Pattern pattern = builtIn.getPattern();
                return pattern.matcher(literal.getLiteral()).matches();
            }
        } else {
            if (d.getIRI().equals(XSDVocabulary.DATE.getIRI())) {
                return datePattern.matcher(literal.getLiteral()).matches();
            } else if (d.getIRI().equals(XSDVocabulary.TIME.getIRI())) {
                return timePattern.matcher(literal.getLiteral()).matches();
            } else if (d.getIRI().equals(XSDVocabulary.DURATION.getIRI())) {
                return durationPattern.matcher(literal.getLiteral()).matches();
            }
            return true;
        }
    }

    private static boolean checkXMLLiteral(OWLLiteral literal) {
        try {
            SAXParsers.initParserWithOWLAPIStandards(null).parse(new InputSource(new StringReader(literal.getLiteral())),
                    new DefaultHandler());
            return true;
        } catch (SAXException | IOException e) {
            return false;
        }
    }

}