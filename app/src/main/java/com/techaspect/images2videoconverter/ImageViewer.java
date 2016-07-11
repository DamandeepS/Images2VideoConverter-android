package com.techaspect.images2videoconverter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

public class ImageViewer extends AppCompatActivity {
    RecyclerView mRecyclerView;
    GPUImage mGPUImage;
    RecyclerView.Adapter adapter;
    private Uri fileUri;

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
        Log.d("IMAGE NAME", "handleIntent: " + uri.getLastPathSegment());
        mGPUImage.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
        mGPUImage.setImage(uri); // this loads image on the current thread, should be run in a thread
//        mGPUImage.setFilter(new GPUImageSepiaFilter());
        fileUri = uri;
        adapter=new FilterRecyclerViewAdapter(this,mGPUImage);
        mRecyclerView.setAdapter(adapter);
    }

    public void applyFilter(View view) {
        if (mRecyclerView.getVisibility()!=View.GONE)
            mRecyclerView.setVisibility(View.GONE);
        else
            mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.clearFocus();
        mRecyclerView.smoothScrollToPosition(0);
    }

    public void saveImage(View view) {
        Bitmap imageBitmap = mGPUImage.getBitmapWithFilterApplied();

        File image = new File(fileUri.getPath());
        FileOutputStream fOS = null;
        try {
            fOS = new FileOutputStream(image);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,80,fOS);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if (fOS != null) {
                    fOS.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void resetImage(View view) {
        mRecyclerView.clearFocus();
        mRecyclerView.smoothScrollToPosition(0);
        mGPUImage.setImage(fileUri);
        mGPUImage.setFilter(new GPUImageFilter());

//        mGPUImage;
    }
}
