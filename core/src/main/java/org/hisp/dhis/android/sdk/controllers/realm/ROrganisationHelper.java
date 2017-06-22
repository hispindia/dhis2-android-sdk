package org.hisp.dhis.android.sdk.controllers.realm;

import android.util.Log;

import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitforcascading;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static org.hisp.dhis.android.sdk.controllers.realm.RealmHelper.query;

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
        Long count = query(new RealmHelper.RealmQuery<Long>() {
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
        return query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
            @Override
            public List<ROrganisationUnit> query(Realm realm) {
                RealmResults<ROrganisationUnit> realmResults = realm.where(ROrganisationUnit.class)
                                                                    .findAll();
                return realm.copyFromRealm(realmResults);
            }
        });

    }

    public static List<ROrganisationUnit> getOrganisationUnitID(final String id) {
        return query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
            @Override
            public List<ROrganisationUnit> query(Realm realm) {
                RealmResults<ROrganisationUnit> realmResults = realm.where(ROrganisationUnit.class)
                                                                    .equalTo("displayName",id)
                                                                    .findAll();
                return realm.copyFromRealm(realmResults);
            }
        });

    }

    public static List<ROrganisationUnit> getOrganisationUnitNAME(final String id) {
        return query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
            @Override
            public List<ROrganisationUnit> query(Realm realm) {
                RealmResults<ROrganisationUnit> realmResults = realm.where(ROrganisationUnit.class)
                                                                    .equalTo("id",id)
                                                                    .findAll();
                return realm.copyFromRealm(realmResults);
            }
        });

    }

//    public static OrganisationUnit getOrganisationUnitID(String id) {
//        return new Select().from(OrganisationUnit.class)
//                .where(Condition.column(OrganisationUnit$Table.DISPLAYNAME).is(id)).querySingle();
//    }

    /**
     * Get organisation from local by level
     */
    public static List<ROrganisationUnit> getOrgFromLocalByLevel(final int level) {
        return query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
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
     * Get organisation from local by top level and lower level
     */
    public static List<ROrganisationUnit> getOrgFromLocalByLevel(final int topLevel, final int lowerLevel) {

        final List<ROrganisationUnit> empty = new ArrayList<>();
        final List<String> parentIds = new ArrayList<>();

        for (int i = topLevel; i <= lowerLevel; i++) {
            final int finalI = i;
            List<ROrganisationUnit> listLevel = RealmHelper
                    .query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
                        @Override
                        public List<ROrganisationUnit> query(Realm realm) {
                            RealmResults<ROrganisationUnit> realmResults;
                            RealmQuery<ROrganisationUnit> query = realm.where(ROrganisationUnit.class)
                                                                       .equalTo("level", finalI);
                            if (finalI == topLevel) {
                                realmResults = query.findAll();
                            } else {
                                realmResults = query.in("parent", parentIds.toArray(new String[0]))
                                                    .findAll();
                            }
                            return realm.copyFromRealm(realmResults);
                        }
                    });

            if (listLevel == null) {
                return empty;
            } else if (finalI == lowerLevel) {
                return listLevel;
            }

            parentIds.clear();
            for (ROrganisationUnit rOrganisationUnit : listLevel) {
                parentIds.add(rOrganisationUnit.getId());
            }

        }
        return empty;

    }

    /**
     * Get all org from local by parentId and skip to lower level
     */
    public static List<ROrganisationUnit> getOrgFromLocalByLevel(final String parentId, final int lowerLevel) {

        final List<ROrganisationUnit> empty = new ArrayList<>();
        final List<String> parentIds = new ArrayList<>();

        final ROrganisationUnit parent = RealmHelper
                .query(new RealmHelper.RealmQuery<ROrganisationUnit>() {
                    @Override
                    public ROrganisationUnit query(Realm realm) {
                        return realm.copyFromRealm(
                                realm.where(ROrganisationUnit.class).equalTo("id", parentId).findFirst());
                    }
                });
        if (parent == null) return empty;

        final int topLevel = parent.getLevel() + 1;
        for (int i = topLevel; i <= lowerLevel; i++) {
            final int finalI = i;
            List<ROrganisationUnit> listLevel = RealmHelper
                    .query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
                        @Override
                        public List<ROrganisationUnit> query(Realm realm) {
                            RealmResults<ROrganisationUnit> realmResults;
                            RealmQuery<ROrganisationUnit> query = realm.where(ROrganisationUnit.class)
                                                                       .equalTo("level", finalI);
                            if (finalI == topLevel) {
                                realmResults = query.equalTo("parent", parentId).findAll();
                            } else {
                                realmResults = query.in("parent", parentIds.toArray(new String[0]))
                                                    .findAll();
                            }
                            return realm.copyFromRealm(realmResults);
                        }
                    });

            if (listLevel == null) {
                return empty;
            } else if (finalI == lowerLevel) {
                return listLevel;
            }

            parentIds.clear();
            for (ROrganisationUnit rOrganisationUnit : listLevel) {
                parentIds.add(rOrganisationUnit.getId());
            }

        }
        return empty;

    }

    /**
     * Get organisation from local by parent id
     */
    public static List<ROrganisationUnit> getOrgFromLocalByParent(final String parent) {
        return query(new RealmHelper.RealmQuery<List<ROrganisationUnit>>() {
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
