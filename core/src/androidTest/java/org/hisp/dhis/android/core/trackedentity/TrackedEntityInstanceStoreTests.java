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

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.database.AbsStoreTestCase;
import org.hisp.dhis.android.core.organisationunit.CreateOrganisationUnitUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Date;

import static com.google.common.truth.Truth.assertThat;
import static org.hisp.dhis.android.core.data.database.CursorAssert.assertThatCursor;

@RunWith(AndroidJUnit4.class)
public class TrackedEntityInstanceStoreTests extends AbsStoreTestCase {
    private static final String UID = "test_uid";
    private static final String ORGANISATION_UNIT = "test_organisationUnit";
    private static final String TRACKED_ENTITY = "test_trackedEntity";
    private static final State STATE = State.ERROR;

    private final Date date;
    private final String dateString;

    private TrackedEntityInstanceStore store;

    public TrackedEntityInstanceStoreTests() {
        this.date = new Date();
        this.dateString = BaseIdentifiableObject.DATE_FORMAT.format(date);
    }

    public static final String[] PROJECTION = {
            TrackedEntityInstanceModel.Columns.UID,
            TrackedEntityInstanceModel.Columns.CREATED,
            TrackedEntityInstanceModel.Columns.LAST_UPDATED,
            TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT,
            TrackedEntityInstanceModel.Columns.TRACKED_ENTITY,
            TrackedEntityInstanceModel.Columns.STATE
    };

    @Before
    @Override
    public void setUp() throws IOException {
        super.setUp();

        store = new TrackedEntityInstanceStoreImpl(databaseAdapter());
        ContentValues organisationUnit = CreateOrganisationUnitUtils.createOrgUnit(1L, ORGANISATION_UNIT);
        ContentValues trackedEntity = CreateTrackedEntityUtils.create(1L, TRACKED_ENTITY);
        database().insert(OrganisationUnitModel.TABLE, null, organisationUnit);
        database().insert(TrackedEntityModel.TABLE, null, trackedEntity);
    }

    @Test
    public void insert_shouldPersistRowInDatabase() {
        long rowId = store.insert(UID, date, date,
                ORGANISATION_UNIT, TRACKED_ENTITY, STATE);

        Cursor cursor = database().query(TrackedEntityInstanceModel.TABLE,
                PROJECTION, null, null, null, null, null);
        assertThat(rowId).isEqualTo(1L);
        assertThatCursor(cursor).hasRow(UID, dateString, dateString,
                ORGANISATION_UNIT, TRACKED_ENTITY, STATE).isExhausted();
    }

    @Test
    public void insert_shouldPersistDeferrableRowInDatabase() {
        String deferredOrganisationUnit = "deferredOrganisationUnit";
        String deferredTrackedEntity = "deferredTrackedEntity";

        database().beginTransaction();

        long rowId = store.insert(UID, date, date,
                deferredOrganisationUnit, deferredTrackedEntity, STATE);
        ContentValues organisationUnit = CreateOrganisationUnitUtils.createOrgUnit(11L, deferredOrganisationUnit);
        ContentValues trackedEntity = CreateTrackedEntityUtils.create(11L, deferredTrackedEntity);
        database().insert(OrganisationUnitModel.TABLE, null, organisationUnit);
        database().insert(TrackedEntityModel.TABLE, null, trackedEntity);
        database().setTransactionSuccessful();
        database().endTransaction();

        Cursor cursor = database().query(TrackedEntityInstanceModel.TABLE, PROJECTION, null, null, null, null, null);
        assertThat(rowId).isEqualTo(1L);
        assertThatCursor(cursor).hasRow(UID, dateString, dateString,
                deferredOrganisationUnit, deferredTrackedEntity, STATE
        ).isExhausted();
    }

    @Test
    public void insert_shouldPersistNullableRowInDatabase() {
        long rowId = store.insert(UID, null, null, ORGANISATION_UNIT, TRACKED_ENTITY, null);
        Cursor cursor = database().query(TrackedEntityInstanceModel.TABLE, PROJECTION, null, null, null, null, null);
        assertThat(rowId).isEqualTo(1L);
        assertThatCursor(cursor).hasRow(
                UID, null, null, ORGANISATION_UNIT, TRACKED_ENTITY, null
        ).isExhausted();
    }

    @Test
    public void delete_shouldRemoveAllRows() {
        database().insert(TrackedEntityInstanceModel.TABLE, null,
                CreateTrackedEntityInstanceUtils.create(UID, ORGANISATION_UNIT, TRACKED_ENTITY));

        store.delete();

        Cursor cursor = database().query(TrackedEntityInstanceModel.TABLE, PROJECTION, null, null, null, null, null);
        assertThatCursor(cursor).isExhausted();
    }

    @Test
    public void delete_shouldDeleteTrackedEntityInstanceWhenDeletingOrganisationUnitForeignKey() {
        store.insert(UID, date, date, ORGANISATION_UNIT, TRACKED_ENTITY, STATE);
        database().delete(OrganisationUnitModel.TABLE,
                OrganisationUnitModel.Columns.UID + "=?", new String[]{ORGANISATION_UNIT});
        Cursor cursor = database().query(TrackedEntityInstanceModel.TABLE, PROJECTION, null, null, null, null, null);
        assertThatCursor(cursor).isExhausted();
    }

    @Test
    public void delete_shouldDeleteTrackedEntityInstanceWhenDeletingTrackedEntityForeignKey() {
        store.insert(UID, date, date, ORGANISATION_UNIT, TRACKED_ENTITY, STATE);
        database().delete(TrackedEntityModel.TABLE,
                TrackedEntityModel.Columns.UID + "=?", new String[]{TRACKED_ENTITY});
        Cursor cursor = database().query(TrackedEntityInstanceModel.TABLE, PROJECTION, null, null, null, null, null);
        assertThatCursor(cursor).isExhausted();
    }

    @Test(expected = SQLiteConstraintException.class)
    public void exception_persistTrackedEntityInstanceWithInvalidOrgUnitForeignKey() {
        store.insert(UID, date, date, "wrong", TRACKED_ENTITY, STATE);
    }

    @Test(expected = SQLiteConstraintException.class)
    public void exception_persistTrackedEntityInstanceWithInvalidTrackedEntityForeignKey() {
        store.insert(UID, date, date, ORGANISATION_UNIT, "wrong", STATE);
    }

    // ToDo: consider introducing conflict resolution strategy
    @Test
    public void close_shouldNotCloseDatabase() {
        assertThat(database().isOpen()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_null_uid() {
        store.insert(null, date, date, ORGANISATION_UNIT, TRACKED_ENTITY, STATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_null_organisationUnit() {
        store.insert(UID, date, date, null, TRACKED_ENTITY, STATE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void insert_null_trackedEntity() {
        store.insert(UID, date, date, ORGANISATION_UNIT, null, STATE);
    }
}
