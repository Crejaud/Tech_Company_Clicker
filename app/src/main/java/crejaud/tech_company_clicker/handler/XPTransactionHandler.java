package crejaud.tech_company_clicker.handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.math.BigInteger;

import crejaud.tech_company_clicker.listener.ClickEventListener;

/**
 * Created by creja_000 on 7/27/2016.
 */

public class XPTransactionHandler implements Transaction.Handler {

    private BigInteger xpGain;

    public XPTransactionHandler(BigInteger xpGain) {
        this.xpGain = xpGain;
    }

    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        if (mutableData.getValue() == null) {
            mutableData.setValue("0");
        }
        else {
            BigInteger currentXp = new BigInteger(mutableData.getValue(String.class).split("/")[0]);
            try {
                currentXp = currentXp.add(xpGain);
            } catch (ArithmeticException e) {
                // Long overflows above max big int
            }

            mutableData.setValue(currentXp.toString() + "/" + mutableData.getValue(String.class).split("/")[1]);
        }
        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        //uhhh?
    }

}
