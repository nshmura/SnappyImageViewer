package com.nshmura.snappyimageviewer.demo;

import com.nshmura.snappyimageviewer.SnappyImageViewer;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.View;
import android.widget.ImageView;

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL";
    private static final String TRANSITION_NAME_PHOTO = "TRANSITION_NAME_PHOTO";

    public static void start(Activity activity, String uri, ImageView imageView) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, uri);

        //noinspection unchecked
        Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                Pair.create((View) imageView, TRANSITION_NAME_PHOTO))
                .toBundle();

        ActivityCompat.startActivity(activity, intent, options);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        String uri = getIntent().getStringExtra(EXTRA_IMAGE_URL);

        SnappyImageViewer snappyImageViewer = (SnappyImageViewer) findViewById(R.id.snappy_image_viewer);

        Picasso.with(this)
                .load(uri)
                .into(snappyImageViewer.getImageView());

        snappyImageViewer.addOnClosedListener(new SnappyImageViewer.OnClosedListener() {
            @Override
            public void onClosed() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setSharedElementReturnTransition(new Fade(Fade.IN));
                }
                ActivityCompat.finishAfterTransition(ImageViewerActivity.this);
            }
        });
        ViewCompat.setTransitionName(snappyImageViewer.getImageView(), TRANSITION_NAME_PHOTO);
    }
}
