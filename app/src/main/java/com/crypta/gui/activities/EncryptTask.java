package com.crypta.gui.activities;

/**
 * Created by D064343 on 22.05.2016.
 */

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.crypta.util.UriHelpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public final class EncryptTask extends AsyncTask<String, Void, String> {

    private final Context mContext;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onEncryptSuccess(String filepath);

        void onError(Exception e);
    }

    EncryptTask(Context context, Callback callback) {
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

                File localFile = UriHelpers.getFileForUri(mContext, Uri.parse(filePath));

                FileInputStream inFile = new FileInputStream(localFile);

                //System.out.println(UriHelpers.getFilePathForUri(mContext, Uri.parse(filePath)));

                String fileOutPath = UriHelpers.getFilePathForUri(mContext, Uri.parse(filePath));

                String extension = UriHelpers.getFileExtensionForUri(mContext, Uri.parse(filePath));

                if (extension.equals(".aes")){
                    return null;
                }

                //System.out.println(UriHelpers.getFileExtensionForUri(mContext, Uri.parse(filePath)));

                String newFileOutNameWithPath = String.format("%s%s", fileOutPath, ".aes");

                //System.out.println(newFileOutNameWithPath);

                // encrypted file
                FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);

                //https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html

                SecureRandom rand = new SecureRandom();
                KeyGenerator generator = KeyGenerator.getInstance("AES");
                generator.init(256,rand);
                SecretKey secret = generator.generateKey();
                System.out.println(secret.toString());


                byte[] iv = new byte[16];
                SecureRandom generateIV = new SecureRandom();
                generateIV.nextBytes(iv);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));

                //file encryption
                byte[] input = new byte[64];
                int bytesRead;

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

                System.out.println("File Encrypted."+newFileOutNameWithPath);

                return newFileOutNameWithPath;

            }

            catch (FileNotFoundException e) {
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
}
