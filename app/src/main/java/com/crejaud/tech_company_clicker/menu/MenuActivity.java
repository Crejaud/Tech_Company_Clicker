package com.crejaud.tech_company_clicker.menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.crejaud.tech_company_clicker.R;
import com.crejaud.tech_company_clicker.clicker.ClickerActivity;
import com.crejaud.tech_company_clicker.listener.NameFinderKeyListener;
import com.crejaud.tech_company_clicker.listener.BigIntegerEventListener;
import com.crejaud.tech_company_clicker.signIn.BaseActivity;
import com.crejaud.tech_company_clicker.signIn.SignInActivity;

public class MenuActivity extends BaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static int REQUEST_ACHIEVEMENTS = 9004;
    private static int REQUEST_LEADERBOARD = 9005;
    private static int RC_SIGN_IN = 9006;

    private static final String TAG = "MainMenu";

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = true;

    private TextView companyNameTextView, currencyTextView, companyLevelTextView;

    private BigIntegerEventListener currencyEventListener, companyLevelEventListener;

    private Button playButton, inviteUsersButton, leaveCompanyButton, createNewCompanyButton, joinCompanyButton;

    private String firebaseUid, companyName, username;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCompaniesRef;
    private DatabaseReference mCompanyRef;
    private DatabaseReference mUsersRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mUidToUsernameRef;

    private GoogleApiClient mGoogleGamesApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // show progress dialog, while loading everything
        showProgressDialog();

        // connect to google play games api client
        mGoogleGamesApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mGoogleGamesApiClient.connect();

        // set ad banner
        setAds();

        // Set up firebase references
        mCompaniesRef = mRootRef.child(getResources().getString(R.string.firebase_db_companies));
        mUsersRef = mRootRef.child(getResources().getString(R.string.firebase_db_users));
        mUidToUsernameRef = mRootRef.child(getString(R.string.firebase_db_uid_to_username));

        // Views
        companyNameTextView = (TextView) findViewById(R.id.company_name_text);
        currencyTextView = (TextView) findViewById(R.id.currency_text);
        companyLevelTextView = (TextView) findViewById(R.id.company_level_text);

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

        getUserInfo();
    }

    private void setAds() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void getUserInfo() {
        // Get Uid
        firebaseUid = getIntent().getExtras().getString(getString(R.string.intent_extra_unique_id));

        mRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot uidToUsernameDataSnapshot = dataSnapshot.child(getString(R.string.firebase_db_uid_to_username)).child(firebaseUid);
                // this is the user's first time playing!
                if (uidToUsernameDataSnapshot.getValue() == null) {
                    // prompt user to create username
                    hideProgressDialog();
                    promptUsernameCreation();
                    return;
                }

                // the user exists
                username = uidToUsernameDataSnapshot.getValue(String.class);
                // set the user ref to the username
                mUserRef = mUsersRef.child(username);

                DataSnapshot userDataSnapshot = dataSnapshot.child(getString(R.string.firebase_db_users)).child(username);
                DataSnapshot companyDataSnapshot = userDataSnapshot.child(getString(R.string.firebase_db_company));

                // user has no company!
                // user does not belong to a company!
                if (companyDataSnapshot.getValue() == null) {
                    // enable create new company button
                    createNewCompanyButton.setEnabled(true);
                    // enable join company button
                    joinCompanyButton.setEnabled(true);

                    companyNameTextView.setText(getString(R.string.company, "-----"));
                    currencyTextView.setText(getString(R.string.currency, "-"));
                    companyLevelTextView.setText(getString(R.string.company_level, "-"));

                    hideProgressDialog();
                    return;
                }

                // user has a company!

                // enable play button
                playButton.setEnabled(true);
                // enable invite button
                inviteUsersButton.setEnabled(true);
                // enable leave company button
                leaveCompanyButton.setEnabled(true);

                // got company name!
                companyName = companyDataSnapshot.getValue(String.class);

                // set Company Name Text
                companyNameTextView.setText(getString(R.string.company, companyName));

                // get the company ref using the company name!
                mCompanyRef = mCompaniesRef.child(companyName);

                // create currency listener
                currencyEventListener = new BigIntegerEventListener(currencyTextView, getApplicationContext().getString(R.string.currency));

                // set listener for company's currency (FOREVER!, since it will be changing!)
                mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

                // create company level listener
                companyLevelEventListener = new BigIntegerEventListener(companyLevelTextView, getApplication().getString(R.string.company_level));

                // set listener for company's level (FOREVER!, since it will be changing!)
                mCompanyRef.child(getResources().getString(R.string.firebase_db_level)).addValueEventListener(companyLevelEventListener);

                // done loading everything, can now hide progress dialog
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void promptUsernameCreation() {
        // Step 1: Get new username name from user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create your username!");
        builder.setMessage("This is how other players will be see you!");

        // Set up the input
        final EditText inputUsername = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputUsername.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputUsername);

        // Set up the buttons
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show progress dialog, while loading everything
                showProgressDialog();

                String newUsername = inputUsername.getText().toString();
                // step 2: create username!
                createUsername(newUsername);
            }
        });

        AlertDialog alertDialog = builder.show();

        // initially disabled
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // default text color to be red
        inputUsername.setTextColor(Color.RED);

        // set key listener for edit text
        inputUsername.addTextChangedListener(new NameFinderKeyListener(inputUsername, mUsersRef, alertDialog, false));
    }

    private void createUsername(String newUsername) {
        username = newUsername;

        // add uid to username linking
        mUidToUsernameRef.child(firebaseUid).setValue(username);

        // add user to users list!
        // get user ref
        mUserRef = mUsersRef.child(username);
        // set currency per click to 1
        mUserRef.child(getString(R.string.firebase_db_currency_per_click)).setValue("1");
        // set user level to 1
        mUserRef.child(getString(R.string.firebase_db_level)).setValue("1");
        // set XP to 0
        // set XPtoLevel to 100
        mUserRef.child(getString(R.string.firebase_db_xp)).setValue("0/100");

        // get company properties and set up text views (it's ok if there is no company)
        getUserInfo();
    }

    private void play() {
        Intent clickerIntent = new Intent(this, ClickerActivity.class);
        clickerIntent.putExtra(getString(R.string.intent_extra_unique_id), firebaseUid);
        clickerIntent.putExtra(getString(R.string.intent_extra_company_name), companyName);
        clickerIntent.putExtra(getString(R.string.intent_extra_username), username);
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
        inputCompanyName.addTextChangedListener(new NameFinderKeyListener(inputCompanyName, mCompaniesRef, alertDialog, false));
    }

    private void createCompany(String newCompanyName) {
        // get new company name
        companyName = newCompanyName;

        // set Company Name Text
        companyNameTextView.setText(getString(R.string.company, companyName));

        //set new company name for user
        mUsersRef.child(username).child(getString(R.string.firebase_db_company)).setValue(companyName);

        mCompanyRef = mCompaniesRef.child(companyName);

        // set the currency of the company to 0 (default)
        mCompanyRef.child(getString(R.string.firebase_db_currency)).setValue("0");

        // set the currency per second to 0 (default)
        mCompanyRef.child(getString(R.string.firebase_db_currency_per_sec)).setValue("0");

        // set the level to 1 (default)
        mCompanyRef.child(getString(R.string.firebase_db_level)).setValue("1");

        // set XP to 0 (default)
        // set XP cap to 100 (default)
        mCompanyRef.child(getString(R.string.firebase_db_xp)).setValue("0/100");

        // set perk points to 0 (default)
        mCompanyRef.child(getString(R.string.firebase_db_perk_points)).setValue("0");

        // set the user of the company in the company's users list
        mCompanyRef.child(getString(R.string.firebase_db_users)).child(username).setValue(true);

        currencyEventListener = new BigIntegerEventListener(currencyTextView, getString(R.string.currency));

        // set listener for company's currency (FOREVER!, since it will be changing!)
        mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

        companyLevelEventListener = new BigIntegerEventListener(companyLevelTextView, getApplication().getString(R.string.company_level));

        // get the company level
        mCompanyRef.child(getResources().getString(R.string.firebase_db_level)).addValueEventListener(companyLevelEventListener);

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
        // Step 1: Get new company name from user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite a player to your company!");
        builder.setMessage("Please add a valid username.");

        // Set up the input
        final EditText inputUsername = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputUsername.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputUsername);

        // Set up the buttons
        builder.setPositiveButton("INVITE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show progress dialog, while loading everything
                showProgressDialog();

                String invitedUser = inputUsername.getText().toString();
                // Step 2: invite the user
                // TODO
                // TEMPORARY
                // force user into company
                mCompanyRef.child(getString(R.string.firebase_db_users)).child(invitedUser).setValue(true);
                mUsersRef.child(invitedUser).child(getString(R.string.firebase_db_company)).setValue(companyName);

                hideProgressDialog();
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
        inputUsername.setTextColor(Color.RED);

        // set key listener for edit text
        inputUsername.addTextChangedListener(new NameFinderKeyListener(inputUsername, mUsersRef, alertDialog, true));
    }

    private void joinCompany() {
        // Step 1: Get new company name from user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join an existing company!");
        builder.setMessage("Please find a valid company name.");

        // Set up the input
        final EditText inputCompanyName = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        inputCompanyName.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(inputCompanyName);

        // Set up the buttons
        builder.setPositiveButton("JOIN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show progress dialog, while loading everything
                showProgressDialog();

                String newCompanyName = inputCompanyName.getText().toString();
                // step 2: join company using new company name
                // TODO
                // TEMPORARY
                // FORCE user into company
                mCompaniesRef.child(newCompanyName).child(getString(R.string.firebase_db_users)).child(username).setValue(true);
                mUserRef.child(getString(R.string.firebase_db_company)).setValue(newCompanyName);

                // get new company name
                companyName = newCompanyName;

                // set Company Name Text
                companyNameTextView.setText(getString(R.string.company, companyName));

                // get the company ref using the company name!
                mCompanyRef = mCompaniesRef.child(companyName);

                // create currency listener
                currencyEventListener = new BigIntegerEventListener(currencyTextView, getApplicationContext().getString(R.string.currency));

                // set listener for company's currency (FOREVER!, since it will be changing!)
                mCompanyRef.child(getString(R.string.firebase_db_currency)).addValueEventListener(currencyEventListener);

                // create company level listener
                companyLevelEventListener = new BigIntegerEventListener(companyLevelTextView, getApplication().getString(R.string.company_level));

                // set listener for company's level (FOREVER!, since it will be changing!)
                mCompanyRef.child(getResources().getString(R.string.firebase_db_level)).addValueEventListener(companyLevelEventListener);

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
        inputCompanyName.addTextChangedListener(new NameFinderKeyListener(inputCompanyName, mCompaniesRef, alertDialog, true));
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
                mCompanyRef.child(getString(R.string.firebase_db_users)).child(username).removeValue();
                // set company of user to null
                mUserRef.child(getString(R.string.firebase_db_company)).removeValue();

                // check to see if users list is null or not
                mCompanyRef.child(getString(R.string.firebase_db_users)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // if users list is empty, then delete company
                        if (dataSnapshot.getValue() == null) {
                            mCompanyRef.child(getString(R.string.firebase_db_currency)).removeEventListener(currencyEventListener);
                            mCompanyRef.removeValue();
                            mCompanyRef = null;
                        }

                        companyNameTextView.setText(getString(R.string.company, "-----"));
                        currencyTextView.setText(getString(R.string.currency, "-"));
                        companyLevelTextView.setText(getString(R.string.company_level, "-"));

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

        builder.show();
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
                mGoogleGamesApiClient.disconnect();
                setResult(resultCode);
                finish();
            }
        }

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                    + resultCode + ", intent=" + data);
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleGamesApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this,requestCode,resultCode, R.string.signin_other_error);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, mGoogleGamesApiClient.isConnected()+ "");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleGamesApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleGamesApiClient,
                    connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
        }
    }
}
