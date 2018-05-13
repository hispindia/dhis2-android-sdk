package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

public interface TrackerAssociateRowActionListener extends DataEntryRowFactory.callbacks {
    public enum STATES{
        ADD,CLEAR,SEARCH
    }

    public void addButtonClicked();
    public void searchButtonClicked();
    public void clearButtonClicked();
    public ValueChangeListener getValue(STATES state);
    public void setValueListeners(STATES state,ValueChangeListener listerner);
}
