package se.arsredovisning_online.signature_validator;

import org.junit.Test;

import java.io.InputStream;

import static junit.framework.TestCase.assertTrue;

public class AOSingleSignatureValidatorTest {
    @Test
    public void validatesSignature() {
        InputStream original = TestUtil.getFixtureFile("/original_årsredovisning.xhtml");
        InputStream visibleData = TestUtil.getFixtureFile("/styrelseledamot_vd_visible_data.txt");
        InputStream signature = TestUtil.getFixtureFile("/signatur_1_Anna_Andersson.xml");
        AOSingleSignatureValidator validator = new AOSingleSignatureValidator(visibleData, "plaintext", original, "sha256", signature, true);

        assertTrue(validator.validate());
    }
}
