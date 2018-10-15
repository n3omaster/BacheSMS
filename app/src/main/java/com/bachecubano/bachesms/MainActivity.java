package com.bachecubano.bachesms;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bachecubano.nautabackgroundlibrary.BackgroundMail;

public class MainActivity extends AppCompatActivity {

    private String number;
    private String text_message;
    private EditText phoneNumber;
    private EditText edText;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private static final int PERMISSION_READ_CONTACTS = 103;

    private Uri uriContact;
    private String contactID;

    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefManager = new PrefManager(getBaseContext());

        phoneNumber = findViewById(R.id.phoneNumber);
        edText = findViewById(R.id.ed_sms_text);
        Button send_sms = findViewById(R.id.sendSMS);

        send_sms.setOnClickListener(v -> {
            number = phoneNumber.getText().toString();
            text_message = edText.getText().toString();
            sendSMS();
        });
    }

    /**
     * Check SMS data and send it with nautabackroundlibrary
     */
    private void sendSMS() {
        if (text_message.length() > 1 && number.length() > 6) {
            if (!number.substring(0, 1).equals("+")) number = "+" + number;
            number = number.replace(" ", "");
            String body = "|$|" + number + "|$|" + text_message + "|$||$|";
            sendEmail("SMS", body);
        } else {
            Toast.makeText(getBaseContext(), "Ha ocurrido un error, los datos son incorrectos. Verifique.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Save this SMS to a internal DataBase
     *
     * @param cell_phone   String
     * @param text_message String
     */
    private void saveSentSMS(String cell_phone, String text_message) {
        DataBaseHelper myDb = new DataBaseHelper(getBaseContext());
        myDb.insertData(cell_phone, text_message);
        myDb.close();
    }

    /**
     * Send Email wrapper
     *
     * @param subject String
     * @param body    String
     */
    private void sendEmail(String subject, String body) {
        if (prefManager.isNautaConfigured()) {
            BackgroundMail.Builder profileBackgroundMail = BackgroundMail.newBuilder(this)
                    .withUsername(PrefManager.nautaUser)
                    .withPassword(PrefManager.nautaPass)
                    .withMailto(Constants.API_BCH)
                    .withType()
                    .withSubject(subject)
                    .withSendingMessage("Enviando ...")
                    .withBody(body)
                    .withOnSuccessCallback(() -> {
                        Toast.makeText(getBaseContext(), "Mensaje enviado correctamente.", Toast.LENGTH_SHORT).show();
                        //Save to Registers
                        saveSentSMS(number, text_message);
                        cleanboxesandsave();
                    })
                    .withOnFailCallback(() -> Toast.makeText(getBaseContext(), "No se pudo enviar, revise conexión.", Toast.LENGTH_SHORT).show());
            profileBackgroundMail.send();
        } else {
            Toast.makeText(getBaseContext(), "No se ha configurado el correo correctamente", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
        }
    }

    private void cleanboxesandsave() {
        phoneNumber.setText("");
        edText.setText("");
    }

    /**
     * Request Balance in account
     */
    private void getBalance() {
        String body = "|$|BacheSMS|$|";
        sendEmail("SALDO", body);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Menu selection Top Right
     *
     * @param item MenuItem
     * @return bool
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_config) {
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
        } else if (id == R.id.massive_send) {
            startActivity(new Intent(getBaseContext(), SendMasiveSMSActivity.class));
        } else if (id == R.id.action_sent) {
            startActivity(new Intent(getBaseContext(), SentSMSActivity.class));
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(getBaseContext(), ProfileActivity.class));
        } else if (id == R.id.action_topup) {
            startActivity(new Intent(getBaseContext(), TransferActivity.class));
        } else if (id == R.id.action_balance) {
            getBalance();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Contact picker
     *
     * @param view Viwe
     */
    public void onClickSelectContact(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS);
            } else {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
            }
        } else {
            startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
        }
    }

    /**
     * @param requestCode int
     * @param resultCode  result Code of user selection
     * @param data        Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();
            retrieveContactNumber();
        }
    }

    private void retrieveContactNumber() {
        String contactNumber = null;
        Cursor cursorID = getContentResolver().query(uriContact, new String[]{ContactsContract.Contacts._ID}, null, null, null);
        if (cursorID != null && cursorID.moveToFirst()) {
            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        if (cursorID != null) {
            cursorID.close();
        }

        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactID},
                null);

        if (cursorPhone != null && cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }
        if (cursorPhone != null) {
            cursorPhone.close();
        }

        Toast.makeText(getBaseContext(), "Contact Phone Number: " + contactNumber, Toast.LENGTH_LONG).show();
        phoneNumber.setText(contactNumber);
    }
}
