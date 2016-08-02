package com.crypta.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import org.spongycastle.crypto.generators.SCrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptTask extends AsyncTask<File, Void, File> {

    private final Context mContext;
    private final Callback mCallback;
    private Exception mException;

    DecryptTask(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
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
    protected File doInBackground(File... params) {
        File file = params[0];

        if (file != null) {

            try {
                // file to be decrypted
                FileInputStream inFile = new FileInputStream(file);

                String fileOutPath = "";
                String fileOutExtension = "";

                fileOutPath = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.') + 1);

                fileOutExtension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.') + 4);

                String newFileOutNameWithPath = String.format("%s%s", fileOutPath, fileOutExtension);

                System.out.println(newFileOutNameWithPath);

                FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);

                // reading the salt
                byte[] salt = new byte[128];
                inFile.read(salt, 0, 128);

                // reading the iv
                byte[] iv = new byte[16];
                inFile.read(iv, 0, 16);

                byte[] key = SCrypt.generate("password".getBytes("UTF-8"), salt, 16384, 8, 8, 32);
                SecretKey secret = new SecretKeySpec(key, "AES");

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

                File decFile = new File(newFileOutNameWithPath);

                // Tell android about the file
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(decFile));
                mContext.sendBroadcast(intent);

                return decFile;

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
            }
        }

        return null;
    }

    public interface Callback {
        void onDecryptSuccess(File result);

        void onError(Exception e);
    }

}