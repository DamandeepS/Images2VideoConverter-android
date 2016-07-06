package com.techaspect.images2videoconverter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private String mGalleryFolder = "com.damandeepsingh.i2v_converter.import";
    static File imagesLocation;
    private static final int REQUEST_IMAGES_FROM_GALLERY = 1;
    public static int fileNumber=0;
    static FFmpeg ffmpeg;
    boolean isFFmpegAvailable;

    public void startRemoving(View view) {
        deleteAllImages();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);
                //set limit on number of images that can be selected, default is 10
                intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 100);
                startActivityForResult(intent, REQUEST_IMAGES_FROM_GALLERY);
            }
        });

        requestNecessaryPermissions();

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        imagesLocation = getImagesLocation();
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        updateAdapter();


    }


    private boolean checkFFmpeg() {
        ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {

                    isFFmpegAvailable=false;
                }

                @Override
                public void onFailure() {
                    isFFmpegAvailable=false;
                }

                @Override
                public void onSuccess() {
                    isFFmpegAvailable=true;
                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device

            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Unsupported Device");

            TextView tv = new TextView(this);
            tv.setText(R.string.FFmpeg_not_supported);

            builder.setView(tv);

            builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.finishAffinity();
                }
            });

            builder.setNegativeButton("Continue", null);
            builder.show();
        }
        return isFFmpegAvailable;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGES_FROM_GALLERY :
                if (resultCode!=RESULT_OK)
                    Toast.makeText(this, "No Images selected", Toast.LENGTH_LONG).show();
                else
                if (data != null) {

                    /* Multiple Images are selected */
                    ArrayList<Parcelable> list = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    // iterate over these images
                    Log.d(TAG, "onActivityResult: list: " + list);
                    if( list != null ) {
                        for (Parcelable parcel : list) {
                            Log.d(TAG, "onActivityResult: " + parcel);
                                importImage(((Image) parcel).path);
                        }
                    }
                }

                updateAdapter();
            break;
        }

    }

    private void importImage(String path) {
        Log.d(TAG, "onActivityResult: " + path);
        try {
            File imageFile = createImageFile();
            Log.d(TAG, "onActivityResult: ImageExists: " + imageFile.exists());
            if (imageFile.exists()) {
                copyFile(new File(path), imageFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileNumber++;
        }
    }

    private File createImageFile() throws IOException {
        checkExistingFileNames();
        String number =  String.format("%03d", fileNumber);
        String imageFileName = "IMAGE_" + number;
//        check if file with same name exists

        Log.d(TAG, "createImageFile: imageFileName: " + imageFileName);
        File file = new File(imagesLocation,imageFileName + ".jpg");
        file.createNewFile();
        return file;
    }

    private void checkExistingFileNames() {
        for (File image: imagesLocation.listFiles())
            if (image.getName().contains(String.valueOf(fileNumber))) {
                fileNumber++;
                checkExistingFileNames();
            }
    }

    private void requestNecessaryPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this,"Please Provide Access to Read/Write External Storage",Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==0 && grantResults[0]==RESULT_OK) {
            Toast.makeText(this,"NAACHO bc", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.delete_all_imported_items:
                deleteAllImages();
                updateAdapter();
                return true;
            case R.id.change_layout:
                if (layoutManager instanceof GridLayoutManager)
                    layoutManager = new LinearLayoutManager(this);
                else
                    layoutManager = new GridLayoutManager(this,3);
                mRecyclerView.setLayoutManager(layoutManager);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateAdapter() {
        adapter = new InputImagesAdapter(this,imagesLocation);
        mRecyclerView.swapAdapter(adapter,true);
        adapter.notifyDataSetChanged();
    }

    public File getImagesLocation() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        imagesLocation = new File(storageDirectory, mGalleryFolder);
        if (!imagesLocation.exists()) {
            imagesLocation.mkdirs();
        }
        return imagesLocation;
    }

    /*public static Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    public static void setBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            if (key != null && bitmap != null) {
                mMemoryCache.put(key, bitmap);
            }
        }
    }


    public static void evictMemoryCache() {
        mMemoryCache.evictAll();
    }*/

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source;
        FileChannel destination;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        destination.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAdapter();
        checkFFmpeg();
        InputImagesAdapter.renameAllImages();  //Sorting and renaming images in order for FFmpeg to work
    }


    public void deleteAllImages() {
        try {
            for (File image : imagesLocation.listFiles()) {
                image.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            adapter.notifyDataSetChanged();
        }
        fileNumber=0;
    }

    public void createVideo(View view) {
        if ( imagesLocation.listFiles().length==0) {
            Toast.makeText(MainActivity.this,"Import at least one image to your project", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Encoding Options");

        // Set up the input
        View dialogView = getLayoutInflater().inflate(R.layout.encoding_options_dialog_layout, null);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(dialogView);
        final EditText videoFileNameEditText = (EditText) dialogView.findViewById(R.id.editText);
        final EditText videoFrameDurationEditText = (EditText) dialogView.findViewById(R.id.editText2);

        // Set up the buttons
                builder.setPositiveButton("Start Encoder", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something

                        float frameDuration=0f;
                        String videoFileName = videoFileNameEditText.getText().toString();
                        String frameDurationString = videoFrameDurationEditText.getText().toString();
                        if (!TextUtils.isEmpty(frameDurationString))
                            frameDuration = Float.parseFloat("0"+frameDurationString); //hack for avoiding Float.parseFloat("."); error

                        if (TextUtils.isEmpty(videoFileName) || videoFileName.contains(" "))
                            videoFileNameEditText.setError("Enter a valid file name");
                        else if (TextUtils.isEmpty(frameDurationString) || frameDuration<=0f) {
                            videoFrameDurationEditText.setError("Enter duration greater than 0");
                        } else if (checkFFmpeg()){

                            //Dismiss once everything is OK.
                            dialog.dismiss();

                            Intent serviceIntent = new Intent(MainActivity.this,ConverterService.class);
                            serviceIntent.putExtra("fileName",videoFileName);
                            serviceIntent.putExtra("frameDuration", frameDuration);
                            startService(serviceIntent);

                        } else
                            Toast.makeText(MainActivity.this,"Either FFmpeg is not available or Busy", Toast.LENGTH_LONG).show();

                        Log.d(TAG, "onClick: FileName Entered " + videoFileName + ", with per frame duration of " + frameDuration + " sec");
                    }
                });
            }
        });

        dialog.show();

    }
}
