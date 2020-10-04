package com.vincent.govermentcomplaintapp.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.SpacesItemDecoration;
import com.vincent.govermentcomplaintapp.adapters.ServicesAdapter;
import com.vincent.govermentcomplaintapp.models.Services;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    private Context mContext;
    private List<Services> serviceslist;

    //firebase vars
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String currentUser;
    private Activity mActivity;

    //adapter vars
    private ServicesAdapter servicesAdapter;
    private RecyclerView servicesRecycler;

    //queries
    private Query firstQuery;
    private Query lastQuery;
    private DocumentSnapshot lastVisible;

    private String userType;


    public HomeFragment(Context context, String userType){
        this.mContext = context;
        this.userType = userType;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mActivity = getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceslist = new ArrayList<>();
        initFirebase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initRecyclerAndAdapter(view);
        getServices();

        return view;
    }

    public void getServices(){

        if(servicesRecycler != null){
            servicesRecycler.addOnScrollListener(
                    new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            boolean reachedBottom = !recyclerView.canScrollVertically(1);
                            if(reachedBottom){
                                Log.d(TAG, "Reached bottom, loading more items");
                                loadMoreServicesItems();
                            }
                        }
                    }
            );
            Log.d(TAG, "Loading initial items");
            loadFirstServicesItems();
        }

    }

    public void loadFirstServicesItems(){
        firstQuery = firestore
                .collection("Services")
                .limit(5);
        firstQuery.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if(querySnapshot != null){
                            Log.d(TAG, "QuerySnapshot not null: checking if empty");
                            if(!querySnapshot.getDocuments().isEmpty()){
                                Log.d(TAG, "QuerySnapshot not empty: getting documents");
                                lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);

                                for(DocumentChange doc_change: querySnapshot.getDocumentChanges()){
                                    Log.d(TAG, "Getting each document change");

                                    if(doc_change.getType() == DocumentChange.Type.ADDED){
                                        Log.d(TAG, "Document added");

                                        Services services = doc_change.getDocument().toObject(Services.class);
                                        serviceslist.add(services);

                                    }

                                    servicesAdapter.notifyDataSetChanged();

                                }
                            }
                        }

                    }
                }
        );

    }

    public void loadMoreServicesItems(){
        lastQuery = firestore
                .collection("Services")
                .startAfter(lastVisible)
                .limit(5);
        lastQuery.addSnapshotListener(
                new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException error) {
                        if(querySnapshot!=null){
                            Log.d(TAG, "Loading more items: querysnapshot not null");
                            if(!querySnapshot.isEmpty()){
                                Log.d(TAG, "Loading more items: querysnapshot not empty");
                                lastVisible = querySnapshot.getDocuments().get(querySnapshot.size() - 1);

                                for(DocumentChange doc_change: querySnapshot.getDocumentChanges()){
                                    if(doc_change.getType() == DocumentChange.Type.ADDED){
                                        Log.d(TAG, "Loading more products: document added");

                                        Services services = doc_change.getDocument().toObject(Services.class);
                                        serviceslist.add(services);
                                    }
                                    servicesAdapter.notifyDataSetChanged();
                                }
                            }

                        }
                    }
                }
        );
    }

    public void initRecyclerAndAdapter(View view){
        servicesRecycler = (RecyclerView) view.findViewById(R.id.services_recycler);
        servicesAdapter = new ServicesAdapter(mContext, serviceslist, userType);
        GridLayoutManager gm = new GridLayoutManager(mContext, 2);
        servicesRecycler.setLayoutManager(gm);
        servicesRecycler.setAdapter(servicesAdapter);
        servicesRecycler.addItemDecoration(new SpacesItemDecoration(10));

    }

    public void initFirebase(){
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getUid();
    }
}