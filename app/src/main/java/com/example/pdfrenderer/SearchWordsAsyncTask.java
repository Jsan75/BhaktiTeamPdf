package com.example.pdfrenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class SearchWordsAsyncTask extends AsyncTask<String, Void, List<WordLocation>> {
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private final String mSearchTerm;
    private final String mFilePath;
    private final OnSearchCompleteListener mListener;

    public SearchWordsAsyncTask(final Context context, final String searchTerm,
                                final OnSearchCompleteListener listener, final String filePath) {
        mContext = context;
        mSearchTerm = searchTerm;
        mListener = listener;
        mFilePath = filePath;
    }

    @Override
    protected List<WordLocation> doInBackground(String... params) {
        String searchTerm = params[0].trim();
        String[] words = searchTerm.split("\\s+");
        return WordSearch.searchWords(mContext, searchTerm, words.length);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(List<WordLocation> results) {
        LinearLayout searchOverlay = ((Activity) mContext).findViewById(R.id.search_Overlay);
        TextView searchResultCount = ((Activity) mContext).findViewById(R.id.searchResultCount);
        LinearLayout prev = ((Activity) mContext).findViewById(R.id.navigateToPrev);
        LinearLayout next = ((Activity) mContext).findViewById(R.id.navigateToNext);
        if (!mSearchTerm.isEmpty() && !results.isEmpty()) {
            if (mListener != null) {
                try {
                    byte[] highlightedPdf = highlightAllSearchResults(results);
                    mListener.onSearchComplete(highlightedPdf, results);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                searchResultCount.setText(results.size() + " matches");
                prev.setVisibility(View.GONE);
                next.setVisibility(View.GONE);
            }
            searchOverlay.setVisibility(View.VISIBLE); // Show the TextView
        } else {
            searchResultCount.setText(results.size() + " matches");
            // Hide the TextView
            new Handler().postDelayed((Runnable) () -> searchOverlay.setVisibility(View.GONE),
                    5000);
        }
    }

    private byte[] highlightAllSearchResults(final List<WordLocation> results) throws IOException {
        PdfHighlighter.setShouldHighlight(true);
        return PdfHighlighter.addHighlight(mFilePath, results);
    }

    public interface OnSearchCompleteListener {

        void onSearchComplete(final byte[] highlightedPdf, final List<WordLocation> results) throws IOException;

        void navigateToNext(final byte[] highlightedPdf, List<WordLocation> results) throws IOException;

        void navigateToPrev(final byte[] highlightedPdf, List<WordLocation> results) throws IOException;
    }
}
