package crejaud.tech_company_clicker.handler;

import android.util.Log;

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

public class IncreaseCurrencyPerClickTransactionHandler implements Transaction.Handler {

    private ReentrantLock lock;

    public IncreaseCurrencyPerClickTransactionHandler(ReentrantLock lock) {
        this.lock = lock;
    }

    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        if (mutableData.getValue() == null) {
            mutableData.setValue("0");
        }
        else {
            BigInteger newCurrencyPerClick = new BigInteger(mutableData.getValue(String.class));
            try {
                newCurrencyPerClick = newCurrencyPerClick.add(new BigInteger("4"));
            } catch (ArithmeticException e) {
                // Long overflows above max big int
            }

            mutableData.setValue(newCurrencyPerClick.toString());
        }
        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
        lock.unlock();
        Log.d("Lock", "Unlocked");
    }
}
