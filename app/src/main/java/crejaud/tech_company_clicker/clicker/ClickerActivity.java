package crejaud.tech_company_clicker.clicker;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import crejaud.tech_company_clicker.R;

public class ClickerActivity extends AppCompatActivity implements
        View.OnClickListener {

    private TextView mCurrencyTextView;
    private Long currencyPerSec;

    private String firebaseUid;

    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCompanyRef;

    private String company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicker);

        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.banner_ad_app_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Get Uid
        firebaseUid = getIntent().getExtras().getString("unique_id");

        // Create company "Yo"
        mRootRef.child("users").child(firebaseUid).setValue("Yo");

        // find user's company
        mRootRef.child("users").child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                company = dataSnapshot.getValue(String.class);
                Log.d("Found Company", company);
                mCompanyRef = mRootRef.child("companies").child(company);
                mCompanyRef.child("currency").setValue(0L);
                mCompanyRef.child("currencyPerSec").setValue(1L);
                mCompanyRef.child("users").child(firebaseUid).setValue(true);

                mCompanyRef.child("currency").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long currency = dataSnapshot.getValue(Long.class);
                        Log.d("Currency!", currency + "");
                        mCurrencyTextView.setText(String.format(currency.toString()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // uhhhh?
                    }
                });

                mCompanyRef.child("currencyPerSec").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currencyPerSec = dataSnapshot.getValue(Long.class);
                        Log.d("Currency per sec", currencyPerSec + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // uhhhh?
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Views
        mCurrencyTextView = (TextView) findViewById(R.id.currency_text);

        // Button listeners
        findViewById(R.id.clicker_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

    }

    private void incrementCurrency() {
        mCompanyRef.child("currency").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(0L);
                }
                else {
                    Long newCurrency = (Long) mutableData.getValue();
                    try {
                        newCurrency += currencyPerSec;
                    } catch (ArithmeticException e) {
                        // Long overflows above Long.MAX_VALUE
                        // Scale
                    }

                    mutableData.setValue(newCurrency);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                //uhhh?
            }
        });
    }

    private void signOut() {
        Log.d("LOGIN", "Sign out clicked!!!");
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clicker_button:
                incrementCurrency();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
        }
    }
}
