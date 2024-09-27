package com.example.pdfrenderer;

import com.itextpdf.kernel.pdf.PdfDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;

public class PdfProcessor {

    public static List<WordLocation> extractWords(String filePath) {
        List<WordLocation> wordLocationList = new ArrayList<>();

        try {
            PdfReader reader = new PdfReader(filePath);
            PdfDocument pdfDoc = new PdfDocument(reader);

            int totalPages = pdfDoc.getNumberOfPages();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                EnglishWordLocationStrategy extractionStrategy = new EnglishWordLocationStrategy(pageNum);
                PdfCanvasProcessor parser = new PdfCanvasProcessor(extractionStrategy);
                parser.processPageContent(pdfDoc.getPage(pageNum));
                extractionStrategy.addWordLocationToList();
                wordLocationList.addAll(extractionStrategy.getWordLocations());
            }

            pdfDoc.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return  wordLocationList;
    }
}
