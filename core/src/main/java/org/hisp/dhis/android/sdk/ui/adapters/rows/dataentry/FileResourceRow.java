package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.FileController;
import org.hisp.dhis.android.sdk.job.JobExecutor;
import org.hisp.dhis.android.sdk.job.NetworkJob;
import org.hisp.dhis.android.sdk.network.APIException;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.ApiResponse;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.FileResourceResponseModels.FileResourceApiResponse;
import org.hisp.dhis.android.sdk.persistence.models.FileResourceResponseModels.FileResourceResponse;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;
import org.hisp.dhis.android.sdk.ui.activities.ExternalAccessActivity;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.utils.api.ValueType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

import static org.hisp.dhis.android.sdk.ui.activities.ExternalAccessActivity.CALLED_METHOD;

public class FileResourceRow extends Row {
    public static final int FILE_RESOURCE_REQUEST = 1434;
    private boolean isUploaded;
    private static final String EMPTY_FIELD = "";
    private DataEntryRowFactory.callbacks callback;
    Uri filePath = null;
    //private final View.OnClickListener clickListener;

    public FileResourceRow(String label, boolean mandatory, String warning,
                           BaseValue baseValue,
                           DataEntryRowTypes rowType) {
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;
        mValue = baseValue;
        mRowType = rowType;

        if (!DataEntryRowTypes.FILE_RESOURCE.equals(rowType)) {
            throw new IllegalArgumentException("Unsupported row type");
        }
        Dhis2Application.getEventBus().register(this);
    }

    @Subscribe
    public void getData(Intent data){
        Log.d("REC_DATA",data.getData().toString());
        filePath = data.getData();


    }



    @Override
    public View getView(FragmentManager fragmentManager, final LayoutInflater inflater, View convertView, final ViewGroup container) {
        View view;

        final FileResourceViewHolder holder;
        if(convertView!=null && convertView.getTag() instanceof FileResourceViewHolder){
            view = convertView;
            holder = (FileResourceViewHolder) view.getTag();

        }else {
            View root = inflater.inflate(R.layout.listview_row_file_resource,container,false);
            TextView label = (TextView) root.findViewById(R.id.text_label);
            TextView mandatoryIndicator = (TextView) root.findViewById(R.id.mandatory_indicator);
            TextView errorLabel = (TextView) root.findViewById(R.id.error_label);
            EditText editText = (EditText) root.findViewById(R.id.edit_text_row);
            ImageButton btnAction = (ImageButton) root.findViewById(R.id.btn_action);
            ImageView imageView = (ImageView) root.findViewById(R.id.image);
            editText.setEnabled(true);

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(inflater.getContext(), ExternalAccessActivity.class);
                    inflater.getContext().startActivity(intent);
                }
            });
            holder = new FileResourceViewHolder(btnAction,"",label,mandatoryIndicator,errorLabel
            ,editText,imageView);
            root.setTag(holder);
            view = root;
        }

        holder.label.setText(mLabel);
        holder.editText.setText(mValue.getValue());




        if (mValue.getValue()!=null && !mValue.getValue().equals("")){
            Log.d("Value_F",mValue.getValue());
//            File fileout = new File(inflater.getContext().getDir("FILE_RESOURCE",Context.MODE_PRIVATE),mValue.getValue());
            File fileout = FileController.readFile("FILE_RESOURCE",mValue.getValue(),inflater.getContext());
            Bitmap bitmap = BitmapFactory.decodeFile(fileout.getPath());
            holder.imageView.setImageBitmap(bitmap);
            holder.btnAction.setImageResource(R.drawable.ic_delete);
            holder.btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(FileController.deleteFIle("FILE_RESOURCE",mValue.getValue(),inflater.getContext())){
                        holder.btnAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(inflater.getContext(), ExternalAccessActivity.class);
                                inflater.getContext().startActivity(intent);
                            }
                        });
                        holder.btnAction.setImageResource(R.drawable.ic_add);
                        mValue.setValue("");
                        holder.editText.setText("");
                        holder.imageView.setVisibility(View.GONE);
                        Dhis2Application.getEventBus().post(new RowValueChangedEvent(mValue,ValueType.FILE_RESOURCE.toString()));

                    }

                }
            });
        }else if (filePath!=null){
            JobExecutor.enqueueJob(new NetworkJob<Object>(1, ResourceType.FILE_RESOURCE) {
                @Override
                public Object execute(){
                    Looper.prepare();
                    try {
                        FileResourceApiResponse response = FileController.uploadFileToServer(filePath, inflater.getContext());
                        if(response.getHttpStatusCode().equals("202")){
                            String fileName = response.getResponse().getFileResource().getId();
                            FileController.copyToInternalLoc(fileName,"FILE_RESOURCE",filePath,inflater.getContext());

                            //holder.imageView.setVisibility(View.VISIBLE);
//              File fileout = new File(inflater.getContext().getDir("FILE_RESOURCE",Context.MODE_PRIVATE),"f");
                            //File fileout = FileController.readFile("FILE_RESOURCE",fileName,inflater.getContext());
                            //Bitmap bitmap = BitmapFactory.decodeFile(fileout.getPath());
                            //holder.imageView.setImageBitmap(bitmap);
                            mValue.setValue(fileName);
                            //holder.editText.setText(fileName);
                            Dhis2Application.getEventBus().post(new RowValueChangedEvent(mValue,ValueType.FILE_RESOURCE.toString()));

                        }else{
                            mError = "File Upload Failed";
//                        holder.errorLabel.setVisibility(View.VISIBLE);
//                        holder.errorLabel.setText("File Upload Failed");
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                        mError = "File Upload Failed";
                    }

                    return new Object();
                }
            });
        }

        if (mError == null) {
            holder.errorLabel.setVisibility(View.GONE);
        } else {
            holder.errorLabel.setVisibility(View.VISIBLE);
            holder.errorLabel.setText(mError);
        }

        if (!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }

        return view;

    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.FILE_RESOURCE.ordinal();
    }



    private class FileResourceViewHolder{
        private final TextView label;
        private final TextView mandatoryIndicator;
        private final TextView errorLabel;
        private final EditText editText;
        private final ImageButton btnAction;
        private final ImageView imageView;
        public final String id;



        public FileResourceViewHolder(ImageButton btn, String id, TextView label, TextView mandatoryIndicator,
                                      TextView errorLabel, EditText editText,ImageView imageView){
            this.btnAction = btn;
            this.id = id;
            this.label = label;
            this.mandatoryIndicator = mandatoryIndicator;
            this.errorLabel = errorLabel;

            this.editText = editText;
            this.imageView = imageView;
        }
    }


    public interface FileResRowListener extends DataEntryRowFactory.callbacks,View.OnClickListener{

        void setActivityResultCallback(ImageResultCallback activityResultCallback);
        ImageResultCallback getActivityResultCallback();
    }

    public interface ImageResultCallback {
        void onActivityResult(Intent data);
    }

//    private class ActionReceiverContext extends Context{
//        private final Context context;
//
//        public void onActivityResult(){
//
//        }
//
//        public ActionReceiverContext(Context context) {
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
//
//
//    }
//
//    public interface ActionReceiverContextCallback {
//
//    }
}
