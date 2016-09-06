package com.crypta;

import android.content.Context;

import com.crypta.activities.EncryptTask;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(MockitoJUnitRunner.class)
public class CryptoModuleTest {

    private final String password = "password";

    @Mock
    Context mMockContext;

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void encryptionTest() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        final File encresult = new File("/res/testExcel.xlsx");
        final File encresource = new File("testExcelEncypted.xlsx");
        new EncryptTask(mMockContext, new EncryptTask.Callback() {
            @Override
            public void onEncryptSuccess(String filepath) {
                assertEquals(encresult, encresource);
            }

            @Override
            public void onError(Exception e) {

            }
        }).execute(encresult.getPath(), password);

    }


}