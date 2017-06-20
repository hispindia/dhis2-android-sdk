package org.hisp.dhis.android.sdk.controllers.realm;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by nhancao on 6/20/17.
 */

public class  RealmHelper {
    private static final String DATABASE_NAME = "aes_realm";

    private static final RealmConfiguration realmConfig = new RealmConfiguration.Builder()
            .name(DATABASE_NAME)
            .modules(new RCoreModule())
            .deleteRealmIfMigrationNeeded()
            .build();

    public static void transaction(RealmTransaction realmTransaction) {
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            realm.beginTransaction();
            realmTransaction.execute(realm);
            realm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public static <T> T query(RealmQuery<T> realmDoing) {
        Realm realm = null;
        try {
            realm = Realm.getInstance(realmConfig);
            return realmDoing.query(realm);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
        return null;
    }

    /**
     * Export database to sdcard
     */
    public static void exportRealmFile(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + context.getPackageName() + "//files//" + DATABASE_NAME;
                String backupDBPath = DATABASE_NAME + ".realm";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    System.out.println("exportRealmFile: Db file has been backup on sdcard");
                } else {
                    System.out.println("exportRealmFile: current db not exists");
                }
            } else {
                System.out.println("exportRealmFile: Sdcard can not Write");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface RealmTransaction {
        void execute(Realm realm);
    }

    public interface RealmQuery<T> {
        T query(Realm realm);
    }
}
