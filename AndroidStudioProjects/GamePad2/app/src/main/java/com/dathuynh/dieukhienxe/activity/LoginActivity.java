package com.dathuynh.dieukhienxe.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.dathuynh.dieukhienxe.Constants;
import com.dathuynh.dieukhienxe.R;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static int RESULT_SUCCESS = 0;

    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    CheckBox _rememberCheckbox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _usernameText = (EditText) findViewById(R.id.input_username);
        _passwordText = (EditText) findViewById(R.id.input_password);
        _loginButton = (Button) findViewById(R.id.btn_login);
        _rememberCheckbox = (CheckBox) findViewById(R.id.rememberme);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login(_usernameText.getText().toString(), _passwordText.getText().toString());
            }
        });

        //if remember me before - just log them in
        getUser();
    }

    public void login(String username, String password) {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        // TODO: Implement your own authentication logic here.

        if (username.isEmpty() || username.length() < 3) {
            RESULT_SUCCESS = 0;
            Toast.makeText(getBaseContext(), "Username must be more than 4 alphanumeric characters", Toast.LENGTH_SHORT).show();
        } else {
            if (password.equals("tiendat"))
                RESULT_SUCCESS = 1;
            else
                RESULT_SUCCESS = 0;
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (RESULT_SUCCESS == 1)
                            onLoginSuccess();
                        else onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 2000);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                _usernameText.setText(getIntent().getStringExtra("name"));
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);

        //check remember me check box

        if (_rememberCheckbox.isChecked()) {
            //save username and password in SharedPreferences
            getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(Constants.PREF_USERNAME, _usernameText.getText().toString())
                    .putString(Constants.PREF_PASSWORD, _passwordText.getText().toString())
                    .commit();
        } else {
            //save username in SharedPreferences
            getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(Constants.PREF_USERNAME, _usernameText.getText().toString())
                    .commit();
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty()) {
            _usernameText.setError("Enter a valid username");
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (password.isEmpty() || password.length() < 2 || password.length() > 10) {
            _passwordText.setError("between 2 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        return valid;
    }

    public void getUser() {
        SharedPreferences pref = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        String username = pref.getString(Constants.PREF_USERNAME, null);
        String password = pref.getString(Constants.PREF_PASSWORD, null);

        if (username != null && password != null) {
            //auto login
            _usernameText.setText(username);
            _passwordText.setText(password);
            login(username, password);
        }
    }

}