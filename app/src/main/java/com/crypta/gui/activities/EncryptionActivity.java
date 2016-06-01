package com.crypta.gui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.crypta.R;

public class EncryptionActivity extends AppCompatActivity {

    private static final String TAG = EncryptionActivity.class.getName();
    private String pwd = "";
    private String pwdConfirm = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button encryptButton = (Button) findViewById(R.id.button);
        encryptButton.setOnClickListener(myhandler1);

    }

    View.OnClickListener myhandler1 = new View.OnClickListener() {
        public void onClick(View v) {
            pwd = ((EditText) findViewById(R.id.editTextPassword)).getText().toString();
            pwdConfirm = ((EditText) findViewById(R.id.confirmEncryptPassword)).getText().toString();
            Intent intent = new Intent();
            if (pwd != null && pwd != "" && pwdConfirm != null && pwdConfirm != "" && pwd.equals(pwdConfirm)) {

                if (pwd.length() > 0) {
                    intent.putExtra("pwd", pwd);

                }
            }
            setResult(RESULT_OK, intent);
            finish();

        }
    };


}
