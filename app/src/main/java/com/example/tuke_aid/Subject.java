package com.example.tuke_aid;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Subject extends AppCompatActivity {

    private LinearLayout buttonLayout;
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);

        back = findViewById(R.id.btback);
        back.setOnClickListener(v -> {
            startActivity(new Intent(Subject.this, Home.class));
            finish();
        });

        buttonLayout = findViewById(R.id.buttonLayout);

        // Получаем UID пользователя из Firebase и выполняем загрузку предметов
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            new LoadSubjectsTask().execute(uid);
        } else {
            // Пользователь не аутентифицирован, выполните необходимые действия
        }
    }

    private class LoadSubjectsTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... params) {
            List<String> subjects = new ArrayList<>();
            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            if (params.length < 1) {
                return subjects; // Если не передан uid пользователя, возвращаем пустой список
            }

            String uid = params[0];

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                // Получение года обучения и специализации пользователя по его uid
                String queryUser = "SELECT Year, SpecializationID FROM Users WHERE UID = ?";
                try (PreparedStatement userStatement = conn.prepareStatement(queryUser)) {
                    userStatement.setString(1, uid);
                    try (ResultSet userResult = userStatement.executeQuery()) {
                        if (userResult.next()) {
                            String yearOfStudy = userResult.getString("Year");
                            int specializationId = userResult.getInt("SpecializationID");

                            // Выполнение запроса к базе данных для получения списка предметов для данного года и специализации
                            String querySubjects = "SELECT s.Name " +
                                    "FROM Subjects s " +
                                    "JOIN Specializations sp ON s.SpecializationID = sp.SpecializationID " +
                                    "WHERE s.Year = ? AND sp.SpecializationID = ?";
                            try (PreparedStatement statement = conn.prepareStatement(querySubjects)) {
                                statement.setString(1, yearOfStudy);
                                statement.setInt(2, specializationId);
                                try (ResultSet resultSet = statement.executeQuery()) {
                                    while (resultSet.next()) {
                                        String subjectName = resultSet.getString("Name");
                                        subjects.add(subjectName);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return subjects;
        }

        @Override
        protected void onPostExecute(List<String> subjects) {
            if (subjects != null && !subjects.isEmpty()) {
                int marginTop = 40; // начальное отступ сверху для первой кнопки
                // Создание кнопок для каждого предмета и добавление их в LinearLayout
                for (String subject : subjects) {
                    Button button = new Button(Subject.this);
                    button.setText(subject);
                    button.setBackgroundResource(R.drawable.custom_button); // установка стиля
                    button.setTypeface(null, Typeface.BOLD);
                    button.setTextSize(26);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(100, marginTop, 100, 0); // установка отступов
                    button.setLayoutParams(params);
                    buttonLayout.addView(button);
                    marginTop += 40;// увеличение отступа для следующей кнопки
                }
            } else {
                // Если список предметов пустой или произошла ошибка при получении данных
                // Можно добавить здесь какую-то обработку ошибки, если нужно
            }
        }
    }
}
