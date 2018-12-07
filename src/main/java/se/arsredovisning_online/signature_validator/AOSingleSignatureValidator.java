package se.arsredovisning_online.signature_validator;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;

public class AOSingleSignatureValidator {
    private final InputStream visibleData;
    private final String visibleDataDigestMethod;
    private final InputStream nonVisibleData;
    private final String nonVisibleDataDigestMethod;
    private final InputStream signature;
    private boolean test;

    public AOSingleSignatureValidator(InputStream visibleData, String visibleDataDigestMethod, InputStream nonVisibleData, String nonVisibleDataDigestMethod, InputStream signature) {
        this(visibleData, visibleDataDigestMethod, nonVisibleData, nonVisibleDataDigestMethod, signature, false);
    }

    public AOSingleSignatureValidator(InputStream visibleData, String visibleDataDigestMethod, InputStream nonVisibleData, String nonVisibleDataDigestMethod, InputStream signature, boolean test) {
        this.visibleData = visibleData;
        this.visibleDataDigestMethod = visibleDataDigestMethod;
        this.nonVisibleData = nonVisibleData;
        this.nonVisibleDataDigestMethod = nonVisibleDataDigestMethod;
        this.signature = signature;
        this.test = test;
    }

    public boolean validate() {
        String visibleDataDigest = DigestMaker.getEncodedDigest(visibleData, visibleDataDigestMethod);
        String nonVisibleDataDigest = DigestMaker.getEncodedDigest(nonVisibleData, nonVisibleDataDigestMethod);
        Document signatureDocument = parseSignatureFile(signature);

        String visibleDataFromSignatureFile = getXPath(signatureDocument, "//*[local-name()='Signature']/*[local-name()='Object']/*[local-name()='bankIdSignedData']/*[local-name()='usrVisibleData']/text()");
        String nonVisibleDataFromSignatureFile = getXPath(signatureDocument, "//*[local-name()='Signature']/*[local-name()='Object']/*[local-name()='bankIdSignedData']/*[local-name()='usrNonVisibleData']/text()");

        return visibleDataDigest.equals(visibleDataFromSignatureFile)
                && nonVisibleDataDigest.equals(nonVisibleDataFromSignatureFile)
                && new BankIdSignatureValidator(signatureDocument, test).validate();
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
}
