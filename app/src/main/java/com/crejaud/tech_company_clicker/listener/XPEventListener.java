package com.crejaud.tech_company_clicker.listener;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.crejaud.tech_company_clicker.R;
import com.crejaud.tech_company_clicker.handler.LevelUpTransactionHandler;

/**
 * Created by creja_000 on 7/26/2016.
 */

public class XPEventListener implements ValueEventListener {

    private ReentrantLock lock;
    private BigInteger xp, xpToNextLevel;
    private TextView xpTextView;
    private DatabaseReference levelRef, xpRef, perkPointsRef, currencyPerClickRef;
    private ProgressBar xpBar;

    public XPEventListener(TextView xpTextView, DatabaseReference levelRef, DatabaseReference xpRef, DatabaseReference perkPointsRef, DatabaseReference currencyPerClickRef, ProgressBar xpBar) {
        this.xpTextView = xpTextView;
        this.lock = new ReentrantLock();
        this.levelRef = levelRef;
        this.xpRef = xpRef;
        this.perkPointsRef = perkPointsRef;
        this.currencyPerClickRef = currencyPerClickRef;
        this.xpBar = xpBar;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
            return;
        }
        else {
            xp = new BigInteger(dataSnapshot.getValue(String.class).split("/")[0]);
            xpToNextLevel = new BigInteger(dataSnapshot.getValue(String.class).split("/")[1]);
        }

        // set progress bar
        BigInteger percentage = xp.multiply(new BigInteger("100")).divide(xpToNextLevel);

        ObjectAnimator animation = ObjectAnimator.ofInt(xpBar, "progress", percentage.intValue());
        animation.setDuration(2000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        Log.d("XP Percentage", percentage.intValue() + "");

        // level up!!!
        if (xp.compareTo(xpToNextLevel) >= 0 && !lock.isLocked()) {
            lock.lock();
            Log.d("Lock", lock.toString());
            Log.d("Lock", "Locked is " + lock.isLocked());
            Log.d("Level Up!", xp + "/" + xpToNextLevel);
            levelRef.runTransaction(new LevelUpTransactionHandler(xpRef, perkPointsRef, currencyPerClickRef, lock));
        }
        xpTextView.setText(String.format(Locale.US, "%,d / %,d", xp, xpToNextLevel));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // uhhhh?
    }

}
