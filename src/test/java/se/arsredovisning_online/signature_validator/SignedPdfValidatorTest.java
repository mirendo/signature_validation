package se.arsredovisning_online.signature_validator;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class SignedPdfValidatorTest {
    @Test
    public void validatesSealedDocument() throws IOException {
        InputStream pdf = TestUtil.getFixtureFile("/Årsredovisning (signerat original) - Demobolaget AB - räkenskapsåret 2017.pdf");
        assertTrue(new SignedPdfValidator(pdf, true).validate());
    }

    @Test
    public void validatesAuditReport() throws IOException {
        InputStream pdf = TestUtil.getFixtureFile("/Revisionsberättelse (signerat original) - Demo med revision AB - räkenskapsåret 2017.pdf");
        assertTrue(new SignedPdfValidator(pdf, true).validate());
    }

    @Test
    public void validatesAuditorEndorsedReport() throws IOException {
        InputStream pdf = TestUtil.getFixtureFile("/Årsredovisning (signerat original) - Demo med revision AB - räkenskapsåret 2017.pdf");
        assertTrue(new SignedPdfValidator(pdf, true).validate());
    }

    @Test
    public void doesNotValidateSealedDocumentWithProdCert() throws IOException {
        InputStream pdf = TestUtil.getFixtureFile("/Årsredovisning (signerat original) - Demobolaget AB - räkenskapsåret 2017.pdf");
        // Use root cert for production to force failure (files are created using test cert)
        assertFalse(new SignedPdfValidator(pdf).validate());
    }
}