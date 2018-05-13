package org.hisp.dhis.android.sdk.persistence.models.FileResourceResponseModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileResourceResponse {
    @JsonProperty
    private String responseType;

    @JsonProperty
    private FileResource fileResource;

    public String getResponseType() {
        return responseType;
    }

    public FileResource getFileResource() {
        return fileResource;
    }
}
