package org.hisp.dhis.android.sdk.synchronization.data.trackedentityinstance;


import org.hisp.dhis.android.sdk.network.APIException;
import org.hisp.dhis.android.sdk.network.DhisApi;
import org.hisp.dhis.android.sdk.persistence.models.ApiResponse;
import org.hisp.dhis.android.sdk.persistence.models.ImportSummary;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.synchronization.data.common.ARemoteDataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

public class TrackedEntityInstanceRemoteDataSource  extends ARemoteDataSource {
    public static  String PROJECT_DONOR;
	
    public TrackedEntityInstanceRemoteDataSource(DhisApi dhisApi) {
        this.dhisApi = dhisApi;
    }


    public TrackedEntityInstance getTrackedEntityInstance(String trackedEntityInstance) {
        final Map<String, String> QUERY_PARAMS = new HashMap<>();
        QUERY_PARAMS.put("fields", "created,lastUpdated");
        TrackedEntityInstance updatedTrackedEntityInstance = dhisApi
                .getTrackedEntityInstance(trackedEntityInstance, QUERY_PARAMS);

        return updatedTrackedEntityInstance;
    }

    public ImportSummary save(TrackedEntityInstance trackedEntityInstance) {
        if (trackedEntityInstance.getCreated() == null) {
            return postTrackedEntityInstance(trackedEntityInstance, dhisApi);
        } else {
            return putTrackedEntityInstance(trackedEntityInstance, dhisApi);
        }
    }

    public List<ImportSummary> save(List<TrackedEntityInstance> trackedEntityInstances) {
        Map<String, List<TrackedEntityInstance>> map = new HashMap<>();
        map.put("trackedEntityInstances", trackedEntityInstances);
        return batchTrackedEntityInstances(map, dhisApi);
    }

    private List<ImportSummary> batchTrackedEntityInstances(Map<String, List<TrackedEntityInstance>> trackedEntityInstances, DhisApi dhisApi) throws
            APIException {
        ApiResponse apiResponse = dhisApi.postTrackedEntityInstances(trackedEntityInstances);
        return apiResponse.getImportSummaries();
    }

    private ImportSummary postTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        Response response = dhisApi.postTrackedEntityInstance(trackedEntityInstance);
		
		 if (response.getReason().equals("OK")) {

            //@Sou_ Ben id Custom
            List<TrackedEntityInstance> tei_list= MetaDataController.getTrackedEntityInstancesFromLocal();
            int count=tei_list.size();
            String seq_count = String.format ("%05d", count);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String year_=String.valueOf(year);

        for(int p=0;p<trackedEntityInstance.getAttributes().size();p++)
        {
            if(trackedEntityInstance.getAttributes().get(p).getTrackedEntityAttributeId().equals("KLSVjftH2xS")){
                PROJECT_DONOR=trackedEntityInstance.getAttributes().get(p).getValue();
                if(PROJECT_DONOR == null) PROJECT_DONOR = "";
            }

            if(trackedEntityInstance.getAttributes().get(p).getTrackedEntityAttributeId().equals("L2doMQ7OtUB")){
                trackedEntityInstance.getAttributes().get(p).setValue("m-PLAN"+PROJECT_DONOR+year_+seq_count);
                dhisApi.postTrackedEntityInstance(trackedEntityInstance);
            }

        }
            trackedEntityInstance.save();
            trackedEntityInstance.setFromServer(true);
        }
		
        return getImportSummary(response);
    }

    private ImportSummary putTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        Response response = dhisApi.putTrackedEntityInstance(trackedEntityInstance.getUid(), trackedEntityInstance);
		
		    if (response.getReason().equals("OK")) {
            //@Sou_ Ben id Custom
            List<TrackedEntityInstance> tei_list= MetaDataController.getTrackedEntityInstancesFromLocal();
            int count=tei_list.size();
            String seq_count = String.format ("%05d", count);
            int year = Calendar.getInstance().get(Calendar.YEAR);
            String year_=String.valueOf(year);

            for(int p=0;p<trackedEntityInstance.getAttributes().size();p++)
            {
                if(trackedEntityInstance.getAttributes().get(p).getTrackedEntityAttributeId().equals("KLSVjftH2xS")){
                    PROJECT_DONOR=trackedEntityInstance.getAttributes().get(p).getValue();
                    if(PROJECT_DONOR == null) PROJECT_DONOR = "";
                }

                if(trackedEntityInstance.getAttributes().get(p).getTrackedEntityAttributeId().equals("L2doMQ7OtUB")){
                    trackedEntityInstance.getAttributes().get(p).setValue("m-PLAN"+PROJECT_DONOR+year_+seq_count);
                    dhisApi.postTrackedEntityInstance(trackedEntityInstance);

                }
            }
            trackedEntityInstance.save();
            trackedEntityInstance.setFromServer(true);
        }
        return getImportSummary(response);
    }
}