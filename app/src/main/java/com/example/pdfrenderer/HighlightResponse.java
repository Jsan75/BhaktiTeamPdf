package com.example.pdfrenderer;

public class HighlightResponse {
    public int quadrant;
    public byte[] highlightedPdfBytes;

    public HighlightResponse(final int quadrant, final byte[] highlightedPdfBytes) {
        this.quadrant = quadrant;
        this.highlightedPdfBytes = highlightedPdfBytes;
    }

    public int getQuadrant() {
        return this.quadrant;
    }

    public void setQuadrant(final int quadrant) {
        this.quadrant = quadrant;
    }

    public byte[] getHighlightedPdfBytes() {
        return this.highlightedPdfBytes;
    }

    public void setHighlightedPdfBytes(final byte[] highlightedPdfBytes) {
        this.highlightedPdfBytes = highlightedPdfBytes;
    }
}
