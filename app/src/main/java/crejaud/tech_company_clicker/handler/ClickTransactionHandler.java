package crejaud.tech_company_clicker.handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.math.BigInteger;

import crejaud.tech_company_clicker.listener.ClickEventListener;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class ClickTransactionHandler implements Transaction.Handler {

    private ClickEventListener clickEventListener;
    private DatabaseReference mUserXPRef;

    public ClickTransactionHandler(ClickEventListener clickEventListener, DatabaseReference mUserXPRef) {
        this.clickEventListener = clickEventListener;
        this.mUserXPRef = mUserXPRef;
    }

    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        if (mutableData.getValue() == null) {
            mutableData.setValue("0");
        }
        else {
            BigInteger newCurrency = new BigInteger(mutableData.getValue(String.class));
            try {
                newCurrency = newCurrency.add(clickEventListener.getCurrencyPerClick());
                mUserXPRef.runTransaction(new XPTransactionHandler(clickEventListener.getCurrencyPerClick()));
            } catch (ArithmeticException e) {
                // Long overflows above max big int
            }

            mutableData.setValue(newCurrency.toString());
        }
        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        //uhhh?
    }
}
