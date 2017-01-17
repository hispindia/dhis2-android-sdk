/*
 * Copyright (c) 2017, University of Oslo
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

 package org.hisp.dhis.android.core.trackedentity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.database.AbsStoreTestCase;
import org.hisp.dhis.android.core.organisationunit.CreateOrganisationUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.hisp.dhis.android.core.data.database.CursorAssert.assertThatCursor;

@RunWith(AndroidJUnit4.class)
public class TrackedEntityAttributeValueStoreIntegrationTests extends AbsStoreTestCase {

    //BaseDataModel:
    private static final State STATE = State.SYNCED;

    //TrackedEntityAttributeValueModel:
    private static final String VALUE = "TestValue";
    private static final String TRACKED_ENTITY_ATTRIBUTE = "TestTrackedEntityAttributeUid";
    private static final String TRACKED_ENTITY_INSTANCE = "TestTrackedEntityInstanceUid";
    private static final String ORGANIZATION_UNIT = "TestOrganizationUnitUid";

    private static final String[] TRACKED_ENTITY_ATTRIBUTE_VALUE_PROJECTION = {
            TrackedEntityAttributeValueModel.Columns.STATE,
            TrackedEntityAttributeValueModel.Columns.VALUE,
            TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_ATTRIBUTE,
            TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE
    };

    private TrackedEntityAttributeValueStore store;

    @Override
    public void setUp() throws IOException {
        super.setUp();
        this.store = new TrackedEntityAttributeValueStoreImpl(database());
    }

    @Test
    public void insert_shouldPersistTrackedEntityAttributeValueInDatabase() {
        insertForeignKeys();

        long rowId = store.insert(STATE, VALUE, TRACKED_ENTITY_ATTRIBUTE, TRACKED_ENTITY_INSTANCE);

        Cursor cursor = database().query(TrackedEntityAttributeValueModel.TRACKED_ENTITY_ATTRIBUTE_VALUE,
                TRACKED_ENTITY_ATTRIBUTE_VALUE_PROJECTION,
                null, null, null, null, null);

        assertThat(rowId).isEqualTo(1L);
        assertThatCursor(cursor).hasRow(STATE, VALUE, TRACKED_ENTITY_ATTRIBUTE, TRACKED_ENTITY_INSTANCE)
                .isExhausted();
    }

    @Test
    public void insert_shouldPersistTrackedEntityAttributeValueNullableInDatabase() {
        insertForeignKeys();

        long rowId = store.insert(STATE, null, TRACKED_ENTITY_ATTRIBUTE, TRACKED_ENTITY_INSTANCE);

        Cursor cursor = database().query(TrackedEntityAttributeValueModel.TRACKED_ENTITY_ATTRIBUTE_VALUE,
                TRACKED_ENTITY_ATTRIBUTE_VALUE_PROJECTION,
                null, null, null, null, null);

        assertThat(rowId).isEqualTo(1L);
        assertThatCursor(cursor).hasRow(STATE, null, TRACKED_ENTITY_ATTRIBUTE, TRACKED_ENTITY_INSTANCE)
                .isExhausted();
    }

    @Test(expected = SQLiteConstraintException.class)
    public void exception_persistTrackedEntityAttributeValueWithInvalidTrackedEntityAttribute() {
        String wrongTrackedEntityAttributeUid = "wrong";
        insertForeignKeys();
        store.insert(STATE, VALUE, wrongTrackedEntityAttributeUid, TRACKED_ENTITY_INSTANCE);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void exception_persistTrackedEntityAttributeValueWithInvalidTrackedEntityInstance() {
        String wrongTrackedEntityInstanceUid = "wrong";
        insertForeignKeys();
        store.insert(STATE, VALUE, TRACKED_ENTITY_ATTRIBUTE, wrongTrackedEntityInstanceUid);
    }

    @Test
    public void delete_shouldDeleteTrackedEntityAttributeValueWhenDeletingTrackedEntityAttribute() {
        //Insert:
        insert_shouldPersistTrackedEntityAttributeValueNullableInDatabase();
        //Delete foreign key row:
        database().delete(TrackedEntityAttributeModel.TRACKED_ENTITY_ATTRIBUTE,
                TrackedEntityAttributeModel.Columns.UID + "=?", new String[]{TRACKED_ENTITY_ATTRIBUTE});
        //Query and confirm that TrackedEntityAttributeValue is also deleted:
        Cursor cursor = database().query(TrackedEntityAttributeValueModel.TRACKED_ENTITY_ATTRIBUTE_VALUE,
                TRACKED_ENTITY_ATTRIBUTE_VALUE_PROJECTION, null, null, null, null, null);
        assertThatCursor(cursor).isExhausted();
    }

    @Test
    public void delete_shouldDeleteTrackedEntityAttributeValueWhenDeletingTrackedEntityInstance() {
        //Insert:
        insert_shouldPersistTrackedEntityAttributeValueNullableInDatabase();
        //Delete foreign key row:
        database().delete(TrackedEntityInstanceModel.TRACKED_ENTITY_INSTANCE,
                TrackedEntityInstanceModel.Columns.UID + "=?", new String[]{TRACKED_ENTITY_INSTANCE});
        //Query and confirm that TrackedEntityAttributeValue is also deleted:
        Cursor cursor = database().query(TrackedEntityAttributeValueModel.TRACKED_ENTITY_ATTRIBUTE_VALUE,
                TRACKED_ENTITY_ATTRIBUTE_VALUE_PROJECTION, null, null, null, null, null);
        assertThatCursor(cursor).isExhausted();
    }

    @Test
    public void close_shouldNotCloseDatabase() {
        store.close();
        assertThat(database().isOpen()).isTrue();
    }

    /**
     * Creates and inserts the foreign key rows in their respective tables.
     * For TrackedEntityAttributeValue : TrackedEntityAttribute , TrackedEntityAttributeInstance
     * For TrackedEntityInstance : OrganisationUnit
     */
    private void insertForeignKeys() {
        ContentValues organisationUnit = CreateOrganisationUnitUtils.createOrgUnit(1L, ORGANIZATION_UNIT);
        ContentValues trackedEntityInstance = CreateTrackedEntityInstanceUtils.createWithOrgUnit(TRACKED_ENTITY_INSTANCE, ORGANIZATION_UNIT);
        ContentValues trackedEntityAttribute = CreateTrackedEntityAttributeUtils.create(1L, TRACKED_ENTITY_ATTRIBUTE, null);

        database().insert(OrganisationUnitModel.ORGANISATION_UNIT, null, organisationUnit);
        database().insert(TrackedEntityInstanceModel.TRACKED_ENTITY_INSTANCE, null, trackedEntityInstance);
        database().insert(TrackedEntityAttributeModel.TRACKED_ENTITY_ATTRIBUTE, null, trackedEntityAttribute);
    }
}
