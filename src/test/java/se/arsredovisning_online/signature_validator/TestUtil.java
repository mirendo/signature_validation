package se.arsredovisning_online.signature_validator;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

class TestUtil {
    static InputStream getFixtureFile(String filename) {
        return TestUtil.class.getResourceAsStream(filename);
    }

    static Document getSignatureDocument(String filename) {
        return parseSignatureFile(getFixtureFile(filename));
    }

    private static Document parseSignatureFile(InputStream signatureAsStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            return dbf.newDocumentBuilder().parse(signatureAsStream);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
