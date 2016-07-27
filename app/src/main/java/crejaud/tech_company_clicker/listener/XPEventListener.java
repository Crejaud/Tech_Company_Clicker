package crejaud.tech_company_clicker.listener;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Logger;
import com.google.firebase.database.ValueEventListener;

import java.math.BigInteger;
import java.util.Locale;

import crejaud.tech_company_clicker.R;
import crejaud.tech_company_clicker.handler.LevelUpTransactionHandler;

/**
 * Created by creja_000 on 7/26/2016.
 */

public class XPEventListener implements ValueEventListener {

    private Context ctx;
    private BigInteger xp, xpToNextLevel;
    private TextView xpTextView;
    private LevelUpTransactionHandler levelUpTransactionHandler;
    private DatabaseReference levelRef, xpRef, xpToNextLevelRef, perkPointsRef;

    public XPEventListener(BigInteger xpToNextLevel, TextView xpTextView, Context ctx, DatabaseReference dbRef) {
        // initially 0 until it can receive the currencyPerClick
        this.xp = new BigInteger("0");
        this.xpToNextLevel = xpToNextLevel;
        this.xpTextView = xpTextView;
        this.ctx = ctx;

        this.levelRef = dbRef.child(ctx.getString(R.string.firebase_db_level));
        this.xpRef = dbRef.child(ctx.getString(R.string.firebase_db_level));
        this.xpToNextLevelRef = dbRef.child(ctx.getString(R.string.firebase_db_level));
        this.perkPointsRef = dbRef.child(ctx.getString(R.string.firebase_db_perk_points));

        this.levelUpTransactionHandler = new LevelUpTransactionHandler(xpRef, xpToNextLevelRef);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() == null) {
            xp = new BigInteger("0");
        }
        else {
            xp = new BigInteger(dataSnapshot.getValue(String.class));
        }

        // level up!!!
        if (xp.compareTo(xpToNextLevel) >= 0) {
            levelRef.runTransaction(levelUpTransactionHandler);
        }

        Log.d("Currency!", xp + "");
        xpTextView.setText(ctx.getResources().getString(R.string.currency, String.format(Locale.US, "%,d", xp)));
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        // uhhhh?
    }

}
