package com.example.pdfrenderer;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class ZoomGestureDetector implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private OnZoomListener onZoomListener;

    public ZoomGestureDetector(Context context) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (onZoomListener != null) {
                    onZoomListener.onZoom(scaleFactor);
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public void setOnZoomListener(OnZoomListener listener) {
        this.onZoomListener = listener;
    }

    public interface OnZoomListener {
        void onZoom(float scaleFactor);
    }
}
