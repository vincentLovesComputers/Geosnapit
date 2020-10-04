package com.vincent.govermentcomplaintapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.adapters.ReviewMessageAdapter;
import com.vincent.govermentcomplaintapp.models.Admin;
import com.vincent.govermentcomplaintapp.models.Complaint;
import com.vincent.govermentcomplaintapp.models.User;
import com.vincent.govermentcomplaintapp.models.ReviewMessage;
import com.vincent.govermentcomplaintapp.models.ReviewRoom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static android.content.ContentValues.TAG;

public class ComplaintReviewsFragment extends Fragment implements View.OnClickListener {


    private static final String MUNICIPALITY_NAME = "municipality";

    private String municipalityName;


    private ReviewRoom reviewRoom;
    private EditText reviewMessage;
    private ImageView checkMark;
    private String currentUser;
    private TextView issueUpdateTxt;
    private Button issueNoBtn;
    private Button issueYesBtn;


    //vars
    private ListenerRegistration mReviewMessageEventListener, mUserListEventListener;
    private RecyclerView mReviewMessageRecyclerView;
    private ReviewMessageAdapter mReviewMessageAdapter;
    private ArrayList<ReviewMessage> mMessages = new ArrayList<>();
    private Set<String> mMessageIds = new HashSet<>();
    private ArrayList<User> mUserList = new ArrayList<>();
    private ArrayList<Admin> mAdminList = new ArrayList<>();
    private FirebaseFirestore firestore;

    private Context mContext;
    private Activity mActivity;

    private View mView;
    private TextView titleView;
    private  DocumentReference newMessageDoc;
    private Map<String, Object> userMessageData = new HashMap<>();
    private Map<String, Object> adminMessageData = new HashMap<>();
    private Map<String, Object> notificationData = new HashMap<>();
    private  ReviewMessage newReviewMessage;
    private DocumentReference joinChatroomRef;
    private ProgressBar progressBar;

    private String userType;
    private String service;


    ComplaintReviewsFragment(Context context, ReviewRoom reviewRoom, String user, String service){
        this.mContext = context;
        this.reviewRoom = reviewRoom;
        this.userType = user;
        this.service = service;

    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_complaint_reviews, container, false);
        mView = view;
        initFirebase();

        //init views from fragment
        initViews(mView);
        mReviewMessageRecyclerView =(RecyclerView) view.findViewById(R.id.review_message_recycler_view);
        checkMark.setOnClickListener(this);

        if(userType.equals("Admin")){

            issueUpdateTxt.setVisibility(View.VISIBLE);
            issueNoBtn.setVisibility(View.VISIBLE);
            issueYesBtn.setVisibility(View.VISIBLE);
        }

        if(reviewRoom.getMunicipailty()!=null && reviewRoom.getReview_room_id()!=null){
            joinChatroom();
            initReviewRoomRecyclerView();
            getReviewroomUsers();
            issueUpdate();
        }else{
            Log.d(TAG, "ComplaintReviewFragment: ReviewRoom null");
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getChatMessages();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mReviewMessageEventListener != null){
            mReviewMessageEventListener.remove();
        }
        if(mUserListEventListener != null){
            mUserListEventListener.remove();
        }
    }

    private void issueUpdate(){
        issueNoBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "please provide update");
                        issueNoBtn.setVisibility(View.INVISIBLE);
                        issueYesBtn.setVisibility(View.INVISIBLE);
                        issueUpdateTxt.setText("Please provide progress on issue!");
                        issueUpdateTxt.setTextColor(getResources().getColor(R.color.red));

                    }
                }
        );

        issueYesBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Issue has been solved");
                        updateDbDoc();

                    }
                }
        );
    }

    private void updateDbDoc(){
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Update doc: " + reviewRoom.getReview_room_id());
        final Map<String, Object> updateData = new HashMap<>();
        updateData.put("issue_update", "yes");
        firestore
                .collection("Services")
                .document(service)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot!=null){
                                        if(documentSnapshot.exists()){
                                            documentSnapshot
                                                    .getReference()
                                                    .collection("Complaints")
                                                    .document(reviewRoom.getReview_room_id())
                                                    .update(updateData)
                                                    .addOnSuccessListener(
                                                            new OnSuccessListener<Void>() {

                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    issueNoBtn.setVisibility(View.INVISIBLE);
                                                                    issueYesBtn.setVisibility(View.INVISIBLE);

                                                                    new AlertDialog.Builder(mContext)

                                                                            .setIcon(android.R.drawable.ic_dialog_alert)

                                                                            .setTitle("Sending notification update")

                                                                            .setMessage("Original user who posted issue will be notified to close the issue")

                                                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    //set what would happen when positive button is clicked
                                                                                    redToComplaintRoom();
                                                                                }
                                                                            }).show();


                                                                    issueUpdateTxt.setTextSize(10);

                                                                    notifyUser();

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
                                    }else{
                                        Log.d(TAG, "Update doc: Document is null");
                                    }
                            }
                        }
                ).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void notifyUser(){

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
                                        .document(reviewRoom.getReview_room_id())
                                        .get()
                                        .addOnSuccessListener(
                                                new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        Complaint complaint = documentSnapshot.toObject(Complaint.class);

                                                        notificationData.put("title", complaint.getTitle());
                                                        notificationData.put("date_posted", complaint.getTimeStamp());
                                                        notificationData.put("date_resolved", FieldValue.serverTimestamp());
                                                        String address = complaint.getStreet_name() + ", " + complaint.getCity() + ", " + complaint.getProvince();
                                                        notificationData.put("address", address);
                                                        notificationData.put("notification_id", documentSnapshot.getId());

                                                        firestore
                                                                .collection("Users")
                                                                .document(reviewRoom.getReview_room_id())
                                                                .get()
                                                                .addOnSuccessListener(
                                                                        new OnSuccessListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                                documentSnapshot
                                                                                        .getReference()
                                                                                        .collection("Notifications")
                                                                                        .document()
                                                                                        .set(notificationData)
                                                                                        .addOnSuccessListener(
                                                                                                new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        Log.d(TAG, "Setting notification data");

                                                                                                    }
                                                                                                }
                                                                                        ).addOnFailureListener(
                                                                                        new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                Log.d(TAG, "Setting user notification error: " + e.getMessage());
                                                                                            }
                                                                                        }
                                                                                );
                                                                            }
                                                                        }
                                                                ).addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.d(TAG, "Error getting user: " + e.getMessage());
                                                                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                                                Log.d(TAG, "Notify user getting document data: " + e.getMessage());
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

    private void getChatMessages() {

            CollectionReference usersRef = firestore
                .collection("Reviewrooms")
                .document(reviewRoom.getReview_room_id())
                .collection("Review Messages");

        mReviewMessageEventListener = usersRef
                .orderBy("timeStamp", Query.Direction.ASCENDING)
                .addSnapshotListener(
                        new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                                if (querySnapshot != null) {
                                    if (!querySnapshot.getDocuments().isEmpty()) {
                                        for (QueryDocumentSnapshot doc : querySnapshot) {
                                            if (doc.exists()) {
                                                Log.d(TAG, "Document found");
                                                ReviewMessage message = doc.toObject(ReviewMessage.class);
                                                if (!mMessageIds.contains(message.getMessage_id())) {
                                                    mMessageIds.add(message.getMessage_id());
                                                    mMessages.add(message);
                                                    mReviewMessageRecyclerView.smoothScrollToPosition(mMessages.size() - 1);
                                                }
                                            }
                                            mReviewMessageAdapter.notifyDataSetChanged();

                                        }

                                    } else {
                                        Log.d(TAG, "Message Document is null");
                                    }
                                }}});}

    public void insertNewMessage() {
        String message = reviewMessage.getText().toString();

        if (!message.equals("")) {
            message = message.replaceAll(Objects.requireNonNull(System.getProperty("line.separator")), "");

            newMessageDoc = firestore
                    .collection("Reviewrooms")
                    .document(reviewRoom.getReview_room_id())
                    .collection("Review Messages")
                    .document();

            newReviewMessage = new ReviewMessage();
            newReviewMessage.setMessage(message);
            newReviewMessage.setMessage_id(newMessageDoc.getId());

            final String finalMessage = message;
            firestore
                    .collection(userType)
                    .document(currentUser)
                    .get()
                    .addOnSuccessListener(
                            new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot!=null){
                                        if (documentSnapshot.exists()) {
                                            Log.d(TAG, "User document exists");
                                            if(userType.equals("Users")){
                                                User user = documentSnapshot.toObject(User.class);
                                                if (user != null) {
                                                    userMessageData.put("message_id", newMessageDoc.getId());
                                                    userMessageData.put("message", finalMessage);
                                                    userMessageData.put("timeStamp", FieldValue.serverTimestamp());
                                                    userMessageData.put("user", user);
                                                    userMessageData.put("user_id", currentUser);
                                                    userMessageData.put("user_type", "Users");
                                                    newReviewMessage.setUser(user);

                                                    newMessageDoc.set(userMessageData);
                                                    mReviewMessageAdapter.notifyDataSetChanged();
                                                    Log.d(TAG, "insertNewMessage: retrieved user client: " + userMessageData.toString());

                                                    Log.d(TAG, user.getName());
                                                    Log.d(TAG, user.getUser_id());

                                                } else {
                                                    Log.d(TAG, "User is null");
                                                }

                                            }else{
                                                Admin admin = documentSnapshot.toObject(Admin.class);
                                                if (admin != null) {
                                                    adminMessageData.put("message_id", newMessageDoc.getId());
                                                    adminMessageData.put("message", finalMessage);
                                                    adminMessageData.put("timeStamp", FieldValue.serverTimestamp());
                                                    adminMessageData.put("admin", admin);
                                                    adminMessageData.put("admin_id", currentUser);
                                                    adminMessageData.put("user_type", "Admin");
                                                    newReviewMessage.setAdmin(admin);

                                                    newMessageDoc.set(adminMessageData);
                                                    Log.d(TAG, "insertNewMessage: retrieved admin client: " + adminMessageData.toString());

                                                    Log.d(TAG, admin.getName());
                                                    Log.d(TAG, admin.getAdmin_id());


                                                } else {
                                                    Log.d(TAG, "Admin is null");
                                                }
                                            }

                                        }else{
                                            Log.d(TAG, "Insert msg: admin document doesn't exist");
                                        }
                                    }else{
                                        Log.d(TAG, "Insert msg: document is null");
                                    }
                                }
                            }
                    ).addOnCompleteListener(
                    new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                clearMessage();
                            }else {
                                View parentLayout = mView.findViewById(android.R.id.content);
                                Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();

                            }

                        }
                    }
            );


        }
    }

    private void joinChatroom() {
        Log.d(TAG, "Join chat: " + reviewRoom.getReview_room_id());

        joinChatroomRef = firestore
                .collection("Reviewrooms")
                .document(reviewRoom.getReview_room_id())
                .collection("User List")
                .document(currentUser);

        firestore
                .collection(userType)
                .document(currentUser)
                .get()
                .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot!=null){
                                    if (documentSnapshot.exists()) {
                                        Log.d(TAG, "User document exists");

                                        if(userType.equals("Users")){
                                            User user = documentSnapshot.toObject(User.class);
                                            if(user!=null){
                                                joinChatroomRef.set(user).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "Join chat room: Successfully joined chat");
                                                                } else {
                                                                    Log.d(TAG, "Join chat room: Eror joining chat: " + task.getException().getMessage());


                                                                }

                                                            }
                                                        }
                                                );
                                            }

                                        }else if(userType.equals("Admin")){
                                            Admin admin = documentSnapshot.toObject(Admin.class);

                                            if(admin != null){
                                                joinChatroomRef.set(admin).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "Join chat room: Admin Successfully joined chat");
                                                                } else {
                                                                    Log.d(TAG, "Join chat room: Admin Eror joining chat: " + task.getException().getMessage());


                                                                }

                                                            }
                                                        }
                                                );
                                            }
                                        }

                                    }else{
                                        Log.d(TAG, "Document not found");
                                    }

                                }else{
                                    Log.d(TAG, "Join chat room: document i null");
                                }


                            }
                        }
                );

    }

    private void getReviewroomUsers() {

        CollectionReference usersRef = firestore
                .collection("ReviewRooms")
                .document(reviewRoom.getReview_room_id())
                .collection("User List");

        mUserListEventListener = usersRef
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "onEvent: Listen failed.", e);
                            return;
                        }

                        if (queryDocumentSnapshots != null) {

                            // Clear the list and add all the users again
                            mUserList.clear();
                            mUserList = new ArrayList<>();

                            if(userType.equals("Users")){
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    User user = doc.toObject(User.class);
                                    mUserList.add(user);
                                }

                                Log.d(TAG, "onEvent: user list size: " + mUserList.size());
                            }

                            else if(userType.equals("Admin")){
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    Admin admin = doc.toObject(Admin.class);
                                    mAdminList.add(admin);
                                }

                                Log.d(TAG, "onEvent: admin list size: " + mAdminList.size());

                            }


                        }
                    }
                });
    }

    public void initReviewRoomRecyclerView() {
        mReviewMessageAdapter = new ReviewMessageAdapter(mMessages, new ArrayList<User>(), new ArrayList<Admin>(), mContext, userType);
        mReviewMessageRecyclerView.setAdapter(mReviewMessageAdapter);
        mReviewMessageRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        mReviewMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mReviewMessageRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mMessages.size() > 0) {
                                mReviewMessageRecyclerView.smoothScrollToPosition(
                                        mReviewMessageRecyclerView.getAdapter().getItemCount() - 1);
                            }

                        }
                    }, 100);
                }
            }
        });
    }

    public void initFirebase() {
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkmark: {
                insertNewMessage();
            }
        }

    }

    private void clearMessage() {
        reviewMessage.setText("");
    }

    private void initViews(View view){
        reviewMessage = (EditText) view.findViewById(R.id.input_message);
        checkMark = (ImageView) view.findViewById(R.id.checkmark);
        issueNoBtn = (Button) view.findViewById(R.id.issue_no_btn);
        issueYesBtn = (Button) view.findViewById(R.id.issue_yes_btn);
        issueUpdateTxt = (TextView) view.findViewById(R.id.issue_update_text);
        progressBar = (ProgressBar) view.findViewById(R.id.compl_rev__prog_bar);
    }
    private void redToComplaintRoom(){
        Intent intent = new Intent(mContext, ComplaintRoomActivity.class);
        intent.putExtra(ComplaintRoomActivity.USER, userType);
        intent.putExtra(ComplaintRoomActivity.SERVICE_TYPE, service);
        startActivity(intent);
        mActivity.finish();
    }

}


