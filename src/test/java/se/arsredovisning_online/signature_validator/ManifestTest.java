package se.arsredovisning_online.signature_validator;

import org.junit.Test;

import static org.junit.Assert.*;

public class ManifestTest {
    @Test
    public void readsManifestFromJson() {
        Manifest manifest = Manifest.createFromStream(TestUtil.getFixtureFile("/manifest.json"));
        assertFalse(manifest.getDocuments().isEmpty());
        assertFalse(manifest.getSignatures().isEmpty());
    }

    @Test
    public void readsDocuments() {
        Manifest manifest = Manifest.createFromStream(TestUtil.getFixtureFile("/manifest.json"));
        Manifest.Document firstDocument = manifest.getDocuments().get(0);
        assertEquals("styrelseledamot_vd_visible_data.txt", firstDocument.getName());
        assertEquals("board_ceo_visible_data", firstDocument.getType());
        assertEquals("plaintext", firstDocument.getDigestMethod());
    }

    @Test
    public void readsSignatures() {
        Manifest manifest = Manifest.createFromStream(TestUtil.getFixtureFile("/manifest.json"));
        Manifest.Signature firstSignature = manifest.getSignatures().get(0);
        assertEquals("signatur_1_Anna_Andersson.xml", firstSignature.getSignatureFile());
        assertEquals("styrelseledamot_vd_visible_data.txt", firstSignature.getVisibleData());
        assertEquals("original_årsredovisning.xhtml", firstSignature.getNonVisibleData());
    }
}