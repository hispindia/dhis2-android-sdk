package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;

public final class NextButtonRow extends Row {
    public static final String CLASS_TAG = StatusRow.class.getSimpleName();

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        view = inflater.inflate(R.layout.listview_row_next,container,false);
        Button btnNext = (Button) view.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dhis2Application.getEventBus().post(new OnNextClick());
            }
        });
        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.EVENT_COORDINATES.ordinal();
    }
}
