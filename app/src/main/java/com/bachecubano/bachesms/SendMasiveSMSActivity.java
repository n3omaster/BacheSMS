package com.bachecubano.bachesms;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bachecubano.nautabackgroundlibrary.BackgroundMail;

public class SendMasiveSMSActivity extends AppCompatActivity {

    private String numbers;
    private String text_message;
    private EditText phoneNumber;
    private EditText edText;

    private PrefManager prefManager;

    public static Intent createIntent(Context context) {
        return new Intent(context, SendMasiveSMSActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_masive_sms);

        prefManager = new PrefManager(getBaseContext());

        phoneNumber = findViewById(R.id.numbers_list);
        edText = findViewById(R.id.ed_sms_text);
        Button send_sms = findViewById(R.id.sendSMS);

        send_sms.setOnClickListener(v -> {
            numbers = phoneNumber.getText().toString();
            text_message = edText.getText().toString();
            sendSMS();
        });
    }

    /**
     * Check SMS data and send it with nautabackroundlibrary
     */
    private void sendSMS() {
        if (text_message.length() > 1 && numbers.length() > 6) {
            if (!numbers.substring(0, 1).equals("+")) numbers = "+" + numbers;
            numbers = numbers.replace(" ", "");
            String body = "|$|" + numbers + "|$|" + text_message + "|$||$|";
            sendEmail("MASIVO", body);
        } else {
            Toast.makeText(getBaseContext(), "Ha ocurrido un error, los datos son incorrectos. Verifique.", Toast.LENGTH_LONG).show();
        }
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
                        saveSentSMS(numbers, text_message);
                        cleanboxesandsave();
                    })
                    .withOnFailCallback(() -> Toast.makeText(getBaseContext(), "No se pudo enviar, revise conexión.", Toast.LENGTH_SHORT).show());
            profileBackgroundMail.send();
        } else {
            Toast.makeText(getBaseContext(), "No se ha configurado el correo correctamente", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
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
     * Clear
     */
    private void cleanboxesandsave() {
        phoneNumber.setText("");
        edText.setText("");
    }
}
