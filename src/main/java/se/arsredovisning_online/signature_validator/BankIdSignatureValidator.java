package se.arsredovisning_online.signature_validator;

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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapted from https://docs.oracle.com/javase/8/docs/technotes/guides/security/xmldsig/Validate.java
 */
public class BankIdSignatureValidator {
    private final ArrayList<String> validationErrors = new ArrayList<>();
    private InputStream signatureFile;
    private Document document;

    public BankIdSignatureValidator(Document document) {
        this.document = document;
        hackDocumentIdAttribute(document);
    }

    public ArrayList<String> getValidationErrors() {
        return validationErrors;
    }

    public boolean validate() {
        try {
            Node signatureNode = getSignatureElement(document);
            XMLSignatureFactory fac = getSignatureFactory();
            DOMValidateContext valContext = getValidateContext(signatureNode);
            XMLSignature signature = getSignature(fac, valContext);

            boolean isValid = signature.validate(valContext);

            if (!isValid) {
                createErrorInfo(valContext, signature);
            }

            return isValid;
        } catch (Exception e) {
            validationErrors.add(e.getMessage());
            return false;
        }
    }

    private Node getSignatureElement(Document doc) {
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new RuntimeException("Cannot find Signature element");
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
            validationErrors.add("Signature is not valid.");
        }

        List refs = signature.getSignedInfo().getReferences();
        for (Object item : refs) {
            Reference reference = (Reference) item;

            if (!reference.validate(valContext)) {
                validationErrors.add("Reference with uri \"" + reference.getURI() + "\" is not valid.");
            }
        }
    }

    private void hackDocumentIdAttribute(Document document) {
        // Horrible hack to avoid "Cannot resolve element with ID bidSignedData" error
        // See https://stackoverflow.com/questions/17331187/xml-dig-sig-error-after-upgrade-to-java7u25
        Element bidSignedData = findSignedData(document);
        Attr idAttr = bidSignedData.getAttributeNode("Id");
        bidSignedData.setIdAttributeNode(idAttr, true);
    }

    private static Element findSignedData(Document doc) {
        return (Element) doc.getElementsByTagName("bankIdSignedData").item(0);
    }

    private static class CertificatePublicKeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }

            for (Object content : keyInfo.getContent()) {
                XMLStructure xmlStructure = (XMLStructure) content;
                if (xmlStructure instanceof X509Data) {
                    List x509Content = ((X509Data) xmlStructure).getContent();
                    for (Object item : x509Content) {
                        if (item instanceof X509Certificate) {
                            X509Certificate cert = ((X509Certificate) item);
                            return new SimpleKeySelectorResult((cert.getPublicKey()));
                        }
                    }
                }

            }
            throw new KeySelectorException("No certificate found");
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
