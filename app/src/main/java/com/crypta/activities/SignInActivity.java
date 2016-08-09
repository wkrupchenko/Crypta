package com.crypta.activities;

import android.content.Intent;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crypta.R;

import org.spongycastle.crypto.digests.SHA3Digest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

/**
 * A login screen that offers login via email/password.
 */
public final class SignInActivity extends AppCompatActivity {


    // UI references.  tags
    private EditText pwdEdittext;
    private TextView hintQuestionLabel;
    private TextView newAcountLinkLabel;
    private TextView newAccountLink;
    private Button loginButton;
    private KeyStore keystore = null;

   /* static {
        // Adds a new spongycastle provider, at a specified position
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }*/

    //convert hash output to human readable hex64 string
    private static void convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        //System.out.println(stringBuffer.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("Crypta");
        setSupportActionBar(toolbar);
        // Set up the login form.
        pwdEdittext = (EditText) findViewById(R.id.pwdEdittext);
        hintQuestionLabel = (TextView) findViewById(R.id.hintQuestionLabel);
        newAcountLinkLabel = (TextView) findViewById(R.id.newAcountLinkLabel);
        newAccountLink = (TextView) findViewById(R.id.newAcountLinkLabel);
        loginButton = (Button) findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = pwdEdittext.getText().toString();

                // Check for a valid password, if the user entered one.
                if (TextUtils.isEmpty(password)) {
                    pwdEdittext.setError("Password can not be empty!");
                } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                    pwdEdittext.setError("Password should be longer that 4 characters!");

                } else {
                    if (Arrays.equals(sha3(password), getUserPassword())) {
                        //pwdEdittext.setText("");

                        Intent it = new Intent(getApplicationContext(),
                                UserActivity.class);
                        it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(it);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Wrong password!",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });

        newAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent it = new Intent(getApplicationContext(),
                        ChangeLocalPasswordActivity.class);
                startActivity(it);
            }
        });

        // load Android keystore
        try {
            keystore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keystore.load(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        // check if app already has an X.509 certificate in keystore and check if the certificate date hasn't expired yet
        try {
            if (keystore.getCertificate("encRSAPair") == null || (keystore.getCertificate("encRSAPair").getType().equals("X.509") && ((X509Certificate) keystore.getCertificate("encRSAPair")).getNotAfter().before(Calendar.getInstance().getTime()))) {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias("encRSAPair")
                        .setSubject(new X500Principal("CN=App Main Certificate, O=Local Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .setKeySize(4096)
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);
                try {
                    KeyPair keyPair = generator.generateKeyPair();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
//                File dir = getApplicationContext().getDir("pwd", Context.MODE_PRIVATE);
//                System.out.println("OUTPUT:"+dir.getAbsolutePath().toString());
//                try {
//                    System.out.println(((KeyStore.PrivateKeyEntry) keystore.getEntry("encRSAPair", null)).getCertificate().toString());
//                } catch (UnrecoverableEntryException e) {
//                    e.printStackTrace();
//                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        File file = new File(getApplicationContext().getFilesDir(), "etc.io");

        if (!file.exists()) {
            Intent it = new Intent(getApplicationContext(),
                    CreateLocalPasswordActivity.class);
            startActivity(it);
            try {
                FileOutputStream outputStream = openFileOutput("etc.io", getApplicationContext().MODE_PRIVATE);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    //adjust length and special characters
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public byte[] sha3(String base) {
        try {
            SHA3Digest md = new SHA3Digest(512);
            byte[] digest = new byte[64];
            md.update(base.getBytes("UTF-8"), 0, base.length());
            md.doFinal(digest, 0);
            convertByteArrayToHexString(digest);
            return digest;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private byte[] getUserPassword() {

        byte[] bytes = null;

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

            convertByteArrayToHexString(bytes);


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

