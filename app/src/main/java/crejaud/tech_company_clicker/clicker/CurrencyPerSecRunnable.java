package crejaud.tech_company_clicker.clicker;

import com.google.firebase.database.DatabaseReference;

import crejaud.tech_company_clicker.handler.IncreaseCurrencyTransactionHandler;
import crejaud.tech_company_clicker.listener.BigIntegerEventListener;

/**
 * Created by creja_000 on 7/28/2016.
 */

public class CurrencyPerSecRunnable implements Runnable {

    private BigIntegerEventListener currencyPerSecEventListener;
    private DatabaseReference mCurrencyRef, mCompanyXPRef;

    public CurrencyPerSecRunnable(BigIntegerEventListener currencyPerSecEventListener, DatabaseReference mCurrencyRef, DatabaseReference mCompanyXPRef) {
        this.currencyPerSecEventListener = currencyPerSecEventListener;
        this.mCurrencyRef = mCurrencyRef;
        this.mCompanyXPRef = mCompanyXPRef;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            // sleep for 1 second
            try {
                Thread.sleep(1000);

                // call increase currency transaction handler on the currency ref
                mCurrencyRef.runTransaction(new IncreaseCurrencyTransactionHandler(currencyPerSecEventListener, mCompanyXPRef));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
