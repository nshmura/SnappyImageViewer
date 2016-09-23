# SnappyImageViewer

Android Image Viewer inspired by StackOverflow's with swipe-to-dimiss and moving animations.

<img src="assets/capture.gif" alt="Gif Animation File">


## Demo
<a href="https://play.google.com/store/apps/details?id=com.nshmura.snappyimageviewer.demo"><img src="assets/googleplay.png"/></a>


## Getting Started

In your `build.gradle`:

```gradle
 repositories {
    jcenter()
 }

 dependencies {
    compile 'com.nshmura:snappyimageviewer:1.0.0'
 }
```

In your layout file:
```xml
  <com.nshmura.snappyimageviewer.SnappyImageViewer
    android:id="@+id/snappy_image_viewer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

Setup the `SnappyImageViewer`:
```java
    SnappyImageViewer snappyImageViewer = (SnappyImageViewer) findViewById(R.id.snappy_image_viewer);
    snappyImageViewer.setImageResource(R.drawable.sample);
    snappyImageViewer.addOnClosedListener(new SnappyImageViewer.OnClosedListener() {
        @Override
        public void onClosed() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setSharedElementReturnTransition(new Fade(Fade.IN));
            }
            ActivityCompat.finishAfterTransition(ImageViewerActivity.this);
        }
    });
```

## TODO
- Pinch to Zoom
- Embed in ViewPager
- Add `ImageViwerActivity` in library


## Contributions

Your contributions always welcome!


## License
```
Copyright (C) 2016 nshmura

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
