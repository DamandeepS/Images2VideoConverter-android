package com.techaspect.images2videoconverter;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;

import java.io.File;
import java.io.IOException;

import static com.techaspect.images2videoconverter.MainActivity.ffmpeg;
import static com.techaspect.images2videoconverter.MainActivity.imagesLocation;

/**
 * Created by damandeeps on 7/4/2016.
 */

public class ConverterService extends IntentService {
    private static final int NOTIFICATION_ID = 121;
    private static String TAG = "ConverterService";
    private String fileName = "TEST";
    private float frameDuration = 1F;
    private NotificationCompat.Builder notificationBuilder;

    public ConverterService(String name) {
        super(name);
    }
    private File file;
    public ConverterService() {
        super("Converter Service");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            fileName = intent.getStringExtra("fileName");
            frameDuration = intent.getFloatExtra("frameDuration",1F);
            createNotification();
            final float videoDuration = imagesLocation.listFiles().length * frameDuration * 1000;
            Log.d(TAG, "doInBackground: Duration: " + imagesLocation.listFiles().length * frameDuration);
            file = this.GetSDPathToFile("aatest", fileName + ".mp4");
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
                    "-pix_fmt",
                    "yuv420p",
                    file.getAbsolutePath()
            };
            // scale=trunc(iw/2)*2:trunc(ih/2)*2 is needed to overcome not divisible by zero error
            final NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            ffmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                String timeString="00:00:00";

                @Override
                public void onStart() {
                    Log.d(TAG, "onStart: ENCODING STARTED");
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }
                @Override
                public void onFinish() {
                    Log.d(TAG, "onFinish: ENCODING FINISH");
//                        dialog.cancel();
                }

                @Override
                public void onSuccess(String message) {
                    Log.d(TAG, "onSuccess: ENCODING SUCCESS " + message );
                    Toast.makeText(ConverterService.this,"File Saved at " + file.getAbsolutePath(),Toast.LENGTH_LONG).show();
                    notificationBuilder.setContentText("File Saved at " + file.getAbsolutePath());
                    String fileLocation="";
                    try {
                        fileLocation = file.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent startMovie = new Intent(Intent.ACTION_VIEW);
                    startMovie.setDataAndType(Uri.fromFile(file),"video/*");

                    Intent openLocation = new Intent(Intent.ACTION_VIEW);
                    openLocation.setDataAndType(Uri.fromFile(file.getParentFile()),"resource/folder");
                    PendingIntent pendingIntentLocation = PendingIntent.getActivity(ConverterService.this,0, openLocation, 0);
                    PendingIntent pendingIntentMovie  = PendingIntent.getActivity(ConverterService.this, 0, startMovie, 0);
                    Bitmap bigImage = BitmapFactory.decodeFile(imagesLocation.listFiles()[(int)Math.floor(imagesLocation.listFiles().length/2)].getAbsolutePath());
                    notificationBuilder.setContentIntent(pendingIntentLocation)
                            .setAutoCancel(true)
                            .setOngoing(false)
                            .setSmallIcon(R.mipmap.ic_notification_icon)
                            .setContentTitle("E2V Encoding Complete: " + fileName)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("Encoding Finished"))
                            .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigImage))
                            .setLargeIcon(bigImage)
                            .addAction(R.mipmap.ic_play,"Play", pendingIntentMovie)
                            .setSubText("FileName: " + fileName + "\nFile Location: " + fileLocation + "\n" + message);
                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }

                @Override
                public void onProgress(String message) {
                    Log.d(TAG, "onProgress: ENCODING PROGRESS " + message );
                    String[] array = message.split("time=");
                    int frameDurationInMilliseconds = findMillisecondsFromTimestampString(timeString);
                    if (array.length>1)
                        timeString = array[1].split(" ")[0];
                    Log.d(TAG, "onProgress: stringArray: " + timeString + " , in milliseconds: " +findMillisecondsFromTimestampString(timeString) );
                    int imageIndex = (imagesLocation.listFiles().length * frameDurationInMilliseconds) / (int) videoDuration;
                    notificationBuilder.setContentText(message)
                            .setOngoing(true)
                            .setSmallIcon(R.mipmap.ic_notification_icon)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                            .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeFile(imagesLocation.listFiles()[imageIndex].getAbsolutePath())))
                            .setProgress((int)videoDuration, frameDurationInMilliseconds, false); // #0;

                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());

                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onFailure: ENCODING FAILURE " + message );
                    notificationBuilder.setContentText(message)
                            .setOngoing(false)
                            .setContentTitle("Encoder Failed")
                            .setSmallIcon(R.mipmap.ic_notification_icon)
                            .setContentText(message)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(message)); // #0;

                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                }
            });
            Log.d(TAG, "onHandleIntent: fileName: " + fileName + ", fileDuration: " + frameDuration);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
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

    private void createNotification() {
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("E2V Encoding : " + fileName )
                .setContentText("Encoding video filename: " + fileName)
                .setSmallIcon(R.mipmap.ic_notification_icon)
                .setAutoCancel(false)
                .setOngoing(true);
    }

    private int findMillisecondsFromTimestampString(String timestamp) {
        String[] timeArray = timestamp.split(":");
        if (timeArray.length!=3)
            return 0;
        int milliseconds=0;
        milliseconds += Float.parseFloat(timeArray[2])*1000;
        milliseconds += Float.parseFloat(timeArray[1])*60000;
        milliseconds += Float.parseFloat(timeArray[0])*360000;
        return milliseconds;
    }
}
