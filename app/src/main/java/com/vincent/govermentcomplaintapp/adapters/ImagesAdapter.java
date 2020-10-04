package com.vincent.govermentcomplaintapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.vincent.govermentcomplaintapp.R;

import java.util.List;

import static android.content.ContentValues.TAG;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder>   {

    private List<Uri> imagesList;
    private Context context;
    private ShapeableImageView imageView;
    private ProgressBar progressBar;

    public ImagesAdapter(Context context, List<Uri> imagesList){
        this.context =context;
        this.imagesList = imagesList;
    }


    @NonNull
    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.images_recycler, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ImagesAdapter.ViewHolder holder, int position) {
        progressBar.setVisibility(View.VISIBLE);
        Uri uri = imagesList.get(position);
        Log.d(TAG, "Image uri " + uri);
        holder.setImage(uri);
    }


    @Override
    public int getItemCount() {
        return imagesList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ViewHolder(View view){
            super(view);
            mView = view;
            progressBar = (ProgressBar) mView.findViewById(R.id.images_loading);
        }

        public void setImage(Uri uri){
            progressBar.setVisibility(View.INVISIBLE);
            imageView = (ShapeableImageView) mView.findViewById(R.id.recycler_image_item);
            imageView.setImageURI(uri);

        }
    }


}
