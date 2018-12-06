package se.arsredovisning_online.signature_validator;

import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class BankIdSignatureValidatorTest {
    @Test
    public void acceptsValidSignatureFile() {
        Document signatureFile = getSignatureDocument("/signatur_1_Anna_Andersson.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertTrue(validator.validate());
    }

    @Test
    public void rejectsInvalidSignatureFile() {
        Document signatureFile = getSignatureDocument("/invalid_signature.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
    }

    @Test
    public void givesErrorMessageRegardingInvalidSignature() {
        Document signatureFile = getSignatureDocument("/invalid_signature.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Signature is not valid."));
    }

    @Test
    public void givesErrorMessageRegardingInvalidKeyInfo() {
        Document signatureFile = getSignatureDocument("/invalid_key_info.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Reference with uri \"#bidKeyInfo\" is not valid."));
    }

    @Test
    public void givesErrorMessageRegardingInvalidSignedData() {
        Document signatureFile = getSignatureDocument("/invalid_signed_data.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Reference with uri \"#bidSignedData\" is not valid."));
    }

    private Document getSignatureDocument(String filename) {
        return parseSignatureFile(this.getClass().getResourceAsStream(filename));
    }

    private Document parseSignatureFile(InputStream signatureAsStream) {
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