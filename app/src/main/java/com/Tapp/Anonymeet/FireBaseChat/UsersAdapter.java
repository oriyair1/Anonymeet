package com.Tapp.Anonymeet.FireBaseChat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Tapp.Anonymeet.R;

import java.util.ArrayList;
import java.util.Random;

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
        this.name = name;




        Random rnd = new Random();
        int num = rnd.nextInt(5);

        if(gender.equals("male")) {
            switch (num) {
                case 0:
                    this.photo = R.drawable.boy3;
                    break;
                case 1:
                    this.photo = R.drawable.boy4;
                    break;
                case 2:
                    this.photo = R.drawable.boy5;
                    break;
                case 3:
                    this.photo = R.drawable.boy6;
                    break;
                case 4:
                    this.photo = R.drawable.boy7;
                    break;

            }
        }
        else {

                switch (num) {
                    case 0:
                        this.photo = R.drawable.girl3;
                        break;
                    case 1:
                        this.photo = R.drawable.girl4;
                        break;
                    case 2:
                        this.photo = R.drawable.girl5;
                        break;
                    case 3:
                        this.photo = R.drawable.girl6;
                        break;
                    case 4:
                        this.photo = R.drawable.girl7;
                        break;

                }

        }


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
    Activity activity;


    public UsersAdapter(Context con, Activity a, MyListener myListener){
        setHasStableIds(true);
        preferences = con.getSharedPreferences("data", Context.MODE_PRIVATE);
        this.mItemClickListener = myListener;
        context = con;
        activity = a;
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
        holder.checkIfLast(position);
        holder.setTheLongClickListener(position);
        holder.lastMessage.setText(db.getLastMessageWith(c.name));
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
        TextView alert;
        TextView lastMessage;
        View divider;
        LinearLayout layout;

        public MyViewHolder(View v){
            super(v);
            image = (ImageView) v.findViewById(R.id.contactImage);
            name = (TextView) v.findViewById(R.id.contactName);
            alert = (TextView)v.findViewById(R.id.alert);
            lastMessage = (TextView)v.findViewById(R.id.lastMessage);
            divider = v.findViewById(R.id.users_divider);
            layout = (LinearLayout)v.findViewById(R.id.main_contact_layout);


            v.setOnClickListener(this);


        }

        public void setTheLongClickListener(final int position) {
            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ContactMenuDialogFragment dialogFragment = new ContactMenuDialogFragment(adapter, position, context);
                    dialogFragment.show(activity.getFragmentManager(), "contact_menu_dialog_fragment");




                    return false;
                }
            });
        }
        public void checkIfLast(int position) {
            if(position == contacts.size()-1) {
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, context.getResources().getDisplayMetrics());
                layout.setLayoutParams(new RecyclerView.LayoutParams(v.getLayoutParams().width, height));
                layout.removeView(divider);
            }
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

