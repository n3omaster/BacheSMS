package com.bachecubano.bachesms;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TransferActivity extends AppCompatActivity {

    private static final int PERMISSION_CALL_PHONE = 103;

    private EditText number;
    private EditText pin;
    private EditText balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        number = findViewById(R.id.transferNumber);
        pin = findViewById(R.id.pin);
        balance = findViewById(R.id.balance);
    }

    /**
     * Button
     *
     * @param view View
     */
    public void rechargeBachecubano(View view) {
        String full_number = number.getText().toString();
        String pin_number = pin.getText().toString();
        String amount_number = balance.getText().toString();

        if (full_number.equals("54663598") && pin_number.length() == 4 && amount_number.length() > 0) {
            Toast.makeText(getBaseContext(), "OK!", Toast.LENGTH_LONG).show();
            makeCall("*234*1*" + full_number + "*" + pin_number + "*" + amount_number + Uri.encode("#"));
        } else {
            Toast.makeText(getBaseContext(), "Ha ocurrido un error, los datos son incorrectos. Verifique.", Toast.LENGTH_LONG).show();
        }
    }

    //Make a phone Call
    private void makeCall(String phoneNumber) {
        //Permission Check para el CALL_PHONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
            } else {
                callPhone(phoneNumber);
            }
        } else {
            callPhone(phoneNumber);
        }
    }

    /**
     * Call Intent
     *
     * @param ad_phone String
     */
    private void callPhone(String ad_phone) {
        Intent intent = new Intent("android.intent.action.CALL");
        intent.setData(Uri.parse("tel:" + ad_phone));
        startActivity(intent);
    }
}
