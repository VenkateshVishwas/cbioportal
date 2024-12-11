package org.cbioportal.file.model;

public record ClinicalSampleAttributesMetadata(
    String cancerStudyIdentifier,
    String dataFilename
) implements GenericDatatypeFileMetadata {
    public String geneticAlterationType() {
        return "CLINICAL";
    }

    public String datatype() {
        return "SAMPLE_ATTRIBUTES";
    }
}
