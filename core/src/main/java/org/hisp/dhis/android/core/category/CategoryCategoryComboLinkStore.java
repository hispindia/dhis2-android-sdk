package org.hisp.dhis.android.core.category;


import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.common.DeletableStore;

import java.util.List;

public interface CategoryCategoryComboLinkStore extends DeletableStore {
    long insert(@NonNull CategoryCategoryComboLinkModel element);

    List<CategoryCategoryComboLink> queryAll();
}
