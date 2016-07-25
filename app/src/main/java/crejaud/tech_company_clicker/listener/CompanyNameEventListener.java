package crejaud.tech_company_clicker.listener;

import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by creja_000 on 7/24/2016.
 */

public class CompanyNameEventListener implements ValueEventListener {

    private EditText inputCompanyName;
    private AlertDialog alertDialog;

    public CompanyNameEventListener(View view, AlertDialog alertDialog) {
        inputCompanyName = (EditText) view;
        this.alertDialog = alertDialog;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        // if company name exists, then set text to RED, set hint text, and set create button to be disabled
        if (dataSnapshot.hasChild(inputCompanyName.getText().toString())) {
            // set text color to red
            inputCompanyName.setTextColor(Color.RED);
            // set create button disabled
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        }
        // if company name does not exist, then set text to GREEN, set hint text, and set create button to be enabled
        else {
            // set text color to green
            inputCompanyName.setTextColor(Color.GREEN);
            // set create button enabled
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
