package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DoubleElementRow extends Row {
        private static final String EMPTY_FIELD = "";
        private static final String DATE_FORMAT = "yyyy-MM-dd";
        Row row1;
        Row row2;


        public DoubleElementRow(Row row1, Row row2){
            this.row1 = row1;
            this.row2 = row2;
        }

        @Override
        public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                            View convertView, ViewGroup container) {
            View view;
            org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DoubleElementRow.DoubleElementRowHolder holder;

            if (convertView != null && convertView.getTag() instanceof org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DoubleElementRow.DoubleElementRowHolder) {
                view = convertView;
                holder = (org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DoubleElementRow.DoubleElementRowHolder) view.getTag();
            } else {
                View root = inflater.inflate(
                        R.layout.listview_row_doubleelement, container, false);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);
                LinearLayout row1View = (LinearLayout) root.findViewById(R.id.row1);
                LinearLayout row2View = (LinearLayout) root.findViewById(R.id.row2);

                holder = new DoubleElementRowHolder(
                        row1.getView(fragmentManager,inflater,convertView,container),
                        row2.getView(fragmentManager,inflater,convertView,container));
                row1View.addView(holder.row1);
                row2View.addView(holder.row2);

                root.setTag(holder);
                view = root;
            }

            return view;
        }

        @Override
        public int getViewType() {
            return DataEntryRowTypes.DOUBLE_ELEMENT_ROW.ordinal();
        }


        private class DoubleElementRowHolder {
            final View row1;
            final View row2;

            public DoubleElementRowHolder(View row1,View row2) {
                this.row1 = row1;
                this.row2 = row2;
            }
        }





}
