package com.crejaud.tech_company_clicker.listener;

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

import com.crejaud.tech_company_clicker.R;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class BigIntegerEventListener implements ValueEventListener {

    private String prefix;
    private BigInteger num;
    private TextView numTextView;

    public BigIntegerEventListener(TextView numTextView, String prefix) {
        this.num = new BigInteger("0");
        this.numTextView = numTextView;
        this.prefix = prefix;
    }

    public BigInteger getNum() {
        return num;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
            num = new BigInteger("0");
        }
        else {
            num = new BigInteger(dataSnapshot.getValue(String.class));
        }
        numTextView.setText(String.format(prefix, String.format(Locale.US, "%,d", num)));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // uhhhh?
    }
}