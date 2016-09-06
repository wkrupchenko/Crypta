package com.crypta.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crypta.R;

import org.spongycastle.crypto.digests.SHA3Digest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;


public class ChangePasswordActivity extends AppCompatActivity {


    private static final String TAG = ChangePasswordActivity.class.getName();
    static int i = 1;
    // UI references.
    private EditText oldPwd;
    private EditText newMasterPwdField;
    private TextView passwordStrengthHint;
    private EditText retypeMasterPwdHint;
    private ProgressBar pb;
    private Button backButtonCreateAccount;
    private Button createAccountButton;
    private EditText pwdHint;
    private KeyStore keystore = null;

    private static void convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        Log.i(TAG, stringBuffer.toString());
    }

    public static byte[] sha3(String base) {
        try {
            SHA3Digest md = new SHA3Digest(512);
            byte[] digest = new byte[64];
            md.update(base.getBytes("UTF-8"), 0, base.length());
            md.doFinal(digest, 0);
            return digest;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_local_password);
        //setContentView(R.layout.activity_create_local_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Change password for encryption");
        setSupportActionBar(toolbar);

        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);
        pb.setMax(100);
        oldPwd = (EditText) findViewById(R.id.oldMasterPwd);
        newMasterPwdField = (EditText) findViewById(R.id.newMasterPwdField);
        retypeMasterPwdHint = (EditText) findViewById(R.id.retypeMasterPwdHint);
        backButtonCreateAccount = (Button) findViewById(R.id.backButtonCreateAccount);
        createAccountButton = (Button) findViewById(R.id.createAccountButton);
        passwordStrengthHint = (TextView) findViewById(R.id.passwordStrengthHint);
        pwdHint = (EditText) findViewById(R.id.pwdHint);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldPwd.getText() != null && oldPwd.getText().length() < 1) {

                    oldPwd.setError("Please enter you current password..!");
                }
                else if (newMasterPwdField.getText() != null && newMasterPwdField.getText().length() < 1) {

                    newMasterPwdField.setError("Please enter master password..!");
                } else if (newMasterPwdField.getText() != null && !isPasswordValid(newMasterPwdField.getText().toString())) {

                    newMasterPwdField.setError("Password should be longer that 4 characters!" +
                            " A strong password should contain at least 1 uppercase character (A-Z), 1 lowercase character (a-z)," +
                            " 1 digit (0-9), 1 special character (\" !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~\") and be at least 10 characters long!");
                } else if (retypeMasterPwdHint.getText() != null && retypeMasterPwdHint.getText().length() < 1) {

                    retypeMasterPwdHint.setError("Please retype master password..!");
                } else if (retypeMasterPwdHint.getText() != null && !isPasswordValid(retypeMasterPwdHint.getText().toString())) {

                    retypeMasterPwdHint.setError("Password should be longer that 4 characters!" +
                            "A strong password should contain at least 1 uppercase character (A-Z), 1 lowercase character (a-z)," +
                            " 1 digit (0-9), 1 special character (\" !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~\") and be at least 10 characters long!");
                } else if (newMasterPwdField.getText() != null && retypeMasterPwdHint.getText() !=null && !newMasterPwdField.getText().toString().equals(retypeMasterPwdHint.getText().toString())) {

                    Toast.makeText(getApplicationContext(),
                            "The two passwords you have entered do not match!",
                            Toast.LENGTH_LONG)
                            .show();
                } else if (pwdHint.getText() != null && pwdHint.getText().length() < 1) {

                    pwdHint.setError("Please type in a hint for the case if you forget your password!");
                } else {
                    Log.i(TAG, "FORM IS OK");
                    String oldP = oldPwd.getText().toString();
                    if (Arrays.equals(oldP.getBytes(), getUserPassword())) {
                        encryptAndSavePassword(keystore, newMasterPwdField.getText().toString(), pwdHint.getText().toString());
                    }
                    else {
                        Toast.makeText(getApplicationContext(),
                                "You have entered a wrong current master password!",
                                Toast.LENGTH_LONG)
                                .show();
                    }

                }


            }

        });

        backButtonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        // newMasterPwdField.setError("Enter your password..!");

        newMasterPwdField.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // TODO Auto-generated method stub
                if (newMasterPwdField.getText().toString().length() == 0) {
                    pb.setVisibility(View.INVISIBLE);
                    passwordStrengthHint.setText("");
                    //newMasterPwdField.setError("Enter your password..!");
                } else {
                    pb.setVisibility(View.VISIBLE);
                    caculation(newMasterPwdField);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

        });

    }

    private void encryptAndSavePassword(KeyStore keystore, String password, String pwdHint) {

        if (password !=null && password.length()>0){


        try {
            keystore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keystore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = null;
            try {
                privateKeyEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry("encRSAPair", null);
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            try {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }

            final byte[] digest = sha3(password);

            //convertByteArrayToHexString(digest);

            final CipherOutputStream cipherStream = new CipherOutputStream(openFileOutput("etc.io", getApplicationContext().MODE_PRIVATE), cipher);
            final CipherOutputStream cipherStreamPwdHint = new CipherOutputStream(openFileOutput("hint.io", getApplicationContext().MODE_PRIVATE), cipher);

            cipherStream.write(password.getBytes("UTF-8"));
            cipherStream.close();
            cipherStreamPwdHint.write(pwdHint.getBytes("UTF-8"));
            cipherStreamPwdHint.close();
            Toast.makeText(getApplicationContext(),
                    "Successfully changed password for file encryption!",
                    Toast.LENGTH_LONG)
                    .show();
            Intent it = new Intent(getApplicationContext(),
                    UserActivity.class);
            startActivity(it);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        }

    }

    //adjust length and special characters
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void cancel() {
        super.onBackPressed();
    }

    protected void caculation(TextView psw) {
        // TODO Auto-generated method stub
        String temp = psw.getText().toString();

        i = i + 1;

        int length = 0, uppercase = 0, lowercase = 0, digits = 0, symbols = 0, bonus = 0, requirements = 0;

        int lettersonly = 0, numbersonly = 0, cuc = 0, clc = 0;

        length = temp.length();
        for (int i = 0; i < temp.length(); i++) {
            if (Character.isUpperCase(temp.charAt(i)))
                uppercase++;
            else if (Character.isLowerCase(temp.charAt(i)))
                lowercase++;
            else if (Character.isDigit(temp.charAt(i)))
                digits++;

            symbols = length - uppercase - lowercase - digits;

        }

        for (int j = 1; j < temp.length() - 1; j++) {

            if (Character.isDigit(temp.charAt(j)))
                bonus++;

        }

        for (int k = 0; k < temp.length(); k++) {

            if (Character.isUpperCase(temp.charAt(k))) {
                k++;

                if (k < temp.length()) {

                    if (Character.isUpperCase(temp.charAt(k))) {

                        cuc++;
                        k--;

                    }

                }

            }

        }

        for (int l = 0; l < temp.length(); l++) {

            if (Character.isLowerCase(temp.charAt(l))) {
                l++;

                if (l < temp.length()) {

                    if (Character.isLowerCase(temp.charAt(l))) {

                        clc++;
                        l--;

                    }

                }

            }

        }

        if (length > 10) {
            requirements++;
        }

        if (uppercase > 0) {
            requirements++;
        }

        if (lowercase > 0) {
            requirements++;
        }

        if (digits > 0) {
            requirements++;
        }

        if (symbols > 0) {
            requirements++;
        }

        if (bonus > 0) {
            requirements++;
        }

        if (digits == 0 && symbols == 0) {
            lettersonly = 1;
        }

        if (lowercase == 0 && uppercase == 0 && symbols == 0) {
            numbersonly = 1;
        }

        if (requirements > 0 && requirements < 3) {
            pb.setProgress(0);
            passwordStrengthHint.setText("password strength: weak");
            passwordStrengthHint.setTextColor(Color.parseColor("#fff64d0a"));
        } else if (requirements > 3 && requirements < 6) {
            pb.setProgress(50);
            passwordStrengthHint.setText("password strength: medium");
            passwordStrengthHint.setTextColor(Color.parseColor("#f57c0a"));
        } else if (requirements == 6) {
            pb.setProgress(100);
            passwordStrengthHint.setText("password strength: strong");
            passwordStrengthHint.setTextColor(Color.parseColor("#ff2FF211"));
        }

    }

    private byte[] getUserPassword() {

        byte[] bytes = null;
        byte[] hintBytes = null;

        try {
            keystore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keystore.load(null);
            KeyStore.PrivateKeyEntry privateKeyEntry = null;
            try {
                privateKeyEntry = (KeyStore.PrivateKeyEntry) keystore.getEntry("encRSAPair", null);
            } catch (UnrecoverableEntryException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            Cipher cipher = null;
            try {
                cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            }
            try {
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }


            CipherInputStream cipherInputStream = new CipherInputStream(openFileInput("etc.io"), cipher);
            ArrayList<Byte> values = new ArrayList<>();
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                values.add((byte) nextByte);
            }

            bytes = new byte[values.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = values.get(i).byteValue();
            }

            CipherInputStream cipherInputStreamHint = new CipherInputStream(openFileInput("hint.io"), cipher);
            ArrayList<Byte> valuesHint = new ArrayList<>();
            int nextByteHint;
            while ((nextByteHint = cipherInputStreamHint.read()) != -1) {
                valuesHint.add((byte) nextByteHint);
            }

            hintBytes = new byte[valuesHint.size()];
            for (int i = 0; i < hintBytes.length; i++) {
                hintBytes[i] = valuesHint.get(i).byteValue();
            }

            //convertByteArrayToHexString(bytes);
            Log.i(TAG, new String(bytes, StandardCharsets.UTF_8));
            Log.i(TAG, new String(hintBytes, StandardCharsets.UTF_8));


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return bytes;

    }


}

