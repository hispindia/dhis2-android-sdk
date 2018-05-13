package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;

import com.raizlabs.android.dbflow.annotation.NotNull;

import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow.AutoCompleteRow;
import org.hisp.dhis.android.sdk.utils.api.ValueType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Created by katana on 21/10/16.
 */

public class DataEntryRowFactory {
    public static Row createDataEntryView(boolean mandatory, boolean allowFutureDate,
                                          String optionSetId, String rowName, BaseValue baseValue,
                                          ValueType valueType, boolean editable,
                                          boolean shouldNeverBeEdited, boolean dataEntryMethod) {
        Row row;
        String trackedEntityAttributeName = rowName;
        if (optionSetId != null) {
            OptionSet optionSet = MetaDataController.getOptionSet(optionSetId);
            if (optionSet == null) {
                row = new ShortTextEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.TEXT);
            } else {
                List<Option> options = MetaDataController.getOptions(optionSetId);

                if (isDataEntryRadioButtons(dataEntryMethod, options)) {
                    row = new RadioButtonsOptionSetRow(trackedEntityAttributeName, mandatory, null,
                            baseValue, options);
                }
                else
                    row = new AutoCompleteRow(trackedEntityAttributeName, mandatory, null, baseValue, optionSet);
            }
        } else if (valueType.equals(ValueType.TEXT)) {
            row = new ShortTextEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.TEXT);
        } else if (valueType.equals(ValueType.LONG_TEXT)) {
            row = new LongEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.LONG_TEXT);
        } else if (valueType.equals(ValueType.NUMBER)) {
            row = new NumberEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.NUMBER);
        } else if (valueType.equals(ValueType.INTEGER)) {
            row = new IntegerEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER);
        } else if (valueType.equals(ValueType.INTEGER_ZERO_OR_POSITIVE)) {
            row = new IntegerZeroOrPositiveEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER_ZERO_OR_POSITIVE);
        } else if (valueType.equals(ValueType.PERCENTAGE)) {
            row = new PercentageEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.PERCENTAGE);
        } else if (valueType.equals(ValueType.INTEGER_POSITIVE)) {
            row = new IntegerPositiveEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER_POSITIVE);
        } else if (valueType.equals(ValueType.INTEGER_NEGATIVE)) {
            row = new IntegerNegativeEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER_NEGATIVE);
        } else if (valueType.equals(ValueType.BOOLEAN)) {
            row = new RadioButtonsRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.BOOLEAN);
        } else if (valueType.equals(ValueType.TRUE_ONLY)) {
            row = new CheckBoxRow(trackedEntityAttributeName, mandatory, null, baseValue);
        } else if (valueType.equals(ValueType.DATE) || valueType.equals(ValueType.AGE)) {
            row = new DatePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if (valueType.equals(ValueType.TIME)) {
            row = new TimePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if (valueType.equals(ValueType.DATETIME)) {
            row = new DateTimePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if(valueType.equals(ValueType.COORDINATE)) {
            row = new QuestionCoordinatesRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.QUESTION_COORDINATES);
        } else  if(valueType.equals(ValueType.PHONE_NUMBER)) {
            row = new PhoneEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.PHONE_NUMBER);
        }  else  if(valueType.equals(ValueType.EMAIL)) {
            row = new EmailAddressEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.EMAIL);
        }  else  if(valueType.equals(ValueType.URL)) {
            row = new URLEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.URL);
        } else if(valueType.equals(ValueType.TRACKER_ASSOCIATE)){
            //should be implemented for ibmc
            //TODO: a custom row rowtpe for tracker associate should be created
//            row = new InvalidEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue,
//                    DataEntryRowTypes.INVALID_DATA_ENTRY);
              row = new TrackerAssociateRow(trackedEntityAttributeName, mandatory, null, baseValue);

        }else if(valueType.equals(ValueType.SECTION_SEPERATOR)) {
            row = new SectionSeperatorRow(baseValue.getValue());
        }else if(valueType.equals(ValueType.FILE_RESOURCE)) {
            row = new FileResourceRow(trackedEntityAttributeName,mandatory,null,baseValue,DataEntryRowTypes.FILE_RESOURCE);
        }else {
            row = new InvalidEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue,
                    DataEntryRowTypes.INVALID_DATA_ENTRY);
        }
        row.setEditable(editable);
        row.setShouldNeverBeEdited(shouldNeverBeEdited);
        return row;
    }


    public static Row createDataEntryView(boolean mandatory, boolean allowFutureDate,
                                          String optionSetId, String rowName, BaseValue baseValue,
                                          ValueType valueType, boolean editable,
                                          boolean shouldNeverBeEdited, boolean dataEntryMethod,callbacks actionListener ) {
        Row row;
        String trackedEntityAttributeName = rowName;
        if (optionSetId != null) {
            OptionSet optionSet = MetaDataController.getOptionSet(optionSetId);
            if (optionSet == null) {
                row = new ShortTextEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.TEXT);
            } else {
                List<Option> options = MetaDataController.getOptions(optionSetId);

                if (isDataEntryRadioButtons(dataEntryMethod, options)) {
                    row = new RadioButtonsOptionSetRow(trackedEntityAttributeName, mandatory, null,
                            baseValue, options);
                }
                else
                    row = new AutoCompleteRow(trackedEntityAttributeName, mandatory, null, baseValue, optionSet);
            }
        } else if (valueType.equals(ValueType.TEXT)) {
            row = new ShortTextEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.TEXT);
        } else if (valueType.equals(ValueType.LONG_TEXT)) {
            row = new LongEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.LONG_TEXT);
        } else if (valueType.equals(ValueType.NUMBER)) {
            row = new NumberEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.NUMBER);
        } else if (valueType.equals(ValueType.INTEGER)) {
            row = new IntegerEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER);
        } else if (valueType.equals(ValueType.INTEGER_ZERO_OR_POSITIVE)) {
            row = new IntegerZeroOrPositiveEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER_ZERO_OR_POSITIVE);
        } else if (valueType.equals(ValueType.PERCENTAGE)) {
            row = new PercentageEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.PERCENTAGE);
        } else if (valueType.equals(ValueType.INTEGER_POSITIVE)) {
            row = new IntegerPositiveEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER_POSITIVE);
        } else if (valueType.equals(ValueType.INTEGER_NEGATIVE)) {
            row = new IntegerNegativeEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INTEGER_NEGATIVE);
        } else if (valueType.equals(ValueType.BOOLEAN)) {
            row = new RadioButtonsRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.BOOLEAN);
        } else if (valueType.equals(ValueType.TRUE_ONLY)) {
            row = new CheckBoxRow(trackedEntityAttributeName, mandatory, null, baseValue);
        } else if (valueType.equals(ValueType.DATE) || valueType.equals(ValueType.AGE)) {
            row = new DatePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if (valueType.equals(ValueType.TIME)) {
            row = new TimePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if (valueType.equals(ValueType.DATETIME)) {
            row = new DateTimePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if(valueType.equals(ValueType.COORDINATE)) {
            row = new QuestionCoordinatesRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.QUESTION_COORDINATES);
        } else  if(valueType.equals(ValueType.PHONE_NUMBER)) {
            row = new PhoneEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.PHONE_NUMBER);
        }  else  if(valueType.equals(ValueType.EMAIL)) {
            row = new EmailAddressEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.EMAIL);
        }  else  if(valueType.equals(ValueType.URL)) {
            row = new URLEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.URL);
        } else if(valueType.equals(ValueType.SECTION_SEPERATOR)) {
            row = new SectionSeperatorRow(baseValue.getValue());
        }else if(valueType.equals(ValueType.TRACKER_ASSOCIATE)){
            //should be implemented for ibmc
            //TODO: a custom row rowtpe for tracker associate should be created
//            row = new InvalidEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue,
//                    DataEntryRowTypes.INVALID_DATA_ENTRY);
              row = new TrackerAssociateRow(trackedEntityAttributeName, mandatory, null, baseValue,(TrackerAssociateRowActionListener)actionListener);

        } else if(valueType.equals(ValueType.FILE_RESOURCE)) {
            row = new FileResourceRow(trackedEntityAttributeName,mandatory,null,baseValue,DataEntryRowTypes.FILE_RESOURCE);
        }else{
            row = new InvalidEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue,
                    DataEntryRowTypes.INVALID_DATA_ENTRY);
        }
        row.setEditable(editable);
        row.setShouldNeverBeEdited(shouldNeverBeEdited);
        return row;
    }

    private static boolean isDataEntryRadioButtons(boolean dataEntryMethod, List<Option> options) {
        return dataEntryMethod && options.size() < 8;
    }

    public interface callbacks {

    }
//
//    private class actionReceiverContext extends Context{
//        private final Context context;
//
//        public actionReceiverContext(Context context) {
//            this.context =context;
//        }
//
//        @Override
//        public AssetManager getAssets() {
//            return context.getAssets();
//        }
//
//        @Override
//        public Resources getResources() {
//            return context.getResources();
//        }
//
//        @Override
//        public PackageManager getPackageManager() {
//            return context.getPackageManager();
//        }
//
//        @Override
//        public ContentResolver getContentResolver() {
//            return context.getContentResolver();
//        }
//
//        @Override
//        public Looper getMainLooper() {
//            return context.getMainLooper();
//        }
//
//        @Override
//        public Context getApplicationContext() {
//            return context.getApplicationContext();
//        }
//
//        @Override
//        public void setTheme(int i) {
//            context.setTheme(i);
//        }
//
//        @Override
//        public Resources.Theme getTheme() {
//            return context.getTheme();
//        }
//
//        @Override
//        public ClassLoader getClassLoader() {
//            return context.getClassLoader();
//        }
//
//        @Override
//        public String getPackageName() {
//            return context.getPackageName();
//        }
//
//        @Override
//        public ApplicationInfo getApplicationInfo() {
//            return context.getApplicationInfo();
//        }
//
//        @Override
//        public String getPackageResourcePath() {
//            return context.getPackageResourcePath();
//        }
//
//        @Override
//        public String getPackageCodePath() {
//            return context.getPackageCodePath();
//        }
//
//        @Override
//        public SharedPreferences getSharedPreferences(String s, int i) {
//            return context.getSharedPreferences(s,i);
//        }
//
//        @Override
//        public FileInputStream openFileInput(String s) throws FileNotFoundException {
//            return context.openFileInput(s);
//        }
//
//        @Override
//        public FileOutputStream openFileOutput(String s, int i) throws FileNotFoundException {
//            return context.openFileOutput(s,i);
//        }
//
//        @Override
//        public boolean deleteFile(String s) {
//            return context.deleteFile(s);
//        }
//
//        @Override
//        public File getFileStreamPath(String s) {
//            return context.getFileStreamPath(s);
//        }
//
//        @Override
//        public File getFilesDir() {
//            return context.getFilesDir();
//        }
//
//        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public File getNoBackupFilesDir() {
//            return context.getNoBackupFilesDir();
//        }
//
//        @Nullable
//        @Override
//        public File getExternalFilesDir(@Nullable String s) {
//            return context.getExternalFilesDir(s);
//        }
//
//        @TargetApi(Build.VERSION_CODES.KITKAT)
//        @Override
//        public File[] getExternalFilesDirs(String s) {
//            return context.getExternalFilesDirs(s);
//        }
//
//        @Override
//        public File getObbDir() {
//            return context.getObbDir();
//        }
//
//        @TargetApi(Build.VERSION_CODES.KITKAT)
//        @Override
//        public File[] getObbDirs() {
//            return context.getObbDirs();
//        }
//
//        @Override
//        public File getCacheDir() {
//            return context.getCacheDir();
//        }
//
//        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public File getCodeCacheDir() {
//            return context.getCodeCacheDir();
//        }
//
//        @Nullable
//        @Override
//        public File getExternalCacheDir() {
//            return context.getExternalCacheDir();
//        }
//
//        @TargetApi(Build.VERSION_CODES.KITKAT)
//        @Override
//        public File[] getExternalCacheDirs() {
//            return context.getExternalCacheDirs();
//        }
//
//        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public File[] getExternalMediaDirs() {
//            return context.getExternalMediaDirs();
//        }
//
//        @Override
//        public String[] fileList() {
//            return context.fileList();
//        }
//
//        @Override
//        public File getDir(String s, int i) {
//            return context.getDir(s,i);
//        }
//
//        @Override
//        public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory) {
//            return context.openOrCreateDatabase(s,i,cursorFactory);
//        }
//
//        @Override
//        public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory, @Nullable DatabaseErrorHandler databaseErrorHandler) {
//            return context.openOrCreateDatabase(s,i,cursorFactory,databaseErrorHandler);
//        }
//
//        @Override
//        public boolean deleteDatabase(String s) {
//            return context.deleteDatabase(s);
//        }
//
//        @Override
//        public File getDatabasePath(String s) {
//            return context.getDatabasePath(s);
//        }
//
//        @Override
//        public String[] databaseList() {
//            return context.databaseList();
//        }
//
//        @Override
//        public Drawable getWallpaper() {
//            return context.getWallpaper();
//        }
//
//        @Override
//        public Drawable peekWallpaper() {
//            return context.peekWallpaper();
//        }
//
//        @Override
//        public int getWallpaperDesiredMinimumWidth() {
//            return context.getWallpaperDesiredMinimumWidth();
//        }
//
//        @Override
//        public int getWallpaperDesiredMinimumHeight() {
//            return context.getWallpaperDesiredMinimumHeight();
//        }
//
//        @Override
//        public void setWallpaper(Bitmap bitmap) throws IOException {
//            context.setWallpaper(bitmap);
//        }
//
//        @Override
//        public void setWallpaper(InputStream inputStream) throws IOException {
//            context.setWallpaper(inputStream);
//        }
//
//        @Override
//        public void clearWallpaper() throws IOException {
//            context.clearWallpaper();
//        }
//
//        @Override
//        public void startActivity(Intent intent) {
//            context.startActivity(intent);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        @Override
//        public void startActivity(Intent intent, @Nullable Bundle bundle) {
//            context.startActivity(intent,bundle);
//        }
//
//        @Override
//        public void startActivities(Intent[] intents) {
//            context.startActivities(intents);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        @Override
//        public void startActivities(Intent[] intents, Bundle bundle) {
//            context.startActivities(intents,bundle);
//        }
//
//        @Override
//        public void startIntentSender(IntentSender intentSender, @Nullable Intent intent, int i, int i1, int i2) throws IntentSender.SendIntentException {
//            context.startIntentSender(intentSender,intent,i,i1,i2);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        @Override
//        public void startIntentSender(IntentSender intentSender, @Nullable Intent intent, int i, int i1, int i2, @Nullable Bundle bundle) throws IntentSender.SendIntentException {
//            context.startIntentSender(intentSender,intent,i,i1,i2,bundle);
//        }
//
//        @Override
//        public void sendBroadcast(Intent intent) {
//            context.sendBroadcast(intent);
//        }
//
//        @Override
//        public void sendBroadcast(Intent intent, @Nullable String s) {
//            context.sendBroadcast(intent,s);
//        }
//
//        @Override
//        public void sendOrderedBroadcast(Intent intent, @Nullable String s) {
//            context.sendOrderedBroadcast(intent,s);
//        }
//
//        @Override
//        public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String s, @Nullable BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s1, @Nullable Bundle bundle) {
//            context.sendOrderedBroadcast(intent,s,broadcastReceiver,handler,i,s1,bundle);
//        }
//
//        @SuppressLint("MissingPermission")
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @Override
//        public void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {
//            context.sendBroadcastAsUser(intent,userHandle);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @SuppressLint("MissingPermission")
//        @Override
//        public void sendBroadcastAsUser(Intent intent, UserHandle userHandle, @Nullable String s) {
//            context.sendBroadcastAsUser(intent,userHandle,s);
//        }
//
//        @SuppressLint("MissingPermission")
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @Override
//        public void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, @Nullable String s, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s1, @Nullable Bundle bundle) {
//            context.sendOrderedBroadcastAsUser(intent,userHandle,s,broadcastReceiver,handler,i,s1,bundle);
//        }
//
//        @SuppressLint("MissingPermission")
//        @Override
//        public void sendStickyBroadcast(Intent intent) {
//            context.sendStickyBroadcast(intent);
//        }
//
//        @SuppressLint("MissingPermission")
//        @Override
//        public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s, @Nullable Bundle bundle) {
//            context.sendStickyOrderedBroadcast(intent,broadcastReceiver,handler,i,s,bundle);
//        }
//
//        @SuppressLint("MissingPermission")
//        @Override
//        public void removeStickyBroadcast(Intent intent) {
//            context.removeStickyBroadcast(intent);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @SuppressLint("MissingPermission")
//        @Override
//        public void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {
//            context.sendStickyBroadcastAsUser(intent,userHandle);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @SuppressLint("MissingPermission")
//        @Override
//        public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s, @Nullable Bundle bundle) {
//            context.sendStickyOrderedBroadcastAsUser(intent,userHandle,broadcastReceiver,handler,i,s,bundle);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @SuppressLint("MissingPermission")
//        @Override
//        public void removeStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {
//            context.removeStickyBroadcastAsUser(intent,userHandle);
//        }
//
//        @Nullable
//        @Override
//        public Intent registerReceiver(@Nullable BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
//            return context.registerReceiver(broadcastReceiver,intentFilter);
//        }
//
//        @Nullable
//        @Override
//        public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, @Nullable String s, @Nullable Handler handler) {
//            return context.registerReceiver(broadcastReceiver,intentFilter,s,handler);
//        }
//
//        @Override
//        public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {
//            context.unregisterReceiver(broadcastReceiver);
//        }
//
//        @Nullable
//        @Override
//        public ComponentName startService(Intent intent) {
//            return context.startService(intent);
//        }
//
//        @Override
//        public boolean stopService(Intent intent) {
//            return context.stopService(intent);
//        }
//
//        @Override
//        public boolean bindService(Intent intent, @NonNull ServiceConnection serviceConnection, int i) {
//            return context.bindService(intent,serviceConnection,i);
//        }
//
//        @Override
//        public void unbindService(@NonNull ServiceConnection serviceConnection) {
//            context.unbindService(serviceConnection);
//        }
//
//        @Override
//        public boolean startInstrumentation(@NonNull ComponentName componentName, @Nullable String s, @Nullable Bundle bundle) {
//            return context.startInstrumentation(componentName,s,bundle);
//        }
//
//        @Nullable
//        @Override
//        public Object getSystemService(@NonNull String s) {
//            return context.getSystemService(s);
//        }
//
//        @TargetApi(Build.VERSION_CODES.M)
//        @Nullable
//        @Override
//        public String getSystemServiceName(@NonNull Class<?> aClass) {
//            return context.getSystemServiceName(aClass);
//        }
//
//        @Override
//        public int checkPermission(@NonNull String s, int i, int i1) {
//            return context.checkPermission(s,i,i1);
//        }
//
//        @Override
//        public int checkCallingPermission(@NonNull String s) {
//            return context.checkCallingPermission(s);
//        }
//
//        @Override
//        public int checkCallingOrSelfPermission(@NonNull String s) {
//            return context.checkCallingOrSelfPermission(s);
//        }
//
//        @TargetApi(Build.VERSION_CODES.M)
//        @Override
//        public int checkSelfPermission(@NonNull String s) {
//            return context.checkSelfPermission(s);
//        }
//
//        @Override
//        public void enforcePermission(@NonNull String s, int i, int i1, @Nullable String s1) {
//            context.enforcePermission(s,i,i1,s1);
//        }
//
//        @Override
//        public void enforceCallingPermission(@NonNull String s, @Nullable String s1) {
//            context.enforceCallingPermission(s,s1);
//        }
//
//        @Override
//        public void enforceCallingOrSelfPermission(@NonNull String s, @Nullable String s1) {
//            context.enforceCallingOrSelfPermission(s,s1);
//        }
//
//        @Override
//        public void grantUriPermission(String s, Uri uri, int i) {
//            context.grantUriPermission(s,uri,i);
//        }
//
//        @Override
//        public void revokeUriPermission(Uri uri, int i) {
//            context.revokeUriPermission(uri,i);
//        }
//
//        @Override
//        public int checkUriPermission(Uri uri, int i, int i1, int i2) {
//            return context.checkUriPermission(uri,i,i1,i2);
//        }
//
//        @Override
//        public int checkCallingUriPermission(Uri uri, int i) {
//            return context.checkCallingUriPermission(uri,i);
//        }
//
//        @Override
//        public int checkCallingOrSelfUriPermission(Uri uri, int i) {
//            return context.checkCallingOrSelfUriPermission(uri,i);
//        }
//
//        @Override
//        public int checkUriPermission(@Nullable Uri uri, @Nullable String s, @Nullable String s1, int i, int i1, int i2) {
//            return context.checkUriPermission(uri,s,s1,i,i1,i2);
//        }
//
//        @Override
//        public void enforceUriPermission(Uri uri, int i, int i1, int i2, String s) {
//            context.enforceUriPermission(uri,i,i1,i2,s);
//        }
//
//        @Override
//        public void enforceCallingUriPermission(Uri uri, int i, String s) {
//            context.enforceCallingUriPermission(uri,i,s);
//        }
//
//        @Override
//        public void enforceCallingOrSelfUriPermission(Uri uri, int i, String s) {
//            context.enforceCallingOrSelfUriPermission(uri,i,s);
//        }
//
//        @Override
//        public void enforceUriPermission(@Nullable Uri uri, @Nullable String s, @Nullable String s1, int i, int i1, int i2, @Nullable String s2) {
//            context.enforceUriPermission(uri,s,s1,i,i1,i2,s2);
//        }
//
//        @Override
//        public Context createPackageContext(String s, int i) throws PackageManager.NameNotFoundException {
//            return context.createPackageContext(s,i);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @Override
//        public Context createConfigurationContext(@NonNull Configuration configuration) {
//            return context.createConfigurationContext(configuration);
//        }
//
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//        @Override
//        public Context createDisplayContext(@NonNull Display display) {
//            return context.createDisplayContext(display);
//        }
//    }

}
