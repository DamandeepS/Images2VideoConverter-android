package com.techaspect.images2videoconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;

import java.io.File;
import java.io.IOException;

/**
 * Created by damandeeps on 6/27/2016.
 */

public class Encode2Video {
    private File imagesLocation;
    private Context context;
    private String outputFileName;
    private ProgressDialog dialog ;
    private int frameDuration;
    private static final String TAG = "Encode2Video";
    private class VideoConverterWorkerTask extends AsyncTask<File,Void,Void> {
        private File file;
        @Override
        protected Void doInBackground(File... files) {
            try {
            file = this.GetSDPathToFile("aatest", outputFileName + ".mp4");
            SequenceEncoder encoder = new SequenceEncoder(file,frameDuration);
            for (File image:imagesLocation.listFiles()) {
                // getting bitmap from drawable path
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                Bitmap centeredBitmap;

                if (bitmap.getWidth() >= bitmap.getHeight()){

                    centeredBitmap = Bitmap.createBitmap(
                            bitmap,
                            bitmap.getWidth()/2 - bitmap.getHeight()/2,
                            0,
                            bitmap.getHeight(),
                            bitmap.getHeight()
                    );

                }else{

                    centeredBitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            bitmap.getHeight()/2 - bitmap.getWidth()/2,
                            bitmap.getWidth(),
                            bitmap.getWidth()
                    );
                }
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(centeredBitmap, 1080,1920, true);
                Log.d(TAG, "doInBackground: Bitmap Width = " + bitmap.getWidth() + " and Bitmap Height = " + bitmap.getHeight());
                Log.d(TAG, "doInBackground: Scaled Bitmap Width = " + scaledBitmap.getWidth() + " and Scaled Bitmap Height = " + scaledBitmap.getHeight());
//                for (int i=0; i< frameDuration; i++)
                    encoder.encodeNativeFrame(this.fromBitmap(scaledBitmap));
                }


                encoder.finish();
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // get full SD path
        File GetSDPathToFile(String filePatho, String fileName) {
            File extBaseDir = Environment.getExternalStorageDirectory();
            if (filePatho == null || filePatho.length() == 0 || filePatho.charAt(0) != '/')
                filePatho = "/" + filePatho;
//            makeDirectory(filePatho);
            File file = new File(extBaseDir.getAbsoluteFile() + filePatho);
            if (!file.exists())
                file.mkdirs();
            Log.d(TAG, "GetSDPathToFile: " + file.getAbsolutePath());
            return new File(file.getAbsolutePath() + "/" + fileName);// file;
        }

        // convert from Bitmap to Picture (jcodec native structure)
        public Picture fromBitmap(Bitmap src) {
            Picture dst = Picture.create((int)src.getWidth(), (int)src.getHeight(), ColorSpace.RGB);
            Log.d(TAG, "fromBitmap: src = [" + src.getWidth() + ", " + src.getHeight() + "]");
            fromBitmap(src, dst);
            Log.d(TAG, "fromBitmap: dst = [" + dst.getWidth() + ", " + dst.getHeight() + "]");
            return dst;
        }

        public void fromBitmap(Bitmap src, Picture dst) {
            int[] dstData = dst.getPlaneData(0);
            int[] packed = new int[src.getWidth() * src.getHeight()];

            src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

            for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
                for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
                    int rgb = packed[srcOff];
                    dstData[dstOff]     = (rgb >> 16) & 0xff;
                    dstData[dstOff + 1] = (rgb >> 8) & 0xff;
                    dstData[dstOff + 2] = rgb & 0xff;
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.cancel();
            Toast.makeText(context,"File Saved at " + file.getAbsolutePath(),Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }

    public Encode2Video(Context context,File imagesLocation) {
        this.context=context;
        this.imagesLocation = imagesLocation;
    }

    public void encodeVideo(String outputFileName,int frameDuration) {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Converting...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        this.frameDuration=frameDuration;
        this.outputFileName = outputFileName;
        VideoConverterWorkerTask task = new VideoConverterWorkerTask();
        task.execute(imagesLocation);

    }
}
