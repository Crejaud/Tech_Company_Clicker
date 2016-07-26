package crejaud.tech_company_clicker.listener;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.util.Locale;

import crejaud.tech_company_clicker.R;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class CurrencyEventListener implements ValueEventListener {

    private Context ctx;
    private BigInteger currency;
    private TextView currencyTextView;

    public CurrencyEventListener(TextView currencyTextView, Context ctx) {
        // initially 0 until it can receive the currencyPerClick
        this.currency = new BigInteger("0");
        this.currencyTextView = currencyTextView;
        this.ctx = ctx;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
            currency = new BigInteger("0");
        }
        else {
            currency = new BigInteger(dataSnapshot.getValue(String.class));
        }
        Log.d("Currency!", currency + "");
        currencyTextView.setText(ctx.getResources().getString(R.string.currency, String.format(Locale.US, "%,d", currency)));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // uhhhh?
    }
}