package org.hisp.dhis.android.sdk.controllers.realm;

import android.util.Log;

import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitforcascading;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by nhancao on 6/20/17.
 */

public class ROrganisationHelper {
    private static final String TAG = ROrganisationHelper.class.getSimpleName();

    /**
     * Save organisation to local
     */
    public static void saveOrgToLocal(List<OrganisationUnitforcascading> organisationslevel) {
        final List<ROrganisationUnit> organisationUnitList = new ArrayList<>();
        for (OrganisationUnitforcascading organisationUnitforcascading : organisationslevel) {
            organisationUnitList.add(ROrganisationUnit.create(organisationUnitforcascading.getId(),
                                                              organisationUnitforcascading.getLabel(),
                                                              organisationUnitforcascading.getLevel(),
                                                              organisationUnitforcascading.getParent() != null ?
                                                              organisationUnitforcascading.getParent().getId() :
                                                              null));
        }
        RealmHelper.transaction(new RealmHelper.RealmTransaction() {
            @Override
            public void execute(Realm realm) {
                long current = System.currentTimeMillis();
                Log.e(TAG, "loadMetaData: current " + current);

                realm.insertOrUpdate(organisationUnitList);

                Log.e(TAG, "loadMetaData: save finish in " + (System.currentTimeMillis() - current));
            }
        });

    }

    /**
     * Check data on local is exist or not
     */
    public static boolean isEmpty() {
        Long count = RealmHelper.query(new RealmHelper.RealmQuery<Long>() {
            @Override
            public Long query(Realm realm) {
                return realm.where(ROrganisationUnit.class).count();
            }
        });
        return count == null || count <= 0;
    }

    /**
     * Get all organisation from local
     */
    public static List<ROrganisationUnit> getAllOrgFromLocal() {
        return RealmHelper.query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
            @Override
            public List<ROrganisationUnit> query(Realm realm) {
                RealmResults<ROrganisationUnit> realmResults = realm.where(ROrganisationUnit.class)
                                                                    .findAll();
                return realm.copyFromRealm(realmResults);
            }
        });

    }

    /**
     * Get organisation from local by level
     */
    public static List<ROrganisationUnit> getOrgFromLocalByLevel(final int level) {
        return RealmHelper.query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
            @Override
            public List<ROrganisationUnit> query(Realm realm) {
                RealmResults<ROrganisationUnit> realmResults = realm.where(ROrganisationUnit.class)
                                                                    .equalTo("level", level)
                                                                    .findAll();
                return realm.copyFromRealm(realmResults);
            }
        });
    }

    /**
     * Get organisation from local by parent id
     */
    public static List<ROrganisationUnit> getOrgFromLocalByParent(final String parent) {
        return RealmHelper.query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
            @Override
            public List<ROrganisationUnit> query(Realm realm) {
                RealmResults<ROrganisationUnit> realmResults = realm.where(ROrganisationUnit.class)
                                                                    .equalTo("parent", parent)
                                                                    .findAll();
                return realm.copyFromRealm(realmResults);
            }
        });
    }

    /**
     * Clear all data organisation on local
     */
    public static void clear() {
        RealmHelper.transaction(new RealmHelper.RealmTransaction() {
            @Override
            public void execute(Realm realm) {
                realm.deleteAll();
            }
        });
    }


}
