package com.example.tuke_aid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NewsAdapter adapter;
    private List<NewsItem> newsListHome;
    private ImageButton btmore;
    private ImageButton mapButton;
    private ImageButton subBt;
    private ImageButton profileBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        newsListHome = new ArrayList<>();
        new Home.LoadNewsFromDatabase().execute();

        btmore = findViewById(R.id.btmore);
        btmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMoreItems();
            }
        });

        mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Map.class);
                startActivity(intent);
                finish();
            }
        });

        subBt = findViewById(R.id.subBt);
        subBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Subject.class);
                startActivity(intent);
                finish();
            }
        });


        profileBt = findViewById(R.id.profileBt);
        profileBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, Profile.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void loadMoreItems() {
        Intent intent = new Intent(Home.this, NewsList.class);
        startActivity(intent);
        finish();
    }

    private class LoadNewsFromDatabase extends AsyncTask<Void, Void, List<NewsItem>> {

        @Override
        protected List<NewsItem> doInBackground(Void... voids) {
            List<NewsItem> newsList = new ArrayList<>();
            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                String query = "SELECT NewsTitle, NewsText FROM News ORDER BY NewsID DESC LIMIT 2";
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
            adapter = new NewsAdapter(newsList, Home.this);
            recyclerView.setAdapter(adapter);
        }

    }


}

