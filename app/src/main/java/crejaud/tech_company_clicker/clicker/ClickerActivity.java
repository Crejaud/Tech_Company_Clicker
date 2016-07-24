package crejaud.tech_company_clicker.clicker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import crejaud.tech_company_clicker.R;
import crejaud.tech_company_clicker.global.AuthApplication;

public class ClickerActivity extends AppCompatActivity implements
        View.OnClickListener {

    private TextView mCurrencyTextView;
    private long currencyPerSec;

    private DatabaseReference mRoofRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mCurrencyRef = mRoofRef.child("currency");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicker);

        // Views
        mCurrencyTextView = (TextView) findViewById(R.id.currency_text);

        // Button listeners
        findViewById(R.id.clicker_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        // get currency per second
        currencyPerSec = 1;

    }

    @Override
    protected void onStart() {
        super.onStart();

        mCurrencyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long currency = dataSnapshot.getValue(Long.class);
                mCurrencyTextView.setText(String.format(currency.toString()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // uhhhh?
            }
        });
    }

    private void incrementCurrency() {
        mCurrencyRef.runTransaction(new Transaction.Handler() {
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
