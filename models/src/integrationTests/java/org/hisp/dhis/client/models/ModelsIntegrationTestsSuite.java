/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.client.models;

import org.hisp.dhis.client.models.constant.ConstantIntegrationTests;
import org.hisp.dhis.client.models.dataelement.CategoryComboIntegrationTests;
import org.hisp.dhis.client.models.dataelement.CategoryIntegrationTests;
import org.hisp.dhis.client.models.dataelement.CategoryOptionComboIntegrationTests;
import org.hisp.dhis.client.models.dataelement.CategoryOptionIntegrationTests;
import org.hisp.dhis.client.models.dataelement.DataElementIntegrationTests;
import org.hisp.dhis.client.models.event.EventIntegrationTest;
import org.hisp.dhis.client.models.option.OptionIntegrationTests;
import org.hisp.dhis.client.models.option.OptionSetIntegrationTests;
import org.hisp.dhis.client.models.organisationunit.OrganisationUnitIntegrationTests;
import org.hisp.dhis.client.models.user.UserCredentialIntegrationTest;
import org.hisp.dhis.client.models.user.UserIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConstantIntegrationTests.class,
        CategoryOptionIntegrationTests.class,
        UserIntegrationTest.class,
        UserCredentialIntegrationTest.class,
        CategoryIntegrationTests.class,
        CategoryOptionIntegrationTests.class,
        CategoryComboIntegrationTests.class,
        CategoryOptionComboIntegrationTests.class,
        DataElementIntegrationTests.class,
        EventIntegrationTest.class,
        OptionSetIntegrationTests.class,
        OptionIntegrationTests.class,
        OrganisationUnitIntegrationTests.class
})
public class ModelsIntegrationTestsSuite {
}
