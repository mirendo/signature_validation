package se.arsredovisning_online.signature_validator;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class AOSignedPdfValidatorTest {
    @Test
    public void validatesSealedDocument() throws IOException {
        InputStream pdf = TestUtil.getFixtureFile("/Årsredovisning (signerat original) - Demobolaget AB - räkenskapsåret 2017.pdf");
        assertTrue(new AOSignedPdfValidator(pdf, true).validate());
    }

    @Test
    public void doesNotValidateSealedDocumentWithProdCert() throws IOException {
        InputStream pdf = TestUtil.getFixtureFile("/Årsredovisning (signerat original) - Demobolaget AB - räkenskapsåret 2017.pdf");
        // Use root cert for production to force failure (files are created using test cert)
        assertFalse(new AOSignedPdfValidator(pdf).validate());
    }
}