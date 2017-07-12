package com.evans.proto.skindetection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Evans on 17-Jan-17.
 */

public class WaitAnalyzeAsyncTask extends AsyncTask<String, Void, ArrayList<String>> {
    public AsyncTaskResult delegate = null;
    Context context = null;
    @Override
    protected ArrayList<String> doInBackground(String... strings) {
        try {
            String path = strings[0];
            URL url = null;
            HttpURLConnection httpURLConnection = null;
            url = new URL ("http://" + context.getText (R.string.hostname) + "/" + path);
            Log.d("Check", "http://" + context.getText (R.string.hostname) + "/" + path);

            do {
                httpURLConnection = (HttpURLConnection) url.openConnection ();
                httpURLConnection.setRequestMethod ("GET");
                httpURLConnection.setDoInput (true);
                httpURLConnection.setUseCaches (false);
                httpURLConnection.setDefaultUseCaches (false);
                httpURLConnection.disconnect ();
                Thread.sleep (1000);
                Log.d("Resp", Integer.toString (httpURLConnection.getResponseCode ()));
            } while (httpURLConnection.getResponseCode () == 404);

            // Reads HTTP Output

            BufferedReader bufferedReader =
                    new BufferedReader (
                            new InputStreamReader (
                                    new BufferedInputStream (
                                            httpURLConnection.getInputStream ()
                                    )
                                    , "UTF-8"
                            )
                    );
            ArrayList<String> resultList = new ArrayList<> ();
            do {
                resultList.add (bufferedReader.readLine ());
            } while (bufferedReader.ready ());
            return resultList;

        } catch (MalformedURLException e) {
            e.printStackTrace ();
        } catch (ProtocolException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList ResultArrayList) {
        super.onPostExecute (ResultArrayList);
        if (delegate != null) {
            delegate.onAnalyzeCompleteResult (ResultArrayList);
        }

    }
}
