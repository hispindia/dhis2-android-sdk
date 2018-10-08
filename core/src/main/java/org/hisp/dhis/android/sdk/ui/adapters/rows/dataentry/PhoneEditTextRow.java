package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;


import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow.TextRow;

public class PhoneEditTextRow extends TextRow {
    private static String rowTypeTemp;
    private static final String TZ_LANG= "sw";
    private static final String VI_LANG= "vi";
    private static final String MY_LANG= "my";
    private static final String IN_LANG= "in";
    private static final String TZ_PHONE= "ingiza namba ya simu";
    private static final String VI_PHONE= "Nhập số điện thoại";
    public PhoneEditTextRow(String label, boolean mandatory, String warning,
                            BaseValue baseValue,
                            DataEntryRowTypes rowType) {
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;
        mValue = baseValue;
        mRowType = rowType;

        if (!DataEntryRowTypes.PHONE_NUMBER.equals(rowType)) {
            throw new IllegalArgumentException("Unsupported row type");
        }
        checkNeedsForDescriptionButton();
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.PHONE_NUMBER.ordinal();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        final ValueEntryHolder holder;

        if (convertView != null && convertView.getTag() instanceof ValueEntryHolder) {
            view = convertView;
            holder = (ValueEntryHolder) view.getTag();
            holder.listener.onRowReused();
        } else {
            View root = inflater.inflate(R.layout.listview_row_edit_text, container, false);
            TextView label = (TextView) root.findViewById(R.id.text_label);
            TextView mandatoryIndicator = (TextView) root.findViewById(R.id.mandatory_indicator);
            TextView warningLabel = (TextView) root.findViewById(R.id.warning_label);
            TextView errorLabel = (TextView) root.findViewById(R.id.error_label);
            EditText editText = (EditText) root.findViewById(R.id.edit_text_row);

//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);
            final UserAccount uslocal= MetaDataController.getUserLocalLang();
            String user_locallang=uslocal.getUserSettings().toString();
            String localdblang=user_locallang;
            if(localdblang.equals(TZ_LANG))
            {
                editText.setHint(TZ_PHONE);
            }
            else if(localdblang.equals(VI_LANG))
            {
                editText.setHint(VI_PHONE);
            }
            else if(localdblang.equals(IN_LANG))
            {
                editText.setHint("Masukkan nomor telepon");
            }
            else if(localdblang.equals(MY_LANG))
            {
                editText.setHint("ဖုန္းနံပါတ္ထည့္ျခင္း");
            }
            else
            {
                editText.setHint(R.string.enter_phone_number);
            }
            editText.setInputType(InputType.TYPE_CLASS_PHONE);

            editText.setSingleLine(true);

            OnTextChangeListener listener = new OnTextChangeListener();
            listener.setRow(this);
            listener.setRowType(rowTypeTemp);
            holder = new ValueEntryHolder(label, mandatoryIndicator, warningLabel, errorLabel, editText, listener);
            holder.listener.setBaseValue(mValue);
            holder.editText.addTextChangedListener(listener);

            rowTypeTemp = mRowType.toString();
            root.setTag(holder);
            view = root;
        }

        //when recycling views we don't want to keep the focus on the edittext
        //holder.editText.clearFocus();

        if (!isEditable()) {
            holder.editText.setEnabled(false);
        } else {
            holder.editText.setEnabled(true);
        }

        if (isShouldNeverBeEdited()) {
            holder.editText.setEnabled(false);
        }

        holder.textLabel.setText(mLabel);
        holder.listener.setBaseValue(mValue);
//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));

        holder.editText.setText(mValue.getValue());
        holder.editText.setSelection(holder.editText.getText().length());

//        if(isDetailedInfoButtonHidden()) {
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }

        if (mWarning == null) {
            holder.warningLabel.setVisibility(View.GONE);
        } else {
            holder.warningLabel.setVisibility(View.VISIBLE);
            holder.warningLabel.setText(mWarning);
        }

        if (mError == null) {
            holder.errorLabel.setVisibility(View.GONE);
        } else {
            holder.errorLabel.setVisibility(View.VISIBLE);
            holder.errorLabel.setText(mError);
        }

        if (!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }

        holder.editText.setOnEditorActionListener(mOnEditorActionListener);

        return view;
    }

}
