package se.arsredovisning_online.signature_validator;

import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AOSignedPdfValidator {
    private final PDDocument document;
    private final boolean test;
    private List<String> validationErrors = new ArrayList<>();

    public AOSignedPdfValidator(InputStream pdf) throws IOException {
        this(pdf, false);
    }

    public AOSignedPdfValidator(InputStream pdf, boolean test) throws IOException {
        this.document = PDDocument.load(pdf);
        this.test = test;
    }

    public boolean validate() {
        validateSeal();
        validateSignatures();
        return validationErrors.isEmpty();
    }

    private void validateSignatures() {
        Manifest manifest = extractManifest();
        if (manifest != null) {
            for (Manifest.Signature signature : manifest.getSignatures()) {
                String visibleDataFilename = signature.getVisibleData();
                String nonVisibleDataFilename = signature.getNonVisibleData();
                String signatureFilename = signature.getSignatureFile();

                InputStream visibleDataStream = getEmbeddedFileAsStream(visibleDataFilename);
                InputStream nonVisibleDataStream = getEmbeddedFileAsStream(nonVisibleDataFilename);
                InputStream signatureStream = getEmbeddedFileAsStream(signatureFilename);

                AOSingleSignatureValidator signatureValidator = new AOSingleSignatureValidator(
                        visibleDataStream,
                        getDigestMethod(visibleDataFilename, manifest),
                        nonVisibleDataStream,
                        getDigestMethod(nonVisibleDataFilename, manifest),
                        signatureStream, test);
                if (!signatureValidator.validate()) {
                    validationErrors.add("Validation of signature " + signatureFilename + " failed.");
                    validationErrors.addAll(signatureValidator.getValidationErrors());
                }
            }
        }
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
        // TODO: Validate
    }

    private Manifest extractManifest() {
        COSInputStream manifestStream = getEmbeddedFileAsStream("manifest.json");
        if (manifestStream != null) {
            return Manifest.createFromStream(manifestStream);
        } else {
            validationErrors.add("PDF does not contain manifest file.");
            return null;
        }
    }

    private COSInputStream getEmbeddedFileAsStream(String filename) {
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
