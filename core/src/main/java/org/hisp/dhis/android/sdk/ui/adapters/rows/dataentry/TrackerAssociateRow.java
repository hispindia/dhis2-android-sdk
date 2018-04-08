package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.ui.fragments.selectprogram.SelectProgramFragmentPreferences;
import org.hisp.dhis.android.sdk.ui.views.FontEditText;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class TrackerAssociateRow extends Row {
    private final String mLabel;
    private final TrackerAssociateRowActionListener rowActionListener;

    public TrackerAssociateRow(String label, boolean mandotory, String warning, BaseValue value){
        mLabel = label;
        this.mValue = value;
        this.mWarning = warning;
        this.mMandatory = mandotory;
        rowActionListener = null;

    }
    public TrackerAssociateRow(String label, boolean mandotory, String warning, BaseValue value,
                               TrackerAssociateRowActionListener rowActionListener){
        this.mLabel = label;
        this.mValue = value;
        this.mWarning = warning;
        this.mMandatory = mandotory;
        this.rowActionListener = rowActionListener;
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        TrackerAssociateHolder holder;
        if(convertView!=null && convertView.getTag() instanceof TrackerAssociateHolder){
            view = convertView;
            holder = (TrackerAssociateHolder) view.getTag();

        }else{
            View root = inflater.inflate(R.layout.listview_row_tracker_assosiate,container,false);
            TextView textLabel = (TextView) root.findViewById(R.id.text_label);
            TextView mandatoryIndicator = (TextView) root.findViewById(R.id.mandatory_indicator);
            TextView warningLabel = (TextView) root.findViewById(R.id.warning_label);
            TextView errorLabel = (TextView) root.findViewById(R.id.error_label);
            FontEditText editText = (FontEditText) root.findViewById(R.id.edit_text_row);
            ImageButton addnewButton = (ImageButton)root.findViewById(R.id.add_new_btn);
            addnewButton.setTag("ADD");

            ImageButton searchButton = (ImageButton)root.findViewById(R.id.search_btn);
            searchButton.setTag("SEARCH");

            ImageButton clearButton = (ImageButton)root.findViewById(R.id.clear_btn);
            clearButton.setTag("CLEAR");

            TrackerAssociateButtonsListener listener = new TrackerAssociateButtonsListener();
            holder = new TrackerAssociateHolder(root,textLabel,mandatoryIndicator,warningLabel
                    ,errorLabel,addnewButton,searchButton,clearButton,editText,listener);

            root.setTag(holder);
            view = root;



        }

        holder.editText.setEnabled(false);
        if(!isEditable()){
            holder.addBtn.setEnabled(false);
            holder.clearBtn.setEnabled(false);
            holder.searchBtn.setEnabled(false);
        }else{
            holder.addBtn.setEnabled(true);
            holder.clearBtn.setEnabled(true);
            holder.searchBtn.setEnabled(true);
        }

        holder.textLabel.setText(mLabel);
        holder.listener.setValue(mValue);
        holder.listener.setEditText(holder.editText);
        String stringValue = mValue.getValue();
        if(isEmpty(stringValue)){
            holder.editText.setText("");
        }else{
            holder.editText.setText(stringValue);
        }

        if(mWarning == null) {
            holder.warningLabel.setVisibility(View.GONE);
        } else {
            holder.warningLabel.setVisibility(View.VISIBLE);
            holder.warningLabel.setText(mWarning);
        }

        if(mError == null) {
            holder.errorLabel.setVisibility(View.GONE);
        } else {
            holder.errorLabel.setVisibility(View.VISIBLE);
            holder.errorLabel.setText(mError);
        }

        if(!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }

        if(this.rowActionListener!=null)holder.listener.setRowActionListener(this.rowActionListener);

        return view;

    }



    @Override
    public int getViewType(){
        return DataEntryRowTypes.TRACKER_ASSOCIATE.ordinal();
    }

    private class TrackerAssociateButtonsListener implements View.OnClickListener{
        private BaseValue value;
        private EditText editText;
        private TrackerAssociateRowActionListener rowActionListener;
        public void setValue(BaseValue value) {
            this.value = value;
        }

        public void setValue(String str){
            value.setValue(str);
            editText.setText(str);
            Dhis2Application.getEventBus().post(new RowValueChangedEvent(value, DataEntryRowTypes.TRACKER_ASSOCIATE.toString()));
        }

        public void setRowActionListener(TrackerAssociateRowActionListener rowActionListener) {
            this.rowActionListener = rowActionListener;

        }

        public void setEditText(EditText editText){
            this.editText = editText;
        }

        @Override
        public void onClick(View view) {
            switch ((String)view.getTag()){
                case "CLEAR":
                    setValue("");
//                    if(rowActionListener!=null){
//                        rowActionListener.clearButtonClicked();
//                        String vl = rowActionListener.getValue(TrackerAssociateRowActionListener.STATES.CLEAR);
//                    }
                    Toast.makeText(view.getRootView().getContext(),"Clear Button Pressed",Toast.LENGTH_SHORT).show();
                    break;

                case "SEARCH":
                    setValue("SEARCH");
//                    if(rowActionListener!=null){
//                        rowActionListener.searchButtonClicked();
//                        String vl = rowActionListener.getValue(TrackerAssociateRowActionListener.STATES.SEARCH);
//                    }
                    Toast.makeText(view.getRootView().getContext(),"Search Button Pressed",Toast.LENGTH_SHORT).show();
                    break;

                case "ADD":
                    //setValue("ADD");
                    if(rowActionListener!=null){
                        rowActionListener.addButtonClicked();
                        //String vl = rowActionListener.getValue(TrackerAssociateRowActionListener.STATES.ADD);
                        rowActionListener.setValueListeners(TrackerAssociateRowActionListener.STATES.ADD
                                , new ValueChangeListener() {
                                    @Override
                                    public void onValueChange(String newVal) {
                                        setValue(newVal);
                                    }
                                });
                    }

                    Toast.makeText(view.getRootView().getContext(),"Add Button Pressed",Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }




    private static class TrackerAssociateHolder {
        final View rootView;
        final TextView textLabel;
        final TextView mandatoryIndicator;
        final TextView warningLabel;
        final TextView errorLabel;
        final FontEditText editText;
        final ImageButton addBtn;
        final ImageButton searchBtn;
        final ImageButton clearBtn;
        final TrackerAssociateRow.TrackerAssociateButtonsListener listener;

        public TrackerAssociateHolder(View rootView, TextView textLabel, TextView mandatoryIndicator, TextView warningLabel, TextView errorLabel, ImageButton addBtn, ImageButton searchBtn, ImageButton clearBtn,FontEditText editText, TrackerAssociateButtonsListener listener) {
            this.rootView = rootView;
            this.textLabel = textLabel;
            this.mandatoryIndicator = mandatoryIndicator;
            this.warningLabel = warningLabel;
            this.errorLabel = errorLabel;
            this.editText = editText;
            this.addBtn = addBtn;
            this.searchBtn = searchBtn;
            this.clearBtn = clearBtn;
            this.listener = listener;
            addBtn.setOnClickListener(listener);
            searchBtn.setOnClickListener(listener);
            clearBtn.setOnClickListener(listener);
        }
    }
}
