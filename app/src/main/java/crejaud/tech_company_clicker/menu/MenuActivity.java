package crejaud.tech_company_clicker.menu;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import crejaud.tech_company_clicker.R;
import crejaud.tech_company_clicker.clicker.ClickerActivity;
import crejaud.tech_company_clicker.listener.ClickEventListener;
import crejaud.tech_company_clicker.listener.CompanyNameKeyListener;
import crejaud.tech_company_clicker.listener.CurrencyEventListener;
import crejaud.tech_company_clicker.signIn.BaseActivity;
import crejaud.tech_company_clicker.signIn.SignInActivity;

public class MenuActivity extends BaseActivity implements
        View.OnClickListener{

    private static int REQUEST_ACHIEVEMENTS = 9004;
    private static int REQUEST_LEADERBOARD = 9005;

    private TextView companyNameTextView, currencyTextView;

    private CurrencyEventListener currencyEventListener;
    private ClickEventListener clickEventListener;

    private Button playButton, inviteUsersButton, leaveCompanyButton, createNewCompanyButton, joinCompanyButton;

    private String firebaseUid, companyName;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCompaniesRef = null;
    private DatabaseReference mUsersRef;
    private DatabaseReference mCompanyRef;

    private GoogleApiClient mGoogleGamesApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // show progress dialog, while loading everything
        showProgressDialog();

        mGoogleGamesApiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        mGoogleGamesApiClient.connect();

        setAds();

        // Set up firebase references
        mCompaniesRef = mRootRef.child(getResources().getString(R.string.firebase_db_companies));
        mUsersRef = mRootRef.child(getResources().getString(R.string.firebase_db_users));

        // Views
        companyNameTextView = (TextView) findViewById(R.id.company_name_text);
        currencyTextView = (TextView) findViewById(R.id.currency_text);

        // Buttons
        playButton = (Button) findViewById(R.id.play_button);
        inviteUsersButton = (Button) findViewById(R.id.invite_users_to_company_button);
        leaveCompanyButton = (Button) findViewById(R.id.leave_company_button);
        createNewCompanyButton = (Button) findViewById(R.id.create_new_company_button);
        joinCompanyButton = (Button) findViewById(R.id.join_company_button);

        playButton.setEnabled(false);
        inviteUsersButton.setEnabled(false);
        leaveCompanyButton.setEnabled(false);
        createNewCompanyButton.setEnabled(false);
        joinCompanyButton.setEnabled(false);

        // Button listeners
        playButton.setOnClickListener(this);
        createNewCompanyButton.setOnClickListener(this);
        inviteUsersButton.setOnClickListener(this);
        leaveCompanyButton.setOnClickListener(this);
        joinCompanyButton.setOnClickListener(this);
        findViewById(R.id.achievements_button).setOnClickListener(this);
        findViewById(R.id.leaderboards_button).setOnClickListener(this);

        setUpTexts();
    }

    private void setAds() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setUpTexts() {
        // Get Uid
        firebaseUid = getIntent().getExtras().getString(getString(R.string.intent_extra_unique_id));

        // get company name from unique firebase id (ONCE!)
        mUsersRef.child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // user does not belong to a company!
                if (dataSnapshot.getValue() == null) {
                    // enable create new company button
                    createNewCompanyButton.setEnabled(true);
                    // enable join company button
                    joinCompanyButton.setEnabled(true);

                    companyNameTextView.setText(getString(R.string.company, "-----"));
                    currencyTextView.setText(getString(R.string.currency, "0"));

                    hideProgressDialog();
                    return;
                }

                // enable play button
                playButton.setEnabled(true);
                // enable invite button
                inviteUsersButton.setEnabled(true);
                // enable leave company button
                leaveCompanyButton.setEnabled(true);

                // got company name!
                companyName = dataSnapshot.getValue(String.class);

                // set Company Name Text
                companyNameTextView.setText(getString(R.string.company, companyName));

                // get the company ref using the company name!
                mCompanyRef = mCompaniesRef.child(companyName);

                currencyEventListener = new CurrencyEventListener(currencyTextView, getApplicationContext());

                // set listener for company's currency (FOREVER!, since it will be changing!)
                mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

                clickEventListener = new ClickEventListener();

                // set listener for company's currency per second (FOREVER!, since it will be changing!)
                mCompanyRef.child(getResources().getString(R.string.firebase_db_currency_per_click)).addValueEventListener(clickEventListener);

                // done loading everything, can now hide progress dialog
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void play() {
        Intent clickerIntent = new Intent(this, ClickerActivity.class);
        clickerIntent.putExtra(getString(R.string.intent_extra_unique_id), firebaseUid);
        clickerIntent.putExtra(getString(R.string.intent_extra_company_name), companyName);
        startActivityForResult(clickerIntent, SignInActivity.RC_SIGN_OUT);
    }

    private void newCompanyAlert() {
        // Step 1: Get new company name from user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a new company!");
        builder.setMessage("Please choose a valid company name.");

        // Set up the input
        final EditText inputCompanyName = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputCompanyName.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputCompanyName);

        // Set up the buttons
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show progress dialog, while loading everything
                showProgressDialog();

                String newCompanyName = inputCompanyName.getText().toString();
                // step 2: create new company using new company name
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

        // set Company Name Text
        companyNameTextView.setText(getString(R.string.company, companyName));

        //set new company name for user
        mUsersRef.child(firebaseUid).setValue(companyName);

        mCompanyRef = mCompaniesRef.child(companyName);

        // set the currency of the company to 0 (default)
        mCompanyRef.child(getString(R.string.firebase_db_currency)).setValue("0");

        // set the currency per second to 1 (default)
        mCompanyRef.child(getString(R.string.firebase_db_currency_per_click)).setValue("1");

        // set the user of the company in the company's users list
        mCompanyRef.child(getString(R.string.firebase_db_users)).child(firebaseUid).setValue(true);

        currencyEventListener = new CurrencyEventListener(currencyTextView, this);

        // set listener for company's currency (FOREVER!, since it will be changing!)
        mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

        clickEventListener = new ClickEventListener();

        // set listener for company's currency per second (FOREVER!, since it will be changing!)
        mCompanyRef.child(getString(R.string.firebase_db_currency_per_click)).addValueEventListener(clickEventListener);

        // disable create company button
        createNewCompanyButton.setEnabled(false);
        // disable join company button
        joinCompanyButton.setEnabled(false);
        // enable play button
        playButton.setEnabled(true);
        // enable invite button
        inviteUsersButton.setEnabled(true);
        // enable leave company button
        leaveCompanyButton.setEnabled(true);

        hideProgressDialog();
    }

    private void inviteUsers() {

    }

    private void joinCompany() {

    }

    private void leaveCompany() {
        // Step 1: Get new company name from user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to leave your company?");
        builder.setMessage("If you're the only player in this company, it will get deleted!");

        // Set up the buttons
        builder.setPositiveButton("LEAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if only player in company, then delete company
                // otherwise, keep company
                showProgressDialog();
                // remove player from users list
                mCompanyRef.child(getString(R.string.firebase_db_users)).child(firebaseUid).removeValue();
                // set company of user to null
                mUsersRef.child(firebaseUid).removeValue();

                // check to see if users list is null or not
                mCompanyRef.child(getString(R.string.firebase_db_users)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // if users list is empty, then delete company
                        if (dataSnapshot.getValue() == null) {
                            mCompanyRef.child(getString(R.string.firebase_db_currency)).removeEventListener(currencyEventListener);
                            mCompanyRef.removeValue();
                            mCompanyRef = null;

                            companyNameTextView.setText(getString(R.string.company, "-----"));
                            currencyTextView.setText(getString(R.string.currency, "0"));

                            // enable create company button
                            createNewCompanyButton.setEnabled(true);
                            // enable join company button
                            joinCompanyButton.setEnabled(true);
                            // disable play button
                            playButton.setEnabled(false);
                            // disable invite button
                            inviteUsersButton.setEnabled(false);
                            // disable leave company button
                            leaveCompanyButton.setEnabled(false);
                        }

                        hideProgressDialog();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.show();
    }

    private void goToAchievements() {
        startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleGamesApiClient),
                REQUEST_ACHIEVEMENTS);
    }

    private void goToLeaderboards() {
        Games.Leaderboards.submitScore(mGoogleGamesApiClient, getString(R.string.leaderboards_player_level_id), 1337);
        startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleGamesApiClient), REQUEST_LEADERBOARD);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SignInActivity.RC_SIGN_OUT) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode);
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_button:
                play();
                break;
            case R.id.create_new_company_button:
                newCompanyAlert();
                break;
            case R.id.invite_users_to_company_button:
                inviteUsers();
                break;
            case R.id.join_company_button:
                joinCompany();
                break;
            case R.id.leave_company_button:
                leaveCompany();
                break;
            case R.id.achievements_button:
                goToAchievements();
                break;
            case R.id.leaderboards_button:
                goToLeaderboards();
                break;
        }
    }
}