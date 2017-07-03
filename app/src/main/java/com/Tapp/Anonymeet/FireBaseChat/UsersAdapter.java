package com.Tapp.Anonymeet.FireBaseChat;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.Tapp.Anonymeet.R;

import java.util.ArrayList;

/**
 * Created by Or on 18/01/2016.
 */
class Contact {
    int photo;
    String name;
    String gender;
    long date;

    public Contact(String name, String gender, long date){

        this.gender = gender;
        this.date = date;
        if(gender.equals("male"))this.photo = R.drawable.boy2;
        else this.photo= R.drawable.girl2;
        this.name=name;

    }

}

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {

    ArrayList<Contact> contacts;
    LayoutInflater inflater;
    View v;
    MyListener mItemClickListener;
    Context context;
    UsersAdapter adapter = this;
    HelperDB db;
    SharedPreferences preferences;


    public UsersAdapter(Context con, MyListener myListener){
        preferences = con.getSharedPreferences("data", Context.MODE_PRIVATE);

        this.mItemClickListener = myListener;
        context = con;
        inflater = LayoutInflater.from(context);
        this.db = new HelperDB(con);
        contacts = db.getContacts();
        Log.i("hiiiiiiiiiii", "contacts: " + contacts.size());

    }

    public void syncContacts(){
        contacts = db.getContacts();
        Log.i("hiiiiiiiiiiiiiiiii", "contacts: " + contacts.size());
        notifyDataSetChanged();

    }

    public void itemInsertedIn(int position) {
        contacts = db.getContacts();
        notifyItemInserted(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        v = inflater.inflate(R.layout.item_recycle_view, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Contact c = contacts.get(position);
        holder.image.setImageResource(c.photo);
        holder.name.setText(c.name);
        holder.position = position;
        preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        int num = preferences.getInt("user " + c.name, 0);
        if(num > 0){
            holder.alert.setText(""+num);
            holder.alert.setVisibility(View.VISIBLE);
        } else{
            holder.alert.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.contacts.size();
    }



    public void delete(int position) {

        String contactName = contacts.get(position).name;
        db.deleteUser(contactName);
        contacts.remove(position);
        if (contacts.size() == 1) {
            notifyDataSetChanged();
        } else {
            notifyItemRemoved(position);
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image;
        TextView name;
        int position;
        TextView alert;

        public MyViewHolder(View v){
            super(v);
            image = (ImageView) v.findViewById(R.id.contactImage);
            name = (TextView) v.findViewById(R.id.contactName);
            alert = (TextView)v.findViewById(R.id.alert);
            v.setOnClickListener(this);
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Delete Contact");
                    builder.setMessage("Are you sure you want to delete your contact? You will lose your chat history too.");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UsersAdapter.this.adapter.delete(getPosition());
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    Dialog dialog = builder.create();
                    dialog.show();


                    return false;
                }
            });

        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(v, getPosition(), name.getText().toString());
        }
    }

}



   interface MyListener {

    public void onItemClick(View view , int position, String name);
}

