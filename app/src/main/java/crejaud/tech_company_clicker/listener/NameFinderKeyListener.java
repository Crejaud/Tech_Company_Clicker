package crejaud.tech_company_clicker.listener;

import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class NameFinderKeyListener implements TextWatcher {

    private EditText mEditText;
    private DatabaseReference namesRef;
    private AlertDialog alertDialog;
    private boolean inclusion;

    public NameFinderKeyListener(EditText mEditText, DatabaseReference namesRef, AlertDialog alertDialog, boolean inclusion) {
        this.mEditText = mEditText;
        this.namesRef = namesRef;
        this.alertDialog = alertDialog;
        this.inclusion = inclusion;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // disable on key stroke no matter what to prevent any possible duplicate company name
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        // on every keystroke, check to see if company name exists
        namesRef.addListenerForSingleValueEvent(new NameFinderEventListener(mEditText, alertDialog, inclusion));}

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
