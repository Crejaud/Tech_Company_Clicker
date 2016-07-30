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

public class NameFinderEventListener implements ValueEventListener {

    private EditText name;
    private AlertDialog alertDialog;
    private boolean inclusion;

    public NameFinderEventListener(EditText editText, AlertDialog alertDialog, boolean inclusion) {
        name = editText;
        this.alertDialog = alertDialog;
        this.inclusion = inclusion;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        if (!inclusion) {
            // if company name exists, is empty, or is too long, then set text to RED, set hint text, and set create button to be disabled
            if ((name.getText().toString().isEmpty() || dataSnapshot.hasChild(name.getText().toString()))) {
                // set text color to red
                name.setTextColor(Color.RED);
                // set create button disabled
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
            // if company name does not exist, then set text to GREEN, set hint text, and set create button to be enabled
            else {
                // set text color to green
                name.setTextColor(Color.GREEN);
                // set create button enabled
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
        }
        else {
            // if company name exists, is empty, or is too long, then set text to RED, set hint text, and set create button to be disabled
            if ((name.getText().toString().isEmpty() || dataSnapshot.hasChild(name.getText().toString()))) {
                // set text color to green
                name.setTextColor(Color.GREEN);
                // set create button enabled
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            }
            // if company name does not exist, then set text to GREEN, set hint text, and set create button to be enabled
            else {
                // set text color to red
                name.setTextColor(Color.RED);
                // set create button disabled
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
