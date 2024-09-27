package com.example.pdfrenderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomOverlayView extends View {
    private Bitmap highlightedBitmap = null;
    private final Paint paint = new Paint();

    public CustomOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomOverlayView(Context context, Bitmap highlightedBitmap) {
        super(context);
        this.highlightedBitmap = highlightedBitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (highlightedBitmap != null) {
            canvas.drawBitmap(highlightedBitmap, 0, 0, paint);
        }
    }
}

