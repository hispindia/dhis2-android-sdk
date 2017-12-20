package org.hisp.dhis.android.core.category;


import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;
import static org.hisp.dhis.android.core.utils.Utils.isNull;

import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;


public class CategoryOptionLinkStoreImpl implements CategoryOptionLinkStore {

    private final DatabaseAdapter databaseAdapter;
    private final SQLiteStatement insertStatement;

    private static final String INSERT_STATEMENT =
            "INSERT INTO " + CategoryOptionLinkModel.TABLE + " (" +
                    CategoryOptionLinkModel.Columns.CATEGORY + ", " +
                    CategoryOptionLinkModel.Columns.OPTION + ") " +
                    "VALUES(?, ?);";


    public CategoryOptionLinkStoreImpl(DatabaseAdapter databaseAdapter) {
        this.databaseAdapter = databaseAdapter;
        this.insertStatement = databaseAdapter.compileStatement(INSERT_STATEMENT);
    }

    @Override
    public long insert(@NonNull CategoryOptionLinkModel entity) {

        validate(entity);

        bind(insertStatement, entity);

        return executeInsert();
    }

    private void validate(@NonNull CategoryOptionLinkModel link) {
        isNull(link.category());
        isNull(link.option());
    }

    private void bind(@NonNull SQLiteStatement sqLiteStatement, @NonNull CategoryOptionLinkModel link) {
        sqLiteBind(sqLiteStatement, 1, link.category());
        sqLiteBind(sqLiteStatement, 2, link.option());
    }

    private int executeInsert() {
        int lastId = databaseAdapter.executeUpdateDelete(CategoryModel.TABLE, insertStatement);
        insertStatement.clearBindings();

        return lastId;
    }

    @Override
    public int delete() {
        return databaseAdapter.delete(CategoryOptionLinkModel.TABLE);
    }
}

