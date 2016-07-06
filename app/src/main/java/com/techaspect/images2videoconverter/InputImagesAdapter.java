package com.techaspect.images2videoconverter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SelectableHolder;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by damandeeps on 6/20/2016.
 */

public class InputImagesAdapter extends RecyclerView.Adapter<InputImagesAdapter.ViewHolder>  {
    private static final String TAG = "InputImagesAdapter";
    private File imagesLocation;
    private Bitmap placeholderBitmap;
    private Context context;
    private ActionMode deleteActionMode;
    private static MultiSelector mMultiSelector = new MultiSelector();
    private ActionMode.Callback mDeleteMode = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ((AppCompatActivity)context).getMenuInflater().inflate(R.menu.recycler_view_item_context, menu);
            mode.setTitle("Selected " + 1+ " image");
            mMultiSelector.setSelectable(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_imported_item:
                    // Delete images from filesystem
                    deleteSelection();
                    mMultiSelector.clearSelections();
                    notifyDataSetChanged();
                    refreshAllImages();
                    mode.finish();
                    return true;
                default:
                    break;
            }
            return false;
        }



        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mMultiSelector.clearSelections();
            mMultiSelector.setSelectable(false);
        }


    };

    private void refreshAllImages() {
        RecyclerView recyclerView = (RecyclerView)((MainActivity)context).findViewById(R.id.recyclerView);
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(this);
    }


    private void deleteSelection() {
        int i=0;
        for (File image: imagesLocation.listFiles()) {
            Log.d(TAG, "deleteSelection: isSelected: " + mMultiSelector.isSelected(i,0) + ", for index: " + i );
            if (mMultiSelector.isSelected(i,0)) {
                try {
                    boolean result = image.delete();
                    Log.d(TAG, "deleteSelection: Image name: " + image.getName() + ", Deleted: " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                notifyItemRemoved(i);
            }
            i++;
        }

        MainActivity.fileNumber=0;
        renameAllImages(); //Done So that FFmpeg could work seamlessly
    }


    protected static void renameAllImages() {
        for (int fileNumber = 0; fileNumber<MainActivity.imagesLocation.listFiles().length;fileNumber++) {
            String number =  String.format("%03d", fileNumber);
            String imageFileName = "IMAGE_" + number;
//        check if file with same name exists

            Log.d(TAG, "createImageFile: imageFileName: " + imageFileName);
            File file = new File(MainActivity.imagesLocation,imageFileName + ".jpg");
            MainActivity.imagesLocation.listFiles()[fileNumber].renameTo(file);
        }
    }


    public InputImagesAdapter(Context context, File imagesLocation) {
        this.context=context;
        this.imagesLocation = imagesLocation;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_image_item,parent,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File imageFile = imagesLocation.listFiles()[position];
        holder.imageUri = Uri.fromFile(imageFile);
        SimpleDraweeView imageView = holder.getmImageView();
        Log.d(TAG, "onBindViewHolder: " + imageFile.getAbsolutePath());
        if (imageFile.exists()) {
            imageView.setImageURI(Uri.fromFile(imageFile));
//                    holder
            /*
            if (checkImageWorkerTask(imageFile, holder.getmImageView())) {
                ImageWorkerTask imageWorkerTask = new ImageWorkerTask(holder.getmImageView());
                AsyncDrawable asyncDrawable = new AsyncDrawable(holder.getmImageView().getResources(), placeholderBitmap, imageWorkerTask);
                holder.getmImageView().setImageDrawable(asyncDrawable);
                imageWorkerTask.execute(imageFile);
            }
            Testing Fresco
            */
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d(TAG, "onClick: isSelectable ----------" + mMultiSelector.isSelectable() + ", selectedPositions " + mMultiSelector.getSelectedPositions());

    }

    private static boolean checkImageWorkerTask(File imageFile, ImageView imageView) {
        ImageWorkerTask imageWorkerTask = getImageWorkerTask(imageView);
        if (imageWorkerTask!=null) {
            final File workerFile = imageWorkerTask.getmImageFile();
            if (workerFile!=imageFile)
                imageWorkerTask.cancel(true);

            else
                return false;
        }
        return true;
    }

    private static ImageWorkerTask getImageWorkerTask(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AsyncDrawable) {
            AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
            return asyncDrawable.getImageWorkerTask();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return imagesLocation.listFiles().length;
    }


    private static class AsyncDrawable extends BitmapDrawable {
        final WeakReference<ImageWorkerTask> taskWeakReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap, ImageWorkerTask imageWorkerTask) {
            super(resources,bitmap);
            taskWeakReference = new WeakReference<>(imageWorkerTask);
        }

        public ImageWorkerTask getImageWorkerTask() {
            return taskWeakReference.get();
        }
    }

    public class ViewHolder extends SwappingHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private SimpleDraweeView mImageView;
        private Uri imageUri;

        public ViewHolder(View itemView) {
            super(itemView,mMultiSelector);
            mImageView = (SimpleDraweeView) itemView.findViewById(R.id.imageView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            setSelectable(mMultiSelector.isSelectable());
            setActivated(mMultiSelector.isSelected(getAdapterPosition(),0));
        }


        public SimpleDraweeView getmImageView() {
            return mImageView;
        }

        @Override
        public void onClick(View view) {
            if (mImageView == null) {
                return;
            }
            Log.d(TAG, "onClick: isSelectable ----------" + mMultiSelector.isSelectable() + ", selectedPositions " + mMultiSelector.getSelectedPositions());

//            mMultiSelector.setSelectable((mMultiSelector.getSelectedPositions().size() > 0));


            if (!mMultiSelector.tapSelection(this)) {
                setActivated(!isActivated());
                mMultiSelector.setSelected(this, isActivated());
            } else if (mMultiSelector.getSelectedPositions().size()==0) {
                if (deleteActionMode!=null) {
                    deleteActionMode.finish();
                }
            }
            if (!mMultiSelector.isSelectable()){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(imageUri, "image/*");
                context.startActivity(intent);
            }
            if (deleteActionMode!=null) {
                if (mMultiSelector.getSelectedPositions().size()>1)
                    deleteActionMode.setTitle("Selected " + mMultiSelector.getSelectedPositions().size()+ " images");
                else
                    deleteActionMode.setTitle("Selected 1 image");
            }
            Log.d(TAG, "onClick: isSelectable ||||||||||" + mMultiSelector.isSelectable() + ", selectedPositions " + mMultiSelector.getSelectedPositions());
        }

        @Override
        public boolean onLongClick(View view) {
            startSelector();
            mMultiSelector.setSelected(this, true);
            return true;
        }

    }

    private void startSelector() {
        AppCompatActivity activity = (MainActivity) context;
        deleteActionMode = activity.startSupportActionMode(mDeleteMode);
    }

}
