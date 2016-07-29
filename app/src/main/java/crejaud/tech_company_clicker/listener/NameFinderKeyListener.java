package crejaud.tech_company_clicker.listener;

import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class NameFinderKeyListener implements View.OnKeyListener {

    private DatabaseReference namesRef;
    private AlertDialog alertDialog;
    private boolean inclusion;

    public NameFinderKeyListener(DatabaseReference namesRef, AlertDialog alertDialog, boolean inclusion) {
        this.namesRef = namesRef;
        this.alertDialog = alertDialog;
        this.inclusion = inclusion;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        // disable on key stroke no matter what to prevent any possible duplicate company name
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // on every keystroke, check to see if company name exists
        namesRef.addListenerForSingleValueEvent(new NameFinderEventListener(view, alertDialog, inclusion));
        return false;
    }
}
