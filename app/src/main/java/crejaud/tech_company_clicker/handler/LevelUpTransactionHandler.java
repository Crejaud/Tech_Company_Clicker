package crejaud.tech_company_clicker.handler;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Logger;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.math.BigInteger;

/**
 * Created by creja_000 on 7/26/2016.
 */

public class LevelUpTransactionHandler implements Transaction.Handler {

    private DatabaseReference xpRef, xpToNextLevelRef;
    private BigInteger constant = new BigInteger("20");

    public LevelUpTransactionHandler(DatabaseReference xpRef, DatabaseReference xpToNextLevelRef) {
        this.xpRef = xpRef;
        this.xpToNextLevelRef = xpToNextLevelRef;
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
            xpRef.setValue("0");

            // set xp to next level!
            BigInteger xpToNextLevel = level.pow(2).divide(constant);
            xpToNextLevelRef.setValue(xpToNextLevel.toString());
        }

        return Transaction.success(mutableData);
    }

    @Override
    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

    }
}

