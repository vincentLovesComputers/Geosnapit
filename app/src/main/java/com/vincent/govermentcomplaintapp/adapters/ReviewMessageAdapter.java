package com.vincent.govermentcomplaintapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.Admin;
import com.vincent.govermentcomplaintapp.models.ReviewMessage;
import com.vincent.govermentcomplaintapp.models.User;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;


public class ReviewMessageAdapter extends RecyclerView.Adapter<ReviewMessageAdapter.ViewHolder>{

    private ArrayList<ReviewMessage> mMessages = new ArrayList<>();
    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<Admin> mAdmin = new ArrayList<>();
    private Context mContext;

    private TextView userNameView;
    private TextView messageView;
    private CircleImageView profilePic;
    private TextView dateView;

    private FirebaseAuth auth;
    private  String currentUser;
    private String userType;



    public ReviewMessageAdapter(ArrayList<ReviewMessage> messages,
                                      ArrayList<User> users,
                                      ArrayList<Admin> admin,
                                      Context context,
                                        String userType
                                ) {
        this.mMessages = messages;
        this.mUsers = users;
        this.mContext = context;
        this.userType = userType;
        this.mAdmin = admin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_message_recycler, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser().getUid();
        String message = mMessages.get(position).getMessage();
        String userType = mMessages.get(position).getUser_type();

        if(mMessages.get(position).getTimeStamp() != null){
            long milliseconds = mMessages.get(position).getTimeStamp().getTime();
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
            holder.setDate(dateString);
        }

        if(userType.equals("Users")){
            String userName = mMessages.get(position).getUser().getName();
            String user_id = mMessages.get(position).getUser().getUser_id();
            String imageUri = mMessages.get(position).getUser().getImage();

            holder.setProfilePic(imageUri);
            if(currentUser.equals(user_id)){
                holder.setMessage(message,ContextCompat.getColor(mContext, R.color.blue));
            }else{
                holder.setMessage(message,ContextCompat.getColor(mContext, R.color.green));
            }

            holder.setUserName(userName);

        }else{
            String adminName = mMessages.get(position).getAdmin().getName() + " (Admin)";
            String imageUri = mMessages.get(position).getAdmin().getImage();


            holder.setProfilePic(imageUri);
            holder.setMessage(message, ContextCompat.getColor(mContext, R.color.red));

            holder.setUserName(adminName);

        }




//        if(Objects.equals(FirebaseAuth.getInstance().getUid(), mMessages.get(position).getUser().getUser_id())){
//            ((ViewHolder)holder).username.setTextColor(ContextCompat.getColor(mContext, R.color.green));
//        }
//        else{
//            ((ViewHolder)holder).username.setTextColor(ContextCompat.getColor(mContext, R.color.blue));
//        }
//
//        ((ViewHolder)holder).username.setText(mMessages.get(position).getUser().getName());
//        ((ViewHolder)holder).message.setText(mMessages.get(position).getMessage());



    }



    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.review_message);
            userNameView = itemView.findViewById(R.id.review_message_username);
            mView = itemView;
        }

        public void setDate(String date){
            dateView = mView.findViewById(R.id.review_message_date);
            dateView.setText(date);

        }

        public void setProfilePic(String image){
            profilePic = mView.findViewById(R.id.message_profile_pic);


            RequestOptions reqOpt = new RequestOptions();
            reqOpt.placeholder(mView.getResources().getDrawable(R.drawable.profile_icon));
            Glide.with(mContext).setDefaultRequestOptions(reqOpt).load(image).into(profilePic);

        }

        public void setMessage(String message, int color){
            messageView.setText(message);
            messageView.setTextColor(color);
        }

        public void setUserName(String userName){
            userNameView.setText(userName);
        }
    }


}
