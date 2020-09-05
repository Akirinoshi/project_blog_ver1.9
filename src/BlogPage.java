import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;

import static javax.swing.UIManager.getString;

public class BlogPage extends Blog {
    private boolean stop;
    private boolean goBack;
    private Statement st;
    private ResultSet rs;
    private String login;
    private BufferedReader reader;
    private BufferedWriter writer;

    BlogPage(Statement st, String login, BufferedReader reader, BufferedWriter writer) {
        this.st = st;
        this.login = login;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void start() {
        stop = false;
        for (; ; ) {
            goBack = false;

            showBlogMessages();

            for (; ; ) {
                if (stop) return;
                if (goBack) break;

                menuFunctions();

                choice();
            }
        }
    }

    private void showBlogMessages() {
        try {
            writer.write("\nLast 10 messages.............................................................\n");

            rs = st.executeQuery("SELECT * FROM messages " +
                                      "WHERE m_id > ((SELECT MAX(m_id) FROM messages) - 10) " +
                                      "ORDER BY m_id  ASC;");
            while (rs.next()) {
                writer.write(rs.getInt("m_id") + ". "
                               + rs.getString("login") + " :" + "\n"
                               + rs.getString("message") + "\n" + rs.getString("date") + "\n\n");
                writer.flush();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void menuFunctions() {
        try {
            writer.write("\n\n\n");
            writer.write("1.Refresh\n");
            writer.write("2.Create new message\n");
            writer.write("9.Exit\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void choice() {
        String choice;
        if (goBack) return;
        if (stop) return;
        try {
            choice = reader.readLine();
            callFunction(choice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callFunction(String choice) {
        try {

            switch (choice) {
                case "1":
                    writer.write("\nRefreshing\n\n");
                    writer.flush();
                    goBack = true;
                    break;
                case "2":
                    writer.write("Create message.\n");
                    writer.flush();
                    createMessage();
                    break;
                case "9":
                    writer.write("");
                    writer.flush();
                    stop = true;
                    break;
                default:
                    writer.write("No such variant.\n");
                    writer.flush();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMessage() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String message;

            blockCheck();

            if (goBack) {
                return;
            }

            writer.write("\nWrite message (max length - 60):\n\n");
            writer.flush();

            message = reader.readLine();

            checkMessageLength(message);

            message = editMessage(message);

            if (goBack) return;

            st.executeUpdate("INSERT INTO messages(m_id,login,message,date) " +
                    " VALUES ( (SELECT MAX(m_id) FROM messages) + 1 ,'"
                    + login
                    + "', '" + message
                    + "', '" + sdf.format(timestamp.getTime()) + "');");
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkMessageLength(String message) {
        try {
            if (message.length() > 60) {
                writer.write("\nLength of message bigger then 60 characters , please write smaller message :\n\n");
                writer.flush();
                goBack = true;
                if (message.length() < 5) {
                    writer.write("\nLength of message smaller then 5 characters , please write bigger message :\n\n");
                    writer.flush();
                    goBack = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void blockCheck() {
        try {
            String access_id = "";
            rs = st.executeQuery("SELECT access_id FROM users WHERE login='" + login + "';");

            while (rs.next()) {
                access_id = rs.getString(1);
            }

            if (Integer.parseInt(access_id) == 4) {
                writer.write("Sorry , you're blocked\n");
                writer.flush();
                goBack = true;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}