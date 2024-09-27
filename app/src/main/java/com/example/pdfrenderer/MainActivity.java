package com.example.pdfrenderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.ScrollHandle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainActivity extends Activity implements SearchWordsAsyncTask.OnSearchCompleteListener {

    private PDFView pdfView;
    private EditText editTextSearch;
    private EditText numberInput;
    private TextView searchResultCount;
    private TextView btnPrevious;
    private TextView btnNext;
    private TextView btnZoomIn;
    private TextView btnZoomOut;
    private String filePath;
    private int currentPage;
    private byte[] highlightedpdf;
    private List<WordLocation> wordLocationList;
    private ScrollHandle scrollHandle;
    private static final String assetFileName = "Bhakti.pdf";
    private static final String PREFS_NAME = "pdf_srmd";
    private static final String PDF_PROCESSING_COMPLETED = "pdf_processing_completed";
    private static final String DATABASE_NAME = "WordLocations.db";
    private static int itr = 0;
    private boolean backSpace = false;

    private static int totalPages;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final ProcessPdfAsyncTask asyncTask = new ProcessPdfAsyncTask(this);

        filePath = AssetFileHelper.copyAssetFileToCache(this, assetFileName);
        assert filePath != null;

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pdf_viewer);
        pdfView = findViewById(R.id.pdfView);
        editTextSearch = findViewById(R.id.searchEditText);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnZoomIn = findViewById(R.id.zoomIn);
        btnZoomOut = findViewById(R.id.zoomOut);
        numberInput = findViewById(R.id.pageNumber);
        TextView totalPagesText = findViewById(R.id.totalPagesText);
        FrameLayout searchLayout = findViewById(R.id.searchBox);
        searchResultCount = findViewById(R.id.searchResultCount);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        // Check if PDF processing has already been completed
        boolean isPdfProcessingCompleted = settings.getBoolean(PDF_PROCESSING_COMPLETED, false);

        if (!isPdfProcessingCompleted) {
            isPdfProcessingCompleted = copyDatabaseFromAssets();
        }

        Log.d("jitesh", "pdf processing: " + isPdfProcessingCompleted);

        if (isPdfProcessingCompleted) {
            editor.putBoolean(PDF_PROCESSING_COMPLETED, true);
            editor.apply();
            searchLayout.setVisibility(View.VISIBLE);
        }

        if (!isPdfProcessingCompleted) {
            // Process PDF and store in SQLite
            asyncTask.execute(filePath);

            // Mark PDF processing as completed
            editor.putBoolean(PDF_PROCESSING_COMPLETED, true);
            editor.apply();
        }

        updateButton();

        pdfView.useBestQuality(true);
        scrollHandle = new CustomScrollHandle(this);

        // Load PDF from assets
        pdfView.fromAsset("Bhakti.pdf")
                .enableSwipe(true)
                .enableDoubletap(true)
                .defaultPage(0)
                .onPageChange((page, pageCount) -> {
                    numberInput.setText(String.valueOf(page + 1));
                    currentPage = page + 1;
                    pdfView.zoomTo(pdfView.getZoom());
                    pdfView.invalidate();
                })
                .scrollHandle(scrollHandle)
                .load();

        currentPage = pdfView.getCurrentPage() + 1;


        try {
            totalPages = getTotalPages();
            totalPagesText.setText("/ " + totalPages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        numberInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String pageNumberText = v.getText().toString();
                int pageNumber = Integer.parseInt(pageNumberText);
                if(pageNumber == 1) {
                    btnPrevious.setEnabled(false);
                    btnPrevious.setTextColor(Color.GRAY);
                    btnNext.setEnabled(true);
                    btnNext.setTextColor(Color.DKGRAY);
                } else {
                    if (pageNumber == totalPages) {
                        btnNext.setEnabled(false);
                        btnNext.setTextColor(Color.GRAY);
                        btnPrevious.setEnabled(true);
                        btnPrevious.setTextColor(Color.DKGRAY);
                    } else {
                        btnPrevious.setEnabled(true);
                        btnPrevious.setTextColor(Color.DKGRAY);
                        btnNext.setEnabled(true);
                        btnNext.setTextColor(Color.DKGRAY);
                    }
                }
                if (pageNumber < 1 || pageNumber > totalPages) {
                    Toast.makeText(this, "Page no. should be between 1 and " + totalPages, Toast.LENGTH_SHORT).show();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                    return false;
                } else {
                    pdfView.jumpTo(pageNumber - 1, false);
                    currentPage = pdfView.getCurrentPage() + 1;
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                    return true;
                }
            }
            return false;
        });

        editTextSearch.setInputType(InputType.TYPE_NULL);

        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                itr = 0;
                performSearch(v.getText().toString().trim(), this);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                editTextSearch.clearFocus();
                backSpace = true;
                return true;
            }
            return false;
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               if (before > count && backSpace) {
                    backSpace = false;
                    int cPage = pdfView.getCurrentPage();
                    pdfView.fromAsset("Bhakti.pdf")
                            .defaultPage(cPage)
                            .scrollHandle(scrollHandle)
                            .onPageChange((page, pageCount) -> {
                                numberInput.setText(String.valueOf(page + 1));
                                currentPage = page + 1;
                                pdfView.zoomTo(pdfView.getZoom());
                                pdfView.invalidate();
                            })
                            .enableDoubletap(true)
                            .enableSwipe(true)
                            .load();
                }
                performSearch(s.toString(), null);
               itr = 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        editTextSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editTextSearch.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                editTextSearch.setInputType(InputType.TYPE_NULL);
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull final Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private int getTotalPages() throws IOException {
        PdfReader reader = new PdfReader(filePath);
        PdfDocument pdfDocument = new PdfDocument(reader);
        int totalPages = pdfDocument.getNumberOfPages();
        pdfDocument.close();
        return totalPages;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.action_search == item.getItemId()) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSearchComplete(final byte[] highlightedPdf, final List<WordLocation> results) throws IOException {
        wordLocationList = results;
        pdfView.fromBytes(highlightedPdf)
                .defaultPage(results.get(itr).getPageNumber())
                .scrollHandle(scrollHandle)
                .onPageChange((page, pageCount) -> {
                    numberInput.setText(String.valueOf(page + 1));
                    currentPage = page + 1;
                    pdfView.zoomTo(pdfView.getZoom());
                    pdfView.invalidate();
                })
                .enableDoubletap(true)
                .enableSwipe(true)
                .load();

        currentPage = results.get(itr).getPageNumber();

        int currentItr = itr + 1;

        searchResultCount.setText("Match " + currentItr + " of " + results.size());
        LinearLayout prev = this.findViewById(R.id.navigateToPrev);
        LinearLayout next = this.findViewById(R.id.navigateToNext);

        prev.setVisibility(View.VISIBLE);
        next.setVisibility(View.VISIBLE);

        actionsPostSearch(highlightedPdf, results, true);

        prev.setOnClickListener(v -> {
            try {
                navigateToPrev(highlightedPdf, results);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        next.setOnClickListener(v -> {
            try {
                navigateToNext(highlightedPdf, results);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void navigateToNext(final byte[] highlightedPdf, List<WordLocation> results) throws IOException {
        int prevItr = itr;
        if (itr < results.size() - 1) {
            itr = itr + 1;
        } else {
            itr = 0;
        }
        actionsPostSearch(highlightedPdf, results, results.get(prevItr).getPageNumber() != results.get(itr).getPageNumber());
    }

    @Override
    public void navigateToPrev(final byte[] highlightedPdf, final List<WordLocation> results) throws IOException {
        int nextItr = itr;
        if (itr > 0) {
            itr = itr - 1;
        } else {
            itr = results.size() - 1;
        }
        actionsPostSearch(highlightedPdf, results, results.get(nextItr).getPageNumber() != results.get(itr).getPageNumber());
    }

    private void performSearch(final String searchTerm,
                               final SearchWordsAsyncTask.OnSearchCompleteListener onSearchCompleteListener) {
        SearchWordsAsyncTask searchTask = new SearchWordsAsyncTask(MainActivity.this, searchTerm,
                onSearchCompleteListener, filePath);
        searchTask.execute(searchTerm);
    }

    private void updateButton() {
        if(currentPage == 0) {
            btnPrevious.setEnabled(false);
            btnPrevious.setTextColor(Color.GRAY);
            btnNext.setEnabled(true);
            btnNext.setTextColor(Color.DKGRAY);
        } else {
            if (currentPage == totalPages - 1) {
                btnNext.setEnabled(false);
                btnNext.setTextColor(Color.GRAY);
                btnPrevious.setEnabled(true);
                btnPrevious.setTextColor(Color.DKGRAY);
            } else {
                btnPrevious.setEnabled(true);
                btnPrevious.setTextColor(Color.DKGRAY);
                btnNext.setEnabled(true);
                btnNext.setTextColor(Color.DKGRAY);
            }
        }

        if (pdfView.getZoom() == PDFView.DEFAULT_MIN_SCALE) {
            btnZoomOut.setEnabled(false);
            btnZoomOut.setTextColor(Color.GRAY);
            btnZoomIn.setEnabled(true);
            btnZoomIn.setTextColor(Color.DKGRAY);
        } else if (pdfView.getZoom() == PDFView.DEFAULT_MAX_SCALE) {
            btnZoomOut.setEnabled(false);
            btnZoomOut.setTextColor(Color.GRAY);
            btnZoomIn.setEnabled(true);
            btnZoomIn.setTextColor(Color.DKGRAY);
        } else {
            btnZoomOut.setEnabled(true);
            btnZoomOut.setTextColor(Color.DKGRAY);
            btnZoomIn.setEnabled(true);
            btnZoomIn.setTextColor(Color.DKGRAY);
        }

        btnZoomIn.setOnClickListener(v -> {
            if (pdfView.getZoom() <= PDFView.DEFAULT_MAX_SCALE) {
                btnZoomOut.setEnabled(true);
                btnZoomOut.setTextColor(Color.DKGRAY);
                btnZoomIn.setEnabled(true);
                btnZoomIn.setTextColor(Color.DKGRAY);
                pdfView.zoomCenteredTo(pdfView.getZoom() + 0.25f, new PointF(pdfView.getWidth()/2.0f, pdfView.getHeight()/2.0f));
                pdfView.invalidate();
            } else {
                btnZoomIn.setEnabled(false);
                btnZoomIn.setTextColor(Color.GRAY);
                btnZoomOut.setEnabled(true);
                btnZoomOut.setTextColor(Color.DKGRAY);
            }
        });

        btnZoomOut.setOnClickListener(v -> {
            if (pdfView.getZoom() > PDFView.DEFAULT_MIN_SCALE) {
                btnZoomOut.setEnabled(true);
                btnZoomOut.setTextColor(Color.DKGRAY);
                btnZoomIn.setEnabled(true);
                btnZoomIn.setTextColor(Color.DKGRAY);
                pdfView.zoomCenteredTo(pdfView.getZoom() - 0.25f, new PointF(pdfView.getWidth()/2.0f, pdfView.getHeight()/2.0f));
                pdfView.invalidate();
            } else {
                btnZoomOut.setEnabled(false);
                btnZoomOut.setTextColor(Color.GRAY);
                btnZoomIn.setEnabled(true);
                btnZoomIn.setTextColor(Color.DKGRAY);
            }
        });

        btnPrevious.setOnClickListener(v -> {
            btnPrevious.setEnabled(true);
            btnPrevious.setTextColor(Color.DKGRAY);
            btnNext.setEnabled(true);
            btnNext.setTextColor(Color.DKGRAY);
            pdfView.jumpTo(pdfView.getCurrentPage() - 1, false);
            currentPage = pdfView.getCurrentPage() + 1;
            numberInput.setText(String.valueOf(currentPage));
            int pageToNavigate = pdfView.getCurrentPage() - 1;
            if(pageToNavigate < 0) {
                btnPrevious.setEnabled(false);
                btnPrevious.setTextColor(Color.GRAY);
            }
        });

        btnNext.setOnClickListener(v -> {
            btnNext.setEnabled(true);
            btnNext.setTextColor(Color.DKGRAY);
            btnPrevious.setEnabled(true);
            btnPrevious.setTextColor(Color.DKGRAY);
            pdfView.jumpTo(pdfView.getCurrentPage() + 1, false);
            currentPage = pdfView.getCurrentPage() + 1;
            numberInput.setText(String.valueOf(currentPage));
            int pageToNavigate = currentPage + 1;
            if(pageToNavigate > totalPages - 1) {
                btnNext.setEnabled(false);
                btnNext.setTextColor(Color.GRAY);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void actionsPostSearch(final byte[] highlightedPdf, final List<WordLocation> results, final boolean jump) throws IOException {
        int currentItr = itr + 1;
        searchResultCount.setText("Match " + currentItr + " of " + results.size());
        currentPage = results.get(itr).getPageNumber();

//        HighlightResponse highlightResponse = PdfHighlighter.highlightWord(highlightedPdf, results.get(itr));

//        if (pdfView.getZoom() < 1.25f) {
//            if (highlightResponse.getQuadrant() == 1) {
//                pdfView.zoomCenteredTo(1.25f, new PointF(pdfView.getWidth() / 3.0f,  pdfView.getHeight() / 3.0f));
//            } else if (highlightResponse.getQuadrant() == 2) {
//                pdfView.zoomCenteredTo(1.25f, new PointF((2.0f * pdfView.getWidth()) / 3.0f, pdfView.getHeight() / 3.0f));
//            } else if (highlightResponse.getQuadrant() == 3) {
//                pdfView.zoomCenteredTo(1.25f, new PointF(pdfView.getWidth() / 3.0f, (2.0f * pdfView.getHeight() / 3.0f)));
//            } else if (highlightResponse.getQuadrant() == 4) {
//                pdfView.zoomCenteredTo(1.25f, new PointF((2.0f * pdfView.getWidth()) / 3.0f, (2.0f * pdfView.getHeight() / 3.0f)));
//            }
//        }

        updateButton();

        numberInput.setText(String.valueOf(currentPage + 1));

        if (jump) {
            pdfView.jumpTo(currentPage, false);
        }
    }

    private boolean copyDatabaseFromAssets() {
        try {
            InputStream myInput = getAssets().open(DATABASE_NAME);
            String outFileName = getDatabasePath(DATABASE_NAME).getPath();

            OutputStream myOutput = Files.newOutputStream(Paths.get(outFileName));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();

            // Check if the database file exists after copying
            File file = new File(outFileName);
            if (file.exists()) {
                // Database copied successfully
                Log.d("Database Copy", "Database copied successfully");
                return true;
            } else {
                // Database copy failed
                Log.d("Database Copy", "Failed to copy database");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

//    private byte[] unHighlightAllSearchResults(final byte[] filePath, final List<WordLocation> results) throws IOException {
//        return PdfHighlighter.unHighlightAllResults(filePath, results);
//    }
}
