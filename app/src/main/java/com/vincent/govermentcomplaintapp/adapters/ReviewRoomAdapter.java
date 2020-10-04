package com.vincent.govermentcomplaintapp.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.ReviewRoom;

import java.util.ArrayList;

public class ReviewRoomAdapter extends RecyclerView.Adapter<ReviewRoomAdapter.ViewHolder>{

    private ArrayList<ReviewRoom> reviewRooms = new ArrayList<>();

    public ReviewRoomAdapter(ArrayList<ReviewRoom> reviewRooms) {
        this.reviewRooms = reviewRooms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_room_recycler, parent, false);
        final ReviewRoomAdapter.ViewHolder holder = new ReviewRoomAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ((ViewHolder)holder).chatroomTitle.setText(reviewRooms.get(position).getMunicipailty());
    }

    @Override
    public int getItemCount() {
        return reviewRooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder

    {
        TextView chatroomTitle;

        public ViewHolder(View itemView) {
            super(itemView);
           // chatroomTitle = itemView.findViewById(R.id.chatroom_title);
        }

    }



}
