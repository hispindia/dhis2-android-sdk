package org.hisp.dhis.android.sdk.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.FileResourceRow;

public class ExternalAccessActivity extends Activity{
    private MyFileResRowListener fileResRowListener;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(fileResRowListener!=null && fileResRowListener.getActivityResultCallback()!=null){
//            fileResRowListener.getActivityResultCallback().onActivityResult(data);
//        }
        Dhis2Application.getEventBus().post(data);
        finish();
    }



    class MyFileResRowListener implements FileResourceRow.FileResRowListener {
        FileResourceRow.ImageResultCallback imageResultCallback;

        @Override
        public void setActivityResultCallback(FileResourceRow.ImageResultCallback activityResultCallback) {
            imageResultCallback = activityResultCallback;
        }

        @Override
        public FileResourceRow.ImageResultCallback getActivityResultCallback() {
            return imageResultCallback;
        }

        @Override
        public void onClick(View view) {
            callForImageFromGallery();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callForImageFromGallery();
    }

    private void callForImageFromGallery(){

        Dhis2Application.getEventBus().register(this);

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose Picture"), 1);
    }

}
