package org.hisp.dhis.android.sdk.ui.fragments.selectprogram;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import java.util.HashMap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.ui.dialogs.AutoCompleteDialogAdapter;
import org.hisp.dhis.android.sdk.ui.dialogs.OrgUnitDialogFragmentForm;

/**
 * Created by Sourabh Bhardwaj on 08-03-2017.
 */

public class SelectAddress extends Activity {


    private Spinner spinner1, spinner2;
    private Button btnSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_address);

        addItemsOnSpinner2();
        addListenerOnButton();
        addListenerOnSpinnerItemSelection();
    }

    // add items into spinner dynamically
    public void addItemsOnSpinner2() {
        spinner2 = (Spinner) findViewById(R.id.spinner2);
        List<String> list = new ArrayList<String>();

        List<OrganisationUnit> orgUnits = MetaDataController
                .getorganisationUnitsLevelWise(3);
//        String[] spinnerArray = new String[orgUnits.size()];
//        HashMap<Integer,String> spinnerMap = new HashMap<Integer, String>();
//
//        for (int i = 0; i < orgUnits.size(); i++)
//        {
//            spinnerMap.put(i,orgUnits.get(i));
//            spinnerArray[i] = Province_NAME.get(i);
//        }
//
//

        List<OrganisationUnit> orgUnits1 = MetaDataController
                .getorganisationUnitsLevelWise(5);
        List<OrganisationUnit> orgUnits2 = MetaDataController
                .getorganisationUnitsLevelWise(7);
        List<OrganisationUnit> orgUnits3 = MetaDataController
                .getorganisationUnitsLevelWise(8);

        for (OrganisationUnit orgUnit : orgUnits) {

            list.add(orgUnit.getId());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter);

    }

    public void addListenerOnSpinnerItemSelection() {
        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    // get the selected dropdown list value
    public void addListenerOnButton() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner2 = (Spinner) findViewById(R.id.spinner2);


        btnSubmit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Toast.makeText(SelectAddress.this,
                        "OnClickListener : " +
                                "\nSpinner 1 : "+ String.valueOf(spinner1.getSelectedItem()) +
                                "\nSpinner 2 : "+ String.valueOf(spinner2.getSelectedItem()),
                        Toast.LENGTH_SHORT).show();
            }

        });
    }
}
