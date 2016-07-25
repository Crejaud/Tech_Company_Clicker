package crejaud.tech_company_clicker.clicker;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import crejaud.tech_company_clicker.R;
import crejaud.tech_company_clicker.handler.ClickTransactionHandler;
import crejaud.tech_company_clicker.listener.ClickEventListener;
import crejaud.tech_company_clicker.listener.CompanyNameKeyListener;
import crejaud.tech_company_clicker.listener.CurrencyEventListener;
import crejaud.tech_company_clicker.signIn.BaseActivity;

public class ClickerActivity extends BaseActivity implements
        View.OnClickListener {

    private TextView mCurrencyTextView;

    // Listeners
    private ClickEventListener clickEventListener;
    private CurrencyEventListener currencyEventListener;

    private String firebaseUid, companyName;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCompaniesRef = null;
    private DatabaseReference mUsersRef;
    private DatabaseReference mCompanyRef;

    private GoogleApiClient mGoogleGamesApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicker);

        mGoogleGamesApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleGamesApiClient.connect();

        // Set up firebase references
        mCompaniesRef = mRootRef.child(getResources().getString(R.string.firebase_db_companies));
        mUsersRef = mRootRef.child(getResources().getString(R.string.firebase_db_users));

        setAds();

        // Views
        mCurrencyTextView = (TextView) findViewById(R.id.currency_text);

        // Button listeners
        findViewById(R.id.clicker_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.new_company_button).setOnClickListener(this);

        // Set up the firebase listeners
        setFirebaseListeners();

    }

    private void setAds() {
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setFirebaseListeners() {
        // Get Uid
        firebaseUid = getIntent().getExtras().getString(getResources().getString(R.string.intent_extra_unique_id));

        // Create company "Yo"
        // mUsersRef.child(firebaseUid).setValue("Yo");

        // get company name from unique firebase id (ONCE!)
        mUsersRef.child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // user does not belong to a company!
                if (dataSnapshot.getValue() == null) {
                    Log.d("Found Company", "FALSE");
                    newCompanyAlert();
                }
                // got company name!
                companyName = dataSnapshot.getValue(String.class);
                Log.d("Found Company", companyName);

                // get the company ref using the company name!
                mCompanyRef = mCompaniesRef.child(companyName);

                // ensure the user of the company is in the company's users list
                mCompanyRef.child(getResources().getString(R.string.firebase_db_users)).child(firebaseUid).setValue(true);

                currencyEventListener = new CurrencyEventListener(mCurrencyTextView);

                // set listener for company's currency (FOREVER!, since it will be changing!)
                mCompanyRef.child(getResources().getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

                clickEventListener = new ClickEventListener();

                // set listener for company's currency per second (FOREVER!, since it will be changing!)
                mCompanyRef.child(getResources().getString(R.string.firebase_db_currency_per_click)).addValueEventListener(clickEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void incrementCurrencyFromClick() {
        Log.d("CurrentPlayerInClicker", Games.Players.getCurrentPlayer(mGoogleGamesApiClient).getDisplayName());
        mCompanyRef.child(getResources().getString(R.string.firebase_db_currency)).runTransaction(new ClickTransactionHandler(clickEventListener));
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


    private void newCompanyAlert() {
        // Step 1: Get new company name from user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a new company!");
        builder.setMessage("This will delete your currency company!!!");

        // Set up the input
        final EditText inputCompanyName = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputCompanyName.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputCompanyName);

        // Set up the buttons
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newCompanyName = inputCompanyName.getText().toString();
                // step 2: delete current company
                // remove event listener for clicking
                if (mCompanyRef != null) {
                    mCompanyRef.child(getResources().getString(R.string.firebase_db_currency)).removeEventListener(currencyEventListener);
                    mCompanyRef.child(getResources().getString(R.string.firebase_db_currency_per_click)).removeEventListener(clickEventListener);
                    // set the company to null (basically deleting the company)
                    mCompanyRef.setValue(null);
                }
                // step 3: create new company using new company name
                createCompany(newCompanyName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.show();

        // initially disabled
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // default text color to be red
        inputCompanyName.setTextColor(Color.RED);

        // set key listener for edit text
        inputCompanyName.setOnKeyListener(new CompanyNameKeyListener(mCompaniesRef, alertDialog));
    }

    private void createCompany(String newCompanyName) {
        // get new company name
        companyName = newCompanyName;

        //set new company name for user
        mUsersRef.child(firebaseUid).setValue(companyName);

        mCompanyRef = mCompaniesRef.child(companyName);

        // set the currency of the company to 0 (default)
        mCompanyRef.child(getResources().getString(R.string.firebase_db_currency)).setValue("0");

        // set the currency per second to 1 (default)
        mCompanyRef.child(getResources().getString(R.string.firebase_db_currency_per_click)).setValue("1");

        // set the user of the company in the company's users list
        mCompanyRef.child(getResources().getString(R.string.firebase_db_users)).child(firebaseUid).setValue(true);

        currencyEventListener = new CurrencyEventListener(mCurrencyTextView);

        // set listener for company's currency (FOREVER!, since it will be changing!)
        mCompanyRef.child(getResources().getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

        clickEventListener = new ClickEventListener();

        // set listener for company's currency per second (FOREVER!, since it will be changing!)
        mCompanyRef.child(getResources().getString(R.string.firebase_db_currency_per_click)).addValueEventListener(clickEventListener);
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
            case R.id.new_company_button:
                newCompanyAlert();
                break;
        }
    }
}
