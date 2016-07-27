package crejaud.tech_company_clicker.clicker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import crejaud.tech_company_clicker.R;
import crejaud.tech_company_clicker.handler.ClickTransactionHandler;
import crejaud.tech_company_clicker.listener.ClickEventListener;
import crejaud.tech_company_clicker.listener.BigIntegerEventListener;
import crejaud.tech_company_clicker.signIn.BaseActivity;

public class ClickerActivity extends BaseActivity implements
        View.OnClickListener {

    private TextView mCurrencyTextView;

    // Listeners
    private ClickEventListener clickEventListener;
    private BigIntegerEventListener currencyEventListener;

    private String firebaseUid, companyName, username;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCompaniesRef = null;
    private DatabaseReference mUsersRef;
    private DatabaseReference mCompanyRef;

    private GoogleApiClient mGoogleGamesApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicker);

        // show progress dialog, while loading everything
        showProgressDialog();

        mGoogleGamesApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleGamesApiClient.connect();

        // Set up firebase references
        mCompaniesRef = mRootRef.child(getString(R.string.firebase_db_companies));
        mUsersRef = mRootRef.child(getString(R.string.firebase_db_users));

        setAds();

        // Views
        mCurrencyTextView = (TextView) findViewById(R.id.currency_text);

        // Button listeners
        findViewById(R.id.clicker_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // Set up the firebase listeners
        setFirebaseListeners();

    }

    private void setAds() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setFirebaseListeners() {
        // Get Uid
        firebaseUid = getIntent().getExtras().getString(getString(R.string.intent_extra_unique_id));
        
        // Get company name
        companyName = getIntent().getExtras().getString(getString(R.string.intent_extra_company_name));

        // Get username
        username = getIntent().getExtras().getString(getString(R.string.intent_extra_username));

        // get the company ref using the company name!
        mCompanyRef = mCompaniesRef.child(companyName);

        // ensure the user of the company is in the company's users list
        mCompanyRef.child(getString(R.string.firebase_db_users)).child(firebaseUid).setValue(true);

        currencyEventListener = new BigIntegerEventListener(mCurrencyTextView, getString(R.string.currency));

        // set listener for company's currency (FOREVER!, since it will be changing!)
        mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

        clickEventListener = new ClickEventListener();

        // set listener for company's currency per second (FOREVER!, since it will be changing!)
        mCompanyRef.child(getString(R.string.firebase_db_currency_per_click)).addValueEventListener(clickEventListener);

        hideProgressDialog();
    }

    private void incrementCurrencyFromClick() {
        Log.d("CurrentPlayerInClicker", Games.Players.getCurrentPlayer(mGoogleGamesApiClient).getDisplayName());
        mCompanyRef.child(getString(R.string.firebase_db_currency)).runTransaction(new ClickTransactionHandler(clickEventListener));
    }

    private void signOut() {
        Log.d("LOGIN", "Sign out clicked!!!");

        if (mGoogleGamesApiClient != null && mGoogleGamesApiClient.isConnected()) {
            Games.signOut(mGoogleGamesApiClient);
            mGoogleGamesApiClient.disconnect();
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clicker_button:
                incrementCurrencyFromClick();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }
}
