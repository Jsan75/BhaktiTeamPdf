package com.example.pdfrenderer;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.itextpdf.kernel.colors.ColorConstants;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class PdfHighlighter {

    private static boolean shouldHighlight = false;

    public static byte[] removeHighlight(final byte[] input, final List<WordLocation> results) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(input);
        PdfReader reader = new PdfReader(inputStream);
        PdfWriter writer = new PdfWriter(outputStream);
        return modifyHighlight(reader, writer, outputStream, results);
    }

    public static byte[] addHighlight(final String filePath, final List<WordLocation> results) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfReader reader = new PdfReader(filePath);
        PdfWriter writer = new PdfWriter(outputStream);
        return modifyHighlight(reader, writer, outputStream, results);
    }

    private static byte[] modifyHighlight(final PdfReader reader, final PdfWriter writer, final ByteArrayOutputStream outputStream, final List<WordLocation> results) {

        PdfDocument pdfDoc = new PdfDocument(reader, writer);

        for (WordLocation wordLocation : results) {
            PdfPage page = pdfDoc.getPage(wordLocation.getPageNumber() + 1);
            PdfCanvas canvas = new PdfCanvas(page);

            if (shouldHighlight) {
                PdfExtGState pdfExtGState = new PdfExtGState().setFillOpacity(0.25f);
                canvas.saveState()
                        .setExtGState(pdfExtGState)
                        .setFillColor(ColorConstants.YELLOW)
                        .rectangle(wordLocation.getX(), wordLocation.getY(), wordLocation.getWidth(), wordLocation.getHeight())
                        .fill()
                        .restoreState();
            } else {
                PdfExtGState pdfExtGState = new PdfExtGState().setFillOpacity(0);
                canvas.saveState()
                        .setExtGState(pdfExtGState)
                        .setFillColor(ColorConstants.WHITE)
                        .rectangle(wordLocation.getX(), wordLocation.getY(), wordLocation.getWidth(), wordLocation.getHeight())
                        .fill()
                        .restoreState();
            }
        }

        pdfDoc.close();

        return outputStream.toByteArray();
    }

    public static void setShouldHighlight(final boolean highlight) {
        shouldHighlight = highlight;
    }



//    public static HighlightResponse highlightWord(final byte[] input, final WordLocation wordLocation) throws IOException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        InputStream inputStream = new ByteArrayInputStream(input);
//        PdfReader reader = new PdfReader(inputStream);
//        PdfWriter writer = new PdfWriter(outputStream);
//
//        PdfDocument pdfDoc = new PdfDocument(reader, writer);
//
//        PdfPage page = pdfDoc.getPage(wordLocation.getPageNumber() + 1);
//        PdfCanvas canvas = new PdfCanvas(page);
//        PdfExtGState pdfExtGState = new PdfExtGState().setFillOpacity(0.5f);
//        canvas.saveState()
//                .setExtGState(pdfExtGState)
//                .setFillColor(ColorConstants.YELLOW)
//                .rectangle(wordLocation.getX(), wordLocation.getY(), wordLocation.getWidth(), wordLocation.getHeight())
//                .fill()
//                .restoreState();
//
//        int quad = 0;
//
//        if ((wordLocation.getHeight() / page.getPageSize().getHeight()) < 0.02f) {
//            float x = (wordLocation.getX() + wordLocation.getWidth() / 2) / page.getPageSize().getWidth();
//            float y = (wordLocation.getY() + wordLocation.getHeight() / 2) / page.getPageSize().getHeight();
//
//            if (x <= 0.5f) {
//                if (y > 0.5f) {
//                    quad = 1;
//                } else {
//                    quad = 3;
//                }
//            } else {
//                if (y > 0.5f) {
//                    quad = 2;
//                } else {
//                    quad = 4;
//                }
//            }
//        }
//
//        pdfDoc.close();
//
//        return new HighlightResponse(quad, outputStream.toByteArray());
//    }
}
