package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.models.ProgramIndicator;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.OnDetailedInfoButtonClick;

public class SectionSeperatorRow extends Row{
    private static final String EMPTY_FIELD = "";


    private String value;

    public SectionSeperatorRow(String value) {
        //super(value);
        this.value = value;

    }

    public void setValue(String val){
        this.value = val;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.SECTION_SEPERATOR.ordinal();
    }



    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        ViewHolder holder;

        if (convertView != null && convertView.getTag() instanceof ViewHolder) {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        } else {
            View root = inflater.inflate(
//                    R.layout.listview_row_indicator, container, false);
                    R.layout.listview_row_section_seperator, container, false);

            holder = new ViewHolder(
                    (TextView) root.findViewById(R.id.section_name)
                    );

            root.setTag(holder);
            view = root;
        }

        holder.textLabel.setText(value);



        return view;
    }

    public static class ViewHolder {
        final TextView textLabel;


        public ViewHolder(TextView textLabel) {
            this.textLabel = textLabel;

        }
    }
}
