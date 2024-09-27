package com.example.pdfrenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.List;

public class ProcessPdfAsyncTask extends AsyncTask<String, Void, List<WordLocation>> {
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;

    public ProcessPdfAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected List<WordLocation> doInBackground(String... params) {
        String pdfPath = params[0];
        return PdfProcessor.extractWords(pdfPath);
    }

    @Override
    protected void onPostExecute(List<WordLocation> wordLocations) {
        WordSearch.insertWordsLocation(mContext, wordLocations);
        FrameLayout searchLayout = ((Activity) mContext).findViewById(R.id.searchBox);
        searchLayout.setVisibility(View.VISIBLE);
        Log.d("jitesh", "process completed");
    }
}


