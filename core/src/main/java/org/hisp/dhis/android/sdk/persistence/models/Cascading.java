package org.hisp.dhis.android.sdk.persistence.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

import org.hisp.dhis.android.sdk.persistence.Dhis2Database;

/**
 * Created by Sourabh Bhardwaj on 11-03-2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Table(databaseName = Dhis2Database.NAME)
public class Cascading extends BaseMetaDataObject {

    @JsonProperty("district")
    @Column(name = "district")
    String district;

    @JsonProperty("taluk")
    @Column(name = "taluk")
    String taluk;

    @JsonProperty("village")
    @Column(name = "village")
    String village;

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTaluk() {
        return taluk;
    }

    public void setTaluk(String taluk) {
        this.taluk = taluk;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    @JsonProperty("habitat")

    @Column(name = "habitat")
    String habitat;
}