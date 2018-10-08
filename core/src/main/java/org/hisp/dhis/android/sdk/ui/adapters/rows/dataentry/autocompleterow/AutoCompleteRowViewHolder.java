package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;

class AutoCompleteRowViewHolder {
    public final TextView textView;
    public final TextView mandatoryIndicator;
    public final TextView warningLabel;
    public final TextView errorLabel;
    public final TextView valueTextView;
    public final ImageButton clearButton;
    private static final String TZ_LANG= "sw";
    private static final String VI_LANG= "vi";
    private static final String IN_LANG= "in";
    private static final String TZ_FINDOPTION= "pata chaguo";
    private static final String VI_FINDOPTION= "Tìm kiếm";
    //        public final View detailedInfoButton;
    public final AutoCompleteOnClearButtonListener onClearButtonListener;
    public final AutoCompleteOnTextChangedListener onTextChangedListener;
    public final AutoCompleteDropDownButtonListener onDropDownButtonListener;

    AutoCompleteRowViewHolder(View view) {
        mandatoryIndicator = (TextView) view.findViewById(R.id.mandatory_indicator);
        textView = (TextView) view.findViewById(R.id.text_label);
        warningLabel = (TextView) view.findViewById(R.id.warning_label);
        errorLabel = (TextView) view.findViewById(R.id.error_label);
        valueTextView = (TextView) view.findViewById(R.id.choose_option);
        clearButton = (ImageButton) view.findViewById(R.id.clear_option_value);
//            this.detailedInfoButton = detailedInfoButton;

        final UserAccount uslocal= MetaDataController.getUserLocalLang();
        String user_locallang=uslocal.getUserSettings().toString();

        String localdblang=user_locallang;
        if(localdblang.equals(TZ_LANG))
        {
            valueTextView.setHint(TZ_FINDOPTION);
        }
        else if(localdblang.equals(VI_LANG))
        {
            valueTextView.setHint(VI_FINDOPTION);
        }
        else if(localdblang.equals(IN_LANG))
        {
            valueTextView.setHint("Temukan opsi");
        }
        else if(localdblang.equals("my"))
        {
            valueTextView.setHint("ေရြးခ်ယ္မႈမ်ားေတြ႕ျခင္း");
        }
        AutoCompleteOnOptionSelectedListener onOptionListener
                = new OnOptionItemSelectedListener(valueTextView);
        onClearButtonListener = new AutoCompleteOnClearButtonListener(valueTextView);
        onTextChangedListener = new AutoCompleteOnTextChangedListener();
        onDropDownButtonListener = new AutoCompleteDropDownButtonListener();
        onDropDownButtonListener.setListener(onOptionListener);

        clearButton.setOnClickListener(onClearButtonListener);
        valueTextView.setOnClickListener(onDropDownButtonListener);
    }

    void setOnTextChangedListener() {
        valueTextView.addTextChangedListener(onTextChangedListener);
    }

    void clearOnTextChangedListener() {
        valueTextView.removeTextChangedListener(onTextChangedListener);
    }
}
