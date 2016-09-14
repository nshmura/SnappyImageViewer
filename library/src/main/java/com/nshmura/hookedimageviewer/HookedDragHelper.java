package com.nshmura.hookedimageviewer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.animation.OvershootInterpolator;

class HookedDragHelper {

    private float CLOSE_VELOCITY_THRESHOLD = 2500;

    private final int viewerWidth;
    private final int viewerHeight;
    private final int imageWidth;
    private final int imageHeight;
    private final Matrix imgInitMatrix;
    private Listener listener;

    private float bmpDegree;
    private float[] workPoint = new float[2];
    private Matrix touchMatrix = new Matrix();
    private Matrix imgMatrix = new Matrix();
    private Matrix imgInvertMatrix = new Matrix();

    private ValueAnimator animator;
    private DegreeVelocityTracker degreeVelocityTracker = new DegreeVelocityTracker();

    interface Listener {
        void onMove(Matrix imgMatrix);

        void onRestored();

        void onCastAwayed();
    }

    HookedDragHelper(int viewerWidth, int viewerHeight, int imageWidth, int imageHeight,
                     Matrix imgInitMatrix, Listener listener) {
        this.viewerWidth = viewerWidth;
        this.viewerHeight = viewerHeight;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imgInitMatrix = imgInitMatrix;
        this.listener = listener;
    }

    void onTouch() {
        touchMatrix.reset();
        imgInvertMatrix.reset();
        imgInitMatrix.invert(imgInvertMatrix);
        bmpDegree = 0;
        degreeVelocityTracker.reset();
    }

    void onMove(float currentX, float currentY, float newX, float newY) {
        float dx = newX - currentX;
        float dy = newY - currentY;

        //Convert current touch point to bitmap's local coordinate.
        workPoint[0] = currentX;
        workPoint[1] = currentY;
        imgInvertMatrix.mapPoints(workPoint);
        float currentTx = workPoint[0] - imageWidth / 2f;
        float currentTy = workPoint[1] - imageHeight / 2f;

        //Convert new touch point to bitmap's local coordinate.
        workPoint[0] = newX;
        workPoint[1] = newY;
        imgInvertMatrix.mapPoints(workPoint);
        float newTx = workPoint[0] - imageWidth / 2f;
        float newTy = workPoint[1] - imageHeight / 2f;

        //calculate the degrees
        float radius0 = (float) Math.atan2(currentTy, currentTx); //current degree
        float radius1 = (float) Math.atan2(newTy, newTx); //new degree

        float radius = (radius1 - radius0);
        if (Math.abs(radius) > Math.PI) {
            if (radius < 0) {
                radius = (float) (radius + 2 * Math.PI);
            } else {
                radius = (float) (radius - 2 * Math.PI);
            }
        }

        float distance = getLength(newTx, newTy); //distance from center of bitmap
        float maxDistance = getLength(imageWidth, imageHeight);
        float factor = (float) Math.min(1, Math.max(0, 1.5 * Math.pow(distance / maxDistance, 1.3)));
        float deltaDegree = (float) (radius * factor * 180 / Math.PI);

        bmpDegree += deltaDegree;
        degreeVelocityTracker.addDegree(deltaDegree);

        touchMatrix.postRotate(deltaDegree, newX, newY);
        touchMatrix.postTranslate(dx, dy);

        workPoint[0] = workPoint[1] = 0; //left top
        touchMatrix.mapPoints(workPoint);
        moveTo(workPoint[0], workPoint[1], bmpDegree);
    }

    void onRelease(int xvel, int yvel) {
        float vel = getLength(Math.abs(xvel), Math.abs(yvel));

        if (vel > CLOSE_VELOCITY_THRESHOLD) {
            castAway(xvel, yvel, degreeVelocityTracker.getDegreeVelocity());

        } else {
            restore();
        }
    }

    void cancelAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }

    private void castAway(int xvel, int yvel, float degreeVel) {
        final float dx = xvel / 1000f;
        final float dy = yvel / 1000f;
        final float dDegree = Math.max(-0.5f, Math.min(0.5f, degreeVel * 5));
        final Matrix centerMatrix = new Matrix(touchMatrix);

        //bitmap's center coordinate
        workPoint[0] = imageWidth / 2f;
        workPoint[1] = imageHeight / 2f;
        imgMatrix.mapPoints(workPoint);
        final float cx = workPoint[0];
        final float cy = workPoint[1];

        cancelAnimation();
        animator = ValueAnimator.ofFloat(1, 0);
        animator.setDuration(3000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            float centerX = cx;
            float centerY = cy;
            float degree = bmpDegree;
            float time = System.nanoTime();

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float currTime = System.nanoTime();
                float msec = (currTime - time) / 1000000f;
                time = currTime;

                centerX += dx * msec;
                centerY += dy * msec;
                degree += dDegree * msec;
                centerMatrix.postRotate(dDegree * msec, centerX, centerY);
                centerMatrix.postTranslate(dx * msec, dy * msec);

                workPoint[0] = workPoint[1] = 0; //left top
                centerMatrix.mapPoints(workPoint);
                float bmpX = workPoint[0];
                float bmpY = workPoint[1];

                moveTo(bmpX, bmpY, degree);

                if (isOutOfView()) {
                    animator.cancel();
                }
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onCastAwayed();
                animator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private boolean isOutOfView() {
        RectF rect = new RectF(0, 0, imageWidth, imageHeight);
        imgMatrix.mapRect(rect);

        boolean outOfLeft = (rect.left < 0 && rect.right < 0);
        boolean outOfRight = (rect.left > viewerWidth && rect.right > viewerWidth);
        boolean outOfTop = (rect.top < 0 && rect.bottom < 0);
        boolean outOfBottom = (rect.top > viewerHeight && rect.bottom > viewerHeight);

        return outOfLeft || outOfRight || outOfTop || outOfBottom;
    }

    private void restore() {
        workPoint[0] = workPoint[1] = 0; //left top
        touchMatrix.mapPoints(workPoint);
        final float bmpX = workPoint[0];
        final float bmpY = workPoint[1];
        final float degree = bmpDegree;

        cancelAnimation();
        animator = ValueAnimator.ofFloat(1, 0);
        animator.setDuration(400);
        animator.setInterpolator(new OvershootInterpolator(1.002f));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                float value = (float) animator.getAnimatedValue();
                moveTo(bmpX * value, bmpY * value, degree * value);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onRestored();
                animator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    private void moveTo(float bmpX, float bmpY, float bmpDegree) {
        imgMatrix.reset();
        imgMatrix.set(imgInitMatrix);
        imgMatrix.postTranslate(bmpX, bmpY);
        imgMatrix.postRotate(bmpDegree, bmpX, bmpY);
        imgMatrix.invert(imgInvertMatrix);
        listener.onMove(imgMatrix);
    }

    private float getLength(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }
}
