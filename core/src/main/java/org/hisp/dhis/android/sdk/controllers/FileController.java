package org.hisp.dhis.android.sdk.controllers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import org.hisp.dhis.android.sdk.persistence.models.ApiResponse;
import org.hisp.dhis.android.sdk.persistence.models.FileResourceResponseModels.FileResourceApiResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;

import retrofit.mime.TypedFile;

public class FileController {
    public static void copyToInternalLoc(String filename, String pathtoSave, Uri fileToCopy, Context context){
        File mydir = context.getDir(pathtoSave,Context.MODE_PRIVATE);
        if(!mydir.exists()){
            mydir.mkdirs();
        }
        FileChannel fiCh = null;
        FileChannel foch = null;
        try {
            File fileout = new File(context.getDir(pathtoSave,Context.MODE_PRIVATE),filename);
            FileOutputStream fileOutputStream = new FileOutputStream(fileout);
            MediaStore.Images.Media.getBitmap(context.getContentResolver(),fileToCopy)
                    .compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static boolean deleteFIle(String folder,String filename,Context context){
        return new File(context.getDir(folder,Context.MODE_PRIVATE),filename).delete();
    }

    public static File readFile(String folder,String filename,Context context){

        return new File(context.getDir(folder,Context.MODE_PRIVATE),filename);
    }
    public static FileResourceApiResponse uploadFileToServer(Uri uri,Context context){
        File file = new File(getRealPathFromURI(uri,context));
        return uploadFileToServer(file);
    }

    public static FileResourceApiResponse uploadFileToServer(File file){
        return DhisController.getInstance().getDhisApi().uploadFile(new TypedFile("multipart/form-data",file));
    }

    public static String getRealPathFromURI(Uri contentUri,Context context) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public interface UploadCompleted{
        public void doAfter();
    }

}
