package se.arsredovisning_online.signature_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.io.InputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Adapted from https://docs.oracle.com/javase/8/docs/technotes/guides/security/xmldsig/Validate.java
 */
public class BankIdSignatureValidator {
    public static String BANKID_ROOT_CERT_TEST = "MIIF0jCCA7qgAwIBAgIISpGbuE9LL/0wDQYJKoZIhvcNAQENBQAwbTEkMCIGA1UE\nCgwbRmluYW5zaWVsbCBJRC1UZWtuaWsgQklEIEFCMR8wHQYDVQQLDBZCYW5rSUQg\nTWVtYmVyIEJhbmtzIENBMSQwIgYDVQQDDBtUZXN0IEJhbmtJRCBSb290IENBIHYx\nIFRlc3QwHhcNMTEwOTIyMTQwMTMzWhcNMzQxMjMxMTQwMTMzWjBtMSQwIgYDVQQK\nDBtGaW5hbnNpZWxsIElELVRla25payBCSUQgQUIxHzAdBgNVBAsMFkJhbmtJRCBN\nZW1iZXIgQmFua3MgQ0ExJDAiBgNVBAMMG1Rlc3QgQmFua0lEIFJvb3QgQ0EgdjEg\nVGVzdDCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBANPXoOB9BQOW8i2C\nKk7U/d8rFNB0ktVlcgBSh8CKvnTsW3i+NrAM5LY9jgAO9vkHT3bl3nK626zePhmh\ndhVXMKAanbcF/NJ/oSF+DKCGx/VgPmCCqVyTMLjID/59diiLg3xNH3NaaBM69qnw\n5yOCYkB2wXxcATLO0eTxvL0vdKGJ2HU2AcEtaMMxrScuNCztPuwjYNP0KrYI+y/J\nGkf2dBhomAhDLdQSSW3zXqYgbQvJ8La2ECgo3rGQQRZG9/5MZ5dOWtpAx0ybeCbh\nCPO8XIBCHrPZxv60gZK1CTwlZUoMTBSivv+vmFrH8JdmUnOP9e/wNhuM9/fQ0h5t\n4BGXoz8M5nxdH6uNJG5SpdxaXYflezBb7YdjgNiF9Yqo3DYTRrZT7dyRLYqlmKQh\nT1pqEov1tkXktQF8r1QJkTJO3x1QEzMNCnHyN8iDOqENSE4nhkzU9ESbXNOhFpnc\nXJqoFwvbeAJpV7fVwn+Jumyc/zsD9t+1Vo1lM95q1geVPfnA5z7NZ+uaayJx4DhL\nMvufDI17fqgiWHe+BMA/vGd8OjFK3JUmCV+7QeG/Z3JWbzU0GeDljqO+H4CQ0+LO\n4E4JGEZtxfUu4/XuOkCqiZ4/shoPOOxaXcZlBEMHsDzei0tNSKIxB+PoDTje/BQC\nlunVZvjcG2ehpeF540EXgzzECaNLAgMBAAGjdjB0MB0GA1UdDgQWBBRK96NqCNoI\nOBcZUyjI2qbWNNhaujAPBgNVHRMBAf8EBTADAQH/MB8GA1UdIwQYMBaAFEr3o2oI\n2gg4FxlTKMjaptY02Fq6MBEGA1UdIAQKMAgwBgYEKgMEBTAOBgNVHQ8BAf8EBAMC\nAQYwDQYJKoZIhvcNAQENBQADggIBAJVcP9Sm2tukKW0Qx8EZG9gdXfCmNMrHXF3g\nvia5zpuSMl9wdXHd1FPdGFshRZJ2sW4mb9vRI81vBIXMFVtLZFzeGHoKyz1g8hfj\nuuLKpItw0OwVNdvSRq/TKKxjVKpvt50Eydgnz4Q59YkFlGVyi7+z74mGfvN06Ssj\n2WIRtr3UD+IC6Tie6Lm/zuZs4gu0ZP/fddKh7gC3syHLNXQmN+9Y0wkdO7H98K/9\nuuIrxWtSOFVatxesw7XJRnq+uYI0IdP8xP8U4S680rTse7nsTguQxzRs2vOyoaXm\nFdf7XQ03btd15Z4yJlEfs9/4ohgafMs49PMkACqyX45/4WBygO0QwMGVIUnKNFBt\n/I+0T2SkWFa2JdcRCSTObb7tesoeTIPgI9UcrMvNOG3gxGpB/H5/s7jTV0AOoDgM\nhOxieGgyTsZ3oP0k6bc47FJ4nE+vifAluyeXioB5JaN2kvm8eqfzC05zSF40V9GA\nzElVDbsBPR/2CE6CMyR+eqip4gDSZ6mnZYPeBecEXU4Xu+RAgqYxjKosfxOpMZsN\n+2BSm5QSRLhHacPQTnoQxujnGuUzh5TdAbWqmS0cKEZJ+CACmVLyOphdRoeEQCqQ\n8DYAyOtq2S4+hAJW+2Xq4NCdvmjm99r2RFkibSlLtqctj1JyzUC6huUiQXx9KZ8n\nFA0TsFHG\n";
    public static String BANKID_ROOT_CERT = "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";

    private final List<String> validationErrors = new ArrayList<>();
    private InputStream signatureFile;
    private Document document;
    private boolean test;
    private Logger logger = LogManager.getLogger(BankIdSignatureValidator.class);

    public BankIdSignatureValidator(Document document) {
        this(document, false);
    }

    public BankIdSignatureValidator(Document document, boolean test) {
        this.document = document;
        this.test = test;
        hackDocumentIdAttribute(document);
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }

    public boolean validate() {
        try {
            return validateSignature();
        } catch (Exception e) {
            addError(e.getMessage());
            return false;
        }
    }

    private boolean validateSignature() throws MarshalException, XMLSignatureException {
        Node signatureNode = getSignatureElement(document);
        XMLSignatureFactory fac = getSignatureFactory();
        DOMValidateContext valContext = getValidateContext(signatureNode);
        XMLSignature signature = getSignature(fac, valContext);

        boolean isValid = signature.validate(valContext);

        if (!isValid) {
            createErrorInfo(valContext, signature);
        }
        if (isValid && validationErrors.isEmpty()) {
            logger.debug("Signaturen är giltig.");
            return true;
        } else {
            logger.debug("Signaturen är ogiltig.");
            return false;
        }
    }

    private Node getSignatureElement(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new RuntimeException("Hittar inte <Signature>-elementet i filen.");
        }
        return nl.item(0);
    }

    private XMLSignatureFactory getSignatureFactory() {
        return XMLSignatureFactory.getInstance("DOM");
    }

    private DOMValidateContext getValidateContext(Node signatureNode) {
        return new DOMValidateContext(new CertificatePublicKeySelector(), signatureNode);
    }

    private XMLSignature getSignature(XMLSignatureFactory fac, DOMValidateContext valContext) throws MarshalException {
        return fac.unmarshalXMLSignature(valContext);
    }

    private void createErrorInfo(DOMValidateContext valContext, XMLSignature signature) throws XMLSignatureException {
        if (!signature.getSignatureValue().validate(valContext)) {
            addError("Signaturen är ogiltig.");
        }

        List refs = signature.getSignedInfo().getReferences();
        for (Object item : refs) {
            Reference reference = (Reference) item;

            if (!reference.validate(valContext)) {
                validationErrors.add("Referens med uri \"" + reference.getURI() + "\" är inte giltig.");
            }
        }
    }

    private boolean addError(String message) {
        logger.debug(message);
        return validationErrors.add(message);
    }

    private void hackDocumentIdAttribute(Document document) {
        // Horrible hack to avoid "Cannot resolve element with ID bidSignedData" error
        // See https://stackoverflow.com/questions/17331187/xml-dig-sig-error-after-upgrade-to-java7u25
        Element bidSignedData = findSignedData(document);
        Attr idAttr = bidSignedData.getAttributeNode("Id");
        bidSignedData.setIdAttributeNode(idAttr, true);
    }

    private Element findSignedData(Document doc) {
        return (Element) doc.getElementsByTagName("bankIdSignedData").item(0);
    }

    private Certificate getRootCertificate() throws CertificateException {
        if (test) {
            logger.debug("Använder BankID:s rotcertifikat för testmiljön.");
        }
        byte[] bytes = test ? Base64.getMimeDecoder().decode(BANKID_ROOT_CERT_TEST) : Base64.getDecoder().decode(BANKID_ROOT_CERT);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return certificateFactory.generateCertificate(new java.io.ByteArrayInputStream(bytes));
    }

    private class CertificatePublicKeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("KeyInfo saknas.");
            }

            X509Certificate firstCertificate = null;
            X509Certificate prevCertificate = null;

            for (Object content : keyInfo.getContent()) {
                XMLStructure xmlStructure = (XMLStructure) content;
                if (xmlStructure instanceof X509Data) {
                    List x509Content = ((X509Data) xmlStructure).getContent();
                    for (Object item : x509Content) {
                        if (item instanceof X509Certificate) {
                            X509Certificate cert = ((X509Certificate) item);
                            if (firstCertificate == null) {
                                firstCertificate = cert;
                            }
                            if (prevCertificate != null) {
                                try {
                                    prevCertificate.verify(cert.getPublicKey());
                                } catch (Exception e) {
                                    addError("Certifikatkedjan är inte giltig.");
                                }
                            }
                            prevCertificate = cert;
                        }
                    }
                }
            }

            if (prevCertificate != null) {
                try {
                    prevCertificate.verify(getRootCertificate().getPublicKey());
                } catch (Exception e) {
                    addError("Certifikatkedjan är inte giltig.");
                }
            }

            if (firstCertificate != null) {
                return new SimpleKeySelectorResult(firstCertificate.getPublicKey());
            } else {
                throw new KeySelectorException("Hittar inget certifikat.");
            }
        }
    }

    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private PublicKey pk;

        SimpleKeySelectorResult(PublicKey pk) {
            this.pk = pk;
        }

        public Key getKey() {
            return pk;
        }
    }
}
