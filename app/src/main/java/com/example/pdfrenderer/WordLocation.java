package com.example.pdfrenderer;

public class WordLocation {
    private String word;
    private int wordCount;
    private int pageNumber;
    private float x;
    private float y;
    private float width;
    private float height;


    public WordLocation(final String word, final int wordCount, final int pageNumber, final float x, final float y,
                        final float width, final float height) {
        this.word = word;
        this.wordCount = wordCount;
        this.pageNumber = pageNumber;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setWord(final String word) {
        this.word = word;
    }

    public String getWord() {
        return this.word;
    }

    public void setWordCount(final int wordCount) {
        this.wordCount = wordCount;
    }

    public int getWordCount() {
        return this.wordCount;
    }

    public void setPageNumber(final int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageNumber() {
        return this.pageNumber;
    }

    public void setX(final float x) {
        this.x = x;
    }

    public float getX() {
        return this.x;
    }

    public void setY(final float y) {
        this.y = y;
    }

    public float getY() {
        return this.y;
    }

    public void setWidth(final float width) {
        this.width = width;
    }

    public float getWidth() {
        return this.width;
    }

    public void setHeight(final float height) {
        this.height = height;
    }

    public float getHeight() {
        return this.height;
    }
}
