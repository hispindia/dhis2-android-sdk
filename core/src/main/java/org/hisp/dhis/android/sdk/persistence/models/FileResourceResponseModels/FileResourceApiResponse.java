package org.hisp.dhis.android.sdk.persistence.models.FileResourceResponseModels;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileResourceApiResponse {
    @JsonProperty
    private String httpStatus;

    @JsonProperty
    private String httpStatusCode;

    @JsonProperty
    private String status;

    @JsonProperty
    private FileResourceResponse response;

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public FileResourceResponse getResponse() {
        return response;
    }

    public void setResponse(FileResourceResponse response) {
        this.response = response;
    }
}
