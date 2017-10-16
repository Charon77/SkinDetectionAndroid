package com.evans.proto.skindetection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements AsyncTaskResult{
    @BindView(R.id.btnCamera) Button btnCamera;
    @BindView(R.id.btnSubmit) Button btnSubmit;
    @BindView(R.id.imageView) ImageView mImageView;
    @BindView(R.id.txtVeredictMain) TextView mTxtVeredictMain;

    private ProgressDialog progressDialog;
    public static String Host = "";

    Uri imageUri = null;

    Typeface nicknameTypeface;
    ProgressDialog connectingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog (this);
        connectingDialog = new ProgressDialog(this);
        connectingDialog.setTitle("Connecting to server, please wait...");
        connectingDialog.setCancelable(false);
        connectingDialog.show();

        (new NetworkDiscovery(this)).execute();

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
                String[] choices = new String[] {"Take a Picture", "Choose from Gallery"};

                final int CAMERA = 0;
                final int GALLERY = 1;
                dialogBuilder.setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case CAMERA:
                                dispatchCameraIntent();
                                break;
                            case GALLERY:
                                dispatchGalleryIntent();
                                break;
                        }
                    }
                });
                dialogBuilder.create().show();
            }
        });


        nicknameTypeface = Typeface.createFromAsset(getAssets(), "fonts/Nickname DEMO.otf");
        btnSubmit.setTypeface(nicknameTypeface);
        btnCamera.setTypeface(nicknameTypeface);
        mTxtVeredictMain.setTypeface(nicknameTypeface);
        mTxtVeredictMain.setText("");

        final Context c = this;
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri != null) {
                    /*
                    AlertDialog.Builder b = new AlertDialog.Builder(c);
                    b.setTitle("Enter server IP");

                    final EditText input = new EditText(MainActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);

                    input.setLayoutParams(lp);

                    b.setView(input);

                    b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Host = input.getText().toString();
                            uploadPic(imageUri);
                        }
                    });

                    b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });


                    b.create().show();
                    */
                    uploadPic(imageUri);

                }
            }
        });

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int SELECT_PICTURE_CALLBACK = 2;

    private void dispatchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private void dispatchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult( intent, SELECT_PICTURE_CALLBACK );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        //     Log.d("AND", data.getData().toString());
        //
        // }
        // if (requestCode == SELECT_PICTURE_CALLBACK && resultCode == RESULT_OK) {
        //     Log.d("AND", data.getData().toString());
        // }

        if ( resultCode != RESULT_OK || (requestCode!= REQUEST_IMAGE_CAPTURE && requestCode != SELECT_PICTURE_CALLBACK) ) {
            return;
        }

        imageUri = data.getData();

        try {
            setPic(data.getData(), mImageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



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
        // mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic(Uri uri, ImageView imageView) throws IOException {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // String mCurrentPhotoPath = uri.toString();

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        // Get the dimensions of the bitmap
        //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //bmOptions.inJustDecodeBounds = true;
        //BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        //int photoW = bmOptions.outWidth;
        //int photoH = bmOptions.outHeight;
        //int photoW = b.getWidth();
        //int photoH = b.getHeight();

        //Log.d("width",Integer.toString(photoW) );
        // Determine how much to scale down the image
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into AsyncTaskResult Bitmap sized to fill the View
        //bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scaleFactor;
        //bmOptions.inPurgeable = true;

        //Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    private void uploadPic(Uri uri)
    {
        //Log.d("FILENAME", filename);
        PictureUploadAsyncTask uploadAsyncTask = new PictureUploadAsyncTask();
        uploadAsyncTask.delegate = this;
        uploadAsyncTask.context = this;

        // Resize


        try {
            Bitmap bMap= MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Bitmap out = Bitmap.createScaledBitmap(bMap, 128, 128, false);
            File resizedFile = createImageFile();

            OutputStream fOut=null;
            try {
                fOut = new BufferedOutputStream(new FileOutputStream(resizedFile));
                out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
                bMap.recycle();
                out.recycle();


                uploadAsyncTask.execute(resizedFile.getAbsolutePath());
            } catch (Exception e) { // TODO

            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        //uploadAsyncTask.execute(filename);
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

        AlertDialog dialog = (new AlertDialog.Builder (this))
                //.setTitle ("Here's your result")
                .setView(R.layout.result_layout)
                .setPositiveButton ("DONE", new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss ();
                    }
                })
                .create ();
        dialog.show();

        ImageView imageView = (ImageView) dialog.getWindow().findViewById(R.id.resultImg);
        try {
            setPic(imageUri, imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView textView = (TextView) dialog.getWindow().findViewById(R.id.textVeredict);

        textView.setTypeface(nicknameTypeface);
        String result = new String(sb);

        textView.setText("");
        mTxtVeredictMain.setText("");
        if (result.compareTo("healthy")==1) {
            textView.setText("#ieathealthy");
            mTxtVeredictMain.setText("healthy");
        }
    }

    @Override
    public void onServerFound(String serverIP) {
        Host = serverIP;
        Log.d("Server IP", serverIP);
        connectingDialog.hide();
    }
}
