package com.sinch.messagingtutorialskeleton;

import android.app.Activity;
import android.os.Bundle;

import com.example.messagingtutorialskeleton.R;
import com.parse.Parse;
import com.parse.ParseObject;


public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Enable Local Data store.
        Parse.enableLocalDatastore(this);

        // Initialize Parse Database
        Parse.initialize(this, "OEi23FbAK56yGAMXD8HDZClKZ3ONoPtMGpIrpeCH", "8CRkfBYIJg23MfobLdIaQVoUoWIEn5TvTp3BneYY");
    }

}
