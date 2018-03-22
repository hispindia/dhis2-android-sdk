/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.sdk.controllers.tracker;

import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;

import org.hisp.dhis.android.sdk.controllers.*;
import org.hisp.dhis.android.sdk.controllers.realm.ROrganisationHelper;
import org.hisp.dhis.android.sdk.controllers.realm.ROrganisationUnit;
import org.hisp.dhis.android.sdk.network.APIException;
import org.hisp.dhis.android.sdk.network.DhisApi;
import org.hisp.dhis.android.sdk.network.response.ApiResponse2;
import org.hisp.dhis.android.sdk.network.response.ImportSummary2;
import org.hisp.dhis.android.sdk.persistence.models.ApiResponse;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.DataValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment$Table;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Event$Table;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem$Table;
import org.hisp.dhis.android.sdk.persistence.models.ImportSummary;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.Relationship;
import org.hisp.dhis.android.sdk.persistence.models.Relationship$Table;
import org.hisp.dhis.android.sdk.persistence.models.SystemInfo;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeGeneratedValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue$Table;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance$Table;
import org.hisp.dhis.android.sdk.persistence.models.User;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.persistence.models.Usersms;
import org.hisp.dhis.android.sdk.persistence.models.Usersmspojo;
import org.hisp.dhis.android.sdk.utils.NetworkUtils;
import org.hisp.dhis.android.sdk.utils.StringConverter;
import org.hisp.dhis.android.sdk.utils.Utils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import org.hisp.dhis.android.sdk.controllers.tracker.Httphandler;
/**
 * @author Simen Skogly Russnes on 24.08.15.
 */
final class TrackerDataSender {

    public static final String CLASS_TAG = TrackerDataSender.class.getSimpleName();
    private static final List<String> ouphones = new ArrayList<String>();
   private static final List<String> ouphones_users_samples = new ArrayList<String>();
   private static final List<String> outei_name = new ArrayList<String>();
   private static final List<String> aes_id = new ArrayList<String>();
   private static final List<String> physican_number = new ArrayList<String>();
   private static final List<String> outei_age = new ArrayList<String>();
    private static final int contact_length=0;
//   private static final Array ouphones = new Array();
    private TrackerDataSender() {
    }

    static void sendEventChanges(DhisApi dhisApi) throws APIException {

        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        List<Event> events = new Select().from(Event.class).where
                (Condition.column(Event$Table.FROMSERVER).is(false)).queryList();
        List<Event> eventsWithFailedThreshold = new Select().from(Event.class)
                .join(FailedItem.class, Join.JoinType.LEFT)
                .on(Condition.column(FailedItem$Table.ITEMID).eq(Event$Table.LOCALID))
                .where(Condition.column(FailedItem$Table.ITEMTYPE).eq(FailedItem.EVENT))
                .and(Condition.column(FailedItem$Table.FAILCOUNT).greaterThan(3))
                .and(Condition.column(Event$Table.FROMSERVER).is(false))
                .queryList();

        List<Event> eventsToPost = new ArrayList<>();
        eventsToPost.addAll(events);
        for (Event event : events) {
            for (Event failedEvent : eventsWithFailedThreshold) {
                if (event.getUid().equals(failedEvent.getUid())) {
                    eventsToPost.remove(event);
                }
            }
        }
        sendEventBatch(dhisApi, events);
    }

    static void sendEventBatch(DhisApi dhisApi, List<Event> events) throws APIException {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (int i = 0; i < events.size(); i++) {/* removing events with local enrollment reference. In this case, the enrollment needs to be synced first*/
            Event event = events.get(i);
            if (Utils.isLocal(event.getEnrollment()) && event.getEnrollment() != null/*if enrollments==null, then it is probably a single event without reg*/) {
                events.remove(i);
                i--;
                continue;
            }
        }
        postEventBatch(dhisApi, events);
    }

    static void postEventBatch(DhisApi dhisApi, List<Event> events) throws APIException {
        Map<String, Event> eventMap = new HashMap<>();
        List<ImportSummary> importSummaries = null;
        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
        ApiResponse apiResponse = null;
        try {
            Map<String, List<Event>> map = new HashMap<>();
            map.put("events", events);
            apiResponse = dhisApi.postEvents(map);

            importSummaries = apiResponse.getImportSummaries();

            for (Event event : events) {
                eventMap.put(event.getUid(), event);

            }

            // toDo @Sou  14/11/2017  create apex event on enrollment sync
            //@Sou Sync incase of Enrollment only (No events)
            if(events!=null)
            {
                if(events.get(0).getDataValues()!=null)
                {
                    for(int i=0;i<=events.get(0).getDataValues().size()-1;i++)
                    {
                        if(events.get(0).getDataValues().get(i).getDataElement()!=null)
                        {
                            if(events.get(0).getDataValues().get(i).getDataElement().toString().equals("tFZQIt6d9pk")&& events.get(0).getDataValues().get(i).getDataElement().toString().equals("true"))
                            {

                                List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                                orgUnitList = ROrganisationHelper.getOrganisationUnitID(events.get(0).getOrganisationUnitId());
                                List<DataValue> dataValues = new ArrayList<>();
                                dataValues.add(events.get(0).getDataValues().get(i));
                                Event event1=new Event();
                                if(events.get(0).getProgramId().equals("eV13Hfo7qiv"))
                                {
                                    event1.setProgramStageId("u75cboMxKPs");
                                }
                                else if(events.get(0).getProgramId().equals("a9cQSlDVI2n"))
                                {
                                    event1.setProgramStageId("GOWaC9DJ8ua");
                                }
                                event1.setDataValues(dataValues);
                                event1.setProgramId(events.get(0).getProgramId());
                                event1.setEventDate(events.get(0).getEventDate());
                                event1.setStatus(events.get(0).getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                                event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                                event1.setTrackedEntityInstance(events.get(0).getTrackedEntityInstance());
                                postEventnew(event1,dhisApi);
                            }
                            else if(events.get(0).getDataValues().get(i).getDataElement().toString().equals("jDiCrciKu7Z")&& events.get(0).getDataValues().get(i).getDataElement().toString().equals("true"))
                            {

                                List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                                orgUnitList = ROrganisationHelper.getOrganisationUnitID(events.get(0).getOrganisationUnitId());
                                Event event1=new Event();
                                List<DataValue> dataValues = new ArrayList<>();
                                dataValues.add(events.get(0).getDataValues().get(i));
                                if(events.get(0).getProgramId().equals("eV13Hfo7qiv"))
                                {
                                    event1.setProgramStageId("u75cboMxKPs");
                                }
                                else if(events.get(0).getProgramId().equals("a9cQSlDVI2n"))
                                {
                                    event1.setProgramStageId("GOWaC9DJ8ua");
                                }
                                event1.setDataValues(dataValues);
                                event1.setProgramId(events.get(0).getProgramId());
                                event1.setEventDate(events.get(0).getEventDate());
                                event1.setStatus(events.get(0).getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                                event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                                event1.setTrackedEntityInstance(events.get(0).getTrackedEntityInstance());
                                postEventnew(event1,dhisApi);
                            }
                            else if(events.get(0).getDataValues().get(i).getDataElement().toString().equals("fczAudE6eS6")&& events.get(0).getDataValues().get(i).getDataElement().toString().equals("true"))
                            {

                                List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                                orgUnitList = ROrganisationHelper.getOrganisationUnitID(events.get(0).getOrganisationUnitId());
                                Event event1=new Event();
                                List<DataValue> dataValues = new ArrayList<>();
                                dataValues.add(events.get(0).getDataValues().get(i));
                                if(events.get(0).getProgramId().equals("eV13Hfo7qiv"))
                                {
                                    event1.setProgramStageId("u75cboMxKPs");
                                }
                                else if(events.get(0).getProgramId().equals("a9cQSlDVI2n"))
                                {
                                    event1.setProgramStageId("GOWaC9DJ8ua");
                                }
                                event1.setDataValues(dataValues);
                                event1.setProgramId(events.get(0).getProgramId());
                                event1.setEventDate(events.get(0).getEventDate());
                                event1.setStatus(events.get(0).getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                                event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                                event1.setTrackedEntityInstance(events.get(0).getTrackedEntityInstance());
                                postEventnew(event1,dhisApi);
                            }
                        }

                    }
                }

            }


            // check if all items were synced successfully
            if (importSummaries != null) {
                SystemInfo systemInfo = dhisApi.getSystemInfo();
                DateTime eventUploadTime = systemInfo.getServerDate();
                for (ImportSummary importSummary : importSummaries) {
                    Event event = eventMap.get(importSummary.getReference());
                    System.out.println("IMPORT SUMMARY: " + importSummary.getDescription());
                    if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                            ImportSummary.OK.equals(importSummary.getStatus())) {
                        if (event != null) {
                            event.setFromServer(true);
                            event.setCreated(eventUploadTime.toString());
                            event.setLastUpdated(eventUploadTime.toString());
                            event.save();
                            clearFailedItem(FailedItem.EVENT, event.getLocalId());
                            //UpdateEventTimestamp(event, dhisApi);
                        }
                    }
                }
            }

        } catch (APIException apiException) {
            //batch sending failed. Trying to re-send one by one
            sendEventChanges(dhisApi, events);

        }
    }

    static void sendEventChanges(DhisApi dhisApi, List<Event> events) throws APIException {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (int i = 0; i < events.size(); i++) {/* removing events with local enrollment reference. In this case, the enrollment needs to be synced first*/
            Event event = events.get(i);
            if (Utils.isLocal(event.getEnrollment()) && event.getEnrollment() != null/*if enrollments==null, then it is probably a single event without reg*/) {
                events.remove(i);
                i--;
                continue;
            }
        }
        Log.d(CLASS_TAG, "got this many events to send:" + events.size());

        for (Event event : events) {
            sendEventChanges(dhisApi, event);
        }
    }

    static void sendEventChanges(DhisApi dhisApi, Event event) throws APIException {
        if (event == null) {
            return;
        }
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }

        if (Utils.isLocal(event.getEnrollment()) && event.getEnrollment() != null/*if enrollments==null, then it is probably a single event without reg*/) {
            return;
        }
        Enrollment enrollment = TrackerController.getEnrollment(event.getEnrollment());
        if (enrollment != null && !enrollment.isFromServer()) { // if enrollment is unsent, send it before events
            sendEnrollmentChanges(dhisApi, enrollment, false);
        }

        if (event.getCreated() == null) {
            postEvent(event, dhisApi);
        } else {
            putEvent(event, dhisApi);
        }
    }

    private static void postEvent(Event event, DhisApi dhisApi) throws APIException {
        try {
              String sample_positive="";
              ouphones.clear();
            ouphones_users_samples.clear();
              outei_name.clear();
              outei_age.clear();
              aes_id.clear();
            physican_number.clear();
            Enrollment enrollment = TrackerController.getEnrollment(event.getEnrollment());
            enrollment.getAttributes();
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "id,created,lastUpdated,name,displayName," +
                    "firstName,surname,gender,birthday,introduction," +
                    "education,employer,interests,jobTitle,languages,email,phoneNumber," +
                    "teiSearchOrganisationUnits[id],organisationUnits[id]");
            UserAccount userAccount1 = dhisApi
                    .getCurrentUserAccount(QUERY_PARAMS);
            String phonenumber=userAccount1.getPhoneNumber();

            //@sou create apex stage at parent for Apex sent to true
            //List<TrackedEntityInstance> trackedEntityInstances = new Select().from(TrackedEntityInstance.class).where(Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(false)).queryList();

            String tei=event.getTrackedEntityInstance();
            List<TrackedEntityInstance> trackedEntityInstances = new Select().from(TrackedEntityInstance.class).where(Condition.column(TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is(tei)).queryList();
            List<TrackedEntityInstance> trackedEntityInstances_new = new Select().from(TrackedEntityInstance.class).where(Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(true)).queryList();
            String jsonStr,jsonStr_tei,jsonStr_samples;
            List<ROrganisationUnit> orgUnitList = new ArrayList<>();
            orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
            Httphandler sh2 = new Httphandler();

//          jsonStr = sh1.makeServiceCall("http://ds-india.org/aes/api/organisationUnits/"+orgUnitList.get(0).getParent()+".json?fields=users[firstName,surName,phoneNumber]");
            jsonStr = sh2.makeServiceCall("http://ds-india.org/aes/api/organisationUnits/"+orgUnitList.get(0).getParent()+".json?fields=users[firstName,surName,phoneNumber]");
            jsonStr_samples = sh2.makeServiceCall("http://ds-india.org/aes/api/organisationUnits/"+orgUnitList.get(0).getId()+".json?fields=users[firstName,surName,phoneNumber]");
            jsonStr_tei = sh2.makeServiceCall("http://ds-india.org/aes/api/trackedEntityInstances/"+event.getTrackedEntityInstance()+".json?fields=attributes[displayName,attribute,value]");

            //http://ds-india.org/aes/api/trackedEntityInstances.json?ou=CPtzIhyn36z&ouMode=DESCENDANTS&paging=false&program=a9cQSlDVI2n&skipPaging=true&filter=B8Ohks1Zf91:LIKE:Sanjay&fields=attributes[displayName,attribute,value]
            Log.e("Response:-", "Response from url: " + jsonStr);
            //// TODO: 8/21/2017  @sou retrieve all parent user no:
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray contacts = jsonObj.getJSONArray("users");
                    int col=contacts.length();

                    for(int v=0;v<contacts.length();v++)
                    {
                        JSONObject jsonObject1 = contacts.getJSONObject(v);
                        String value_phoneNumber = jsonObject1.optString("phoneNumber");
                        Log.d("ouphone:",value_phoneNumber);
                        if(!value_phoneNumber.equals(""))
                        {
                            ouphones.add(value_phoneNumber);
                        }

                    }

                }
                catch (final JSONException e) {

                }
            }
            if (jsonStr_samples != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr_samples);
                    JSONArray contacts = jsonObj.getJSONArray("users");
                    int col=contacts.length();

                    for(int v=0;v<contacts.length();v++)
                    {
                        JSONObject jsonObject1 = contacts.getJSONObject(v);
                        String value_phoneNumber = jsonObject1.optString("phoneNumber");
                        Log.d("ouphone:",value_phoneNumber);
                        if(!value_phoneNumber.equals(""))
                        {
                            Log.d("user-mobile:",value_phoneNumber);
                            ouphones_users_samples.add(value_phoneNumber);
                        }

                    }

                }
                catch (final JSONException e) {

                }
            }

            if(enrollment.getAttributes()!=null)
            {
                for(int i=0;i<enrollment.getAttributes().size();i++)
                {
                    if(enrollment.getAttributes().get(i).getTrackedEntityAttributeId().equals("B8Ohks1Zf91"))
                    {
                        if(!enrollment.getAttributes().get(i).getValue().equals(""))
                        {
                            outei_name.add(enrollment.getAttributes().get(i).getValue());
                        }
                    }

                    if(enrollment.getAttributes().get(i).getTrackedEntityAttributeId().equals("g6aPl383VUZ"))
                    {
                        if(!enrollment.getAttributes().get(i).getValue().equals(""))
                        {
                            outei_age.add(enrollment.getAttributes().get(i).getValue());
                        }
                    }

                    if(enrollment.getAttributes().get(i).getTrackedEntityAttributeId().equals("GHF1cOxnBE9"))
                    {
                        if(!enrollment.getAttributes().get(i).getValue().equals(""))
                        {
                            aes_id.add(enrollment.getAttributes().get(i).getValue());
                        }
                    }

                    if(enrollment.getAttributes().get(i).getTrackedEntityAttributeId().equals("t3qChQsypyi"))
                    {
                        if(!enrollment.getAttributes().get(i).getValue().equals(""))
                        {
                            physican_number.add(enrollment.getAttributes().get(i).getValue());
                        }
                    }

                }
            }

            for(int i=0;i<event.getDataValues().size();i++) {
                //Sample sent to Apex Lab - CSF
                if (event.getDataValues().get(i).getDataElement().toString().equals("tFZQIt6d9pk")) {
                    if (event.getDataValues().get(i).getValue().equals("true")) {
                        String msg = "";
                        if (ouphones.isEmpty()) {
                            Log.d("nulllist", "ouphone");
                        } else {
                            for (int ouph = 0; ouph < ouphones.size(); ouph++) {
                                if (!outei_name.isEmpty()) {
                                    msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                                } else if (outei_name.isEmpty()) {
//                                          if(!aes_id.get(0).equals(""))
//                                          {
//                                              msg=msg+"--"+"- Aes id: "+ aes_id.get(0);
//                                          }

                                }
                                if (!outei_age.isEmpty()) {
                                    msg = msg + "Age:" + outei_age.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "Age:---";
                                }
                                Httphandler sh = new Httphandler();
                                sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones.get(ouph) + "&message=CSF sample sent to apex lab, patient name:" + msg);
                                msg = "";
                            }

                        }
                    }
                }


                //Sample sent to Apex Lab - Serum
                if (event.getDataValues().get(i).getDataElement().toString().equals("jDiCrciKu7Z")) {
                    if (event.getDataValues().get(i).getValue().equals("true")) {
                        String msg = "";
                        if (ouphones.isEmpty()) {
                            Log.d("nulllist", "ouphone");
                        } else {
                            for (int ouph = 0; ouph < ouphones.size(); ouph++) {
                                if (!outei_name.isEmpty()) {
                                    msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                                }
                                if (!outei_age.isEmpty()) {
                                    msg = msg + "Age:" + outei_age.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "Age:---";
                                }
                                Httphandler sh = new Httphandler();
                                sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones.get(ouph) + "&message=Serum sample sent to apex lab, patient name:" + msg);
                                msg = "";
                            }
                        }
                    }
                }
                //Sample sent to Apex Lab - Whole blood
                if (event.getDataValues().get(i).getDataElement().toString().equals("fczAudE6eS6")) {
                    if (event.getDataValues().get(i).getValue().equals("true")) {
                        String msg = "";
                        if (ouphones.isEmpty()) {
                            Log.d("nulllist", "ouphone");
                        } else {
                            for (int ouph = 0; ouph < ouphones.size(); ouph++) {
                                if (!outei_name.isEmpty()) {
                                    msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                                }
                                if (!outei_age.isEmpty()) {
                                    msg = msg + "Age:" + outei_age.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "Age:---";
                                }
                                Httphandler sh = new Httphandler();
                                sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones.get(ouph) + "&message=Whole Blood sample sent to apex lab, patient name:" + msg);
                                msg = "";
                            }
                        }
                    }
                }

                //CSF Nimhans Lab
                if (event.getDataValues().get(i).getDataElement().toString().equals("UUkruYKpd0P")) {
                    if (event.getDataValues().get(i).getValue().equals("true")) {
                        String msg = "";
                        if (ouphones.isEmpty()) {
                            Log.d("nulllist", "ouphone");
                        } else {
                            for (int ouph = 0; ouph < ouphones.size(); ouph++) {
                                if (!outei_name.isEmpty()) {
                                    msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                                }
                                if (!outei_age.isEmpty()) {
                                    msg = msg + "Age:" + outei_age.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "Age:---";
                                }
                                Httphandler sh = new Httphandler();
                                sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones.get(ouph) + "&message=CSF Sample sent to NIMHANS Lab, patient name:" + msg);
                            }
                        }
                    }
                }
                if (event.getDataValues().get(i).getDataElement().toString().equals("NNzfSz0GT5l")) {
                    if (event.getDataValues().get(i).getValue().equals("true")) {
                        String msg = "";
                        if (ouphones.isEmpty()) {
                            Log.d("nulllist", "ouphone");
                        } else {
                            for (int ouph = 0; ouph < ouphones.size(); ouph++) {
                                if (!outei_name.isEmpty()) {
                                    msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                                }
                                if (!outei_age.isEmpty()) {
                                    msg = msg + "Age:" + outei_age.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "Age:---";
                                }
                                Httphandler sh = new Httphandler();
                                sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones.get(ouph) + "&message=Serum Sample sent to NIMHANS Lab, patient name:" + msg);
                            }
                        }
                    }
                }
                if (event.getDataValues().get(i).getDataElement().toString().equals("FMjQDMqKhPQ")) {
                    if (event.getDataValues().get(i).getValue().equals("true")) {
                        String msg = "";
                        if (ouphones.isEmpty()) {
                            Log.d("nulllist", "ouphone");
                        } else {
                            for (int ouph = 0; ouph < ouphones.size(); ouph++) {
                                if (!outei_name.isEmpty()) {
                                    msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                                }
                                if (!outei_age.isEmpty()) {
                                    msg = msg + "Age:" + outei_age.get(0);
                                } else if (outei_name.isEmpty()) {
                                    msg = msg + "Age:---";
                                }
                                Httphandler sh = new Httphandler();
                                sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones.get(ouph) + "&message=Whole Blood Sample sent to NIMHANS Lab, patient name:" + msg);
                            }
                        }
                    }
                }

                //Apex LAb -CSF
                if (event.getDataValues().get(i).getDataElement().toString().equals("tFZQIt6d9pk") && event.getDataValues().get(i).getValue().toString().equals("true")) {


                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("u75cboMxKPs");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("GOWaC9DJ8ua");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    Log.d("Sms before:", "error");
                    //@sou_ TO send sms on user mobile

                }

                if (event.getDataValues().get(i).getDataElement().toString().equals("jDiCrciKu7Z") && event.getDataValues().get(i).getValue().toString().equals("true")) {


                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("u75cboMxKPs");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("GOWaC9DJ8ua");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    Log.d("Sms before:", "error");
                    //@sou_ TO send sms on user mobile

                }

                if (event.getDataValues().get(i).getDataElement().toString().equals("fczAudE6eS6") && event.getDataValues().get(i).getValue().toString().equals("true")) {


                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("u75cboMxKPs");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("GOWaC9DJ8ua");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    Log.d("Sms before:", "error");
                    //@sou_ TO send sms on user mobile

                }

                //Sample sent to nimhans lab-CSF
                if (event.getDataValues().get(i).getDataElement().toString().equals("UUkruYKpd0P") && event.getDataValues().get(i).getValue().toString().equals("true")) {
                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("MEmsKMPTFQ5");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("xuNYdOl17GZ");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    //@sou_ TO send sms on user mobile
                }
                if (event.getStatus().equals("COMPLETED")) {
                    if (event.getDataValues().get(i).getDataElement().toString().equals("cQb70BNRVP2")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Haemophilus influenzae PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("z8cBAMynjMD")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Neisseria meningitidis PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("TEwh6deEqLx")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Scrub Typhus PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("u1tVuQK0SdP")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Streptococcus pneumoniae PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("u1tVuQK0SdP")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Streptococcus pneumoniae PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("UELFMLIBwIB")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - HSV PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("FOcozyCNLRg")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - West Nile Virus IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("S8WGc5h1GLh")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Enterovirus PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("xw487RVnvqZ")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Dengue IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("JGUJLviOcFS")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("JGUJLviOcFS")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("rshKNXqDUUG")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("zTcOSPpQv1U")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - Leptospira DNA PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("FUmazOWerB5")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - Trioplex PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("PEzWnQoTQuc")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Trioplex PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("AlGyRi4L2Dq")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("UDnFjfw1b1l")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum- Leptospira IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("njvwZYFjBX5")) {
                        if (event.getDataValues().get(i).getValue().equals("Scrub typhus positive")) {
                            sample_positive = sample_positive + "Whole blood - Scrub Typhus PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("K24hMmaJvrV")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF sample 2 - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("KVJSXRivsHL")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum sample 2 - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("DG9PTsQmliZ")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("LpT8hxDYDHq")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("N6Yfs0jO6FY")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - JE IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("SyVZXV49iO9")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("gPIcDt4ug3L")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Discharge serum - JE IgM,";

                        }
                    }

                    if (event.getDataValues().get(i).getDataElement().toString().equals("e03UnsK7g4Y")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Discharge serum - Scrub typhus IgM,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("DHpb0qQ61ZE")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("tNhYQKLuGnM")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("vVBtXVY5Mwf")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Dengue NS1 ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("mcZ5Pb1SIcU")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Dengue IgM ELISA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("GBLuVWTAzYM")) {
                        if (event.getDataValues().get(i).getValue().equals("Probable scrub typhus") || event.getDataValues().get(i).getValue().equals("Confirmed scrub typhus")) {
                            sample_positive = sample_positive + "Serum - Scrub Typhus IFA,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("CvndfGdof4G")) {
                        if (event.getDataValues().get(i).getValue().equals("Zika positive") || event.getDataValues().get(i).getValue().equals("Dengue positive") || event.getDataValues().get(i).getValue().equals("Chikungunya positive") || event.getDataValues().get(i).getValue().equals("Multiple positive for Zika, Dengue and Chikungunya")) {
                            sample_positive = sample_positive + "CSF - Trioplex PCR,";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("sGWZEwEHEKJ")) {
                        if (event.getDataValues().get(i).getValue().equals("JE positive") || event.getDataValues().get(i).getValue().equals("Dengue 1 positive") || event.getDataValues().get(i).getValue().equals("Dengue 2 positive") || event.getDataValues().get(i).getValue().equals("Dengue 3 positive") || event.getDataValues().get(i).getValue().equals("Dengue 4 positive") || event.getDataValues().get(i).getValue().equals("West Nile positive") || event.getDataValues().get(i).getValue().equals("Flavivirus positive")) {
                            sample_positive = sample_positive + "Serum - PRNT";

                        }
                    }
                    if (event.getDataValues().get(i).getDataElement().toString().equals("R1oKDLzfRxA")) {
                        if (event.getDataValues().get(i).getValue().equals("Zika positive") || event.getDataValues().get(i).getValue().equals("Dengue positive") || event.getDataValues().get(i).getValue().equals("Chikungunya positive") || event.getDataValues().get(i).getValue().equals("Multiple positive for Zika, Dengue and Chikungunya")) {
                            sample_positive = sample_positive + "Serum - Trioplex PCR,";

                        }
                    }
                }
            }
            if (event.getStatus().equals("COMPLETED")) {
                String msg = "";
                for (int ouph = 0; ouph < ouphones_users_samples.size(); ouph++) {
                    msg = "";
                    if (!outei_name.isEmpty()) {
                        msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                    } else if (outei_name.isEmpty()) {
                        if (!aes_id.isEmpty()) {
                            msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                        } else {
                            msg = msg + "--" + "- Aes id: ";
                        }
                    }
                    if (!outei_age.isEmpty()) {
                        msg = msg + "Age:" + outei_age.get(0);
                    } else if (outei_name.isEmpty()) {
                        msg = msg + "Age:---";
                    }
                    Httphandler sh = new Httphandler();
                    sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones_users_samples.get(ouph) + "&message=These Samples are +ve of patient:" + msg + "-" + sample_positive);
                }
                Httphandler sh1 = new Httphandler();

                if (physican_number.size() > 0) {
                    sh1.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + physican_number.get(0) + "&message=These Samples are +ve of patient:" + msg + "-" + sample_positive);
                }
            }
                else
                {

                }

            Response response = dhisApi.postEvent(event);

            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    // also, we will need to find UUID of newly created event,
                    // which is contained inside of HTTP Location header
                    Header header = NetworkUtils.findLocationHeader(response.getHeaders());
                    // change state and save event
                    event.setFromServer(true);
                    event.save();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                    UpdateEventTimestamp(event, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        }
    }

    private static void postEventnew(Event event, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.postEvent(event);
            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    // also, we will need to find UUID of newly created event,
                    // which is contained inside of HTTP Location header
                    Header header = NetworkUtils.findLocationHeader(response.getHeaders());
                    // change state and save event
                    event.setFromServer(true);
                    event.save();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                    UpdateEventTimestamp(event, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        }
    }

    private static void putEvent(Event event, DhisApi dhisApi) throws APIException {
        try {
            List<ROrganisationUnit> orgUnitList = new ArrayList<>();
            String sample_positive="";
            for(int i=0;i<event.getDataValues().size();i++)
            {
                if (event.getDataValues().get(i).getDataElement().toString().equals("tFZQIt6d9pk") && event.getDataValues().get(i).getValue().toString().equals("true")) {


                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("u75cboMxKPs");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("GOWaC9DJ8ua");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    Log.d("Sms before:", "error");
                    //@sou_ TO send sms on user mobile

                }
                if (event.getDataValues().get(i).getDataElement().toString().equals("jDiCrciKu7Z") && event.getDataValues().get(i).getValue().toString().equals("true")) {


                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("u75cboMxKPs");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("GOWaC9DJ8ua");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    Log.d("Sms before:", "error");
                    //@sou_ TO send sms on user mobile

                }

                if (event.getDataValues().get(i).getDataElement().toString().equals("fczAudE6eS6") && event.getDataValues().get(i).getValue().toString().equals("true")) {


                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("u75cboMxKPs");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("GOWaC9DJ8ua");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
//                        List<ROrganisationUnit> orgUnitList = new ArrayList<>();
//                        orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    Log.d("Sms before:", "error");
                    //@sou_ TO send sms on user mobile

                }

                //Sample sent to nimhans lab-CSF
                if (event.getDataValues().get(i).getDataElement().toString().equals("UUkruYKpd0P") && event.getDataValues().get(i).getValue().toString().equals("true")) {
                    List<ROrganisationUnit> orgUnitList1 = new ArrayList<>();
                    orgUnitList = ROrganisationHelper.getOrganisationUnitID(event.getOrganisationUnitId());
                    Event event1 = new Event();
                    List<DataValue> dataValues = new ArrayList<>();
                    dataValues.add(event.getDataValues().get(i));
                    if (event.getProgramId().equals("eV13Hfo7qiv")) {
                        event1.setProgramStageId("MEmsKMPTFQ5");
                    } else if (event.getProgramId().equals("a9cQSlDVI2n")) {
                        event1.setProgramStageId("xuNYdOl17GZ");
                    }
                    event1.setDataValues(dataValues);
                    event1.setProgramId(event.getProgramId());
                    event1.setEventDate(event.getEventDate());
                    event1.setStatus(event.getStatus());
                    event1.setOrganisationUnitId(orgUnitList.get(0).getParent());
                    event1.setTrackedEntityInstance(event.getTrackedEntityInstance());
                    postEventnew(event1, dhisApi);
                    //@sou_ TO send sms on user mobile
                }
                if(event.getStatus().equals("COMPLETED"))
                {
                    if(event.getDataValues().get(i).getDataElement().toString().equals("cQb70BNRVP2")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Haemophilus influenzae PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("z8cBAMynjMD")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Neisseria meningitidis PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("TEwh6deEqLx")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Scrub Typhus PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("u1tVuQK0SdP")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Streptococcus pneumoniae PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("u1tVuQK0SdP")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Streptococcus pneumoniae PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("UELFMLIBwIB")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - HSV PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("FOcozyCNLRg")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - West Nile Virus IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("S8WGc5h1GLh")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF- Enterovirus PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("xw487RVnvqZ")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Dengue IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("JGUJLviOcFS")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("JGUJLviOcFS")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("rshKNXqDUUG")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("zTcOSPpQv1U")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - Leptospira DNA PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("FUmazOWerB5")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - Trioplex PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("PEzWnQoTQuc")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Trioplex PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("AlGyRi4L2Dq")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("UDnFjfw1b1l")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum- Leptospira IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("njvwZYFjBX5")) {
                        if (event.getDataValues().get(i).getValue().equals("Scrub typhus positive")) {
                            sample_positive = sample_positive + "Whole blood - Scrub Typhus PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("K24hMmaJvrV")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF sample 2 - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("KVJSXRivsHL")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum sample 2 - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("DG9PTsQmliZ")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("LpT8hxDYDHq")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("N6Yfs0jO6FY")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "CSF - JE IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("SyVZXV49iO9")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("gPIcDt4ug3L")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Discharge serum - JE IgM,";

                        }
                    }

                    if(event.getDataValues().get(i).getDataElement().toString().equals("e03UnsK7g4Y")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Discharge serum - Scrub typhus IgM,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("DHpb0qQ61ZE")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("tNhYQKLuGnM")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Scrub typhus IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("vVBtXVY5Mwf")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Dengue NS1 ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("mcZ5Pb1SIcU")) {
                        if (event.getDataValues().get(i).getValue().equals("Positive") || event.getDataValues().get(i).getValue().equals("Equivocal")) {
                            sample_positive = sample_positive + "Serum - Dengue IgM ELISA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("GBLuVWTAzYM")) {
                        if (event.getDataValues().get(i).getValue().equals("Probable scrub typhus")||event.getDataValues().get(i).getValue().equals("Confirmed scrub typhus")) {
                            sample_positive = sample_positive + "Serum - Scrub Typhus IFA,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("CvndfGdof4G")) {
                        if (event.getDataValues().get(i).getValue().equals("Zika positive")||event.getDataValues().get(i).getValue().equals("Dengue positive")||event.getDataValues().get(i).getValue().equals("Chikungunya positive")||event.getDataValues().get(i).getValue().equals("Multiple positive for Zika, Dengue and Chikungunya")) {
                            sample_positive = sample_positive + "CSF - Trioplex PCR,";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("sGWZEwEHEKJ")) {
                        if (event.getDataValues().get(i).getValue().equals("JE positive")||event.getDataValues().get(i).getValue().equals("Dengue 1 positive")||event.getDataValues().get(i).getValue().equals("Dengue 2 positive")||event.getDataValues().get(i).getValue().equals("Dengue 3 positive")||event.getDataValues().get(i).getValue().equals("Dengue 4 positive")||event.getDataValues().get(i).getValue().equals("West Nile positive")||event.getDataValues().get(i).getValue().equals("Flavivirus positive")) {
                            sample_positive = sample_positive + "Serum - PRNT";

                        }
                    }
                    if(event.getDataValues().get(i).getDataElement().toString().equals("R1oKDLzfRxA")) {
                        if (event.getDataValues().get(i).getValue().equals("Zika positive")||event.getDataValues().get(i).getValue().equals("Dengue positive")||event.getDataValues().get(i).getValue().equals("Chikungunya positive")||event.getDataValues().get(i).getValue().equals("Multiple positive for Zika, Dengue and Chikungunya")) {
                            sample_positive = sample_positive + "Serum - Trioplex PCR,";

                        }
                    }
                    }
                    }
            if (event.getStatus().equals("COMPLETED")) {
                String msg = "";
                for (int ouph = 0; ouph < ouphones_users_samples.size(); ouph++) {
                    msg = "";
                    if (!outei_name.isEmpty()) {
                        msg = msg + outei_name.get(0) + " Aes id: " + aes_id.get(0);
                    } else if (outei_name.isEmpty()) {
                        if (!aes_id.isEmpty()) {
                            msg = msg + "--" + "- Aes id: " + aes_id.get(0);
                        } else {
                            msg = msg + "--" + "- Aes id: ";
                        }
                    }
                    if (!outei_age.isEmpty()) {
                        msg = msg + "Age:" + outei_age.get(0);
                    } else if (outei_name.isEmpty()) {
                        msg = msg + "Age:---";
                    }
                    Httphandler sh = new Httphandler();
                    sh.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + ouphones_users_samples.get(ouph) + "&message=These Samples are +ve of patient:" + msg + "-" + sample_positive);
                }
                Httphandler sh1 = new Httphandler();

                if (physican_number.size() > 0) {
                    sh1.makeServiceCall("http://bulksms.mysmsmantra.com:8080/WebSMS/SMSAPI.jsp?username=hispindia&password=hisp1234&sendername=HSSPIN&mobileno=" + physican_number.get(0) + "&message=These Samples are +ve of patient:" + msg + "-" + sample_positive);
                }
            }
                else
                {

                }

            Response response = dhisApi.putEvent(event.getEvent(), event);

            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    event.setFromServer(true);
                    event.save();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                    UpdateEventTimestamp(event, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        }
    }

    private static void putEventnew(Event event, DhisApi dhisApi) throws APIException {
        try {

            Response response = dhisApi.putEvent(event.getEvent(), event);

            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    event.setFromServer(true);
                    event.save();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                    UpdateEventTimestamp(event, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        }
    }

    private static void updateEventReferences(long localId, String newReference) {
        new Update(DataValue.class).set(Condition.column
                (DataValue$Table.EVENT).is
                (newReference)).where(Condition.column(DataValue$Table.LOCALEVENTID).is(localId)).async().execute();

        new Update(Event.class).set(Condition.column
                (Event$Table.EVENT).is
                (newReference), Condition.column(Event$Table.FROMSERVER).
                is(true)).where(Condition.column(Event$Table.LOCALID).is(localId)).async().execute();
        Event event = new Event();
        event.save();
        event.delete();//for triggering modelchangelistener
    }

    private static void UpdateEventTimestamp(Event event, DhisApi dhisApi) throws APIException {
        try {
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "created,lastUpdated");
            Event updatedEvent = dhisApi
                    .getEvent(event.getEvent(), QUERY_PARAMS);

            // merging updated timestamp to local event model
            event.setCreated(updatedEvent.getCreated());
            event.setLastUpdated(updatedEvent.getLastUpdated());
            event.save();
        } catch (APIException apiException) {
            NetworkUtils.handleApiException(apiException);
        }
    }

    static void postEnrollmentBatch(DhisApi dhisApi, List<Enrollment> enrollments) throws APIException {
        Map<String, Enrollment> enrollmentMap = new HashMap<>();
        List<ImportSummary2> importSummaries = null;
        ApiResponse2 apiResponse = null;
        try {
            Map<String, List<Enrollment>> map = new HashMap<>();
            map.put("enrollments", enrollments);
            apiResponse = dhisApi.postEnrollments(map);
            importSummaries = apiResponse.getImportSummaries();
            for (Enrollment enrollment : enrollments) {
                enrollmentMap.put(enrollment.getUid(), enrollment);
            }

            // check if all items were synced successfully
            if (importSummaries != null) {
                SystemInfo systemInfo = dhisApi.getSystemInfo();
                DateTime enrollmentUploadTime = systemInfo.getServerDate();
                for (ImportSummary2 importSummary : importSummaries) {
                    Enrollment enrollment = enrollmentMap.get(importSummary.getReference());
                    System.out.println("IMPORT SUMMARY: " + importSummary.getDescription());
                    if (ImportSummary2.Status.SUCCESS.equals(importSummary.getStatus()) ||
                            ImportSummary2.Status.OK.equals(importSummary.getStatus())) {
                        enrollment.setFromServer(true);
                        enrollment.setCreated(enrollmentUploadTime.toString());
                        enrollment.setLastUpdated(enrollmentUploadTime.toString());
                        enrollment.save();
                        clearFailedItem(FailedItem.ENROLLMENT, enrollment.getLocalId());
                        //UpdateEnrollmentTimestamp(enrollment, dhisApi);
                    }
                }
            }

        } catch (APIException apiException) {
            //batch sending failed. Trying to re-send one by one
            sendEnrollmentChanges(dhisApi, enrollments, false);

        }
    }

    static void sendEnrollmentChanges(DhisApi dhisApi, boolean sendEvents) throws APIException {
        List<Enrollment> enrollments = new Select().from(Enrollment.class).where(Condition.column(Enrollment$Table.FROMSERVER).is(false)).queryList();


        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        if (enrollments.size() <= 1) {
            sendEnrollmentChanges(dhisApi, enrollments, sendEvents);
        } else if (enrollments.size() > 1) {
            postEnrollmentBatch(dhisApi, enrollments);
        }
    }

    static void sendEnrollmentChanges(DhisApi dhisApi, List<Enrollment> enrollments, boolean sendEvents) throws APIException {
        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        for (int i = 0; i < enrollments.size(); i++) {/* workaround for not attempting to upload enrollments with local tei reference*/
            Enrollment enrollment = enrollments.get(i);
            if (Utils.isLocal(enrollment.getTrackedEntityInstance())) {
                enrollments.remove(i);
                i--;
            }
        }
        Log.d(CLASS_TAG, "got this many enrollments to send:" + enrollments.size());
        for (Enrollment enrollment : enrollments) {
            sendEnrollmentChanges(dhisApi, enrollment, sendEvents);
        }
    }

    static void sendEnrollmentChanges(DhisApi dhisApi, Enrollment enrollment, boolean sendEvents) throws APIException {
        if (enrollment == null) {
            return;
        }
        if (Utils.isLocal(enrollment.getTrackedEntityInstance())) {//don't send enrollment with locally made uid
            return;
        }
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        TrackedEntityInstance trackedEntityInstance = TrackerController.getTrackedEntityInstance(enrollment.getTrackedEntityInstance());

        if (trackedEntityInstance == null) {
            return;
        } else {
            if (!trackedEntityInstance.isFromServer()) { // if TEI is not sent to server and trying to send enrollment first. Send TEI before enrollment
                sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstance, false);
            }
        }

        boolean success;

        if (enrollment.getCreated() == null) {
            success = postEnrollment(enrollment, dhisApi);
            if (success && sendEvents) {
                List<Event> events = TrackerController.getEventsByEnrollment(enrollment.getLocalId());
                sendEventChanges(dhisApi, events);
            }
        } else {
            success = putEnrollment(enrollment, dhisApi);
            if (success && sendEvents) {
                List<Event> events = TrackerController.getEventsByEnrollment(enrollment.getLocalId());
                sendEventChanges(dhisApi, events);
            }
        }
    }

    private static boolean postEnrollment(Enrollment enrollment, DhisApi dhisApi) throws APIException {
        try {
            //@sou_ // TODO: 8/29/2017  age month set value
//            for(int i=0;i<enrollment.getAttributes().size();i++)
//            {
//                if (enrollment.getAttributes().get(i).getTrackedEntityAttributeId().equals("oQioOj2ECeU"))
//                {
//                    List<TrackedEntityAttributeValue> trackedEntityAttributeValues =TrackerController.getVisibleTrackedEntityAttributeValues(enrollment.getAttributes().get(i).getTrackedEntityAttributeId());
//                    enrollment.setAttributes("ss",);
//                }
//            }
            Response response = dhisApi.postEnrollment(enrollment);
//            int age_month=0;
//            for(int p=0;p<enrollment.getAttributes().size();p++)
//            {
//                if(enrollment.getAttributes().get(p).getTrackedEntityAttributeId().equals("g6aPl383VUZ"))
//                {
//                    String age_value=enrollment.getAttributes().get(p).getValue();
//                     age_month=(Integer.parseInt(age_value)*12);
//                    enrollment.getAttributes().get(p).setTrackedEntityAttributeId("oQioOj2ECeU");
//                    enrollment.getAttributes().get(p).setValue("77");
////                if(enrollment.getAttributes().get(p).getTrackedEntityAttributeId().equals("oQioOj2ECeU"))
////                {
////                    enrollment.getAttributes().get(p).setValue(age_month);
////                }
//                }
//
//            }
            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.ENROLLMENT, enrollment.getLocalId());

                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    // change state and save enrollment
                    //enrollment.setState(State.SYNCED);
                    enrollment.setFromServer(true);
                    enrollment.save();
                    clearFailedItem(FailedItem.ENROLLMENT, enrollment.getLocalId());
                    UpdateEnrollmentTimestamp(enrollment, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEnrollmentSendException(apiException, enrollment);
            return false;
        }
        return true;
    }

    private static boolean putEnrollment(Enrollment enrollment, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.putEnrollment(enrollment.getEnrollment(), enrollment);
            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.ENROLLMENT, enrollment.getLocalId());

                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    //enrollment.setState(State.SYNCED);.
                    enrollment.setFromServer(true);
                    enrollment.save();
                    clearFailedItem(FailedItem.ENROLLMENT, enrollment.getLocalId());
                    UpdateEnrollmentTimestamp(enrollment, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEnrollmentSendException(apiException, enrollment);
            return false;
        }
        return true;
    }

    private static void updateEnrollmentReferences(long localId, String newReference) {
        //updating any local events that had reference to local enrollment to new
        //reference from server.
        Log.d(CLASS_TAG, "updating enrollment references");
        new Update(Event.class).set(Condition.column
                (Event$Table.ENROLLMENT).is
                (newReference)).where(Condition.column(Event$Table.LOCALENROLLMENTID).is(localId)).async().execute();

        new Update(Enrollment.class).set(Condition.column
                (Enrollment$Table.ENROLLMENT).is
                (newReference), Condition.column(Enrollment$Table.FROMSERVER)
                .is(true)).where(Condition.column(Enrollment$Table.LOCALID).is
                (localId)).async().execute();
    }

    private static void UpdateEnrollmentTimestamp(Enrollment enrollment, DhisApi dhisApi) throws APIException {
        try {
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "created,lastUpdated");
            Enrollment updatedEnrollment = dhisApi
                    .getEnrollment(enrollment.getEnrollment(), QUERY_PARAMS);

            // merging updated timestamp to local enrollment model
            enrollment.setCreated(updatedEnrollment.getCreated());
            enrollment.setLastUpdated(updatedEnrollment.getLastUpdated());
            enrollment.save();
        } catch (APIException apiException) {
            NetworkUtils.handleApiException(apiException);
        }
    }

    //Todo @sou_attribute fetch from current tei
    static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, boolean sendEnrollments) throws APIException {
        List<TrackedEntityInstance> trackedEntityInstances = new Select().from(TrackedEntityInstance.class).where(Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(false)).queryList();

        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        if (trackedEntityInstances.size() <= 1) {
            sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstances, sendEnrollments);
        } else {
            postTrackedEntityInstanceBatch(dhisApi, trackedEntityInstances);
        }
        // sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstances, sendEnrollments);
    }

    static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, List<TrackedEntityInstance> trackedEntityInstances, boolean sendEnrollments) throws APIException {
        if (trackedEntityInstances == null || trackedEntityInstances.isEmpty()) {
            return;
        }
        Log.d(CLASS_TAG, "got this many teis to send:" + trackedEntityInstances.size());

        for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
            sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstance, sendEnrollments);
        }
    }

    static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, TrackedEntityInstance trackedEntityInstance, boolean sendEnrollments) throws APIException {
        if (trackedEntityInstance == null) {
            return;
        }
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        boolean success;
        if (trackedEntityInstance.getCreated() == null) {
            success = postTrackedEntityInstance(trackedEntityInstance, dhisApi);
        } else {
            success = putTrackedEntityInstance(trackedEntityInstance, dhisApi);
        }
        if (success && sendEnrollments) {
            List<Enrollment> enrollments = TrackerController.getEnrollments(trackedEntityInstance);
            sendEnrollmentChanges(dhisApi, enrollments, sendEnrollments);
        }
    }

    static void postTrackedEntityInstanceBatch(DhisApi dhisApi, List<TrackedEntityInstance> trackedEntityInstances) throws APIException {
        Map<String, TrackedEntityInstance> trackedEntityInstanceMap = new HashMap<>();
        List<ImportSummary2> importSummaries = null;

        ApiResponse2 apiResponse = null;
        try {
            Map<String, List<TrackedEntityInstance>> map = new HashMap<>();
            map.put("trackedEntityInstances", trackedEntityInstances);
            apiResponse = dhisApi.postTrackedEntityInstances(map);

            importSummaries = apiResponse.getImportSummaries();

            for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
                trackedEntityInstanceMap.put(trackedEntityInstance.getUid(), trackedEntityInstance);
            }

            // check if all items were synced successfully
            if (importSummaries != null) {
                SystemInfo systemInfo = dhisApi.getSystemInfo();
                DateTime eventUploadTime = systemInfo.getServerDate();
                for (ImportSummary2 importSummary : importSummaries) {
                    TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceMap.get(importSummary.getReference());
                    System.out.println("IMPORT SUMMARY: " + importSummary.getDescription());
                    if (ImportSummary2.Status.SUCCESS.equals(importSummary.getStatus()) ||
                            ImportSummary2.Status.OK.equals(importSummary.getStatus())) {
                        trackedEntityInstance.setFromServer(true);
                        trackedEntityInstance.setCreated(eventUploadTime.toString());
                        trackedEntityInstance.setLastUpdated(eventUploadTime.toString());
                        trackedEntityInstance.save();
                        clearFailedItem(FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                        //UpdateTrackedEntityInstanceTimestamp(trackedEntityInstance, dhisApi);
                    }
                }
            }

        } catch (APIException apiException) {
            //batch sending failed. Trying to re-send one by one
            sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstances, false);

        }
    }

    //@sou_ enrollment attributes for sms send
    private static boolean postTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.postTrackedEntityInstance(trackedEntityInstance);
            String msg;
for(int i=0;i<trackedEntityInstance.getAttributes().size();i++)
{
    if(trackedEntityInstance.getAttributes().get(i).getTrackedEntityAttributeId()=="B8Ohks1Zf91")
    {
        String patient_name=trackedEntityInstance.getAttributes().get(i).getValue();


    }

    if(trackedEntityInstance.getAttributes().get(i).getTrackedEntityAttributeId()=="eZAMzTucu0x")
    {
        String aes_id=trackedEntityInstance.getAttributes().get(i).getValue();

    }

}

            if (response.getStatus() == 200) {


                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    // change state and save trackedentityinstance

                    //trackedEntityInstance.setState(State.SYNCED);
                    trackedEntityInstance.setFromServer(true);
                    trackedEntityInstance.save();
                    clearFailedItem(FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                    UpdateTrackedEntityInstanceTimestamp(trackedEntityInstance, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleTrackedEntityInstanceSendException(apiException, trackedEntityInstance);
            return false;
        }
        return true;
    }

    private static boolean putTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.putTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance(), trackedEntityInstance);
            if (response.getStatus() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    //trackedentityinstance.setState(State.SYNCED);
                    trackedEntityInstance.setFromServer(true);
                    trackedEntityInstance.save();
                    clearFailedItem(FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                    UpdateTrackedEntityInstanceTimestamp(trackedEntityInstance, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleTrackedEntityInstanceSendException(apiException, trackedEntityInstance);
            return false;
        }
        return true;
    }

    private static void updateTrackedEntityInstanceReferences(long localId, String newTrackedEntityInstanceReference, String oldTempTrackedEntityInstanceReference) {
        //update references with uid received from server
        new Update(TrackedEntityAttributeValue.class).set(Condition.column
                (TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID).is
                (newTrackedEntityInstanceReference)).where(Condition.column(TrackedEntityAttributeValue$Table.LOCALTRACKEDENTITYINSTANCEID).is(localId)).async().execute();

        new Update(Event.class).set(Condition.column(Event$Table.
                TRACKEDENTITYINSTANCE).is(newTrackedEntityInstanceReference)).where(Condition.
                column(Event$Table.TRACKEDENTITYINSTANCE).is(oldTempTrackedEntityInstanceReference)).async().execute();

        new Update(Enrollment.class).set(Condition.column
                (Enrollment$Table.TRACKEDENTITYINSTANCE).is(newTrackedEntityInstanceReference)).
                where(Condition.column(Enrollment$Table.TRACKEDENTITYINSTANCE).is
                        (oldTempTrackedEntityInstanceReference)).async().execute();

        long updated = new Update(Relationship.class).set(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEA
        ).is(newTrackedEntityInstanceReference)).where(Condition.
                column(Relationship$Table.TRACKEDENTITYINSTANCEA).is(oldTempTrackedEntityInstanceReference)).count();

        updated += new Update(Relationship.class).set(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEB
        ).is(newTrackedEntityInstanceReference)).where(Condition.
                column(Relationship$Table.TRACKEDENTITYINSTANCEB).is(oldTempTrackedEntityInstanceReference)).count();

        Log.d(CLASS_TAG, "updated relationships: " + updated);

                    /* mechanism for triggering updating of relationships
                    * a relationship can only be uploaded if both involved teis are sent to server
                    * and have a valid UID.
                    * So, we check if this tei was just updated with a valid reference, and if there now
                    * exist >0 relationships that are valid. If >0 relationship is valid, it
                    * should get uploaded, as it is the first time it has been valid. */
        boolean hasValidRelationship = false;
        if (Utils.isLocal(oldTempTrackedEntityInstanceReference)) {
            List<Relationship> teiIsB = new Select().from(Relationship.class).where(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEB).is(newTrackedEntityInstanceReference)).queryList();
            List<Relationship> teiIsA = new Select().from(Relationship.class).where(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEA).is(newTrackedEntityInstanceReference)).queryList();
            if (teiIsB != null) {
                for (Relationship relationship : teiIsB) {
                    if (!Utils.isLocal(relationship.getTrackedEntityInstanceA())) {
                        hasValidRelationship = true;
                    }
                }
            }
            if (teiIsA != null) {
                for (Relationship relationship : teiIsA) {
                    if (!Utils.isLocal(relationship.getTrackedEntityInstanceB())) {
                        hasValidRelationship = true;
                    }
                }
            }
        }
        boolean fullySynced = !(hasValidRelationship && updated > 0);

        new Update(TrackedEntityInstance.class).set(Condition.column
                (TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is
                (newTrackedEntityInstanceReference), Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(fullySynced)).
                where(Condition.column(TrackedEntityInstance$Table.LOCALID).is(localId)).async().execute();
    }

    private static void UpdateTrackedEntityInstanceTimestamp(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        try {
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "created,lastUpdated");
            TrackedEntityInstance updatedTrackedEntityInstance = dhisApi
                    .getTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance(), QUERY_PARAMS);

            // merging updated timestamp to local trackedentityinstance model
            trackedEntityInstance.setCreated(updatedTrackedEntityInstance.getCreated());
            trackedEntityInstance.setLastUpdated(updatedTrackedEntityInstance.getLastUpdated());
            trackedEntityInstance.save();
        } catch (APIException apiException) {
            NetworkUtils.handleApiException(apiException);
        }
    }


    static void clearFailedItem(String type, long id) {
        FailedItem item = TrackerController.getFailedItem(type, id);
        if (item != null) {
            item.async().delete();
        }
    }

    private static void handleImportSummary(ImportSummary importSummary, String type, long id) {
        try {
            if (ImportSummary.ERROR.equals(importSummary.getStatus())) {
                Log.d(CLASS_TAG, "failed.. ");
                NetworkUtils.handleImportSummaryError(importSummary, type, 200, id);
            }
        } catch (Exception e) {
            Log.e(CLASS_TAG, "Unable to process import summary", e);
        }
    }

    private static List<ImportSummary> getImportSummaries(Response response) {
        List<ImportSummary> importSummaries = new ArrayList<>();

        try {
            JsonNode node = DhisController.getInstance().getObjectMapper()
                    .readTree(new StringConverter()
                            .fromBody(response.getBody(), String.class));
            if (node == null) {
                return null;
            }
            ApiResponse apiResponse = null;
            String body = new StringConverter().fromBody(response.getBody(), String.class);
            Log.d(CLASS_TAG, body);
            apiResponse = DhisController.getInstance().getObjectMapper().
                    readValue(body, ApiResponse.class);
            if (apiResponse != null && apiResponse.getImportSummaries() != null && !apiResponse.getImportSummaries().isEmpty()) {
                return (apiResponse.getImportSummaries());
            }

        } catch (ConversionException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return importSummaries;
    }

    private static ImportSummary getImportSummary(Response response) {
        //because the web api almost randomly gives the responses in different forms, this
        //method checks which one it is that is being returned, and parses accordingly.
        try {
            JsonNode node = DhisController.getInstance().getObjectMapper().
                    readTree(new StringConverter().fromBody(response.getBody(), String.class));
            if (node == null) {
                return null;
            }
            if (node.has("response")) {
                return getPutImportSummary(response);
            } else {
                return getPostImportSummary(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ImportSummary getPostImportSummary(Response response) {
        ImportSummary importSummary = null;
        try {
            String body = new StringConverter().fromBody(response.getBody(), String.class);
            Log.d(CLASS_TAG, body);
            importSummary = DhisController.getInstance().getObjectMapper().
                    readValue(body, ImportSummary.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        return importSummary;
    }

    private static ImportSummary getPutImportSummary(Response response) {
        ApiResponse apiResponse = null;
        try {
            String body = new StringConverter().fromBody(response.getBody(), String.class);
            Log.d(CLASS_TAG, body);
            apiResponse = DhisController.getInstance().getObjectMapper().
                    readValue(body, ApiResponse.class);
            if (apiResponse != null && apiResponse.getImportSummaries() != null && !apiResponse.getImportSummaries().isEmpty()) {
                return (apiResponse.getImportSummaries().get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        return null;
    }
}

