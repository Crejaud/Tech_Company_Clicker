package crejaud.tech_company_clicker.handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Logger;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.math.BigInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by creja_000 on 7/26/2016.
 */

public class LevelUpTransactionHandler implements Transaction.Handler {

    private DatabaseReference xpRef, perkPointsRef, currencyPerClickRef;
    private BigInteger constant = new BigInteger("100");
    private ReentrantLock lock;

    public LevelUpTransactionHandler(DatabaseReference xpRef, DatabaseReference perkPointsRef, DatabaseReference currencyPerClickRef, ReentrantLock lock) {
        this.xpRef = xpRef;
        this.perkPointsRef = perkPointsRef;
        this.currencyPerClickRef = currencyPerClickRef;
        this.lock = lock;
    }

    @Override
    public Transaction.Result doTransaction(MutableData mutableData) {
        if (mutableData.getValue() == null) {
            mutableData.setValue("1");
        }
        else {
            BigInteger level = new BigInteger(mutableData.getValue(String.class));
            try {
                // level up!
                level = level.add(new BigInteger("1"));
            } catch (ArithmeticException e) {
                // Long overflows above max big int
            }

            mutableData.setValue(level.toString());

            // set xp to 0!
            // set xp to next level!
            xpRef.setValue("0/" + level.pow(2).multiply(constant).toString());

            // increment perk points if this is for a company
            if (perkPointsRef != null) {
                perkPointsRef.runTransaction(new PerkPointsTransactionHandler(lock));
            }
            // increment currency per click
            else {
                currencyPerClickRef.runTransaction(new IncreaseCurrencyPerClickTransactionHandler(lock));
            }
        }

        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

    }
}

