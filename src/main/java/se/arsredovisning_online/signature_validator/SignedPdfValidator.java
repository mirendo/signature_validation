package se.arsredovisning_online.signature_validator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;

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
            logger.info("Validerar bifogade signaturer mot bifogade original.");
            Manifest manifest = extractManifest(document);
            if (manifest != null) {
                for (Manifest.Signature signature : manifest.getSignatures()) {
                    String visibleDataFilename = signature.getVisibleData();
                    String nonVisibleDataFilename = signature.getNonVisibleData();
                    String signatureFilename = signature.getSignatureFile();

                    logger.info("Validerar " + signatureFilename);

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
                        addError("Validering av " + signatureFilename + " misslyckades.");
                        validationErrors.addAll(signatureValidator.getValidationErrors());
                    }
                }
            }
        } catch (IOException e) {
            addError(e.getMessage());
        }
    }

    private void addError(String message) {
        validationErrors.add(message);
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
        logger.info("Vi har ännu inte implementerat validering av PDF-filens signatur.");
        // TODO: Validate
    }

    private Manifest extractManifest(PDDocument document) {
        logger.debug("Läser innehållsförteckning.");
        COSInputStream manifestStream = getEmbeddedFileAsStream(document, "manifest.json");
        if (manifestStream != null) {
            return Manifest.createFromStream(manifestStream);
        } else {
            validationErrors.add("PDF:en har ingen bifogad innehållsförteckning (manifest.json).");
            return null;
        }
    }

    private COSInputStream getEmbeddedFileAsStream(PDDocument document, String filename) {
        PDEmbeddedFilesNameTreeNode embeddedFiles = document.getDocumentCatalog().getNames().getEmbeddedFiles();
        if (embeddedFiles == null) {
            validationErrors.add("PDF:en innehåller inga bifogade filer.");
        } else {
            try {
                PDComplexFileSpecification manifestFileSpecification = embeddedFiles.getNames().get(filename);
                return manifestFileSpecification.getEmbeddedFile().createInputStream();
            } catch (IOException e) {
                validationErrors.add("Misslyckades med att läsa bifogade filer.");
                validationErrors.add(e.getMessage());
            }
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        if (argList.contains("-v")) {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration config = ctx.getConfiguration();
            LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
            loggerConfig.setLevel(Level.DEBUG);
            ctx.updateLoggers();
            argList.remove("-v");
        }

        boolean test = false;
        if (argList.contains("-t")) {
            test = true;
            argList.remove("-t");
        }

        if (argList.size() != 1) {
            System.out.println("Användning: ");
            System.out.println(SignedPdfValidator.class.getCanonicalName() + " [-v] pdf-file");
            System.out.println("   alternativt ");
            System.out.println("mvn run [-v] pdf-file");
            exit(1);
        }

        SignedPdfValidator validator = new SignedPdfValidator(new FileInputStream(argList.get(0)), test);
        if (validator.validate()) {
            System.out.println("Signaturerna stämmer.");
        } else {
            System.out.println("Signaturerna stämmer inte.");
        }
    }
}
