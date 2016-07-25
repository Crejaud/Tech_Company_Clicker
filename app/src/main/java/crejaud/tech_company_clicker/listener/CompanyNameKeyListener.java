package crejaud.tech_company_clicker.listener;

import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class CompanyNameKeyListener implements View.OnKeyListener {

    private DatabaseReference mCompaniesRef;
    private AlertDialog alertDialog;

    public CompanyNameKeyListener(DatabaseReference mCompaniesRef, AlertDialog alertDialog) {
        this.mCompaniesRef = mCompaniesRef;
        this.alertDialog = alertDialog;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        // on every keystroke, check to see if company name exists
        mCompaniesRef.addListenerForSingleValueEvent(new CompanyNameEventListener(view, alertDialog));
        return false;
    }
}
