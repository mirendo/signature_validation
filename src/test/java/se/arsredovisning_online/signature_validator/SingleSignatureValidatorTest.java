package se.arsredovisning_online.signature_validator;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class SingleSignatureValidatorTest {
    @Test
    public void acceptsValidSignatureForDocuments() {
        InputStream visibleData = TestUtil.getFixtureFile("/styrelseledamot_vd_visible_data.txt");
        InputStream nonVisibleData = TestUtil.getFixtureFile("/original_årsredovisning.xhtml");
        InputStream signature = TestUtil.getFixtureFile("/signatur_1_Anna_Andersson.xml");
        SingleSignatureValidator validator = new SingleSignatureValidator(visibleData, "plaintext", nonVisibleData, "sha256", signature, true);

        assertTrue(validator.validate());
    }

    @Test
    public void rejectsInvalidVisibleData() throws IOException {
        InputStream visibleData = IOUtils.toInputStream("incorrect visible data", "UTF-8");
        InputStream nonVisibleData = TestUtil.getFixtureFile("/original_årsredovisning.xhtml");
        InputStream signature = TestUtil.getFixtureFile("/signatur_1_Anna_Andersson.xml");
        SingleSignatureValidator validator = new SingleSignatureValidator(visibleData, "plaintext", nonVisibleData, "sha256", signature, true);

        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Synligt data (\"user visible data\") i separat fil matchar inte signaturfilen."));
    }

    @Test
    public void rejectsInvalidNonVisibleData() {
        InputStream visibleData = TestUtil.getFixtureFile("/styrelseledamot_vd_visible_data.txt");
        InputStream nonVisibleData = TestUtil.getFixtureFile("/signatur_1_Anna_Andersson.xml");
        InputStream signature = TestUtil.getFixtureFile("/signatur_1_Anna_Andersson.xml");
        SingleSignatureValidator validator = new SingleSignatureValidator(visibleData, "plaintext", nonVisibleData, "sha256", signature, true);

        assertFalse(validator.validate());
        assertThat(validator.getValidationErrors(), hasItem("Osynligt data (\"non-visible data\") i separat fil matchar inte signaturfilen."));
    }

    @Test
    public void rejectsInvalidSignature() {
        InputStream visibleData = TestUtil.getFixtureFile("/styrelseledamot_vd_visible_data.txt");
        InputStream nonVisibleData = TestUtil.getFixtureFile("/original_årsredovisning.xhtml");
        InputStream signature = TestUtil.getFixtureFile("/signatur_1_Anna_Andersson.xml");
        // Use root cert for production to force failure (files are created using test cert)
        SingleSignatureValidator validator = new SingleSignatureValidator(visibleData, "plaintext", nonVisibleData, "sha256", signature);

        assertFalse(validator.validate());
    }
}
