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

package org.hisp.dhis.android.core.option;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import java.util.Date;

import static org.hisp.dhis.android.core.utils.StoreUtils.nonNull;
import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class OptionStoreImpl implements OptionStore {

    private static final String INSERT_STATEMENT = "INSERT INTO " + OptionModel.TABLE + " (" +
            OptionModel.Columns.UID + ", " +
            OptionModel.Columns.CODE + ", " +
            OptionModel.Columns.NAME + ", " +
            OptionModel.Columns.DISPLAY_NAME + ", " +
            OptionModel.Columns.CREATED + ", " +
            OptionModel.Columns.LAST_UPDATED + ", " +
            OptionModel.Columns.OPTION_SET + ")" +
            "VALUES (?, ?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_STATEMENT = "UPDATE " + OptionModel.TABLE + " SET " +
            OptionModel.Columns.UID + " =?, " +
            OptionModel.Columns.CODE + " =?, " +
            OptionModel.Columns.NAME + " =?, " +
            OptionModel.Columns.DISPLAY_NAME + " =?, " +
            OptionModel.Columns.CREATED + " =?, " +
            OptionModel.Columns.LAST_UPDATED + " =?, " +
            OptionModel.Columns.OPTION_SET + " =? " +
            " WHERE " + OptionModel.Columns.UID + " =?;";

    private static final String DELETE_STATEMENT = "DELETE FROM " + OptionModel.TABLE +
            " WHERE " + OptionModel.Columns.UID + " =?;";

    private final SQLiteStatement insertStatement;
    private final SQLiteStatement updateStatement;
    private final SQLiteStatement deleteStatement;

    private final DatabaseAdapter databaseAdapter;

    public OptionStoreImpl(DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        this.insertStatement = databaseAdapter.compileStatement(INSERT_STATEMENT);
        this.updateStatement = databaseAdapter.compileStatement(UPDATE_STATEMENT);
        this.deleteStatement = databaseAdapter.compileStatement(DELETE_STATEMENT);
    }

    @Override
    public long insert(@NonNull String uid,
                       @NonNull String code,
                       @NonNull String name,
                       @NonNull String displayName,
                       @NonNull Date created,
                       @NonNull Date lastUpdated,
                       @NonNull String optionSet) {
        nonNull(uid);
        nonNull(optionSet);
        bindArguments(insertStatement, uid, code, name, displayName, created, lastUpdated, optionSet);

        // execute and clear bindings
        Long insert = databaseAdapter.executeInsert(OptionModel.TABLE, insertStatement);
        insertStatement.clearBindings();

        return insert;
    }

    @Override
    public int update(@NonNull String uid,
                      @NonNull String code,
                      @NonNull String name,
                      @NonNull String displayName,
                      @NonNull Date created,
                      @NonNull Date lastUpdated,
                      @NonNull String optionSet,
                      @NonNull String whereOptionUid) {
        nonNull(uid);
        nonNull(optionSet);
        nonNull(whereOptionUid);
        bindArguments(updateStatement, uid, code, name, displayName, created, lastUpdated, optionSet);

        // bind the where argument
        sqLiteBind(updateStatement, 8, whereOptionUid);

        // execute and clear bindings
        int update = databaseAdapter.executeUpdateDelete(OptionModel.TABLE, updateStatement);
        updateStatement.clearBindings();

        return update;
    }

    @Override
    public int delete(@NonNull String uid) {
        nonNull(uid);
        sqLiteBind(deleteStatement, 1, uid);

        int delete = databaseAdapter.executeUpdateDelete(OptionModel.TABLE, deleteStatement);
        deleteStatement.clearBindings();
        return delete;
    }

    private void bindArguments(@NonNull SQLiteStatement sqliteStatement,
                               @NonNull String uid,
                               @NonNull String code,
                               @NonNull String name,
                               @NonNull String displayName,
                               @NonNull Date created,
                               @NonNull Date lastUpdated,
                               @NonNull String optionSet) {
        sqLiteBind(sqliteStatement, 1, uid);
        sqLiteBind(sqliteStatement, 2, code);
        sqLiteBind(sqliteStatement, 3, name);
        sqLiteBind(sqliteStatement, 4, displayName);
        sqLiteBind(sqliteStatement, 5, created);
        sqLiteBind(sqliteStatement, 6, lastUpdated);
        sqLiteBind(sqliteStatement, 7, optionSet);
    }
}
