package com.techaspect.images2videoconverter;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

public class ImageViewer extends AppCompatActivity {
    RecyclerView mRecyclerView;
    GPUImage mGPUImage;
    RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.imageViewer));
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        // Later when image should be saved saved:
//        mGPUImage.saveToPictures("GPUImage", "ImageWithFilter.jpg", null);
//        imageView
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent==null)
            return;
        if (intent.getData()==null)
            return;
        Uri uri = intent.getData();
        mGPUImage.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        mGPUImage.setImage(uri); // this loads image on the current thread, should be run in a thread
//        mGPUImage.setFilter(new GPUImageSepiaFilter());
        adapter=new FilterRecyclerViewAdapter(this,mGPUImage);
        mRecyclerView.setAdapter(adapter);
    }
}
