package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.messagingtutorialskeleton.R;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/*
 *  First page where user logs in / signs up.
 *  This class is connected to the parse database
 */
public class LoginActivity extends Activity {

    Button loginButton, signUpButton;
    EditText usernameField, passwordField;

    /*
     *   Setting up parse database and creating a login / signup feature for users
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Gather all the ids from activity_login.xml to retrieve and store the data
        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signupButton);
        usernameField = (EditText) findViewById(R.id.loginUsername);
        passwordField = (EditText) findViewById(R.id.loginPassword);


        initializeParse();
        setLogin(usernameField, passwordField);
        setSignup(usernameField, passwordField);
    }


    /*
     *   To stop the sinch service when app is not in used
     */
    @Override
    public void onDestroy() {
        stopService(new Intent(this, MessageService.class));
        super.onDestroy();
    }


    void redirectPage(){
        final Intent intent = new Intent(getApplicationContext(), ListUsersActivity.class);
        final Intent serviceIntent = new Intent(getApplicationContext(), MessageService.class);

        startActivity(intent);
        startService(serviceIntent);
    }
    /*
     *   Setup parse login
     */
    void setLogin(final EditText usrField, final EditText pwField) {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usrField.getText().toString();
                String password = pwField.getText().toString();

                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    public void done(ParseUser user, com.parse.ParseException e) {
                        if (user != null) {
                            redirectPage();
                            Toast.makeText(getApplicationContext(),
                                    "Login successfully!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "There was an error logging in.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }


    /*
     *   Setup parse sign up
     */
    void setSignup(final EditText usrField, final EditText pwField) {
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = usrField.getText().toString();
                String password = pwField.getText().toString();

                ParseUser user = new ParseUser();
                user.setUsername(username);
                user.setPassword(password);

                user.signUpInBackground(new SignUpCallback() {
                    public void done(com.parse.ParseException e) {
                        if (e == null) {
                            redirectPage();
                            Toast.makeText(getApplicationContext(),
                                    "Signup success!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "There was an error signing up."
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /*
     *   Initialize parse database to be seen on parse.com and inputting secret key
     */
    void initializeParse() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "OEi23FbAK56yGAMXD8HDZClKZ3ONoPtMGpIrpeCH", "8CRkfBYIJg23MfobLdIaQVoUoWIEn5TvTp3BneYY");
    }

}
