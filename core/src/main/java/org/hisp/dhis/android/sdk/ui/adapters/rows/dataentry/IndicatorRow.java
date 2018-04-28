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

import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.models.ProgramIndicator;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.OnDetailedInfoButtonClick;

public final class IndicatorRow extends NonEditableTextViewRow {
    private static final String EMPTY_FIELD = "";

    private final ProgramIndicator mIndicator;

    public IndicatorRow(ProgramIndicator indicator, String value, String description) {
        super(value);
        mIndicator = indicator;
        mValue = value;
        mDescription = description;
        checkNeedsForDescriptionButton();
    }

    public void setValue(String val){
        this.mValue = val;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.INDICATOR.ordinal();
    }

    public ProgramIndicator getIndicator() {
        return mIndicator;
    }

    @Override
    public String getName() {
        if (mIndicator.getName() != null) {
            return mIndicator.getName();
        } else {
            return EMPTY_FIELD;
        }
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
                    R.layout.listview_row_indicator_with_add_button, container, false);
            View detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout); // need to keep reference
            holder = new ViewHolder(
                    (TextView) root.findViewById(R.id.text_label),
                    (TextView) root.findViewById(R.id.indicator_row),
                    detailedInfoButton);

            root.setTag(holder);
            view = root;
        }

        holder.textLabel.setText(getName());

        if(!isEditable())
        {
            holder.textValue.setEnabled(false);
        }
        else
            holder.textValue.setEnabled(true);

        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
        holder.textValue.setText(mValue);

        if(isDetailedInfoButtonHidden())
        {
            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
        }
        else {
            holder.detailedInfoButton.setVisibility(View.VISIBLE);
        }
        holder.textValue.setOnEditorActionListener(mOnEditorActionListener);

        return view;
    }
}
