package org.hisp.dhis.android.sdk.ui.nhancv;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.joanzapata.android.BaseAdapterHelper;

import org.hisp.dhis.android.sdk.R;

import java.util.List;

/**
 * Created by nhancao on 6/22/17.
 */
public class OptionDialog<T extends Model> extends DialogFragment {
    private static final String TAG = OptionDialog.class.getSimpleName();

    protected LinearLayout lvSearch;
    protected EditText etSearch;
    protected ListView lvItem;

    private ItemClickListener<T> onItemClickListener;
    private OptionAdapter<T> adapter;
    private List<T> modelList;

    public static Model createModel(final String id, final String displayName) {
        return new Model() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }
        };
    }

    public static <T extends Model> OptionDialog newInstance(List<T> modelList,
                                                             ItemClickListener<T> onItemClickListener) {
        OptionDialog<T> optionDialog = new OptionDialog<>();
        optionDialog.setModelList(modelList);
        optionDialog.setOnItemClickListener(onItemClickListener);
        return optionDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return inflater.inflate(R.layout.dialog_option, container, false);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), getTheme());
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lvSearch = (LinearLayout) view.findViewById(R.id.dialog_option_lv_search);
        etSearch = (EditText) view.findViewById(R.id.dialog_option_et_search);
        lvItem = (ListView) view.findViewById(R.id.dialog_option_lv_item);

        lvItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (modelList != null) {
                    lvItemClick(modelList.get(position));
                }
            }
        });


        init();
    }

    void init() {

        adapter = new OptionAdapter<T>(getContext(), R.layout.item_dialog_option) {
            @Override
            protected void convert(BaseAdapterHelper helper, T item) {
                TextView tvDisplay = helper.getView(R.id.item_dialog_option_title);
                tvDisplay.setText(AppUtils.highlightText(etSearch.getText().toString(), item.getDisplayName(),
                                                         Color.parseColor("#7A7986")));
            }
        };
        lvItem.setAdapter(adapter);
        if (modelList != null) {
            adapter.replaceAll(modelList);
            lvSearch.setVisibility((modelList.size() <= 5) ? View.GONE : View.VISIBLE);
        }

        etSearch.addTextChangedListener(new NTextChange(new NTextChange.TextListener() {
            @Override
            public void after(Editable editable) {
                String query = etSearch.getText().toString();
                adapter.getFilter().filter(query);
            }

            @Override
            public void before() {

            }
        }));

    }

    void lvItemClick(T model) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(model);
        }
        dismiss();
    }

    public void setOnItemClickListener(ItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setModelList(List<T> modelList) {
        this.modelList = modelList;
    }

    public void show(FragmentManager manager) {
        super.show(manager, OptionDialog.class.getSimpleName());
    }

}
