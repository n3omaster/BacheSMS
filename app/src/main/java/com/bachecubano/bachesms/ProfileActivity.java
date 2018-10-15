package com.bachecubano.bachesms;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bachecubano.nautabackgroundlibrary.BackgroundMail;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends MainActivity {

    private EditText name;
    private EditText cellphone;

    private final int PICK_IMAGE_REQUEST = 1;

    private String mCurrentPhotoPath;
    private static String imagePath = "";
    private static Uri imageUri;

    private static final int PERMISSION_READ_EXTERNAL = 101;
    private static final int CAMERA_IMAGE_REQUEST = 102;

    private static final String PREFS_NAME = "BacheSMSPrefs";

    public static Intent createIntent(Context context) {
        return new Intent(context, ProfileActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Layout elements
        name = findViewById(R.id.name);
        cellphone = findViewById(R.id.cellphone);
        ImageView profilePic = findViewById(R.id.profilePicture);

        //Shared Preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String strcellphone = settings.getString("cellphone", "");
        String strusername = settings.getString("username", "");
        String savedImagePath = settings.getString("savedImagePath", "");

        //Set Strings data
        name.setText(strusername);
        cellphone.setText(strcellphone);

        //Permission Check para la lectura en la externa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_EXTERNAL);
            }
        }
    }


    /**
     * Permission checks
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getBaseContext(), "Gracias!", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                    Toast.makeText(this, "Es necesario examinar sus fotos", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Update profile data
     *
     * @param view
     */
    public void updateProfileData(View view) {

        String sttrName = name.getText().toString();
        String strCellphone = cellphone.getText().toString();

        if (!sttrName.equals("") && strCellphone.length() == 8) {

            saveUserPreferences(sttrName, strCellphone, imageUri);
            String body = "|$|" + sttrName + "|$|" + strCellphone + "|$||$||$||$||$||$|";

            PrefManager prefManager = new PrefManager(getBaseContext());
            if (prefManager.isNautaConfigured()) {

                BackgroundMail.Builder profileBackgroundMail = BackgroundMail.newBuilder(this)
                        .withUsername(PrefManager.nautaUser)
                        .withPassword(PrefManager.nautaPass)
                        .withMailto(Constants.API_BCH)
                        .withType()
                        .withSubject("PERFIL")
                        .withSendingMessage("Actualizando su perfil")
                        .withBody(body)
                        .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getBaseContext(), "Perfil Actualizado", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                            @Override
                            public void onFail() {
                                Toast.makeText(getBaseContext(), "No se pudo actualizar su perfil.", Toast.LENGTH_SHORT).show();
                            }
                        });

                //Si tiene foto de perfil -> imagePath
                if (!imagePath.equals("")) {
                    profileBackgroundMail.withAttachments(imagePath);
                }

                //Send Now
                profileBackgroundMail.send();

            } else {
                Toast.makeText(getBaseContext(), "No se ha configurado el correo correctamente", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getBaseContext(), SettingsActivity.class));
            }

        } else {
            Toast.makeText(getBaseContext(), "Los datos introducidos no son correctos", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Save temporary data written here
     */
    private void saveUserPreferences(String sttrName, String strCellphone, Uri imageUri) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("username", sttrName);
        editor.putString("cellphone", strCellphone);
        editor.apply();
    }

    /**
     * Select picture intent form internal device
     *
     * @param v
     */
    public void loadImageFromLibrary(View v) {
        Intent loadPictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
        if (loadPictureIntent.resolveActivity(getPackageManager()) != null) {
            loadPictureIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(loadPictureIntent, "Seleccione una foto"), PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(getBaseContext(), "No se han podido revisar sus fotos.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Invoke CAMERA_CAPTURE
     *
     * @param v
     */
    public void loadImageFromCamera(View v) {
        if (checkPermission()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.d("Crash", "Cannot create imageFile()");
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(this, "com.bachecubano.bachesms.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAMERA_IMAGE_REQUEST);
                } else {
                    Toast.makeText(getBaseContext(), "No se ha encontrado el hardware necesario. ERR55", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "No se ha encontrado el hardware necesario. ERR56", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "No tienes permiso para usar la cámara", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Result of the Image selector
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        ImageView profilePicture = findViewById(R.id.profilePicture);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            imagePath = ImageFilePath.getPath(getBaseContext(), uri);
            imageUri = Uri.parse("file://" + imagePath);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profilePicture.setImageBitmap(bitmap);
                profilePicture.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {

            Uri uri = Uri.parse("file://" + mCurrentPhotoPath);
            imagePath = ImageFilePath.getPath(getBaseContext(), uri);
            imageUri = Uri.parse("file://" + imagePath);

            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profilePicture.setImageBitmap(imageBitmap);
                profilePicture.requestLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cheack for Permission to take shoots with camera
     *
     * @return
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_IMAGE_REQUEST);
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Create the image to save it the picture
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "BacheSMS_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
}
