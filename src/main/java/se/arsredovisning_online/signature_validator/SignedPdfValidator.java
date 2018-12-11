package se.arsredovisning_online.signature_validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignedPdfValidator {
    private InputStream pdf;
    private final boolean test;
    private List<String> validationErrors = new ArrayList<>();
    private Logger logger = LogManager.getLogger(SignedPdfValidator.class);

    public SignedPdfValidator(InputStream pdf) throws IOException {
        this(pdf, false);
    }

    public SignedPdfValidator(InputStream pdf, boolean test) throws IOException {
        this.pdf = pdf;
        this.test = test;
    }

    public boolean validate() {
        validateSeal();
        validateSignatures();
        return validationErrors.isEmpty();
    }

    private void validateSignatures() {
        try (PDDocument document = PDDocument.load(pdf)) {
            logger.info("Validating embedded signatures.");
            Manifest manifest = extractManifest(document);
            if (manifest != null) {
                for (Manifest.Signature signature : manifest.getSignatures()) {
                    String visibleDataFilename = signature.getVisibleData();
                    String nonVisibleDataFilename = signature.getNonVisibleData();
                    String signatureFilename = signature.getSignatureFile();

                    logger.info("Validating " + signatureFilename);

                    InputStream visibleDataStream = getEmbeddedFileAsStream(document, visibleDataFilename);
                    InputStream nonVisibleDataStream = getEmbeddedFileAsStream(document, nonVisibleDataFilename);
                    InputStream signatureStream = getEmbeddedFileAsStream(document, signatureFilename);

                    SingleSignatureValidator signatureValidator = new SingleSignatureValidator(
                            visibleDataStream,
                            getDigestMethod(visibleDataFilename, manifest),
                            nonVisibleDataStream,
                            getDigestMethod(nonVisibleDataFilename, manifest),
                            signatureStream, test);
                    if (!signatureValidator.validate()) {
                        addError(signatureFilename);
                        validationErrors.addAll(signatureValidator.getValidationErrors());
                    }
                }
            }
        } catch (IOException e) {
            addError(e.getMessage());
        }
    }

    private void addError(String message) {
        validationErrors.add("Validation of signature " + message + " failed.");
    }

    private String getDigestMethod(String visibleDataFilename, Manifest manifest) {
        Manifest.Document document = manifest.getDocumentByName(visibleDataFilename);
        if (document != null) {
            return document.getDigestMethod();
        } else {
            return "";
        }
    }

    private void validateSeal() {
        logger.info("PDF signature validation not yet implemented.");
        // TODO: Validate
    }

    private Manifest extractManifest(PDDocument document) {
        logger.debug("Reading manifest file");
        COSInputStream manifestStream = getEmbeddedFileAsStream(document, "manifest.json");
        if (manifestStream != null) {
            return Manifest.createFromStream(manifestStream);
        } else {
            validationErrors.add("PDF does not contain manifest file.");
            return null;
        }
    }

    private COSInputStream getEmbeddedFileAsStream(PDDocument document, String filename) {
        PDEmbeddedFilesNameTreeNode embeddedFiles = document.getDocumentCatalog().getNames().getEmbeddedFiles();
        if (embeddedFiles == null) {
            validationErrors.add("No attachments in pdf.");
        } else {
            try {
                PDComplexFileSpecification manifestFileSpecification = embeddedFiles.getNames().get(filename);
                return manifestFileSpecification.getEmbeddedFile().createInputStream();
            } catch (IOException e) {
                validationErrors.add("Error extracting attchments from pdf.");
                validationErrors.add(e.getMessage());
            }
        }
        return null;
    }
}
