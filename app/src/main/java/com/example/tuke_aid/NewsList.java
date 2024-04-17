package com.example.tuke_aid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import android.content.Intent;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NewsList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<NewsItem> newsList;
    private ImageButton back;
    private ImageButton more;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        recyclerView = findViewById(R.id.newsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        newsList = new ArrayList<>();
        new LoadNewsFromDatabaseTask().execute();

        back = findViewById(R.id.btback);
        back.setOnClickListener(v -> {
            startActivity(new Intent(NewsList.this, Home.class));
            finish();
        });

        more = findViewById(R.id.btgonext);
        if (isAdmin()) {
            more.setVisibility(View.VISIBLE);
        } else {
            more.setVisibility(View.GONE);
        }
        more.setOnClickListener(v -> {
            startActivity(new Intent(NewsList.this, AddNews.class));
            finish();
        });
    }

    private boolean isAdmin() {
        String adminUid = "tnpmKN8i9rY4qTFcNHiDk4C0xm92";
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return adminUid.equals(currentUserUid);
    }

    private class LoadNewsFromDatabaseTask extends AsyncTask<Void, Void, List<NewsItem>> {

        @Override
        protected List<NewsItem> doInBackground(Void... voids) {
            List<NewsItem> newsList = new ArrayList<>();
            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                String query = "SELECT NewsTitle, NewsText FROM News";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        String newsTitle = resultSet.getString("NewsTitle");
                        String newsText = resultSet.getString("NewsText");
                        newsList.add(new NewsItem(newsTitle, newsText));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return newsList;
        }

        @Override
        protected void onPostExecute(List<NewsItem> newsList) {
            adapter = new NewsAdapter(newsList, NewsList.this);
            recyclerView.setAdapter(adapter);
        }

    }
}
