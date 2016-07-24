package crejaud.tech_company_clicker.global;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;


/**
 * Created by creja_000 on 7/23/2016.
 */

public class AuthApplication extends Application {
    public static GoogleApiClient mGoogleApiClient;
    public static FirebaseAuth mAuth;
}
