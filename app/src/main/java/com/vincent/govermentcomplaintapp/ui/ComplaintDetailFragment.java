package com.vincent.govermentcomplaintapp.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vincent.govermentcomplaintapp.R;
import com.vincent.govermentcomplaintapp.SpacesItemDecoration;
import com.vincent.govermentcomplaintapp.adapters.ComplaintImagesAdapter;

import java.util.ArrayList;

public class ComplaintDetailFragment extends Fragment  {

    private TextView streetNameView;
    private TextView cityView;
    private TextView provinceView;
    private TextView descriptionView;
    private TextView dateView;

    private Context mContext;

    private String date;
    private String streetName;
    private String city;
    private String province;
    private String description;
    private ArrayList<String> images;


    ComplaintDetailFragment(Context context, String date, String streetName, String city, String province, String description, ArrayList<String> images){
        this.mContext =context;
        this.date = date;
        this.streetName = streetName;
        this.city = city;
        this.province = province;
        this.description = description;
        this.images = images;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_complaint_detail, container, false);
        initViews(view);
        setViews();
        initRecyclerAndAdapter(view);
        return view;
    }

    public void initRecyclerAndAdapter(View view){
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.complaint_images_recycler_view);
        ComplaintImagesAdapter complaintImagesAdapter = new ComplaintImagesAdapter(mContext, images);
        GridLayoutManager gm = new GridLayoutManager(mContext, 2);
        recyclerView.setLayoutManager(gm);
        recyclerView.setAdapter(complaintImagesAdapter);
        recyclerView.addItemDecoration(new SpacesItemDecoration(10));
    }

    public void setViews(){
        streetNameView.setText(streetName);
        cityView.setText(city);
        dateView.setText(date);
        provinceView.setText(province);
        descriptionView.setText(description);

    }

    public void initViews(View view){
        streetNameView = (TextView) view.findViewById(R.id.street_name);
        descriptionView = (TextView) view.findViewById(R.id.complaint_description);
        cityView = (TextView) view.findViewById(R.id.city);
        provinceView = (TextView) view.findViewById(R.id.province);
        dateView = (TextView) view.findViewById(R.id.date_of_complaint);

    }
}