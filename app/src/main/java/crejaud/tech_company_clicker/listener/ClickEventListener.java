package crejaud.tech_company_clicker.listener;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class ClickEventListener implements ValueEventListener {

    private BigInteger currencyPerClick;

    public ClickEventListener() {
        // initially 1 until it can receive the currencyPerClick
        this.currencyPerClick = new BigInteger("1");
    }

    public BigInteger getCurrencyPerClick() {
        return currencyPerClick;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
            currencyPerClick = new BigInteger("1");
        }
        else {
            currencyPerClick = new BigInteger(dataSnapshot.getValue(String.class));
        }
        Log.d("Currency per click", currencyPerClick + "");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // uhhhh?
    }
}
