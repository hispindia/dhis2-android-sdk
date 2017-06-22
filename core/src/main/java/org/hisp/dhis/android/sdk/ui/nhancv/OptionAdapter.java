package org.hisp.dhis.android.sdk.ui.nhancv;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;

import com.joanzapata.android.QuickAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nhancao on 4/26/17.
 */

public abstract class OptionAdapter<T extends Model> extends QuickAdapter<T> implements Filterable {

    private List<T> originalList;

    public OptionAdapter(Context context, int layoutResId) {
        super(context, layoutResId);
        originalList = new ArrayList<>();
    }

    @Override
    public void replaceAll(List<T> elem) {
        super.replaceAll(elem);
        originalList = elem;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.toString().length() == 0) {
                    results.count = originalList.size();
                    results.values = originalList;
                } else {
                    List<T> resultsData = new ArrayList<>();
                    String searchStr = constraint.toString();
                    for (T item : originalList) {
                        if (AppUtils.isContainText(searchStr, item.getDisplayName())) {
                            resultsData.add(item);
                        }
                    }
                    results.count = resultsData.size();
                    results.values = resultsData;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (constraint == null || constraint.length() == 0) {
                    replaceAll(originalList);
                } else {
                    replaceAll((List<T>) results.values);
                }
            }
        };
    }
}
