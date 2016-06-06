package com.crypta.util;

/**
 * Created by D064343 on 22.05.2016.
 */

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class Encrypt {

    private String out;

    public String encrypt(Context context, String filePath, String password) throws Exception {

        if (filePath != null && filePath.length() > 0 && password != null && password.length() > 0) {
            // file to be encrypted

            File localFile = UriHelpers.getFileForUri(context, Uri.parse(filePath));

            FileInputStream inFile = new FileInputStream(localFile);

            System.out.println(UriHelpers.getFilePathForUri(context, Uri.parse(filePath)));

            String fileOutPath = UriHelpers.getFilePathForUri(context, Uri.parse(filePath));

            String newFileOutNameWithPath = String.format("%s%s", fileOutPath, ".aes");;

            System.out.println(newFileOutNameWithPath);

            // encrypted file
            FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);

            // password, iv and salt should be transferred to the other end
            // in a secure manner

            // salt is used for encoding
            // writing it to a file
            // salt should be transferred to the recipient securely
            // for decryption
            byte[] salt = new byte[8];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);
            FileOutputStream saltOutFile = new FileOutputStream(fileOutPath+".salt");
            saltOutFile.write(salt);
            saltOutFile.close();

            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536,
                    256);
            SecretKey secretKey = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            //
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();

            // iv adds randomness to the text and just makes the mechanism more
            // secure
            // used while initializing the cipher
            // file to store the iv
            FileOutputStream ivOutFile = new FileOutputStream(fileOutPath+".iv");
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            ivOutFile.write(iv);
            ivOutFile.close();

            //file encryption
            byte[] input = new byte[64];
            int bytesRead;

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

            System.out.println("File Encrypted.");

            out = newFileOutNameWithPath;

        }

        return out;
    }

}
