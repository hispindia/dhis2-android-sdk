package org.hisp.dhis.android.core.dataelement;

import android.support.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.D2Factory;
import org.hisp.dhis.android.core.common.GenericCallData;
import org.hisp.dhis.android.core.data.database.AbsStoreTestCase;
import org.hisp.dhis.android.core.dataset.DataSetEndpointCall;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceStoreImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;

@RunWith(AndroidJUnit4.class)
public class DataElementEndpointCallRealIntegrationShould extends AbsStoreTestCase {
    /**
     * A quick integration test that is probably flaky, but will help with finding bugs related to the
     * metadataSyncCall. It works against the demo server.
     */
    private D2 d2;
    private DataElementEndpointCall dataElementCall;

    @Before
    @Override
    public void setUp() throws IOException {
        super.setUp();
        d2 = D2Factory.create("https://play.dhis2.org/android-current/api/", databaseAdapter());
        dataElementCall = createCall();
    }

    private DataElementEndpointCall createCall() {
        ResourceHandler resourceHandler =
                new ResourceHandler(new ResourceStoreImpl(databaseAdapter()));
        GenericCallData data = GenericCallData.create(databaseAdapter(), resourceHandler, d2.retrofit());

        Set<String> uids = new HashSet<>();

        uids.add("FTRrcoaog83");
        uids.add("P3jJH5Tu5VC");
        uids.add("FQ2o8UBlcrS");

        return DataElementEndpointCall.create(data, uids);
    }

    // @Test
    public void download_data_elements() throws Exception {
        retrofit2.Response loginResponse = d2.logIn("android", "Android123").call();
        assertThat(loginResponse.isSuccessful()).isTrue();

        /*  This test won't pass independently of DataElementEndpointCall and
            CategoryComboEndpointCall, as the foreign keys constraints won't be satisfied.
            To run the test, you will need to disable foreign key support in database in
            DbOpenHelper.java replacing 'foreign_keys = ON' with 'foreign_keys = OFF' and
            uncomment the @Test tag */

        retrofit2.Response dataElementResponse = dataElementCall.call();
        assertThat(dataElementResponse.isSuccessful()).isTrue();
    }

    @Test
    public void stub() {
    }
}
