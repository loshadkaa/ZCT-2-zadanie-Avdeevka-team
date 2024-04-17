package com.example.tuke_aid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignUpNext extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_next);

        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_values, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = (String) parentView.getItemAtPosition(position);
                int specializationId = -1; // Здесь будет храниться идентификатор специализации

                // Присваиваем уникальный идентификатор каждому элементу спиннера
                switch (selectedItem) {
                    case "Hosp.Informatika":
                        specializationId = 1;
                        break;
                    case "Int.Systemy":
                        specializationId = 2;
                        break;
                    case "Informatika-KB":
                        specializationId = 3;
                        break;
                    default:
                        // Обработка для непредвиденных значений
                        break;
                }

                // Теперь у вас есть выбранное значение из спиннера и его уникальный идентификатор
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        findViewById(R.id.btnext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String uid = user.getUid();
                    String year = "";
                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                    if (selectedRadioButtonId == R.id.selectYear1) {
                        year = "First";
                    } else if (selectedRadioButtonId == R.id.selectYear2) {
                        year = "Second";
                    } else if (selectedRadioButtonId == R.id.selectYear3) {
                        year = "Third";
                    }

                    int specializationId = spinner.getSelectedItemPosition() + 1;

                    new SaveYearAndSpecializationTask(uid, year, specializationId).execute();

                    goToHome();
                    Toast.makeText(SignUpNext.this, "Год и специализация успешно сохранены.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpNext.this, "Не удалось получить текущего пользователя.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class SaveYearAndSpecializationTask extends AsyncTask<Void, Void, Void> {

        private String uid;
        private String year;
        private int specializationId;

        public SaveYearAndSpecializationTask(String uid, String year, int specializationId) {
            this.uid = uid;
            this.year = year;
            this.specializationId = specializationId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                String query = "UPDATE Users SET Year = ?, SpecializationID = ? WHERE UID = ?";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, year);
                    statement.setInt(2, specializationId);
                    statement.setString(3, uid);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Ошибка при соединении с базой данных: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Можно добавить здесь дополнительные действия после завершения сохранения,
        }
    }

    private void goToHome() {
        Intent intent = new Intent(SignUpNext.this, Home.class);
        startActivity(intent);
        finish();
    }
}