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

package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.realm.ROrganisationHelper;
import org.hisp.dhis.android.sdk.controllers.realm.ROrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.AbsTextWatcher;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.ui.nhancv.ItemClickListener;
import org.hisp.dhis.android.sdk.ui.nhancv.Model;
import org.hisp.dhis.android.sdk.ui.nhancv.OptionDialog;

import java.util.ArrayList;
import java.util.List;

public class EditTextRow extends Row {
    private static final String TAG = EditTextRow.class.getSimpleName();
    public static String ORG_LEVEL_3 = "";
    public static String ORG_LEVEL_5 = "";
    public static String ORG_LEVEL_6 = "";

    private static final String EMPTY_FIELD = "";
    private static int LONG_TEXT_LINE_COUNT = 3;
    private static String rowTypeTemp;

    public EditTextRow(String label, boolean mandatory, String warning, BaseValue baseValue, DataEntryRowTypes rowType) {
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;
        mValue = baseValue;
        mRowType = rowType;

        if (!DataEntryRowTypes.TEXT.equals(rowType) &&
                !DataEntryRowTypes.LONG_TEXT.equals(rowType) &&
                !DataEntryRowTypes.NUMBER.equals(rowType) &&
                !DataEntryRowTypes.INTEGER.equals(rowType) &&
                !DataEntryRowTypes.INTEGER_NEGATIVE.equals(rowType) &&
                !DataEntryRowTypes.INTEGER_ZERO_OR_POSITIVE.equals(rowType) &&
                !DataEntryRowTypes.PHONE_NUMBER.equals(rowType) &&
                !DataEntryRowTypes.PINCODE.equals(rowType) &&
                !DataEntryRowTypes.PATIENTNAME.equals(rowType) &&
                !DataEntryRowTypes.AGE.equals(rowType) &&
                !DataEntryRowTypes.INTEGER_POSITIVE.equals(rowType) &&
                !DataEntryRowTypes.ORGANISATION_UNIT.equals(rowType)) {
            throw new IllegalArgumentException("Unsupported row type");
        }
        checkNeedsForDescriptionButton();

    }

    @Override
    public View getView(final FragmentManager fragmentManager, LayoutInflater inflater,
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
            final EditText editText = (EditText) root.findViewById(R.id.edit_text_row);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);

            if (DataEntryRowTypes.TEXT.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                editText.setHint(R.string.enter_text);
                editText.setSingleLine(true);
            } else if (DataEntryRowTypes.LONG_TEXT.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                editText.setHint(R.string.enter_long_text);
                editText.setLines(LONG_TEXT_LINE_COUNT);
            } else if (DataEntryRowTypes.NUMBER.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                editText.setHint(R.string.enter_number);
                editText.setSingleLine(true);
            } else if (DataEntryRowTypes.INTEGER.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                editText.setHint(R.string.enter_integer);
                editText.setSingleLine(true);
            } else if (DataEntryRowTypes.INTEGER_NEGATIVE.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_SIGNED);
                editText.setHint(R.string.enter_negative_integer);
                editText.setFilters(new InputFilter[]{new NegInpFilter()});
                editText.setSingleLine(true);
            } else if (DataEntryRowTypes.INTEGER_ZERO_OR_POSITIVE.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setHint(R.string.enter_positive_integer_or_zero);
                editText.setFilters(new InputFilter[]{new PosOrZeroFilter()});
                editText.setSingleLine(true);
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(2);
                editText.setFilters(filterArray);
            } else if (DataEntryRowTypes.INTEGER_POSITIVE.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setHint(R.string.enter_positive_integer);
                editText.setFilters(new InputFilter[]{new PosFilter()});
                editText.setSingleLine(true);
            }
            else if(DataEntryRowTypes.PHONE_NUMBER.equals(mRowType)) {
                System.out.println("dataentryrowtype:"+DataEntryRowTypes.PHONE_NUMBER);
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
                editText.setHint(R.string.enter_phone_number);
                editText.setSingleLine(true);
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(10);
                editText.setFilters(filterArray);
            }

            else if(DataEntryRowTypes.PINCODE.equals(mRowType)) {
                System.out.println("dataentryrowtype:"+DataEntryRowTypes.PINCODE);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setHint(R.string.enter_pincode);
                editText.setSingleLine(true);
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(6);
                editText.setFilters(filterArray);
            }

            else if(DataEntryRowTypes.PATIENTNAME.equals(mRowType)) {
                System.out.println("dataentryrowtype:"+DataEntryRowTypes.PINCODE);
                editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                editText.setHint(R.string.enter_person);
                editText.setSingleLine(true);

                editText.setFilters(new InputFilter[] {
                        new InputFilter() {
                            @Override
                            public CharSequence filter(CharSequence cs, int start,
                                                       int end, Spanned spanned, int dStart, int dEnd) {
                                // TODO Auto-generated method stub
                                if(cs.equals("")){ // for backspace
                                    return cs;
                                }
                                if(cs.toString().matches("[a-zA-Z ]+")){
                                    return cs;
                                }
                                return "";
                            }
                        }
                });
            }
            else if(DataEntryRowTypes.AGE.equals(mRowType)) {
                System.out.println("dataentryrowtype:"+DataEntryRowTypes.AGE);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setHint(R.string.enter_age1);
                editText.setSingleLine(true);
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(2);

                editText.setFilters(filterArray);
            }
            else if(DataEntryRowTypes.ORGANISATION_UNIT.equals(mRowType)) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
                editText.setHint(R.string.organisation_unit_select);
                editText.setSingleLine(true);

                //@nhancv TODO: logic to show dialog here
                if (mLabel.toLowerCase().contains("state") ||
                    mLabel.toLowerCase().contains("district") ||
                    mLabel.toLowerCase().contains("block") ||
                    mLabel.toLowerCase().contains("village")) {
                    editText.setFocusable(false);
                    editText.setClickable(true);
                    editText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final List<Model> modelList = new ArrayList<>();
                            List<ROrganisationUnit> orgUnitList = new ArrayList<>();
                            if (mLabel.toLowerCase().contains("state")) {
                                orgUnitList = ROrganisationHelper.getOrgFromLocalByLevel(3);
                            } else if (mLabel.toLowerCase().contains("district")) {
                                orgUnitList = ROrganisationHelper.getOrgFromLocalByLevel(ORG_LEVEL_3, 5);
                            } else if (mLabel.toLowerCase().contains("block")) {
                                orgUnitList = ROrganisationHelper.getOrgFromLocalByLevel(ORG_LEVEL_5, 6);
                            } else if (mLabel.toLowerCase().contains("village")) {
                                orgUnitList = ROrganisationHelper.getOrgFromLocalByLevel(ORG_LEVEL_6, 7);
                            }

                            //convert to list Model
                            for (final ROrganisationUnit rOrganisationUnit : orgUnitList) {
                                modelList.add(OptionDialog.createModel(rOrganisationUnit.getId(),
                                                                       rOrganisationUnit.getDisplayName()));
                            }

                            OptionDialog.newInstance(modelList, new ItemClickListener<Model>() {
                                @Override
                                public void onItemClick(Model model) {
                                    Log.e(TAG, "onClick:id " + model.getId());
                                    Log.e(TAG, "onClick:getDisplayName " + model.getDisplayName());
                                    if (mLabel.toLowerCase().contains("state")) {
                                        ORG_LEVEL_3 = model.getId();
                                    } else if (mLabel.toLowerCase().contains("district")) {
                                        ORG_LEVEL_5 = model.getId();
                                    } else if (mLabel.toLowerCase().contains("block")) {
                                        ORG_LEVEL_6 = model.getId();
                                    }
                                    editText.setText(model.getDisplayName());
                                }
                            }).show(fragmentManager);
                        }
                    });
                }

            }

            OnTextChangeListener listener = new OnTextChangeListener();
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

        return view;
    }

    @Override
    public int getViewType() {
        return mRowType.ordinal();
    }

    private static class ValueEntryHolder {
        final TextView textLabel;
        final TextView mandatoryIndicator;
        final TextView warningLabel;
        final TextView errorLabel;
        final EditText editText;
        //        final View detailedInfoButton;
        final OnTextChangeListener listener;

        public ValueEntryHolder(TextView textLabel,
                                TextView mandatoryIndicator, TextView warningLabel,
                                TextView errorLabel, EditText editText,
                                OnTextChangeListener listener) {
            this.textLabel = textLabel;
            this.mandatoryIndicator = mandatoryIndicator;
            this.warningLabel = warningLabel;
            this.errorLabel = errorLabel;
            this.editText = editText;
//            this.detailedInfoButton = detailedInfoButton;
            this.listener = listener;
        }
    }

    private class OnTextChangeListener extends AbsTextWatcher {
        private BaseValue value;
        RunProgramRulesDelayedDispatcher runProgramRulesDelayedDispatcher = new RunProgramRulesDelayedDispatcher();

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        public void onRowReused() {
            if (runProgramRulesDelayedDispatcher != null) {
                runProgramRulesDelayedDispatcher.dispatchNow();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            String newValue = s != null ? s.toString() : EMPTY_FIELD;
            if (!newValue.equals(value.getValue())) {
                //newValue = removeInvalidDecimalSeparatorsFromNumberRows(newValue);
                value.setValue(newValue);
                RowValueChangedEvent rowValueChangeEvent = new RowValueChangedEvent(value, EditTextRow.rowTypeTemp);
                rowValueChangeEvent.setRow(EditTextRow.this);
                Dhis2Application.getEventBus().post(rowValueChangeEvent);
                runProgramRulesDelayedDispatcher.dispatchDelayed(new RunProgramRulesEvent(value));
            }
        }
    }

    /**
     * Number fields should never start or end with the decimal separator "."
     *
     * @param value The text that is currently in the edit text field
     * @return Clean text with trailing dots removed or a zero added at the start of the string if
     * it starts with a dot
     */
    @NonNull
    private static String removeInvalidDecimalSeparatorsFromNumberRows(String value) {
        if (rowTypeTemp.equals(DataEntryRowTypes.NUMBER.name())) {
            if (value.endsWith(".")) {
                value = value.substring(0, value.length() - 1);
            }
            if (value.startsWith(".")) {
                value = String.format("0%s", value);
            }
        }
        return value;
    }

    private static class NegInpFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence str, int start, int end,
                                   Spanned spn, int spnStart, int spnEnd) {

            if ((str.length() > 0) && (spnStart == 0) && (str.charAt(0) != '-')) {
                return EMPTY_FIELD;
            }

            return str;
        }
    }

    private static class PosOrZeroFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence str, int start, int end,
                                   Spanned spn, int spStart, int spEnd) {

            if ((str.length() > 0) && (spn.length() > 0) && (spn.charAt(0) == '0')) {
                return EMPTY_FIELD;
            }

            if ((spn.length() > 0) && (spStart == 0)
                    && (str.length() > 0) && (str.charAt(0) == '0')) {
                return EMPTY_FIELD;
            }

            return str;
        }
    }

    private static class PosFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence str, int start, int end,
                                   Spanned spn, int spnStart, int spnEnd) {

            if ((str.length() > 0) && (spnStart == 0) && (str.charAt(0) == '0')) {
                return EMPTY_FIELD;
            }

            return str;
        }
    }
}
