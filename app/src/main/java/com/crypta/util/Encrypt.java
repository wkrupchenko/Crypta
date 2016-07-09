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
import java.util.Arrays;

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

            String extension = UriHelpers.getFileExtensionForUri(context, Uri.parse(filePath));

            System.out.println(UriHelpers.getFileExtensionForUri(context, Uri.parse(filePath)));

            String newFileOutNameWithPath = String.format("%s%s", fileOutPath, ".aes");;

            System.out.println(newFileOutNameWithPath);

            // encrypted file
            FileOutputStream outFile = new FileOutputStream(newFileOutNameWithPath);

            byte[] salt = new byte[32];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(salt);

            SecretKeyFactory factory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536,
                    256);
            SecretKey secretKey = factory.generateSecret(keySpec);
            SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

            byte[] iv = new byte[16];
            SecureRandom generateIV = new SecureRandom();
            generateIV.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret,new IvParameterSpec(iv));
            AlgorithmParameters params = cipher.getParameters();

            //file encryption
            byte[] input = new byte[64];
            int bytesRead;

            assert extension != null;
            byte[] ext = extension.getBytes("US-ASCII");

            outFile.write(salt);
            outFile.write(iv);
            System.out.println(Arrays.toString(salt));
            System.out.println(Arrays.toString(iv));

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
