package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.messagingtutorialskeleton.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static com.parse.ParseUser.*;

/*
 * Lists the user's friends
 * Created by Alicia on 12/4/2015.
 */
public class ListUsersActivity extends Activity {

    String currentUserId;
    ListView usersListView;
    ArrayList<String> names;
    ArrayAdapter<String> namesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list);

        currentUserId = getCurrentUser().getObjectId();
        names = new ArrayList<String>();
        usersListView = (ListView) findViewById(R.id.usersListView);
        namesArrayAdapter =
                new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.user_list_item, names);

        getUserList(currentUserId, names, usersListView, namesArrayAdapter);


    }


    /*
     *   Using ParseQuery to gather all the users in order to form a list
     *   Each user is clickable and will open a chat message between the current
     *   user and selected user.
     */
    void getUserList(String userId, final ArrayList<String> nameList,
                     final ListView listView, final ArrayAdapter<String> adapter){

        ParseQuery<ParseUser> query = getQuery();
        query.whereNotEqualTo("objectId", userId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < userList.size(); i++) {
                        nameList.add(userList.get(i).getUsername().toString());
                    }

                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> a, View v, int i, long l) {
                            openConversation(names, i);
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /*
     *   Opens up a conversation between the current user and specific user
     */
    public void openConversation(ArrayList<String> names, int pos) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", names.get(pos));
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> user, ParseException e) {
                if (e == null) {
                    //start the messaging activity
                    Toast.makeText(getApplicationContext(),
                            "Clicked user!",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Error finding that user",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
