package com.example.tuke_aid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class AddNews extends AppCompatActivity {

    private ImageButton userListTextView;

    List<String> emailList = new ArrayList<>();
    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        back = findViewById(R.id.btback);
        back.setOnClickListener(v -> {
            startActivity(new Intent(AddNews.this, Home.class));
            finish();
        });

        userListTextView = findViewById(R.id.sendEmailButton);

        ImageButton sendEmailButton = findViewById(R.id.sendEmailButton);
        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.newsTextInput);
                String textToSave = editText.getText().toString();

                EditText titleText = findViewById(R.id.newsTitleInput);
                String titleToSave = titleText.getText().toString();

                new SaveTextToDatabase().execute(titleToSave, textToSave);
                new getEmailsFromDatabase().execute();
            }
        });
    }


    private class getEmailsFromDatabase extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {

            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                String query = "SELECT Email FROM UserMails";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        String email = resultSet.getString("Email");
                        emailList.add(email);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return emailList;
        }

        @Override
        protected void onPostExecute(List<String> emailList) {
            sendEmailOnClick(emailList);
            goToNews();
            Toast.makeText(AddNews.this, "Новая новость успешно добавлена.", Toast.LENGTH_SHORT).show();
        }
    }


    private class SaveTextToDatabase extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String titleToSave = strings[0];
            String textToSave = strings[1];

            String jdbcUrl = "jdbc:mysql://tukeaidzct.c32aakcwaetz.eu-north-1.rds.amazonaws.com:3306/TUKEAID";
            String username = "admin";
            String password = "tukeaid99";

            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                String query = "INSERT INTO News (NewsTitle, NewsText) VALUES (?, ?)";
                try (PreparedStatement statement = conn.prepareStatement(query)) {
                    statement.setString(1, titleToSave);
                    statement.setString(2, textToSave);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
        }
    }

    public void sendEmailOnClick(List<String> emailList) {
        for (String email : emailList) {
            AmazonSESSample amazonSESSample = new AmazonSESSample(
                    "aidtuke@gmail.com",
                    "TUKEAID",
                    email,
                    "AKIA4SL2C5CXQ3TU6AVD",
                    "BLbNxMkMttLW/PWugA+ch4BTIAxj4PfM7LdzRWoQ0xkf",
                    "<h1>TUKEAID Aktuality</h1>" + "<p>New news has appeared in TUKEAID</p>"
            );
            amazonSESSample.start();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public class AmazonSESSample extends Thread{

        private final String from;
        private final String fromName;
        private final String to;
        private final String smtpUsername;
        private final String smtpPassword;
        private final String body;

        public AmazonSESSample(String from, String fromName, String to, String smtpUsername, String smtpPassword, String body) {
            this.from = from;
            this.fromName = fromName;
            this.to = to;
            this.smtpUsername = smtpUsername;
            this.smtpPassword = smtpPassword;
            this.body = body;
        }

        static final String HOST = "email-smtp.eu-north-1.amazonaws.com";

        static final int PORT = 587;

        static final String SUBJECT = "Amazon SES test";

        @Override
        public void run() {

            // Create a Properties object to contain connection configuration information.
            Properties props = System.getProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", PORT);
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.auth", "true");

            // Create a Session object to represent a mail session with the specified properties.
            Session session = Session.getDefaultInstance(props);

            // Create a message with the specified information.
            MimeMessage msg = new MimeMessage(session);
            try {
                msg.setFrom(new InternetAddress(from, fromName));
                msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
                msg.setSubject(SUBJECT);
                msg.setContent(body,"text/html");

                // Create a transport.
                Transport transport = session.getTransport();

                // Send the message.
                try
                {
                    System.out.println("Sending...");

                    // Connect to Amazon SES using the SMTP username and password you specified above.
                    transport.connect(HOST, smtpUsername, smtpPassword);

                    // Send the email.
                    transport.sendMessage(msg, msg.getAllRecipients());
                    System.out.println("Email sent!");
                }
                catch (Exception ex) {
                    System.out.println("The email was not sent.");
                    System.out.println("Error message: " + ex.getMessage());
                }
                finally
                {
                    // Close and terminate the connection.
                    transport.close();
                }
            } catch (UnsupportedEncodingException | MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    private void goToNews() {
        Intent intent = new Intent(AddNews.this, NewsList.class);
        startActivity(intent);
        finish();
    }
}
