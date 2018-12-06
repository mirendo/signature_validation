package se.arsredovisning_online.signature_validator;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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
    private final ArrayList<String> validationErrors;
    private InputStream signatureFile;

    public BankIdSignatureValidator(InputStream signatureFile) {
        this.signatureFile = signatureFile;
        this.validationErrors = new ArrayList<>();
    }

    public boolean validate() {
        try {
            Document doc = parseSignatureFile();
            Node signatureNode = getSignatureElement(doc);

            XMLSignatureFactory fac = getSignatureFactory();
            DOMValidateContext valContext = getValidateContext(signatureNode);
            XMLSignature signature = getSignature(fac, valContext);

            // Validate the XMLSignature (generated above)
            boolean isValid = signature.validate(valContext);

            // Check core validation status
            if (!isValid) {
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

            return isValid;
        } catch (Exception e) {
            validationErrors.add(e.getMessage());
            return false;
        }
    }

    public ArrayList<String> getValidationErrors() {
        return validationErrors;
    }

    private XMLSignature getSignature(XMLSignatureFactory fac, DOMValidateContext valContext) throws MarshalException {
        return fac.unmarshalXMLSignature(valContext);
    }

    private DOMValidateContext getValidateContext(Node signatureNode) {
        return new DOMValidateContext(new CertificatePublicKeySelector(), signatureNode);
    }

    private XMLSignatureFactory getSignatureFactory() {
        return XMLSignatureFactory.getInstance("DOM");
    }

    private Node getSignatureElement(Document doc) {
        // Find Signature element
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new RuntimeException("Cannot find Signature element");
        }
        return nl.item(0);
    }

    private Document parseSignatureFile() throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(signatureFile);

        // Horrible hack to avoid "Cannot resolve element with ID bidSignedData" error
        // See https://stackoverflow.com/questions/17331187/xml-dig-sig-error-after-upgrade-to-java7u25
        Element bidSignedData = findSignedData(doc);
        Attr idAttr = bidSignedData.getAttributeNode("Id");
        bidSignedData.setIdAttributeNode(idAttr, true);

        return doc;
    }

    private static Element findSignedData(Document doc) {
        return (Element) doc.getElementsByTagName("bankIdSignedData").item(0);
    }

    private static class CertificatePublicKeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo,
                                        KeySelector.Purpose purpose,
                                        AlgorithmMethod method,
                                        XMLCryptoContext context)
                throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("Null KeyInfo object!");
            }

            for (Object aList : keyInfo.getContent()) {
                XMLStructure xmlStructure = (XMLStructure) aList;
                if (xmlStructure instanceof X509Data) {
                    List x509Content = ((X509Data) xmlStructure).getContent();
                    for (Object o : x509Content) {
                        if (o instanceof X509Certificate) {
                            X509Certificate cert = ((X509Certificate) o);
                            return new SimpleKeySelectorResult((cert.getPublicKey()));
                        }
                    }
                }

            }
            throw new KeySelectorException("No certificate found");
        }

        static boolean algEquals(String algURI, String algName) {
            if (algName.equalsIgnoreCase("DSA") &&
                    algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1)) {
                return true;
            } else if (algName.equalsIgnoreCase("RSA") &&
                    algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1)) {
                return true;
            } else {
                return false;
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
