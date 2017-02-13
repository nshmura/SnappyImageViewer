package com.nshmura.snappyimageviewer.demo;

import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView imageView = (ImageView) findViewById(R.id.image);

        final String uri = "https://developer.android.com/images/home/nougat_bg.jpg";

        Picasso.with(this)
                .load(uri)
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewerActivity.start(MainActivity.this, uri, imageView);
            }
        });
    }
}
