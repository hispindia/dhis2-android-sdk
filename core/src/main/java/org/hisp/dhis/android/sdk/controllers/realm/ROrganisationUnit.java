package org.hisp.dhis.android.sdk.controllers.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by nhancao on 6/20/17.
 */

public class ROrganisationUnit extends RealmObject {
    @PrimaryKey
    private String id;
    private String displayName;
    private int level;
    private String parent;

    public static ROrganisationUnit create(String id, String displayName, int level, String parent) {
        ROrganisationUnit organisationUnit = new ROrganisationUnit();
        organisationUnit.setId(id);
        organisationUnit.setDisplayName(displayName);
        organisationUnit.setLevel(level);
        organisationUnit.setParent(parent);
        return organisationUnit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
