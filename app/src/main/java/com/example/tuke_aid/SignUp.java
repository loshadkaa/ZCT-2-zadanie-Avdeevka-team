package com.example.tuke_aid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUp extends AppCompatActivity {

    private ImageButton btback;
    private ImageButton btnext;
    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.TextLogin);
        passwordEditText = findViewById(R.id.TextPassword);

        mAuth = FirebaseAuth.getInstance();

        btback = findViewById(R.id.btback);
        btback.setOnClickListener(v -> {
            startActivity(new Intent(SignUp.this, MainActivity.class));
            finish();
        });

        btnext = findViewById(R.id.btnext);
        btnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUp.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    String uid = user.getUid();
                                    new SaveUidToDatabaseTask().execute(uid);
                                    Toast.makeText(SignUp.this, "Continue registration", Toast.LENGTH_SHORT).show();
                                    goToSignUpNext();
                                }
                            } else {
                                Toast.makeText(SignUp.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                goToMainActivity();
                            }
                        });
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(SignUp.this, "Authorization via Google was successful. " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
//                                String name = user.getDisplayName();
                                String uid = user.getUid();
                                new SaveUidToDatabaseTask().execute(uid);
                                Toast.makeText(SignUp.this, "Authorization via Google has been successfully completed.", Toast.LENGTH_SHORT).show();
                                goToSignUpNext();
                            }
                        } else {
                            Toast.makeText(SignUp.this, "Authorization via Google failed." + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            goToMainActivity();
                        }
                    }
                });
    }

    private class SaveUidToDatabaseTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String uid = strings[0];
            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                String query = "INSERT INTO Users (UID, Year, SpecializationID) VALUES (?, NULL, NULL)";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, uid);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Можно добавить здесь действия, которые нужно выполнить после завершения сохранения UID в базу данных
        }
    }



    private void goToSignUpNext() {
        Intent intent = new Intent(SignUp.this, SignUpNext.class);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

