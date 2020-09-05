import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;

public class Statics extends Blog {
    private boolean stop;
    private boolean goBack;
    private Statement st;
    private ResultSet rs;
    private String login;
    private BufferedReader reader;
    private BufferedWriter writer;

    Statics(Statement st, String login, BufferedReader reader, BufferedWriter writer) {
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
            if (stop) {
                return;
            }

            menuFunctions();

            choice();
        }
    }

    @Override
    public void menuFunctions() {
        try {
            writer.write("\n1.Top users of forum \n");
            writer.write("2.Own static \n");
            writer.write("9.Back\n");
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
                    topUsers();
                    break;
                case "2":
                    ownStatic();
                    break;
                case "9":
                    stop = true;
                    break;
                default:
                    writer.write("No such variant\n");
                    writer.flush();
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ownStatic() {
        try {
            rs = st.executeQuery("SELECT access_name ,(SELECT COUNT(*) " +
                    "FROM messages WHERE login='" + login + "') " +
                    "FROM access_rights WHERE access_id = (SELECT access_id FROM users WHERE login ='" + login + "');");

            while (rs.next()) {
                writer.write("\nLogin : " + login + "\n" + "Status : " + rs.getString("access_name") + "\n" + "Number of messages : " + rs.getString("count") + "\n");
                writer.flush();
            }
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void topUsers() {
        try {
            writer.write("\nBest of the best on the forum:\nUser\tMessages\n\n");
            writer.flush();

            rs = st.executeQuery("SELECT users.login , COUNT(messages.*) " +
                    "FROM " +
                    "users INNER JOIN messages " +
                    "ON users.login = messages.login " +
                    "GROUP BY users.login " +
                    "ORDER BY count DESC " +
                    "LIMIT 5 OFFSET 0;");

            while (rs.next()) {
                writer.write(rs.getString("login") + "\t" + rs.getString("count") + "\n");
                writer.flush();
            }
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}