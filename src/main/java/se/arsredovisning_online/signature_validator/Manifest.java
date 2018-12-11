package se.arsredovisning_online.signature_validator;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Manifest {
    private final List<Document> documents = new ArrayList<>();
    private final List<Signature> signatures = new ArrayList<>();

    public static Manifest createFromStream(InputStream json) {
        return new Gson().fromJson(new InputStreamReader(json), Manifest.class);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    public Document getDocumentByName(String name) {
        Optional<Document> optionalDocument = documents.stream().filter((document) -> document.name.equals(name)).findFirst();
        return optionalDocument.orElse(null);
    }

    public static class Document {
        private String name;
        private String type;
        private String digest_method;

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getDigestMethod() {
            return digest_method;
        }
    }

    public static class Signature {
        private String signature_file;
        private String visible_data;
        private String non_visible_data;

        public String getSignatureFile() {
            return signature_file;
        }

        public String getVisibleData() {
            return visible_data;
        }

        public String getNonVisibleData() {
            return non_visible_data;
        }
    }
}
