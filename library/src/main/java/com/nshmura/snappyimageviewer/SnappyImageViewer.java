package com.nshmura.snappyimageviewer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class SnappyImageViewer extends FrameLayout {

    public static final int INVALID_POINTER = -1;

    private static final int STATE_IDLE = 0;
    private static final int STATE_DRAGGING = 1;
    private static final int STATE_PINCHING = 2;
    private static final int STATE_ZOOMED = 5;
    private static final int STATE_SETTLING = 4;
    private static final int STATE_CLOSED = 5;

    private int state = STATE_IDLE;

    private SnappyImageView imageView;
    private List<OnClosedListener> onClosedListeners = new ArrayList<>();

    //for drag
    private SnappyDragHelper snappyDragHelper;
    private int activePointerId;
    private VelocityTracker velocityTracker;
    private int maxVelocity;
    private float currentX;
    private float currentY;

    private RectF initialImageRect = new RectF();
    private Matrix imgInitMatrix = new Matrix();

    public interface OnClosedListener {
        void onClosed();
    }

    public SnappyImageViewer(Context context) {
        super(context);
        init();
    }

    public SnappyImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SnappyImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SnappyImageViewer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        maxVelocity = vc.getScaledMaximumFlingVelocity();

        imageView = new SnappyImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(layoutParams);
        addView(imageView);

        imageView.setListener(new SnappyImageView.Listener() {
            @Override
            public void onImageChanged() {
                updateSize();
            }

            @Override
            public void onDraw() {
                if (snappyDragHelper == null && imageView.getDrawable() != null) {
                    updateSize();
                }
            }
        });
    }

    public void addOnClosedListener(OnClosedListener listener) {
        onClosedListeners.add(listener);
    }

    public void setImageBitmap(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public void setImageResource(int resId) {
        imageView.setImageResource(resId);
    }

    public void setImageURI(Uri uri) {
        imageView.setImageURI(uri);
    }

    public void setImageDrawable(Drawable drawable) {
        imageView.setImageDrawable(drawable);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void updateSize() {
        if (imageView.getDrawable() == null) {
            return;
        }

        float imageWidth = imageView.getDrawable().getIntrinsicWidth();
        float imageHeight = imageView.getDrawable().getIntrinsicHeight();

        float viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        float widthRatio = viewWidth / imageWidth;
        float heightRatio = viewHeight / imageHeight;

        float scale = widthRatio < heightRatio ? widthRatio : heightRatio;
        float imgWidth = imageWidth * scale;
        float imgHeight = imageHeight * scale;

        float x = (viewWidth - imgWidth) / 2f;
        float y = (viewHeight - imgHeight) / 2f;

        initialImageRect.set(x, y, x + imgWidth, y + imgHeight);
        imgInitMatrix.reset();
        imgInitMatrix.postScale(scale, scale);
        imgInitMatrix.postTranslate(x, y);

        if (snappyDragHelper != null) {
            snappyDragHelper.cancelAnimation();
        }

        snappyDragHelper = new SnappyDragHelper(getWidth(), getHeight(), (int) imageWidth, (int) imageHeight, imgInitMatrix,
                new SnappyDragHelper.Listener() {
                    @Override
                    public void onMove(Matrix bmpMatrix) {
                        imageView.setImageMatrix(bmpMatrix);
                    }

                    @Override
                    public void onRestored() {
                        setState(STATE_IDLE);
                    }

                    @Override
                    public void onCastAwayed() {
                        setState(STATE_CLOSED);
                        notifyClosed();
                    }
                });
        imageView.setImageMatrix(imgInitMatrix);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateSize();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        handleTouchEventForDrag(event);

        return true;
    }

    private void handleTouchEventForDrag(MotionEvent event) {
        if (imageView.getDrawable() == null) {
            return;
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = event.getPointerId(event.getActionIndex());
                currentX = event.getX();
                currentY = event.getY();
                snappyDragHelper.onTouch();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();

                if (activePointerId == event.getPointerId(event.getActionIndex())) {
                    if ((state == STATE_IDLE && inImageView(x, y)) || state == STATE_DRAGGING) {
                        snappyDragHelper.onMove(currentX, currentY, x, y);
                        setState(STATE_DRAGGING);
                    }

                    currentX = x;
                    currentY = y;
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (activePointerId == event.getPointerId(event.getActionIndex())) {
                    int xvel = 0;
                    int yvel = 0;
                    if (velocityTracker != null) {
                        velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                        xvel = (int) velocityTracker.getXVelocity(activePointerId);
                        yvel = (int) velocityTracker.getYVelocity(activePointerId);

                        velocityTracker.recycle();
                        velocityTracker = null;
                    }
                    if (state == STATE_DRAGGING) {
                        snappyDragHelper.onRelease(xvel, yvel);
                        setState(STATE_SETTLING);
                    }
                    activePointerId = INVALID_POINTER;
                }
                break;
        }
    }

    private boolean inImageView(float x, float y) {
        return initialImageRect.left <= x && x <= initialImageRect.right
                && initialImageRect.top <= y && y <= initialImageRect.bottom;
    }

    private void setState(int state) {
        this.state = state;
    }

    private void notifyClosed() {
        for (OnClosedListener listener : onClosedListeners) {
            listener.onClosed();
        }
    }
}
