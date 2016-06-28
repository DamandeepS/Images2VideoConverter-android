package com.techaspect.images2videoconverter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by damandeeps on 6/20/2016.
 */

public class ImageWorkerTask extends AsyncTask<File,Void,Bitmap> {
    private static final int TARGET_IMAGEVIEW_WIDTH = 200;
    private static final int TARGET_IMAGEVIEW_HEIGHT = 200;
    WeakReference<ImageView> imageViewWeakReference;
    private File mImageFile;

    public ImageWorkerTask(ImageView imageView) {
        imageViewWeakReference = new WeakReference<>(imageView);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        mImageFile = params[0];
        Bitmap bitmap = decodeSampledBitmap(mImageFile);
        return bitmap;

    }

    private Bitmap decodeSampledBitmap(File mImageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mImageFile.getAbsolutePath(),options);
        options.inSampleSize = calculateInSampleSize(options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(mImageFile.getAbsolutePath(),options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        final int photoWidth = options.outWidth;
        final int photoHeight = options.outHeight;
        int scaleFactor = 1;
        if (photoWidth>TARGET_IMAGEVIEW_WIDTH || photoHeight> TARGET_IMAGEVIEW_HEIGHT) {
            final int halfPhotoWidth = photoWidth/2;
            final int halfPhotoHeight = photoHeight/2;

            while (halfPhotoWidth/scaleFactor>TARGET_IMAGEVIEW_WIDTH
                    || halfPhotoHeight/scaleFactor>TARGET_IMAGEVIEW_HEIGHT) {
                scaleFactor*=2;
            }
        }
        return scaleFactor;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (isCancelled()) {
            bitmap = null;
        }
        if (bitmap!=null && imageViewWeakReference!=null) {
            ImageView imageView = imageViewWeakReference.get();
            if (imageView!=null) {
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    public File getmImageFile() {
        return mImageFile;
    }
}
