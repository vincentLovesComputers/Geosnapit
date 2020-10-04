package com.vincent.govermentcomplaintapp.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.vincent.govermentcomplaintapp.ui.ComplaintRoomActivity;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.models.Complaint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder>  {
    private Context mContext;
    private List<Complaint> complaintList;
    private String service;

    private TextView titleView;
    private TextView descriptionView;
    private TextView dateView;
    private ImageView exploreButton;
    private TextView addressView;
    private MaterialCardView complaintCardView;
    private ComplaintRoomActivity complaintRoomActivity;
    private MaterialCardView issueUpdateCardView;
    private TextView issueTextView;
    private Button issueYesResolvedBtn;
    private Button issueNotResolvedBtn;
    private ImageView alertIcon;

    private String user;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUser;



    public ComplaintAdapter(Context context, List<Complaint> complaintList, ComplaintRoomActivity complaintRoomActivity, String user, String service){
        this.mContext = context;
        this.complaintList = complaintList;
        this.complaintRoomActivity = complaintRoomActivity;
        this.user = user;
        this.service = service;
    }

    @NonNull
    @Override
    public ComplaintAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.complaint_recycler, parent, false);

        return new ComplaintAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintAdapter.ViewHolder holder, int position) {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser().getUid();

        String title = complaintList.get(position).getTitle();
        String description = complaintList.get(position).getDescription();
        long milliseconds = complaintList.get(position).getTimeStamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(milliseconds)).toString();
        String city = complaintList.get(position).getCity();
        String streetName = complaintList.get(position).getStreet_name();
        String province = complaintList.get(position).getProvince();
        String postCode = complaintList.get(position).getPost_code();
        String municipality = complaintList.get(position).getMunicipality();
        String complaint_id = complaintList.get(position).getUser();
        String issueUpdate = complaintList.get(position).getIssue_update();

        String address = streetName + ", " + city;

        if (
                complaintList.get(position).getIssue_update().matches("yes")
        ) {
            if(user.equals("Admin")){
                holder.waitingForOrigUser();
            }else if(user.equals("Users")){
               if(currentUser.matches(complaint_id)){
                   holder.showIssueUpdate(complaint_id);
               }else{
                   holder.waitingForOrigUser();

               }
            }

        }



        //more details
        ArrayList<String> images = complaintList.get(position).getImages();

        holder.setTitle(title);
        holder.setDescription(description);
        holder.setDate(dateString);
        holder.setAddress(address);

        //exploreBtnporessed
        holder.exploreComplaint(description, dateString, streetName, city, province, postCode, images, municipality, complaint_id);

    }

    @Override
    public int getItemCount() {

        if(complaintList!=null){
            Log.d(TAG, "Size of list -->" + String.valueOf(complaintList.size()));
            return complaintList.size();

        }else{
            return 0;
        }
        }

    class ViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ViewHolder(View view){
            super(view);
            mView = view;
            exploreButton = (ImageView) mView.findViewById(R.id.complaint_explore);
            complaintCardView = (MaterialCardView) mView.findViewById(R.id.complaint_card_view);
            descriptionView = (TextView) mView.findViewById(R.id.complaint_description);
            issueTextView = (TextView) mView.findViewById(R.id.issue_resolved_txt);
            issueYesResolvedBtn = mView.findViewById(R.id.yes_issue_resolved_btn);
            issueNotResolvedBtn = mView.findViewById(R.id.issue_no_resolved);
            issueUpdateCardView = mView.findViewById(R.id.issue_resolved_card_view);
            alertIcon = mView.findViewById(R.id.issue_alert_icon);
            addressView = (TextView) mView.findViewById(R.id.complaint_address);

        }

        public void waitingForOrigUser(){

            exploreButton.setVisibility(View.INVISIBLE);
            complaintCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.red));
            descriptionView.setVisibility(View.INVISIBLE);
            issueUpdateCardView.setVisibility(View.VISIBLE);
            issueTextView.setVisibility(View.VISIBLE);
            alertIcon.setVisibility(View.VISIBLE);
            addressView.setVisibility(View.INVISIBLE);
            issueTextView.setText("Issue queued as resolved. Waiting for confirmation.");
            complaintCardView.setEnabled(false);
            exploreButton.setEnabled(false);
        }

        public void showIssueUpdate(final String doc_id){
            addressView.setVisibility(View.INVISIBLE);
            exploreButton.setVisibility(View.INVISIBLE);
            descriptionView.setVisibility(View.INVISIBLE);
            issueTextView.setVisibility(View.VISIBLE);
            issueNotResolvedBtn.setVisibility(View.VISIBLE);
            issueYesResolvedBtn.setVisibility(View.VISIBLE);
            issueUpdateCardView.setVisibility(View.VISIBLE);

            complaintCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.red));

            complaintCardView.setEnabled(false);
            exploreButton.setEnabled(false);


            issueYesResolvedBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firestore
                                    .collection("Services")
                                    .document(service)
                                    .get()
                                    .addOnSuccessListener(
                                            new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    documentSnapshot
                                                            .getReference()
                                                            .collection("Complaints")
                                                            .document(doc_id)
                                                            .delete()
                                                            .addOnSuccessListener(
                                                                    new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Toast.makeText(mContext, "Complaint removed!", Toast.LENGTH_SHORT).show();
                                                                            notifyDataSetChanged();
                                                                        }
                                                                    }
                                                            ).addOnFailureListener(
                                                            new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    notifyDataSetChanged();
                                                                }
                                                            }
                                                    );

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

            issueNotResolvedBtn.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            exploreButton.setVisibility(View.VISIBLE);
                            descriptionView.setVisibility(View.VISIBLE);

                            issueTextView.setVisibility(View.INVISIBLE);
                            issueNotResolvedBtn.setVisibility(View.INVISIBLE);
                            issueYesResolvedBtn.setVisibility(View.INVISIBLE);
                            issueUpdateCardView.setVisibility(View.INVISIBLE);

                            complaintCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.white));
                            complaintCardView.setEnabled(true);
                            exploreButton.setEnabled(true);

                            firestore
                            .collection("Services")
                                    .document(service)
                                    .get()
                                    .addOnSuccessListener(
                                            new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if(documentSnapshot !=null){
                                                        if(documentSnapshot.exists()){
                                                            documentSnapshot
                                                                    .getReference()
                                                                    .collection("Complaints")
                                                                    .document(doc_id)
                                                                    .update(
                                                                            "issue_update", "no"
                                                                    ).addOnSuccessListener(
                                                                    new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            new AlertDialog.Builder(mContext)

                                                                                    .setIcon(android.R.drawable.ic_dialog_alert)

                                                                                    .setTitle("Sending notification update")

                                                                                    .setMessage("Admin will be notified the issue is not resolved")

                                                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                                            //set what would happen when positive button is clicked
                                                                                            Intent intent = new Intent(mContext, ComplaintRoomActivity.class);
                                                                                            intent.putExtra(ComplaintRoomActivity.SERVICE_TYPE, service);
                                                                                            mContext.startActivity(intent);
                                                                                            notifyDataSetChanged();


                                                                                        }
                                                                                    }).show();


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

        public void exploreComplaint(final String description, final String dateString, final String streetName, final String city, final String province, final String postCode , final ArrayList<String> images, final String municipality, final String complaint_id){


            complaintCardView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Document id : " + complaint_id);
                            Log.d(TAG, "Document Name : " + description);
                            complaintRoomActivity.redToComplaintProgress(description, dateString, streetName, city, province, postCode, images, municipality, complaint_id);


                        }
                    }
            );


        }

        public void setAddress(String addressTxt){

            addressView.setText(addressTxt);
        }


        public void setDate(String date){
            dateView = (TextView) mView.findViewById(R.id.complaint_date);
            dateView.setText(date);

        }

        public void setTitle(String title){
            titleView = (TextView) mView.findViewById(R.id.complaint_title);
            titleView.setText(title);
        }

        public void setDescription(String description){

            descriptionView.setText(description);

        }
    }

}

