package com.example.pdfrenderer;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnglishWordLocationStrategy implements IEventListener, ITextExtractionStrategy {
    private final int pageNum;
    List<WordLocation> wordLocations = new ArrayList<>();
    private final StringBuilder wordBuilder = new StringBuilder();
    private float minX = Float.MAX_VALUE;
    private float minY = Float.MAX_VALUE;
    private float maxX = Float.MIN_VALUE;
    private float maxY = Float.MIN_VALUE;
    private Rectangle rect;
    private TextRenderInfo prevTextRenderInfo = null;
    private final List<Rectangle> rectangles = new ArrayList<>();

    public EnglishWordLocationStrategy(final int pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public void eventOccurred(IEventData data, EventType type) {
        if (type == EventType.RENDER_TEXT) {

            TextRenderInfo renderInfo = (TextRenderInfo) data;
            renderInfo.preserveGraphicsState();

            PdfFont font = renderInfo.getFont();

            String fontName = font.getFontProgram().getFontNames().getFontName();

            List<TextRenderInfo> text = renderInfo.getCharacterRenderInfos();

            for (TextRenderInfo t : text) {
                extractEnglishWords(t.getText(), fontName, t);
            }
        }
    }

    private void extractEnglishWords(final String letter, final String fontName,
                                     final TextRenderInfo renderInfo) {
        if (letter.matches("(?i)[a-zA-Z]+") && !fontName.contains("Guj")) {
            if (prevTextRenderInfo == null ||
                    prevTextRenderInfo.getDescentLine().getStartPoint().get(1) ==
                            renderInfo.getDescentLine().getStartPoint().get(1)) {
                rect = updateBoundingRectangle(renderInfo);
                wordBuilder.append(letter);
                prevTextRenderInfo = renderInfo;
            } else {
                addWordLocationToList();
                prevTextRenderInfo = renderInfo;
                wordBuilder.append(letter);
            }
        } else if (wordBuilder.length() > 0 && letter.equals(" ") && !fontName.contains("Guj")) {
            wordBuilder.append(letter);
            rectangles.add(rect);
            resetBoundingRectangle();
        } else if (wordBuilder.length() > 0 && !wordBuilder.toString().matches("\\s+")) {
            addWordLocationToList();
        }
    }

    public void addWordLocationToList() {
        if (wordBuilder.length() > 0) {
            rectangles.add(rect);
            String[] words = wordBuilder.toString().trim().split("\\s+");
            for (int i = 0; i < words.length; i++) {
                for (int j = i; j < words.length; j++) {
                    StringBuilder combination = new StringBuilder();
                    Rectangle wordRect = new Rectangle(Float.MAX_VALUE, Float.MAX_VALUE,0.0f,0.0f);
                    for (int k = i; k <= j; k++) {
                        combination.append(words[k]).append(" ");
                        wordRect = combineRectangles(wordRect, k);
                    }
                    wordLocations.add(new WordLocation(combination.toString().trim(), j - i + 1, pageNum - 1, wordRect.getX(), wordRect.getY(), wordRect.getWidth(), wordRect.getHeight()));
                }
            }
            rectangles.clear();
            resetBoundingRectangle();
            wordBuilder.setLength(0);
        }
    }

    private Rectangle combineRectangles(final Rectangle rect, final int current) {
        float currentX = rectangles.get(current).getX();
        float currentY = rectangles.get(current).getY();
        float currentWidth = rectangles.get(current).getWidth();
        float currentHeight = rectangles.get(current).getHeight();
        minX = Math.min(rect.getX(), currentX);
        minY = Math.min(rect.getY(), currentY);
        maxX = Math.max(minX + rect.getWidth(), minX + currentWidth);
        maxY = Math.max(minY + rect.getHeight(), minY + currentHeight);
        return new Rectangle(minX, minY, rect.getWidth() + currentWidth, Math.abs(maxY - minY));
    }

    private Rectangle updateBoundingRectangle(TextRenderInfo textRenderInfo) {
        minX = Math.min(minX, Math.min(textRenderInfo.getDescentLine().getStartPoint().get(0),
                textRenderInfo.getDescentLine().getEndPoint().get(0)));
        minY = Math.min(textRenderInfo.getAscentLine().getStartPoint().get(1),
                textRenderInfo.getDescentLine().getStartPoint().get(1));
        maxX = Math.max(textRenderInfo.getAscentLine().getStartPoint().get(0),
                textRenderInfo.getAscentLine().getEndPoint().get(0));
        maxY = Math.max(textRenderInfo.getAscentLine().getEndPoint().get(1),
                textRenderInfo.getDescentLine().getEndPoint().get(1));
        return new Rectangle(minX, minY, Math.abs(maxX - minX), Math.abs(maxY - minY));
    }

    private void resetBoundingRectangle() {
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        maxY = Float.MIN_VALUE;
    }

    @Override
    public Set<EventType> getSupportedEvents() {
        Set<EventType> eventTypes = new HashSet<>();
        eventTypes.add(EventType.RENDER_TEXT);
        return eventTypes;
    }

    @Override
    public String getResultantText() {
        return null;
    }

    public List<WordLocation> getWordLocations() {
        return wordLocations;
    }
}
