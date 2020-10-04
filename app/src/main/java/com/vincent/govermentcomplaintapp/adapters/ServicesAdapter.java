package com.vincent.govermentcomplaintapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.vincent.govermentcomplaintapp.ui.ComplaintRoomActivity;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.Services;

import java.util.List;

import static android.content.ContentValues.TAG;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder>  {
    public  static final String USER = "userType";

    private ImageView iconImg;
    private TextView typeView;
    private MaterialCardView servicesCardView;
    private Context context;
    private List<Services> servicesList;

    private String user;

    public ServicesAdapter(Context context, List<Services> servicesList, String userType){
        this.context = context;
        this.servicesList = servicesList;
        this.user= userType;
    }


    @NonNull
    @Override
    public ServicesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.services_recycler, parent, false);
        return new ViewHolder (view);
    }


    @Override
    public void onBindViewHolder(@NonNull ServicesAdapter.ViewHolder holder, int position) {

        Log.d(TAG, "Service adapter: " + user);

        final String serviceType = servicesList.get(position).getService_type();
        final String serviceIcon = servicesList.get(position).getIcon();

        holder.setServiceType(serviceType);
        holder.setServiceIcon(serviceIcon, serviceType);
        holder.onCardClick(serviceType);

    }


    @Override
    public int getItemCount() {
        return servicesList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public ViewHolder(View view )
        {
            super(view);
            mView = view;
            servicesCardView = (MaterialCardView) mView.findViewById(R.id.services_card);
        }
        public void onCardClick(final String serviceType){
            servicesCardView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            servicesCardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                            redToComplaints(serviceType);
                        }
                    }
            );
        }

        public void redToComplaints(String serviceType){
            Intent intent = new Intent(context, ComplaintRoomActivity.class);
            intent.putExtra(ComplaintRoomActivity.SERVICE_TYPE, serviceType);
            intent.putExtra(ComplaintRoomActivity.USER, user);
            context.startActivity(intent);
        }


        public void setServiceType(String type){
            typeView = (TextView) mView.findViewById(R.id.service_type);
            typeView.setText(type);
        }

        public void setServiceIcon(String image, String type){
            iconImg = (ImageView) mView.findViewById(R.id.service_icon);
            iconImg.setMaxWidth(50);
            iconImg.setMaxHeight(50);
            iconImg.setContentDescription(type);
            iconImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

            RequestOptions reqOpt = new RequestOptions();
            reqOpt.placeholder(R.drawable.placeholder_img);
            Glide.with(context).setDefaultRequestOptions(reqOpt).load(image).into(iconImg);

        }
    }
}
