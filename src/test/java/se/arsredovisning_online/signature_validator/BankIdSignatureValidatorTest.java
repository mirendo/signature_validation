package se.arsredovisning_online.signature_validator;

import org.junit.Test;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class BankIdSignatureValidatorTest {
    @Test
    public void acceptsValidSignatureFile() {
        InputStream signatureFile = this.getClass().getResourceAsStream("/signatur_1_Anna_Andersson.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertTrue(validator.validate());
    }

    @Test
    public void rejectsInvalidSignatureFile() {
        InputStream signatureFile = this.getClass().getResourceAsStream("/invalid_signature.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
    }

    @Test
    public void givesErrorMessageRegardingInvalidSignature() {
        InputStream signatureFile = this.getClass().getResourceAsStream("/invalid_signature.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Signature is not valid."));
    }

    @Test
    public void givesErrorMessageRegardingInvalidKeyInfo() {
        InputStream signatureFile = this.getClass().getResourceAsStream("/invalid_key_info.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Reference with uri \"#bidKeyInfo\" is not valid."));
    }

    @Test
    public void givesErrorMessageRegardingInvalidSignedData() {
        InputStream signatureFile = this.getClass().getResourceAsStream("/invalid_signed_data.xml");
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureFile);
        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Reference with uri \"#bidSignedData\" is not valid."));
    }
}