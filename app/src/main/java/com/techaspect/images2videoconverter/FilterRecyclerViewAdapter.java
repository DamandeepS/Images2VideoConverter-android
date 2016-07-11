package com.techaspect.images2videoconverter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.ArrayList;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLightenBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

/**
 * Created by damandeeps on 7/8/2016.
 */

public class FilterRecyclerViewAdapter extends RecyclerView.Adapter<FilterRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "FRecyclerViewAdapter";
    private Context context;
    private GPUImage mGPUImage;
    private int brightnessFilterValue=25;
    private int contrastFilterValue=50;
    private ArrayList<Filters> filters = new ArrayList<>();

    public FilterRecyclerViewAdapter(Context context, GPUImage mGPUImage) {
        this.context = context;
        this.mGPUImage = mGPUImage;
        initialiseFilters();
    }

    private void initialiseFilters() {

        filters.add( new Filters("Normal", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPUImage.setFilter(new GPUImageFilter());
                mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
            }
        }));
        filters.add( new Filters("Sepia", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPUImage.setFilter(new GPUImageSepiaFilter());
                mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
            }
        }));
        filters.add( new Filters("Lighten and Blend", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPUImage.setFilter(new GPUImageLightenBlendFilter());
                mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
            }
        }));
        filters.add( new Filters("Color Invert", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPUImage.setFilter(new GPUImageColorInvertFilter());
                mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
            }
        }));
        filters.add( new Filters("Emboss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPUImage.setFilter(new GPUImageEmbossFilter());
                mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
            }
        }));
        filters.add( new Filters("Brightness", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_seekbar_layout, (ViewGroup)((AppCompatActivity)context).findViewById(R.id.your_dialog_root_element));
                dialogBuilder.setView(layout);
                final SeekBar yourDialogSeekBar = (SeekBar)layout.findViewById(R.id.dialog_seekbar);
//                dialogBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//                dialogBuilder.getWindow().getWindowStyle()
                yourDialogSeekBar.setProgress(100);
                yourDialogSeekBar.setMax(200);
                final Dialog dialog = dialogBuilder.create();
                SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        int seekBarValue =  seekBar.getProgress()-100;
                        mGPUImage.setFilter(new GPUImageBrightnessFilter((float)(seekBarValue)/200));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
                        dialog.dismiss();
                    }
                };
                yourDialogSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                dialog.show();
            }
        }));
        filters.add( new Filters("Contrast", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_seekbar_layout, (ViewGroup)((AppCompatActivity)context).findViewById(R.id.your_dialog_root_element));
                dialogBuilder.setView(layout);
                final SeekBar yourDialogSeekBar = (SeekBar)layout.findViewById(R.id.dialog_seekbar);
                yourDialogSeekBar.setMax(100);
//                dialogBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                yourDialogSeekBar.setProgress(contrastFilterValue);
                final Dialog dialog = dialogBuilder.create();
                dialogBuilder.setTitle("Contrast");
                SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        int seekBarValue =  seekBar.getProgress();
                        mGPUImage.setFilter(new GPUImageContrastFilter((float)(seekBarValue*2)/100));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mGPUImage.setImage(mGPUImage.getBitmapWithFilterApplied());
                        dialog.dismiss();
                    }
                };
                yourDialogSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
//                dialogBuilder.s
                dialogBuilder.show();
            }
        }));
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.filter_button,parent,false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Filters filter = filters.get(position);
        holder.setFilter(filter);
        Log.d(TAG, "onBindViewHolder: " + position);
    }

    @Override
    public int getItemCount() {
        return filters.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Button mButton;
        public ViewHolder(View itemView) {
            super(itemView);
            mButton = (Button) itemView.findViewById(R.id.button);
        }


        public void setFilter(Filters filter) {
            this.mButton.setText(filter.getFilterName());
            this.mButton.setOnClickListener(filter.getOnClickListener());
        }
    }

    private class Filters {
        private String filterName;
        private View.OnClickListener onClickListener;

        public Filters(String filterName, View.OnClickListener onClickListener) {
            this.filterName = filterName;
            this.onClickListener = onClickListener;
        }

        public View.OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public String getFilterName() {
            return filterName;
        }
    }
}
