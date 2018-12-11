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

public class SingleSignatureValidator {
    private final InputStream visibleData;
    private final String visibleDataDigestMethod;
    private final InputStream nonVisibleData;
    private final String nonVisibleDataDigestMethod;
    private boolean test;
    private List<String> validationErrors = new ArrayList<>();
    private final Document signatureDocument;
    private Logger logger = LogManager.getLogger(SingleSignatureValidator.class);

    public SingleSignatureValidator(InputStream visibleData, String visibleDataDigestMethod, InputStream nonVisibleData, String nonVisibleDataDigestMethod, InputStream signature) {
        this(visibleData, visibleDataDigestMethod, nonVisibleData, nonVisibleDataDigestMethod, signature, false);
    }

    public SingleSignatureValidator(InputStream visibleData, String visibleDataDigestMethod, InputStream nonVisibleData, String nonVisibleDataDigestMethod, InputStream signature, boolean test) {
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
                "Synligt data (\"user visible data\") i separat fil matchar inte signaturfilen.")) {
            logger.debug("Synligt data (\"user visible data\") i separat fil matchar signaturfilen.");
        }
    }

    private void checkNonVisibleData() {
        if (compareData(
                nonVisibleData,
                nonVisibleDataDigestMethod,
                "//*[local-name()='Signature']/*[local-name()='Object']/*[local-name()='bankIdSignedData']/*[local-name()='usrNonVisibleData']/text()",
                "Osynligt data (\"non-visible data\") i separat fil matchar inte signaturfilen.")) {
            logger.debug("Osynligt data (\"non-visible data\") i separat fil matchar signaturfilen.");
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
