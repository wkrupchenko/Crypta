package com.crypta.gui.activities;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptTask extends AsyncTask<File, Void, String> {

    private final Context mContext;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onDecryptSuccess(String result);
        void onError(Exception e);
    }

    DecryptTask(Context context, Callback callback) {
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
            mCallback.onDecryptSuccess(result);
        }
    }

    @Override
    protected String doInBackground(File... params) {
        File file = params[0];

        if (file != null) {

            try {
                // file to be decrypted
                FileInputStream inFile = new FileInputStream(file);

                //System.out.println(result.getAbsolutePath());

                String fileOutPath = "";

                int i = file.getAbsolutePath().lastIndexOf('.');
                if (i >= 0) {
                    fileOutPath = file.getAbsolutePath().substring(0, i);
                }

                String newFileOutNameWithPath = String.format("%s%s", fileOutPath, "DECR.jpeg");


                //System.out.println(newFileOutNameWithPath);


                FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);

                // reading the salt
                byte[] salt = new byte[32];
                inFile.read(salt,0,32);

                // reading the iv
                byte[] iv = new byte[16];
                inFile.read(iv, 0, 16);

                SecretKeyFactory factory = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA1");
                KeySpec keySpec = new PBEKeySpec("password".toCharArray(), salt, 65536,
                        256);
                SecretKey tmp = factory.generateSecret(keySpec);
                SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

                // file decryption
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

                byte[] in = new byte[64];
                int read;
                while ((read = inFile.read(in)) != -1) {
                    byte[] output = cipher.update(in, 0, read);
                    if (output != null)
                        outFile.write(output);
                }

                byte[] output = cipher.doFinal();
                if (output != null)
                outFile.write(output);
                inFile.close();
                outFile.flush();
                outFile.close();
                System.out.println("File Decrypted.");

                return newFileOutNameWithPath;
            }

            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}