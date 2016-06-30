package com.techaspect.images2videoconverter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

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
    private float frameDuration;
    FFmpeg ffmpeg;

    private static final String TAG = "Encode2Video";
    private class VideoConverterWorkerTask extends AsyncTask<File,Void,Void> {
        private File file;
        @Override
        protected Void doInBackground(File... files) {
            try {
                Log.d(TAG, "doInBackground: Duration: " + imagesLocation.listFiles().length * frameDuration);
                file = this.GetSDPathToFile("aatest", outputFileName + ".mp4");
                String[] command = {
                        "-framerate",
                        "1/" + frameDuration,
                        "-i",
                        imagesLocation.getAbsolutePath() + "/IMAGE_%03d.jpg",
                        "-c:v",
                        "libx264",
                        "-vf",
                        "scale=iw*min(1920/iw\\,1080/ih):ih*min(1920/iw\\,1080/ih), pad=1920:1080:(1920-iw*min(1920/iw\\,1080/ih))/2:(1080-ih*min(1920/iw\\,1080/ih))/2",
                        "-r",
                        "30",
                        "-t",
                        String.valueOf((int)(imagesLocation.listFiles().length * frameDuration * 10)),
                        "-pix_fmt",
                        "yuv420p",
                        file.getAbsolutePath()
                };
                // scale=trunc(iw/2)*2:trunc(ih/2)*2 is needed to overcome not divisible by zero error

                ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {

                    @Override
                    public void onStart() {
                        Log.d(TAG, "onStart: ENCODING STARTED");
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "onFinish: ENCODING FINISH");
                        dialog.cancel();
                    }

                    @Override
                    public void onSuccess(String message) {
                        Log.d(TAG, "onSuccess: ENCODING SUCCESS " + message );
                        Toast.makeText(context,"File Saved at " + file.getAbsolutePath(),Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onProgress(String message) {
                        Log.d(TAG, "onProgress: ENCODING PROGRESS " + message );
                    }

                    @Override
                    public void onFailure(String message) {
                        Log.d(TAG, "onFailure: ENCODING FAILURE " + message );
                    }
                });
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        // get full SD path
        File GetSDPathToFile(String filePatho, String fileName) {
            File extBaseDir = Environment.getExternalStorageDirectory();
            if (filePatho == null || filePatho.length() == 0 || filePatho.charAt(0) != '/')
                filePatho = "/" + filePatho;
            File file = new File(extBaseDir.getAbsoluteFile() + filePatho);
            if (!file.exists())
                file.mkdirs();
            Log.d(TAG, "GetSDPathToFile: " + file.getAbsolutePath());
            return new File(file.getAbsolutePath() + "/" + fileName);// file;
        }

    }

    public Encode2Video(Context context,File imagesLocation, FFmpeg ffmpeg) {
        this.context = context;
        this.ffmpeg = ffmpeg;
        this.imagesLocation = imagesLocation;
    }

    public void encodeVideo(String outputFileName, float frameDuration) {
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
