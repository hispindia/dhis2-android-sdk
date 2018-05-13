package org.hisp.dhis.android.sdk.persistence.models.FileResourceResponseModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileResource {
    @JsonProperty
    private String name;

    @JsonProperty
    private String id;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
