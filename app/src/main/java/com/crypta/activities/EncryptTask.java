package com.crypta.activities;

/**
 * Created by D064343 on 22.05.2016.
 */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.crypta.util.UriHelpers;

import org.spongycastle.crypto.generators.SCrypt;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptTask extends AsyncTask<String, Void, String> {

    private static final String TAG = EncryptTask.class.getName();
    private final Context mContext;
    private final Callback mCallback;
    private Exception mException;
    private KeyStore keystore = null;

    public EncryptTask(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onEncryptSuccess(result);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        String filePath = params[0];

        if (filePath != null && filePath.length() > 0) {

            try {

                File path = mContext.getDir("keystore", 0);

                File keysFile = new File(path, "store.pwd");

                if (!path.exists()) {
                    if (!path.mkdirs()) {
                        mException = new RuntimeException("Unable to create directory: " + path);
                    }
                } else if (!path.isDirectory()) {
                    mException = new IllegalStateException("Path is not a directory: " + path);
                    return null;
                } else {

                    if (!keysFile.isFile()) {
                        keysFile.createNewFile();
                    }

                    if (keysFile.isFile()) {

                        File localFile = UriHelpers.getFileForUri(mContext, Uri.parse(filePath));

                        FileInputStream inFile = new FileInputStream(localFile);

                        //System.out.println(UriHelpers.getFilePathForUri(mContext, Uri.parse(filePath)));

                        String fileOutPath = UriHelpers.getFilePathForUri(mContext, Uri.parse(filePath));

                        String extension = UriHelpers.getFileExtensionForUri(mContext, Uri.parse(filePath));

                        if (extension.startsWith(".enc")) {
                            return null;
                        }

                        //System.out.println(UriHelpers.getFileExtensionForUri(mContext, Uri.parse(filePath)));

                        String newFileOutNameWithPath = String.format("%s%s", fileOutPath, String.format("%s%s", ".enc", extension));

                        //System.out.println(newFileOutNameWithPath);

                        // encrypted file
                        //FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);
                        DataOutputStream outFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newFileOutNameWithPath)));

                        //https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html

                        //generate random salt
                        SecureRandom rand = new SecureRandom();
                        byte[] salt = new byte[128];
                        rand.nextBytes(salt);

                        //using key derivation function generate cryptographic key
                        byte[] key = SCrypt.generate(params[1].getBytes("UTF-8"), salt, 16384, 8, 8, 32);
                        SecretKey secretKey = new SecretKeySpec(key, "AES");

                        KeyGenerator generator = KeyGenerator.getInstance("AES");
                        generator.init(256, rand);
                        SecretKey secret = generator.generateKey();

                        //System.out.println(secret.toString());

                        // encrypt file with random key fro mStep 1
                        byte[] iv = new byte[16];
                        SecureRandom generateIV = new SecureRandom();
                        generateIV.nextBytes(iv);

                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

                        //file encryption
                        byte[] input = new byte[64];
                        int bytesRead;

                        //wrtie salt and iv to output file
                        outFile.writeUTF(params[2]);
                        outFile.write(salt);
                        outFile.write(iv);

                        while ((bytesRead = inFile.read(input)) != -1) {
                            byte[] output = cipher.update(input, 0, bytesRead);
                            if (output != null)
                                outFile.write(output);
                        }

                        byte[] output = cipher.doFinal();
                        if (output != null)
                            outFile.write(output);

                        inFile.close();
                        outFile.flush();
                        outFile.close();

                        Log.w(TAG, "File Encrypted." + newFileOutNameWithPath);

                        return newFileOutNameWithPath;

                    } else {
                        mException = new RuntimeException("Unable to create keystore file: " + keysFile.getName());
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public interface Callback {
        void onEncryptSuccess(String filepath);

        void onError(Exception e);
    }
}
