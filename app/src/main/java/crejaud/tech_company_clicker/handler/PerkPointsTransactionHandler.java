package crejaud.tech_company_clicker.handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.math.BigInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by creja_000 on 7/27/2016.
 */

public class PerkPointsTransactionHandler implements Transaction.Handler {

    private ReentrantLock lock;

    public PerkPointsTransactionHandler(ReentrantLock lock) {
        this.lock = lock;
    }

    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        if (mutableData.getValue() == null) {
            // wot? set to 0
            mutableData.setValue("0");
        }
        else {
            BigInteger newCurrency = new BigInteger(mutableData.getValue(String.class));
            try {
                newCurrency = newCurrency.add(new BigInteger("1"));
            } catch (ArithmeticException e) {
                // Long overflows above max big int
            }

            mutableData.setValue(newCurrency.toString());
        }
        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        lock.unlock();
    }

}
