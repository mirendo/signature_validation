package se.arsredovisning_online.signature_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AOSingleSignatureValidator {
    private final InputStream visibleData;
    private final String visibleDataDigestMethod;
    private final InputStream nonVisibleData;
    private final String nonVisibleDataDigestMethod;
    private boolean test;
    private List<String> validationErrors = new ArrayList<>();
    private final Document signatureDocument;
    private Logger logger = LogManager.getLogger(AOSingleSignatureValidator.class);

    public AOSingleSignatureValidator(InputStream visibleData, String visibleDataDigestMethod, InputStream nonVisibleData, String nonVisibleDataDigestMethod, InputStream signature) {
        this(visibleData, visibleDataDigestMethod, nonVisibleData, nonVisibleDataDigestMethod, signature, false);
    }

    public AOSingleSignatureValidator(InputStream visibleData, String visibleDataDigestMethod, InputStream nonVisibleData, String nonVisibleDataDigestMethod, InputStream signature, boolean test) {
        this.visibleData = visibleData;
        this.visibleDataDigestMethod = visibleDataDigestMethod;
        this.nonVisibleData = nonVisibleData;
        this.nonVisibleDataDigestMethod = nonVisibleDataDigestMethod;
        this.test = test;
        this.signatureDocument = parseSignatureFile(signature);
    }

    public boolean validate() {
        checkVisibleData();
        checkNonVisibleData();
        validateSignature();

        return validationErrors.isEmpty();
    }

    private void validateSignature() {
        BankIdSignatureValidator validator = new BankIdSignatureValidator(signatureDocument, test);
        if (!validator.validate()) {
            validationErrors.addAll(validator.getValidationErrors());
        }
    }

    private void checkVisibleData() {
        if (compareData(
                visibleData,
                visibleDataDigestMethod,
                "//*[local-name()='Signature']/*[local-name()='Object']/*[local-name()='bankIdSignedData']/*[local-name()='usrVisibleData']/text()",
                "User visible data in external file does not match signature file.")) {
            logger.debug("User visible data in external file matches signature file.");
        }
    }

    private void checkNonVisibleData() {
        if (compareData(
                nonVisibleData,
                nonVisibleDataDigestMethod,
                "//*[local-name()='Signature']/*[local-name()='Object']/*[local-name()='bankIdSignedData']/*[local-name()='usrNonVisibleData']/text()",
                "Non-visible data in external file does not match signature file.")) {
            logger.debug("Non-visible data in external file matches signature file.");
        }
    }

    private boolean compareData(InputStream nonVisibleData, String nonVisibleDataDigestMethod, String xPath, String errorMessage) {
        String nonVisibleDataDigest = DigestMaker.getEncodedDigest(nonVisibleData, nonVisibleDataDigestMethod);
        String nonVisibleDataFromSignatureFile = getXPath(signatureDocument, xPath);
        boolean valid = nonVisibleDataDigest.equals(nonVisibleDataFromSignatureFile);
        if (!valid) {
            validationErrors.add(errorMessage);
            logger.debug(errorMessage);
        }
        return valid;
    }

    private String getXPath(Document document, String path) {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            return xPath.evaluate(path, document);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private static Document parseSignatureFile(InputStream signatureAsStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        try {
            return dbf.newDocumentBuilder().parse(signatureAsStream);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
