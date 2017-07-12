package com.evans.proto.skindetection;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AsyncTaskResult{
    @BindView(R.id.btnCamera) Button btnCamera;
    @BindView(R.id.btnSubmit) Button btnSubmit;
    @BindView(R.id.imageView) ImageView mImageView;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog (this);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentPhotoPath != null) {
                    uploadPic(mCurrentPhotoPath);
                } else {
                }
            }
        });

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's AsyncTaskResult camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    // private void dispatchTakePictureIntent() {
    //     Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //     if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
    //         startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    //     }
    // }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save AsyncTaskResult file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into AsyncTaskResult Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private void uploadPic(String filename)
    {
        Log.d("FILENAME", filename);
        PictureUploadAsyncTask uploadAsyncTask = new PictureUploadAsyncTask();
        uploadAsyncTask.delegate = this;
        uploadAsyncTask.context = this;
        uploadAsyncTask.execute(filename);
    }

    @Override
    public void onPictureUploadedResult(String result) {
        // Log.d("V", "Please visit: " + result);
        WaitAnalyzeAsyncTask waitAnalyzeAsyncTask = new WaitAnalyzeAsyncTask ();
        waitAnalyzeAsyncTask.delegate = this;
        waitAnalyzeAsyncTask.context = this;
        waitAnalyzeAsyncTask.execute (result);
        progressDialog.setMessage ("Please wait. This should take about ten seconds.");
        progressDialog.show ();
    }

    @Override
    public void onAnalyzeCompleteResult(final ArrayList<String> ResultArrayList) {
        progressDialog.dismiss ();
        StringBuilder sb = new StringBuilder ();

        for (String s : ResultArrayList)
        {
            sb.append (s);
            sb.append ("\n");
        }

        (new AlertDialog.Builder (this))
                .setTitle ("Here's your result")
                .setMessage (new String(sb))
                .setPositiveButton ("DONE", new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss ();
                    }
                })
                .create ()
                .show ();
    }
}
