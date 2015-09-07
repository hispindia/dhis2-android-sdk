package org.hisp.dhis.android.sdk.models.metadata;

import org.hisp.dhis.android.sdk.models.common.IIdentifiableObjectStore;

import java.util.List;

public interface IProgramStageStore extends IIdentifiableObjectStore<ProgramStage> {
    List<ProgramStage> query(Program program);
}