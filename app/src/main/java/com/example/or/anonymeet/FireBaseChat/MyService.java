package com.example.or.anonymeet.FireBaseChat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.or.anonymeet.GPS.FindPeopleActivity;
import com.example.or.anonymeet.R;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class MyService extends Service implements ChildEventListener{


    MessagesDB myDB;
    SQLiteDatabase db;
    Firebase myFirebaseChat;
    NotificationManager nm;
    SharedPreferences preferences;
    SharedPreferences.Editor se;
    public static boolean isActive;

    public MyService() {

}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("hiiiiiiiiiiii", "onCreate");
        isActive = true;
        myDB = new MessagesDB(this);
        db = myDB.getWritableDatabase();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        String myNickname = preferences.getString("nickname", "");
        myFirebaseChat = new Firebase("https://anonymeetapp.firebaseio.com/Chat");
        myFirebaseChat = myFirebaseChat.child(myNickname);
        myFirebaseChat.addChildEventListener(this);





    }



    @Override
    public void onDestroy() {
        Log.i("hiiiiiiiiiiii", "onCreate");
        myFirebaseChat.removeEventListener(this);
        isActive = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i("hiiiiiiiiiiii", "onStartCommand");
        preferences = getSharedPreferences("data", MODE_PRIVATE);
        se = preferences.edit();
        return super.onStartCommand(intent, flags, startId);
    }





    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if(dataSnapshot.child("message").getValue()!=null) {
            se.putString("check", dataSnapshot.child("message").getValue().toString());
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        String message;
        if (dataSnapshot.child("read").getValue().toString().equals("false")) {
            Log.i("hiiiiiiiiii", "a message has been recieved");
            if (dataSnapshot.child("message").getValue().toString().length() > 36 && dataSnapshot.child("message").getValue().toString().substring(0, 36).equals("cbd9b0a2-d183-45ee-9582-27df3020ff65")) {

                message = dataSnapshot.child("message").getValue().toString().substring(36);
                myDB.insertMessage(dataSnapshot.getKey().toString(), dataSnapshot.child("message").getValue().toString().substring(36), false);


            } else {
                message = dataSnapshot.child("message").getValue().toString();
            }
            myDB.insertMessage(dataSnapshot.getKey().toString(), message, false);


            if (ChatActivity.isActive() && ChatActivity.userWith.equals(dataSnapshot.getKey().toString())) {
                ChatActivity.recyclerAdapter.syncMessages();
                ChatActivity.scrollDown();
            } else {

                Notification.Builder n = new Notification.Builder(getApplicationContext())
                        .setContentTitle("New message from " + dataSnapshot.getKey().toString())
                        .setContentText(dataSnapshot.child("message").getValue().toString())
                        .setSmallIcon(R.drawable.contact)
                        .setAutoCancel(true)
                        .setTicker("hiiiiii")
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_SOUND);
                TaskStackBuilder t = TaskStackBuilder.create(getApplicationContext());
                Intent i = new Intent(getApplicationContext(), FindPeopleActivity.class);
                i.putExtra("fromNoti", true);
                i.putExtra("usernameFrom", dataSnapshot.getKey().toString());
                t.addParentStack(FindPeopleActivity.class);
                t.addNextIntent(i);
                PendingIntent pendingIntent = t.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                n.setContentIntent(pendingIntent);
                nm.notify(0, n.build());
            }
            if (MessagesActivity.isActive) {
                MessagesActivity.usersAdapter.syncContacts();

            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
