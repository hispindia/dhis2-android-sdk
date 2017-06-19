package org.hisp.dhis.android.sdk.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.raizlabs.android.dbflow.structure.Model;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.loaders.DbLoader;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitProgramRelationship;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Sourabh on 3/7/2017.
 */

public class OrgUnitDialogFragment3 extends AutoCompleteDialogFragment
        implements LoaderManager.LoaderCallbacks<OrgUnitDialogFragmentForm> {
    public static final int ID = 450126;
    private static final int LOADER_ID = 1;
    private static final String PROGRAMTYPE = "programType";

    private final String TAG = OrgUnitDialogFragment2.class.getSimpleName();
    private static  String orgId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if(bundle!=null){
            this.orgId = bundle.getString("orgid");
        }
        Log.e(TAG, "onCreate: 3rd" + orgId);
    }

    public static OrgUnitDialogFragment3 newInstance(OnOptionSelectedListener listener,
                                                     ProgramType... programKinds) {
        OrgUnitDialogFragment3 fragment = new OrgUnitDialogFragment3();
        Bundle args = new Bundle();
        if (programKinds != null) {
            String[] programKindStrings = new String[programKinds.length];
            for (int i = 0; i < programKinds.length; i++) {
                programKindStrings[i] = programKinds[i].name();
            }
            args.putStringArray(PROGRAMTYPE, programKindStrings);
        }
        fragment.setArguments(args);
        fragment.setOnOptionSetListener(listener);
        return fragment;
    }
    public static OrgUnitDialogFragment3 newInstance1(OnOptionSelectedListener listener,
                                                      ProgramType... programKinds) {
        OrgUnitDialogFragment3 fragment = new OrgUnitDialogFragment3();
        Bundle args = new Bundle();
        if (programKinds != null) {
            String[] programKindStrings = new String[programKinds.length];
            for (int i = 0; i < programKinds.length; i++) {
                programKindStrings[i] = programKinds[i].name();
            }
            args.putStringArray(PROGRAMTYPE, programKindStrings);
        }
        fragment.setArguments(args);
        fragment.setOnOptionSetListener(listener);
        return fragment;
    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        setDialogLabel(R.string.dialog_organisation_units);
//        setDialogId(ID);
//        mProgressBar.setVisibility(View.VISIBLE);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, getArguments(), this);
    }

    @Override
    public Loader<OrgUnitDialogFragmentForm> onCreateLoader(int id, Bundle args) {
        if (LOADER_ID == id && isAdded()) {
            List<Class<? extends Model>> modelsToTrack = new ArrayList<>();
            modelsToTrack.add(OrganisationUnitProgramRelationship.class);
            modelsToTrack.add(OrganisationUnit.class);
            modelsToTrack.add(Program.class);

            String[] kinds = args.getStringArray(PROGRAMTYPE);
            ProgramType[] types = null;
            if (kinds != null) {
                types = new ProgramType[kinds.length];
                for (int i = 0; i < kinds.length; i++) {
                    types[i] = ProgramType.valueOf(kinds[i]);
                }
            }
            return new DbLoader<>(
                    getActivity().getBaseContext(),
                    modelsToTrack,
                    new OrgUnitQuery(types)
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<OrgUnitDialogFragmentForm> loader,
                               OrgUnitDialogFragmentForm data) {
        if (loader.getId() == LOADER_ID) {
            getAdapter().swapData(data.getOptionAdapterValueList());

            if (MetaDataController.isDataLoaded(getActivity())) {
                mProgressBar.setVisibility(View.GONE);

                if(data.getType().equals(OrgUnitDialogFragmentForm.Error.NO_ASSIGNED_ORGANISATION_UNITS)) {
                    this.setNoItemsTextViewVisibility(View.VISIBLE);
                    this.setTextToNoItemsTextView(getString(R.string.no_organisation_units));
                }
                else if(data.getType().equals(OrgUnitDialogFragmentForm.Error.NO_PROGRAMS_TO_ORGANSATION_UNIT)) {
                    this.setNoItemsTextViewVisibility(View.VISIBLE);
                    this.setTextToNoItemsTextView(getString(R.string.no_programs));
                }
                else {
                    this.setNoItemsTextViewVisibility(View.GONE);
                    this.setTextToNoItemsTextView("");
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<OrgUnitDialogFragmentForm> loader) {
        getAdapter().swapData(null);
    }

    static class OrgUnitQuery implements Query<OrgUnitDialogFragmentForm> {

        private final ProgramType[] kinds;

        public OrgUnitQuery(ProgramType... kinds) {
            this.kinds = kinds;
        }

        @Override
        public OrgUnitDialogFragmentForm query(Context context) {
            OrgUnitDialogFragmentForm mForm = new OrgUnitDialogFragmentForm();

            List<OrganisationUnit> orgUnits = queryUnits(orgId);
            List<AutoCompleteDialogAdapter.OptionAdapterValue> values = new ArrayList<>();
            if(orgUnits.isEmpty()) {
                mForm.setType(OrgUnitDialogFragmentForm.Error.NO_ASSIGNED_ORGANISATION_UNITS);
            }
            else {
                for (OrganisationUnit orgUnit : orgUnits) {
                    values.add(new AutoCompleteDialogAdapter.OptionAdapterValue(orgUnit.getId(), orgUnit.getLabel()));
                    if (hasPrograms(orgUnit.getId(), this.kinds)) {
                        values.add(new AutoCompleteDialogAdapter.OptionAdapterValue(orgUnit.getId(), orgUnit.getLabel()));
                    } else {
                        mForm.setType(OrgUnitDialogFragmentForm.Error.NO_PROGRAMS_TO_ORGANSATION_UNIT);
                    }
                }
            }

            if(!values.isEmpty()) {
                Collections.sort(values);
                mForm.setType(OrgUnitDialogFragmentForm.Error.NONE); // if has values, no errors
            }

            mForm.setOrganisationUnits(orgUnits);
            mForm.setOptionAdapterValueList(values);
            return mForm;
        }

        private List<OrganisationUnit> queryUnits(String orgId) {
//            return MetaDataController
//                    .getAssignedOrganisationUnits();

            return MetaDataController
                    .getLevel6OrgUnitWithParentLevel5(orgId);
        }

        private boolean hasPrograms(String unitId, ProgramType... kinds) {
            List<Program> programs = MetaDataController
                    .getProgramsForOrganisationUnit(
                            unitId, kinds
                    );
            return (programs != null && !programs.isEmpty());
        }
    }
}