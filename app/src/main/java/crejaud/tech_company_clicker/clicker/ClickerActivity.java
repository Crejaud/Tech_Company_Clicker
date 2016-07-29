package crejaud.tech_company_clicker.clicker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import crejaud.tech_company_clicker.R;
import crejaud.tech_company_clicker.handler.IncreaseCurrencyTransactionHandler;
import crejaud.tech_company_clicker.listener.BigIntegerEventListener;
import crejaud.tech_company_clicker.listener.XPEventListener;
import crejaud.tech_company_clicker.signIn.BaseActivity;

public class ClickerActivity extends BaseActivity implements
        View.OnClickListener {

    private TextView mCurrencyTextView, mCompanyLevelTextView, mCompanyXPTextView,
            mPerkPointsTextView, mUserXPTextView, mUserLevelTextView,
            mCompanyNameTextView, mCurrencyPerSecTextView, mCurrencyPerClickTextView;

    // Listeners
    private BigIntegerEventListener currencyEventListener, companyLevelEventListener, perkPointsEventListener,
            userLevelEventListener, currencyPerSecEventListener, currencyPerClickEventListener;
    private XPEventListener companyXpEventListener, userXpEventListener;

    private String firebaseUid, companyName, username;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCompaniesRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mCompanyRef;

    private GoogleApiClient mGoogleGamesApiClient;

    private ProgressBar mCompanyXPProgressBar, mUserXPProgressBar;

    private CurrencyPerSecRunnable currencyPerSecRunnable;
    private Thread currencyPerSecThread = null;

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
        mCompanyLevelTextView = (TextView) findViewById(R.id.company_level_text);
        mCompanyNameTextView = (TextView) findViewById(R.id.company_name_text);
        mCompanyXPTextView = (TextView) findViewById(R.id.company_xp_text);
        mCurrencyPerSecTextView = (TextView) findViewById(R.id.currency_per_sec_text);
        mPerkPointsTextView = (TextView) findViewById(R.id.perk_points_text);
        mUserXPTextView = (TextView) findViewById(R.id.user_xp_text);
        mUserLevelTextView = (TextView) findViewById(R.id.user_level_text);
        mCurrencyPerClickTextView = (TextView) findViewById(R.id.currency_per_click_text);

        mCompanyXPProgressBar = (ProgressBar) findViewById(R.id.company_xp_bar);
        mUserXPProgressBar = (ProgressBar) findViewById(R.id.user_xp_bar);

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

        // set company name to text view
        mCompanyNameTextView.setText(companyName);

        // Get username
        username = getIntent().getExtras().getString(getString(R.string.intent_extra_username));

        // get the company ref using the company name!
        mCompanyRef = mCompaniesRef.child(companyName);

        // ensure the user of the company is in the company's users list
        mCompanyRef.child(getString(R.string.firebase_db_users)).child(firebaseUid).setValue(true);

        // set listener for company's currency (FOREVER!, since it will be changing!)
        currencyEventListener = new BigIntegerEventListener(mCurrencyTextView, getString(R.string.currency));
        mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

        // set listener for company's currency per second (FOREVER!, since it will be changing!)
        currencyPerSecEventListener = new BigIntegerEventListener(mCurrencyPerSecTextView, getString(R.string.currency_per_sec));
        mCompanyRef.child(getString(R.string.firebase_db_currency_per_sec)).addValueEventListener(currencyPerSecEventListener);

        // set listener for company's xp (FOREVER!, since it will be changing!)
        companyXpEventListener = new XPEventListener(mCompanyXPTextView, mCompanyRef.child(getString(R.string.firebase_db_level)),
                mCompanyRef.child(getString(R.string.firebase_db_xp)), mCompanyRef.child(getString(R.string.firebase_db_perk_points)), null, mCompanyXPProgressBar);
        mCompanyRef.child(getString(R.string.firebase_db_xp)).addValueEventListener(companyXpEventListener);

        // set listener for company's level (FOREVER!, since it will be changing!)
        companyLevelEventListener = new BigIntegerEventListener(mCompanyLevelTextView, getString(R.string.company_level));
        mCompanyRef.child(getString(R.string.firebase_db_level)).addValueEventListener(companyLevelEventListener);

        // set listener for company's perk points (FOREVER!, since it will be changing!)
        perkPointsEventListener = new BigIntegerEventListener(mPerkPointsTextView, getString(R.string.perk_points));
        mCompanyRef.child(getString(R.string.firebase_db_perk_points)).addValueEventListener(perkPointsEventListener);

        // create currency per sec runnable thread
        currencyPerSecRunnable = new CurrencyPerSecRunnable(currencyPerSecEventListener, mCompanyRef.child(getString(R.string.firebase_db_currency)),
                mCompanyRef.child(getString(R.string.firebase_db_xp)));
        currencyPerSecThread = new Thread(currencyPerSecRunnable);
        currencyPerSecThread.start();

        // get the user ref using the username!
        mUserRef = mUsersRef.child(username);

        // set listener for user's xp (FOREVER!, since it will be changing!)
        userXpEventListener = new XPEventListener(mUserXPTextView, mUserRef.child(getString(R.string.firebase_db_level)),
                mUserRef.child(getString(R.string.firebase_db_xp)), null, mUserRef.child(getString(R.string.firebase_db_currency_per_click)), mUserXPProgressBar);
        mUserRef.child(getString(R.string.firebase_db_xp)).addValueEventListener(userXpEventListener);

        // set listener for company's level (FOREVER!, since it will be changing!)
        userLevelEventListener = new BigIntegerEventListener(mUserLevelTextView, username + "level : %s");
        mUserRef.child(getString(R.string.firebase_db_level)).addValueEventListener(userLevelEventListener);

        // set listener for company's user's currency per click (FOREVER!, since it will be changing!)
        currencyPerClickEventListener = new BigIntegerEventListener(mCurrencyPerClickTextView, getString(R.string.currency_per_click));
        mUserRef.child(getString(R.string.firebase_db_currency_per_click)).addValueEventListener(currencyPerClickEventListener);

        hideProgressDialog();
    }

    private void incrementCurrencyFromClick() {
        Log.d("CurrentPlayerInClicker", Games.Players.getCurrentPlayer(mGoogleGamesApiClient).getDisplayName());
        mCompanyRef.child(getString(R.string.firebase_db_currency)).runTransaction(new IncreaseCurrencyTransactionHandler(currencyPerClickEventListener, mUserRef.child(getString(R.string.firebase_db_xp))));
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

    @Override
    protected void onPause() {
        super.onPause();
        currencyPerSecThread.interrupt();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
