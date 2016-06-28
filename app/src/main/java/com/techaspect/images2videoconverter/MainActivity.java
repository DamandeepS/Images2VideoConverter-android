package com.techaspect.images2videoconverter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.LruCache;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private String mGalleryFolder = "com.damandeepsingh.i2v_converter.import";
    private File imagesLocation;
    private static final int REQUEST_IMAGES_FROM_GALLERY = 1;

    public void startRemoving(View view) {
        deleteAllImages();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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


        imagesLocation = getImagesLocation();

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this,3);
        mRecyclerView.setLayoutManager(layoutManager);
        updateAdapter();
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
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";

        return File.createTempFile(imageFileName,".jpg", imagesLocation);
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.delete_all_imported_items) {
            deleteAllImages();
            updateAdapter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateAdapter() {
        adapter = new InputImagesAdapter(this, imagesLocation);
        mRecyclerView.swapAdapter(adapter, true);
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

        adapter = new InputImagesAdapter(this,imagesLocation);
        mRecyclerView.swapAdapter(adapter,true);
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
    }

    public void createVideo(View view) {
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
                        int frameDuration=0;
                        String videoFileName = videoFileNameEditText.getText().toString();
                        String frameDurationString = videoFrameDurationEditText.getText().toString();
                        if (!TextUtils.isEmpty(frameDurationString))
                            frameDuration = Integer.parseInt(frameDurationString);
                        Encode2Video e2v = new Encode2Video(MainActivity.this, imagesLocation);
                        if (TextUtils.isEmpty(videoFileName) || videoFileName.contains(" "))
                            videoFileNameEditText.setError("Enter a valid file name");
                        else if (TextUtils.isEmpty(frameDurationString) || frameDuration<=0) {
                            videoFrameDurationEditText.setError("Enter duration greater than 0");
                        } else {
                            //Dismiss once everything is OK.
                            dialog.dismiss();
                            e2v.encodeVideo(videoFileName, frameDuration);

                        }

                        Log.d(TAG, "onClick: FileName Entered " + videoFileName + ", with per frame duration of " + frameDuration + " ms");
                    }
                });
            }
        });

        dialog.show();

    }
}
