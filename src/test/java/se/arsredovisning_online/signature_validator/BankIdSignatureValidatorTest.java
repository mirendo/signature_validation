package se.arsredovisning_online.signature_validator;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class BankIdSignatureValidatorTest {
    @Test
    public void acceptsValidSignatureFile() {
        Document signatureFile = TestUtil.getSignatureDocument("/signatur_1_Anna_Andersson.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile, true);
        assertTrue(validator.validate());
    }

    @Test
    public void rejectsInvalidSignatureFile() {
        Document signatureFile = TestUtil.getSignatureDocument("/invalid_signature.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile, true);
        assertFalse(validator.validate());
    }

    @Test
    public void givesErrorMessageRegardingInvalidSignature() {
        Document signatureFile = TestUtil.getSignatureDocument("/invalid_signature.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile, true);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Signature is not valid."));
    }

    @Test
    public void givesErrorMessageRegardingInvalidKeyInfo() {
        Document signatureFile = TestUtil.getSignatureDocument("/invalid_key_info.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile, true);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Reference with uri \"#bidKeyInfo\" is not valid."));
    }

    @Test
    public void givesErrorMessageRegardingInvalidSignedData() {
        Document signatureFile = TestUtil.getSignatureDocument("/invalid_signed_data.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile, true);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Reference with uri \"#bidSignedData\" is not valid."));
    }

    @Test
    public void givesErrorRegardingInvalidRootCertificate() {
        Document signatureFile = TestUtil.getSignatureDocument("/signatur_1_Anna_Andersson.xml");
        // Use root cert for production to force failure (files are created using test cert)
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile, false);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Certificate chain is invalid."));
    }
}