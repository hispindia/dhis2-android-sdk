package org.hisp.dhis.android.sdk.persistence.models;

/**
 * Created by Sourabh Bhardwaj on 21-05-2017.
 */


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import org.hisp.dhis.android.sdk.persistence.Dhis2Database;


@JsonIgnoreProperties(ignoreUnknown = true)
@Table(databaseName = Dhis2Database.NAME)
public class LabReport extends BaseMetaDataObject {

    @JsonProperty("aesepid")
    @Column(name = "aesepid")
    String aesepid;
    @JsonProperty("NIMHANS_AES_ID")
    @Column(name = "NIMHANS_AES_ID")
    String NIMHANS_AES_ID;
    @JsonProperty("pname")
    @Column(name = "pname")
    String pname;
    @JsonProperty("age")
    @Column(name = "age")
    String age;
    //    @JsonProperty("evdate")
//    @Column(name = "evdate")
//    String evdate;
    @JsonProperty("gender")
    @Column(name = "gender")
    String gender;
    @JsonProperty("samplecollected_csf")
    @Column(name = "samplecollected_csf")
    String samplecollected_csf;
    @JsonProperty("samplesenttoapexlab_csf")
    @Column(name = "samplesenttoapexlab_csf")
    String samplesenttoapexlab_csf;
    //    @JsonProperty("name")
//    @Column(name = "name")
//    String name;
    @JsonProperty("samplecollected_serum")
    @Column(name = "samplecollected_serum")
    String samplecollected_serum;
    @JsonProperty("samplesenttoapexlab_serum")
    @Column(name = "samplesenttoapexlab_serum")
    String samplesenttoapexlab_serum;
    @JsonProperty("labresult_csf_wbccount")
    @Column(name = "labresult_csf_wbccount")
    String labresult_csf_wbccount;
    @JsonProperty("samplecollected_wholeblood")
    @Column(name = "samplecollected_wholeblood")
    String samplecollected_wholeblood;
    @JsonProperty("csf_gulucoselevel")
    @Column(name = "csf_gulucoselevel")
    String csf_gulucoselevel;
    @JsonProperty("csf_proteinlevel")
    @Column(name = "csf_proteinlevel")
    String csf_proteinlevel;
    @JsonProperty("csf_sample2_wbc_count")
    @Column(name = "csf_sample2_wbc_count")
    String csf_sample2_wbc_count;
    @JsonProperty("csf_sample2_jeigmcount")
    @Column(name = "csf_sample2_jeigmcount")
    String csf_sample2_jeigmcount;
    @JsonProperty("serum_jeigmcount")
    @Column(name = "serum_jeigmcount")
    String serum_jeigmcount;
    @JsonProperty("serum_igmden")
    @Column(name = "serum_igmden")
    String serum_igmden;

    @JsonProperty("serum_scrumtyphusigm")
    @Column(name = "serum_scrumtyphusigm")
    String serum_scrumtyphusigm;

    @JsonProperty("labresult_csf_jeigmcount")
    @Column(name="labresult_csf_jeigmcount")
    String labresult_csf_jeigmcount;


    public String getAesepid() {
        return aesepid;
    }

    public void setAesepid(String aesepid) {
        this.aesepid = aesepid;
    }

    public String getNIMHANS_AES_ID() {
        return NIMHANS_AES_ID;
    }

    public void setNIMHANS_AES_ID(String NIMHANS_AES_ID) {
        this.NIMHANS_AES_ID = NIMHANS_AES_ID;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSamplecollected_csf() {
        return samplecollected_csf;
    }

    public void setSamplecollected_csf(String samplecollected_csf) {
        this.samplecollected_csf = samplecollected_csf;
    }

    public String getSamplesenttoapexlab_csf() {
        return samplesenttoapexlab_csf;
    }

    public void setSamplesenttoapexlab_csf(String samplesenttoapexlab_csf) {
        this.samplesenttoapexlab_csf = samplesenttoapexlab_csf;
    }

    public String getSamplecollected_serum() {
        return samplecollected_serum;
    }

    public void setSamplecollected_serum(String samplecollected_serum) {
        this.samplecollected_serum = samplecollected_serum;
    }

    public String getSamplesenttoapexlab_serum() {
        return samplesenttoapexlab_serum;
    }

    public void setSamplesenttoapexlab_serum(String samplesenttoapexlab_serum) {
        this.samplesenttoapexlab_serum = samplesenttoapexlab_serum;
    }

    public String getSamplecollected_wholeblood() {
        return samplecollected_wholeblood;
    }

    public void setSamplecollected_wholeblood(String samplecollected_wholeblood) {
        this.samplecollected_wholeblood = samplecollected_wholeblood;
    }

    public String getLabresult_csf_wbccount() {
        return labresult_csf_wbccount;
    }

    public void setLabresult_csf_wbccount(String labresult_csf_wbccount) {
        this.labresult_csf_wbccount = labresult_csf_wbccount;
    }

    public String getLabresult_csf_jeigmcount() {
        return labresult_csf_jeigmcount;
    }

    public void setLabresult_csf_jeigmcount(String labresult_csf_jeigmcount) {
        this.labresult_csf_jeigmcount = labresult_csf_jeigmcount;
    }

    public String getCsf_gulucoselevel() {
        return csf_gulucoselevel;
    }

    public void setCsf_gulucoselevel(String csf_gulucoselevel) {
        this.csf_gulucoselevel = csf_gulucoselevel;
    }

    public String getCsf_proteinlevel() {
        return csf_proteinlevel;
    }

    public void setCsf_proteinlevel(String csf_proteinlevel) {
        this.csf_proteinlevel = csf_proteinlevel;
    }

    public String getCsf_sample2_wbc_count() {
        return csf_sample2_wbc_count;
    }

    public void setCsf_sample2_wbc_count(String csf_sample2_wbc_count) {
        this.csf_sample2_wbc_count = csf_sample2_wbc_count;
    }

    public String getCsf_sample2_jeigmcount() {
        return csf_sample2_jeigmcount;
    }

    public void setCsf_sample2_jeigmcount(String csf_sample2_jeigmcount) {
        this.csf_sample2_jeigmcount = csf_sample2_jeigmcount;
    }

    public String getSerum_jeigmcount() {
        return serum_jeigmcount;
    }

    public void setSerum_jeigmcount(String serum_jeigmcount) {
        this.serum_jeigmcount = serum_jeigmcount;
    }

    public String getSerum_igmden() {
        return serum_igmden;
    }

    public void setSerum_igmden(String serum_igmden) {
        this.serum_igmden = serum_igmden;
    }

    public String getSerum_scrumtyphusigm() {
        return serum_scrumtyphusigm;
    }

    public void setSerum_scrumtyphusigm(String serum_scrumtyphusigm) {
        this.serum_scrumtyphusigm = serum_scrumtyphusigm;
    }

}
