package com.evans.proto.skindetection;

import java.util.ArrayList;

/**
 * Created by Evans on 13-Jan-17.
 */

public interface AsyncTaskResult {
    void onPictureUploadedResult(String result);
    void onAnalyzeCompleteResult(ArrayList<String> ResultArrayList);
    void onServerFound(String serverIP);
}
