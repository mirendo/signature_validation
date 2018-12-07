package se.arsredovisning_online.signature_validator;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DigestMaker {
    public static String getEncodedDigest(InputStream data, String digestMethod) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] bytes = getDigestBytes(IOUtils.toByteArray(data), digestMethod);
            return encoder.encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] getDigestBytes(byte[] bytes, String digestMethod) {
        switch (digestMethod) {
            case "plaintext":
                return bytes;
            case "sha256":
                return getSha256Digest(bytes);
            default:
                throw new RuntimeException("Unhandled digest method: " + digestMethod);
        }
    }

    private static byte[] getSha256Digest(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
