package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow;

import android.os.Bundle;
import android.view.View;

import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.Option$Table;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.ui.dialogs.AutoCompleteDialogAdapter;
import org.hisp.dhis.android.sdk.ui.dialogs.AutoCompleteDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class OptionDialogFragment extends AutoCompleteDialogFragment {
    private static final String EXTRA_OPTIONSET = "extra:optionsset";
    private static final String VI_LANG= "vi";
    private static final String TZ_LANG= "sw";
    private static final String IN_LANG= "in";
    public static OptionDialogFragment newInstance(String optionSetId,
                                                   OnOptionSelectedListener listener) {
        OptionDialogFragment dialogFragment = new OptionDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_OPTIONSET, optionSetId);
        dialogFragment.setArguments(args);
        dialogFragment.setOnOptionSetListener(listener);
        return dialogFragment;
    }

    private List<AutoCompleteDialogAdapter.OptionAdapterValue> getOptions() {
        List<AutoCompleteDialogAdapter.OptionAdapterValue> values = new ArrayList<>();
        String optionSetId = getArguments().getString(EXTRA_OPTIONSET);
        List<Option> options = new Select(Option$Table.NAME).from(Option.class).
                where(Condition.column(Option$Table.OPTIONSET).
                        is(optionSetId)).orderBy(Option$Table.SORTINDEX).queryList();
        if (options != null && !options.isEmpty()) {
            for (Option option : options) {
                values.add(new AutoCompleteDialogAdapter.OptionAdapterValue(option.getName(), option.getName()));
            }
        }
        return values;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final UserAccount uslocal= MetaDataController.getUserLocalLang();
        String user_locallang=uslocal.getUserSettings().toString();
        String localdblang=user_locallang;
        if(localdblang.equals(VI_LANG))
        {
            setDialogLabel("Tìm kiếm");
        }
        else if(localdblang.equals(TZ_LANG))
        {
            setDialogLabel("Pata Chaguo");
        }
        else if(localdblang.equals(IN_LANG))
        {
            setDialogLabel("Temukan opsi");
        }
        else if(localdblang.equals("my"))
        {
            setDialogLabel("ေရြးခ်ယ္မႈမ်ားေတြ႕ျခင္း");
        }
        else
        {
            setDialogLabel(R.string.find_option);
        }

        getAdapter().swapData(getOptions());
    }
}
