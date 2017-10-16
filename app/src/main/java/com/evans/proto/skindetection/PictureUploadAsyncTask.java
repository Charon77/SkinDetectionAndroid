package com.evans.proto.skindetection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class PictureUploadAsyncTask extends AsyncTask<String, Void, String> {
    AsyncTaskResult delegate = null;
    Context context = null;
    @Override
    protected String doInBackground(String... strings) {
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                String filename = strings[0];

                URL url = null;
                url = new URL("http://" + MainActivity.Host + "/upload");

                Log.d("PictureUploadAsyncTask", url.toString());

                String attachmentName = "picture";
                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";


                HttpURLConnection httpURLConnection = null;
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + boundary);


                // Writes HTTP Input

                BufferedWriter httpBufferedWriter = new BufferedWriter(
                        new OutputStreamWriter(
                                new BufferedOutputStream(
                                        httpURLConnection.getOutputStream()
                                )
                        )
                );

                // Start content wrapper
                httpBufferedWriter.write(twoHyphens + boundary + crlf);
                httpBufferedWriter.write("Content-Disposition: form-data; filename=\"image.jpg\"; name=\"" +
                        attachmentName + "\";" + crlf);
                httpBufferedWriter.write("Content-Type: image/jpeg;" + crlf);
                httpBufferedWriter.write("Content-Transfer-Encoding: binary" + crlf);
                httpBufferedWriter.write(crlf);

                httpBufferedWriter.flush();

                FileInputStream fileInputStream = null;
                fileInputStream = new FileInputStream(filename);

                Log.d("FILESIZE", Integer.toString(fileInputStream.available()));

                final BufferedReader fileReadStream = new BufferedReader(
                        new InputStreamReader(
                                fileInputStream
                        )
                );

                //Unsafe
                httpBufferedWriter.flush();

                while (fileInputStream.available() > 0) {
                    byte[] buffer = new byte[4096];
                    fileInputStream.read(buffer);
                    httpURLConnection.getOutputStream().write(buffer);
                }

                httpURLConnection.getOutputStream().flush();
                //httpBufferedWriter.write("SDLFKJ");


                // Stop content wrapper
                httpBufferedWriter.write(crlf);
                httpBufferedWriter.write(twoHyphens + boundary + twoHyphens + crlf);

                httpBufferedWriter.flush();
                httpBufferedWriter.close();

                // Reads HTTP Output

                BufferedReader bufferedReader =
                        new BufferedReader(
                                new InputStreamReader(
                                        new BufferedInputStream(
                                                httpURLConnection.getInputStream()
                                        )
                                        , "UTF-8"
                                )
                        );

                StringBuffer stringBuffer = new StringBuffer();
                String line = "";

                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }

                Log.d("READ", new String(stringBuffer));

                return new String(stringBuffer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute (s);
        if(delegate != null) {
            delegate.onPictureUploadedResult (s);
        }
    }
}
