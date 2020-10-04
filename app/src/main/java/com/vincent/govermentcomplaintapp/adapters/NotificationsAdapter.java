package com.vincent.govermentcomplaintapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.UserNotification;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private Context mContext;
    private List<UserNotification> userNotificationsList;

    private TextView titleView;
    private TextView datePostedView;
    private TextView dateResolvedView;
    private Button yesBtn;
    private Button noBtn;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUser;

    public NotificationsAdapter(Context context, List<UserNotification> userNotificationsList){
        this.mContext = context;
        this.userNotificationsList = userNotificationsList;

    }

    @NonNull
    @Override
    public NotificationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.notifications_recycler, parent, false);
        return new NotificationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.ViewHolder holder, int position) {

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser().getUid();

        String title = userNotificationsList.get(position).getTitle();
        long resolved_milliseconds = userNotificationsList.get(position).getDate_resolved().getTime();
        String dateResolved = DateFormat.format("MM/dd/yyyy", new Date(resolved_milliseconds)).toString();
        String notiId = userNotificationsList.get(position).getNotification_id();

        long posted_milliseconds = userNotificationsList.get(position).getDate_posted().getTime();
        String datePosted = DateFormat.format("MM/dd/yyyy", new Date(posted_milliseconds)).toString();

        holder.setTitle(title);
        holder.setDatePosted(datePosted);
        holder.setDateResolved(dateResolved);
        holder.yesClicked(notiId);

    }

    @Override
    public int getItemCount() {
        return userNotificationsList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ViewHolder(View view){
            super(view);
            mView = view;
        }

        public void setTitle(String title){
            titleView = (TextView) mView.findViewById(R.id.notification_title);
            titleView.setText(title);

        }

        public void setDatePosted(String datePosted){
            datePostedView =(TextView) mView.findViewById(R.id.complaint_published_date);
            datePostedView.setText("Date published: " + datePosted);

        }

        public void setDateResolved(String dateResolved){
            dateResolvedView = (TextView) mView.findViewById(R.id.complaint_resolved_date);
            dateResolvedView.setText("Date resolved: " + dateResolved);

        }

        public void yesClicked(final String notiId){
            yesBtn = (Button) mView.findViewById(R.id.yes_issue_resolved_btn);
            yesBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Yes complaint updated: deleting document ");
                            firestore
                                    .collection("Users")
                                    .document(currentUser)
                                    .get()
                                    .addOnSuccessListener(
                                            new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    documentSnapshot
                                                            .getReference()
                                                            .collection("Notifications")
                                                            .document(notiId)
                                                            .delete()
                                                            .addOnSuccessListener(
                                                                    new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.d(TAG, "Document deleted");
                                                                            Toast.makeText(mContext, "Issue deleted", Toast.LENGTH_SHORT).show();
                                                                            notifyDataSetChanged();
                                                                        }
                                                                    }
                                                            ).addOnFailureListener(
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }

                                                    );
                                                    notifyDataSetChanged();
                                                }
                                            }
                                    ).addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                            );

                        }
                    }
            );
        }



    }
}
