package com.crypta.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Decrypt {
    private String out;

    public String decrypt(Context context, File result, String password) throws Exception {

        if (result != null && password != null && password.length() > 0) {
            // file to be decrypted

            FileInputStream inFile = new FileInputStream(result);

            //System.out.println(result.getAbsolutePath());

            String fileOutPath = "";

            int i = result.getAbsolutePath().lastIndexOf('.');
            if (i >= 0) {
                fileOutPath = result.getAbsolutePath().substring(0,i);
            }

            String newFileOutNameWithPath = String.format("%s%s", fileOutPath, "DECR.jpeg");


            //System.out.println(newFileOutNameWithPath);


            FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);

            // reading the salt
            byte[] salt = new byte[32];
            inFile.read(salt,0,32);

            // reading the iv
            byte[] iv = new byte[16];
            inFile.read(iv,0,16);

            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536,
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

            out = newFileOutNameWithPath;
        }

        return out;
    }
}