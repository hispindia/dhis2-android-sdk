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

package org.hisp.dhis.android.sdk.ui.fragments.common;

import android.app.ProgressDialog;
import android.util.Log;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.DhisController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.ProgramRule;
import org.hisp.dhis.android.sdk.persistence.models.ProgramRuleAction;
import org.hisp.dhis.android.sdk.persistence.models.ProgramRuleVariable;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.ui.fragments.eventdataentry.EventDataEntryRuleHelper;
import org.hisp.dhis.android.sdk.utils.comparators.ProgramRulePriorityComparator;
import org.hisp.dhis.android.sdk.utils.services.ProgramRuleService;
import org.hisp.dhis.android.sdk.utils.services.VariableService;
import org.hisp.dhis.android.sdk.utils.support.DateUtils;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.AbsDatePickerRow.EMPTY_FIELD;


/**
 * Abstract Fragment that can be extended by Fragments that want to make use of Program Rules.
 *
 * @param <D>
 */
public abstract class AbsProgramRuleFragment<D> extends BaseFragment {

    private static final String TAG = AbsProgramRuleFragment.class.getSimpleName();
    protected IProgramRuleFragmentHelper programRuleFragmentHelper;
    private ProgressDialog progressDialog;
    private static final String QUARANTINE_SCHEDULER = "SH5ad8iQpQB";
    private static final String QUARANTINE="IXdxLjRSFT8";

    public IProgramRuleFragmentHelper getProgramRuleFragmentHelper() {
        return programRuleFragmentHelper;
    }

    public void setProgramRuleFragmentHelper(IProgramRuleFragmentHelper programRuleFragmentHelper) {
        this.programRuleFragmentHelper = programRuleFragmentHelper;
    }

    /**
     * Evaluates the ProgramRules for the current program and the current data values and applies
     * the results. This is for example used for hiding views if a rule contains skip logic
     * If no rules exist for Enrollment, this won't be run
     */
    public void evaluateAndApplyProgramRules() {

        if (programRuleFragmentHelper == null ||
                programRuleFragmentHelper.getEnrollment() == null ||
                programRuleFragmentHelper.getEnrollment().getProgram() == null ||
                programRuleFragmentHelper.getEnrollment().getProgram().getProgramRules() == null ||
                programRuleFragmentHelper.getEnrollment().getProgram().getProgramRules().isEmpty()) {
            return;
        }
        if (programRuleFragmentHelper.blockingSpinnerNeeded()) {
            //showBlockingProgressBar();
        }
        VariableService.initialize(programRuleFragmentHelper.getEnrollment(), programRuleFragmentHelper.getEvent());
        programRuleFragmentHelper.mapFieldsToRulesAndIndicators();
        ArrayList<String> affectedFieldsWithValue = new ArrayList<>();

        List<ProgramRule> programRules = programRuleFragmentHelper.getProgramRules();

        Collections.sort(programRules, new ProgramRulePriorityComparator());
        for (ProgramRule programRule : programRules) {
            try {
                boolean evaluatedTrue = ProgramRuleService.evaluate(programRule.getCondition());
                Log.d("PROGRAM RULE", "evaluating program rule");
                Log.d("RULE " ,  programRule.getCondition());
//                Log.d("Element " , programRule.getProgramRuleActions().get(0).getDataElement());
                for (ProgramRuleAction action : programRule.getProgramRuleActions()) {
                    if (evaluatedTrue) {
                        applyProgramRuleAction(action, affectedFieldsWithValue);
                    }
                }
            } catch (Exception e) {
                Log.e("PROGRAM RULE", "Error evaluating program rule", e);
            }
        }

        if(programRuleFragmentHelper.getEvent() != null && programRuleFragmentHelper.getEvent().getProgramStageId().equals(QUARANTINE_SCHEDULER)){
            List<DataValue> dataValues = programRuleFragmentHelper.getEvent().getDataValues();
            List<Event> events = programRuleFragmentHelper.getEnrollment().getEvents(true);
            Map<String,Event> mappedEvents = new HashMap<String,Event>();
            List<String> listOfDates = new ArrayList<>();
            for(Event event:events){
                if(event.getProgramStageId().equals(QUARANTINE)){
                    mappedEvents.put(event.getEventDate(),event);
                }
            }
            String quarantineEndDate = programRuleFragmentHelper.getDataElementValue("wqxQiTEfioS").getValue();
            if(dataValues.size()>1 && quarantineEndDate.length()>0){


                for(DataValue dv :dataValues){
                    switch(dv.getDataElement()){
                        case "RdNf0Z7OsMe": //checkin 1
//                            }else
                            if(!dv.getValue().equals("")){
                                listOfDates.add(dv.getValue());
                            }
                            break;

                        case "Gdv9G1PsASA"://checkin 2
                            if(!dv.getValue().equals("")){
                                listOfDates.add(dv.getValue());
                            }
                            break;


                        case "sJUq4qKJRD8"://checkin 3
                            if(!dv.getValue().equals("")){
                                listOfDates.add(dv.getValue());
                            }
                            break;

                        case "RJ1Fi9WdGfO"://checkin 4
                            if(!dv.getValue().equals("")){
                                listOfDates.add(dv.getValue());
                            }
                            break;

                        case "Ghq39zfrljF": //checkin 5
//
                            if(!dv.getValue().equals("")){
                                listOfDates.add(dv.getValue());
                            }
                            break;


                        case "PXFKBOkzPa3"://checkin 6
//
                            if(!dv.getValue().equals("")){
                                listOfDates.add(dv.getValue());
                            }
                            break;
                    }
                }

                for(Event event:events){
                    if(event.getProgramStageId().equals(QUARANTINE)){
                        if(!listOfDates.contains(event.getEventDate())){
                            event.delete();
                        }
                    }
                }

                for(String date:listOfDates){
                    if(mappedEvents.get(date)==null){
                        Event event =  getEvent(programRuleFragmentHelper.getEnrollment().getOrgUnit(),
                                        programRuleFragmentHelper.getEnrollment().getProgram().getUid()
                                        ,-1,programRuleFragmentHelper.getEnrollment().getLocalId()
                                        ,MetaDataController.getProgramStage(QUARANTINE),
                                        DhisController.getInstance().getSession().getCredentials().getUsername());
                                event.setDueDate(quarantineEndDate);
                                event.setEventDate(date);
                                event.save();
                    }
                }

            }
        }

        if(programRuleFragmentHelper instanceof EventDataEntryRuleHelper){
            ((EventDataEntryRuleHelper)programRuleFragmentHelper).freezDataEntry();
        }


        if (!affectedFieldsWithValue.isEmpty()) {
            programRuleFragmentHelper.showWarningHiddenValuesDialog(programRuleFragmentHelper.getFragment(), affectedFieldsWithValue);
        }
        //hideBlockingProgressBar();
        programRuleFragmentHelper.updateUi();
    }

    private Event getEvent(String orgUnitId, String programId, long eventId, long enrollmentId,
                           ProgramStage programStage, String username) {
        Event event;
        if (eventId < 0) {
            event = new Event(orgUnitId, Event.STATUS_ACTIVE, programId, programStage, null, null, null);
            if (enrollmentId > 0) {
                Enrollment enrollment = TrackerController.getEnrollment(enrollmentId);
                if (enrollment != null) {
                    event.setLocalEnrollmentId(enrollmentId);
                    event.setEnrollment(enrollment.getEnrollment());
                    event.setTrackedEntityInstance(enrollment.getTrackedEntityInstance());
                    LocalDate dueDate = new LocalDate(DateUtils.parseDate(enrollment.getEnrollmentDate())).plusDays(programStage.getMinDaysFromStart());
                    event.setDueDate(dueDate.toString());
                }
            }
            List<DataValue> dataValues = new ArrayList<>();
            for (ProgramStageDataElement dataElement : programStage.getProgramStageDataElements()) {
                dataValues.add(
                        new DataValue(event, EMPTY_FIELD, dataElement.getDataelement(), false, username)
                );
            }
            event.setDataValues(dataValues);
        } else {
            event = TrackerController.getEvent(eventId);
            if(event == null) {
                getEvent(orgUnitId, programId, -1, enrollmentId, programStage, username); // if event is null, create a new one
            }
        }
        return event;
    }

    protected void applyProgramRuleAction(ProgramRuleAction programRuleAction, List<String> affectedFieldsWithValue) {

        switch (programRuleAction.getProgramRuleActionType()) {
            case HIDEFIELD: {
                Log.i("Apply programrule:", "HIDEFIELD");
                programRuleFragmentHelper.applyHideFieldRuleAction(programRuleAction, affectedFieldsWithValue);
                break;
            }
            case HIDESECTION: {
                Log.i("Apply programrule:", "HIDESECTION");
                programRuleFragmentHelper.applyHideSectionRuleAction(programRuleAction);
                break;
            }
            case SHOWWARNING: {
                Log.i("Apply programrule:", "SHOWWARNING");
                programRuleFragmentHelper.applyShowWarningRuleAction(programRuleAction);
                break;
            }
            case SHOWERROR: {
                Log.i("Apply programrule:", "SHOWERROR");
                programRuleFragmentHelper.applyShowErrorRuleAction(programRuleAction);
                break;
            }
            case ASSIGN: {
                Log.i("Apply programrule:", "ASSIGN");
                applyAssignRuleAction(programRuleAction);
                break;
            }
            case CREATEEVENT: {
                Log.i("Apply programrule:", "CREATEEVENT");
                programRuleFragmentHelper.applyCreateEventRuleAction(programRuleAction);
                break;
            }
            case DISPLAYKEYVALUEPAIR: {
                Log.i("Apply programrule:", "DISPLAYKEYVALUEPAIR");
                programRuleFragmentHelper.applyDisplayKeyValuePairRuleAction(programRuleAction);
                break;
            }
            case DISPLAYTEXT: {
                Log.i("Apply programrule:", "DISPLAYTEXT");
                programRuleFragmentHelper.applyDisplayTextRuleAction(programRuleAction);
                break;
            }
            case ERRORONCOMPLETE: {
                Log.i("Apply programrule:", "ERRORONCOMPLETE");
                programRuleFragmentHelper.applyErrorOnCompleteRuleAction(programRuleAction);
                break;
            }
            case HIDEPROGRAMSTAGE: {
                Log.i("Apply programrule:", "HIDEPROGRAMSTAGE");
                programRuleFragmentHelper.applyHideProgramStageRuleAction(programRuleAction);
                break;
            }
            case SETMANDATORYFIELD: {
                Log.i("Apply programrule:", "SETMANDATORYFIELD");
                programRuleFragmentHelper.applySetMandatoryFieldRuleAction(programRuleAction);
                break;
            }
            case WARNINGONCOMPLETE: {
                Log.i("Apply programrule:", "WARNINGONCOMPLETE");
                programRuleFragmentHelper.applyWarningOnCompleteRuleAction(programRuleAction);
                break;
            }
        }
    }

    protected void applyAssignRuleAction(ProgramRuleAction programRuleAction) {
        String stringResult = ProgramRuleService.getCalculatedConditionValue(programRuleAction.getData());
        String programRuleVariableName = programRuleAction.getContent();
        ProgramRuleVariable programRuleVariable;
        if (programRuleVariableName != null) {
            programRuleVariableName = programRuleVariableName.substring(2, programRuleVariableName.length() - 1);
            programRuleVariable = VariableService.getInstance().getProgramRuleVariableMap().get(programRuleVariableName);
            programRuleVariable.setVariableValue(stringResult);
            programRuleVariable.setHasValue(true);
        }
        String dataElementId = programRuleAction.getDataElement();
        if (dataElementId != null) {
            DataValue dataValue = programRuleFragmentHelper.getDataElementValue(dataElementId);
            if (dataValue != null) {
                dataValue.setValue(stringResult);
                programRuleFragmentHelper.flagDataChanged(true);
                programRuleFragmentHelper.saveDataElement(dataElementId);
            }
        }
        String trackedEntityAttributeId = programRuleAction.getTrackedEntityAttribute();
        if (trackedEntityAttributeId != null) {
            TrackedEntityAttributeValue trackedEntityAttributeValue = programRuleFragmentHelper.getTrackedEntityAttributeValue(trackedEntityAttributeId);
            if (trackedEntityAttributeValue != null) {
                trackedEntityAttributeValue.setValue(stringResult);
                programRuleFragmentHelper.flagDataChanged(true);
                programRuleFragmentHelper.saveTrackedEntityAttribute(trackedEntityAttributeId);
            }
        }
        programRuleFragmentHelper.disableCalculatedFields(programRuleAction);
    }
    
    public void showBlockingProgressBar() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null && isAdded()) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        progressDialog = ProgressDialog.show(
                                getContext(), "", getString(R.string.please_wait), true, false);
                    }
                }
            });
        }
    }

    public void hideBlockingProgressBar() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null && isAdded()) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                            progressDialog.dismiss();
                        } else {
                            Log.w("HIDE PROGRESS",
                                    "Unable to hide progress dialog: AbsProgramRuleFragment.progressDialog is null");
                        }
                    }
                }
            });
        }
    }
}