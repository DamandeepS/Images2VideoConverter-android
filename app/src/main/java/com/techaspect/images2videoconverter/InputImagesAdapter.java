package com.techaspect.images2videoconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.File;

/**
 * Created by damandeeps on 6/20/2016.
 */

public class InputImagesAdapter extends RecyclerView.Adapter<InputImagesAdapter.ViewHolder>  {
    private static final String TAG = "InputImagesAdapter";
    private File imagesLocation;
    private Context context;
    private ActionMode deleteActionMode;
//    private int colorFilter = Color.parseColor("#cb233445");
    private MultiSelector mMultiSelector = new MultiSelector();


    private ActionMode.Callback mDeleteMode = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ((AppCompatActivity)context).getMenuInflater().inflate(R.menu.recycler_view_item_context, menu);
            mode.setTitle("Selected 1 image");
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

                    /*
                        refreshAllImages actually resets the adapter
                     */

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
        recyclerView.swapAdapter(this,true);
    }


    private void deleteSelection() {
        int i=0;
        for (File image: imagesLocation.listFiles()) {
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
        renameAllImages(); /*
                              Done So that FFmpeg could work seamlessly
                           */
    }


    protected static void renameAllImages() {
        for (int fileNumber = 0; fileNumber<MainActivity.imagesLocation.listFiles().length;fileNumber++) {
            @SuppressLint("DefaultLocale") String number =  String.format("%03d", fileNumber);
            String imageFileName = "IMAGE_" + number;
            /*
                check if file with same name exists
             */

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


    @SuppressLint("PrivateResource")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File imageFile = imagesLocation.listFiles()[position];
        holder.imageUri = Uri.fromFile(imageFile);
        holder.getIndex().setText(String.valueOf(position + 1));
        Drawable drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        drawable.setAlpha(1);
        drawable.setFilterBitmap(true);
        drawable.setColorFilter(new ColorFilter());
        holder.setSelectionModeBackgroundDrawable(drawable);
        SimpleDraweeView imageView = holder.getmImageView();
        imageView.setImageBitmap(null);
        if (mMultiSelector.isSelectable())
            if (!holder.isActivated())
                holder.getIndex().setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Display2);
            else {
                holder.getIndex().setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Display4);
                holder.getIndex().setTextColor(Color.parseColor("#ffffff"));
            }
        if (imageFile.exists()) {
            imageView.setImageURI(Uri.fromFile(imageFile));
            /*
            Testing Fresco
            */
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(context, config);
    }

    @Override
    public int getItemCount() {
        return imagesLocation.listFiles().length;
    }

    public class ViewHolder extends SwappingHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private ImageView mImageView;
        private TextView index;
        private Uri imageUri;

        public ViewHolder(View itemView) {
            super(itemView,mMultiSelector);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView);
            index = (TextView) itemView.findViewById(R.id.index);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            setSelectable(mMultiSelector.isSelectable());
            setActivated(mMultiSelector.isSelected(getAdapterPosition(),0));
        }

        public ImageView getmImageView() {
            return mImageView;
        }

        @Override
        public void onClick(View view) {
            if (mImageView == null)
                return;

            if (!mMultiSelector.tapSelection(this)) {
                setActivated(!this.isActivated());
                mMultiSelector.setSelected(this, this.isActivated());
            } else if (mMultiSelector.getSelectedPositions().size() == 0 && deleteActionMode != null) {
                        deleteActionMode.finish();
                    return;
            }

            if (!mMultiSelector.isSelectable()){
                Intent intent = new Intent(context, ImageViewer.class);
                intent.setDataAndType(imageUri, "image/*");
                context.startActivity(intent);
            }
            if (deleteActionMode!=null)
                if (mMultiSelector.getSelectedPositions().size()>1)
                    deleteActionMode.setTitle("Selected " + mMultiSelector.getSelectedPositions().size()+ " images");
                else
                    deleteActionMode.setTitle("Selected 1 image");
        }

        @Override
        public boolean onLongClick(View view) {
            startSelector();
            mMultiSelector.setSelected(this, true);
            return true;
        }


        @SuppressLint("PrivateResource")
        @Override
        public void setActivated(boolean isActivated) {
            Log.d(TAG, "isActivated: " + isActivated + ", Index: " + index);
            if (index != null && mMultiSelector.isSelectable())
                if (isActivated) {
                    index.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Display4);
                    index.setTextColor(Color.parseColor("#ffffff"));
                }
                else
                    index.setTextAppearance(context, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Display2);
            super.setActivated(isActivated);
        }

        public TextView getIndex() {
            return index;
        }
    }

    private void startSelector() {
        AppCompatActivity activity = (MainActivity) context;
        mMultiSelector.clearSelections();
        deleteActionMode = activity.startSupportActionMode(mDeleteMode);
    }

}
