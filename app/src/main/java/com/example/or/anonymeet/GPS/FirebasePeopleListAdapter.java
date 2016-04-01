package com.example.or.anonymeet.GPS;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.or.anonymeet.R;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * Created by gamrian on 01/04/2016.
 */
public class FirebasePeopleListAdapter extends FirebaseRecyclerViewAdapter<String, FirebasePeopleListAdapter.ViewHolder> {

    ArrayList<String> userNames;
    ArrayList<String> addresses;
    ListListener listener;

    public FirebasePeopleListAdapter(Query ref, Class<String> modelClass) {
        super(ref, modelClass);
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name_text;
        TextView address_text;
        boolean gender;
        String usernameTo;

        public ViewHolder(View itemView) {
            super(itemView);

            name_text = (TextView) itemView.findViewById(R.id.name_text);
            address_text = (TextView) itemView.findViewById(R.id.address_text);

            itemView.setOnClickListener(this);
        }

        public void onClick(View v) {
            listener.startChat(usernameTo);
        }
    }
}
