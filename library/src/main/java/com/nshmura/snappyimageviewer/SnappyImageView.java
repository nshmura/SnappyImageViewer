package com.nshmura.snappyimageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

class SnappyImageView extends ImageView {

    interface Listener {
        void onImageChanged();
        void onDraw();
    }

    private SnappyImageView.Listener mListener;

    public SnappyImageView(Context context) {
        super(context);
    }


    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        if (mListener != null) {
            mListener.onImageChanged();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mListener != null) {
            mListener.onImageChanged();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (mListener != null) {
            mListener.onImageChanged();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mListener != null) {
            mListener.onImageChanged();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mListener != null) {
            mListener.onDraw();
        }
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }
}
