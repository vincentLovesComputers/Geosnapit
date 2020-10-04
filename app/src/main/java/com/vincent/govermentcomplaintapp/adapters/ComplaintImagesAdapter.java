package com.vincent.govermentcomplaintapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.vincent.govermentcomplaintapp.R;

import java.util.List;

import static android.content.ContentValues.TAG;



public class ComplaintImagesAdapter extends RecyclerView.Adapter<ComplaintImagesAdapter.ViewHolder>  {

    private Context context;
    private ShapeableImageView imageView;
    private List<String> imagesList;


    public ComplaintImagesAdapter(Context context, List<String> imagesList){
        this.context =context;
        this.imagesList = imagesList;
    }


    @NonNull
    @Override
    public ComplaintImagesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.images_recycler, parent, false);
        return new ComplaintImagesAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ComplaintImagesAdapter.ViewHolder holder, int position) {

        String imageDownloadUri = imagesList.get(position);
        Log.d(TAG, "Image: " + imageDownloadUri);
        holder.setImage(imageDownloadUri);
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
        }

        public void setImage(String dwnLoadUri){
            imageView = (ShapeableImageView) mView.findViewById(R.id.recycler_image_item);

            RequestOptions reqOpt = new RequestOptions();
            reqOpt.placeholder(R.drawable.placeholder_img);
            Glide.with(context).setDefaultRequestOptions(reqOpt).load(dwnLoadUri).into(imageView);

        }
    }
}
