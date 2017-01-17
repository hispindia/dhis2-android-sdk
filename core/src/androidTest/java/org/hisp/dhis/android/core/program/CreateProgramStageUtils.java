package org.hisp.dhis.android.core.program;

import android.content.ContentValues;

import org.hisp.dhis.android.core.common.FormType;

public class CreateProgramStageUtils {

    private static final String CODE = "test_code";
    private static final String NAME = "test_name";
    private static final String DISPLAY_NAME = "test_display_name";

    private static final String EXECUTION_DATE_LABEL = "test_executionDateLabel";
    private static final Integer ALLOW_GENERATE_NEXT_VISIT = 0;
    private static final Integer VALID_COMPLETE_ONLY = 0;
    private static final String REPORT_DATE_TO_USE = "test_reportDateToUse";
    private static final Integer OPEN_AFTER_ENROLLMENT = 0;

    private static final Integer REPEATABLE = 0;
    private static final Integer CAPTURE_COORDINATES = 1;
    private static final FormType FORM_TYPE = FormType.DEFAULT;
    private static final Integer DISPLAY_GENERATE_EVENT_BOX = 1;
    private static final Integer GENERATED_BY_ENROLMENT_DATE = 1;
    private static final Integer AUTO_GENERATE_EVENT = 0;
    private static final Integer SORT_ORDER = 0;
    private static final Integer HIDE_DUE_DATE = 1;
    private static final Integer BLOCK_ENTRY_FORM = 0;
    private static final Integer MIN_DAYS_FROM_START = 5;
    private static final Integer STANDARD_INTERVAL = 7;

    // used for timestamps
    private static final String DATE = "2017-01-05T15:39:00.000";

    public static ContentValues create(long id, String uid, String programId) {
        ContentValues programStage = new ContentValues();
        programStage.put(ProgramStageModel.Columns.ID, id);
        programStage.put(ProgramStageModel.Columns.UID, uid);
        programStage.put(ProgramStageModel.Columns.CODE, CODE);
        programStage.put(ProgramStageModel.Columns.NAME, NAME);
        programStage.put(ProgramStageModel.Columns.DISPLAY_NAME, DISPLAY_NAME);
        programStage.put(ProgramStageModel.Columns.CREATED, DATE);
        programStage.put(ProgramStageModel.Columns.LAST_UPDATED, DATE);
        programStage.put(ProgramStageModel.Columns.EXECUTION_DATE_LABEL, EXECUTION_DATE_LABEL);
        programStage.put(ProgramStageModel.Columns.ALLOW_GENERATE_NEXT_VISIT, ALLOW_GENERATE_NEXT_VISIT);
        programStage.put(ProgramStageModel.Columns.VALID_COMPLETE_ONLY, VALID_COMPLETE_ONLY);
        programStage.put(ProgramStageModel.Columns.REPORT_DATE_TO_USE, REPORT_DATE_TO_USE);
        programStage.put(ProgramStageModel.Columns.OPEN_AFTER_ENROLLMENT, OPEN_AFTER_ENROLLMENT);
        programStage.put(ProgramStageModel.Columns.REPEATABLE, REPEATABLE);
        programStage.put(ProgramStageModel.Columns.CAPTURE_COORDINATES, CAPTURE_COORDINATES);
        programStage.put(ProgramStageModel.Columns.FORM_TYPE, FORM_TYPE.name());
        programStage.put(ProgramStageModel.Columns.DISPLAY_GENERATE_EVENT_BOX, DISPLAY_GENERATE_EVENT_BOX);
        programStage.put(ProgramStageModel.Columns.GENERATED_BY_ENROLMENT_DATE, GENERATED_BY_ENROLMENT_DATE);
        programStage.put(ProgramStageModel.Columns.AUTO_GENERATE_EVENT, AUTO_GENERATE_EVENT);
        programStage.put(ProgramStageModel.Columns.SORT_ORDER, SORT_ORDER);
        programStage.put(ProgramStageModel.Columns.HIDE_DUE_DATE, HIDE_DUE_DATE);
        programStage.put(ProgramStageModel.Columns.BLOCK_ENTRY_FORM, BLOCK_ENTRY_FORM);
        programStage.put(ProgramStageModel.Columns.MIN_DAYS_FROM_START, MIN_DAYS_FROM_START);
        programStage.put(ProgramStageModel.Columns.STANDARD_INTERVAL, STANDARD_INTERVAL);
        programStage.put(ProgramStageModel.Columns.PROGRAM, programId);

        return programStage;
    }
}
