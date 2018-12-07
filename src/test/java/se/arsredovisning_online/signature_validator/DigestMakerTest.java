package se.arsredovisning_online.signature_validator;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class DigestMakerTest {
    @Test
    public void base64EncodesPlaintext() {
        String input = "abc123";
        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        assertEquals("YWJjMTIz", DigestMaker.getEncodedDigest(stream, "plaintext"));
    }

    @Test
    public void encodesUsingSha256() {
        String input = "abc123";
        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        assertEquals("bKE9UspwyIPg8LsQHkJaiehiTeUdstI5JZOvaoQRgJA=", DigestMaker.getEncodedDigest(stream, "sha256"));
    }
}