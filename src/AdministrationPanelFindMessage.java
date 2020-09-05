import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;

import static javax.swing.UIManager.getString;

public class AdministrationPanelFindMessage extends AdministrationPanel {
    private boolean stop;
    private Statement st;
    private ResultSet rs;
    private BufferedReader reader;
    private BufferedWriter writer;

    AdministrationPanelFindMessage(Statement st, BufferedReader reader, BufferedWriter writer) {
        this.st = st;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void start() {
        for (; ; ) {
            if (stop) return;

            menuFunctions();

            choice();
        }
    }

    @Override
    public void menuFunctions() {
        try {
            writer.write("\n1.By username\n");
            writer.write("2.By date (from/to)\n");
            writer.write("3.By text\n");
            writer.write("9.Exit\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void choice() {
        String choice;
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
                    findMessageByUsername();
                    break;
                case "2":
                    findMessageByDate();
                    break;
                case "3":
                    findMessageByText();
                    break;
                case "9":
                    writer.write("Exit\n");
                    writer.flush();
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

    private void findMessageByUsername() {
        try {
            String username;

            writer.write("\nWrite username : ");
            writer.flush();
            username = reader.readLine();

            if (userNotExist(username)) {
                writer.write("No such username in database.\n");
                writer.flush();
                return;
            }

            rs = st.executeQuery("SELECT * FROM messages WHERE login='" + username + "';");
            while (rs.next()) {
                writer.write(rs.getInt("m_id") + ". " + rs.getString("login") + " :" + "\n" + rs.getString("message") + "\n" + rs.getString("date") + "\n\n");
                writer.flush();
            }
            stop = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void findMessageByDate() {
        try {
            String dateFrom, dateTo;

            writer.write("Write date in format ****-**-** and time if need **:**:**\n");
            writer.write("From : ");
            writer.flush();
            dateFrom = reader.readLine();
            dateFrom = editMessage(dateFrom);
            writer.write("\nTo : ");
            writer.flush();
            dateTo = reader.readLine();
            dateTo = editMessage(dateTo);


            try {
                rs = st.executeQuery("SELECT * FROM messages " +
                        "WHERE date between '" + dateFrom + "'::timestamp and '" + dateTo + "'::timestamp order by date ;");

                while (rs.next()) {
                    writer.write(rs.getInt("m_id") + ". " + rs.getString("login") + " :" + "\n" + rs.getString("message") + "\n" + rs.getString("date") + "\n\n");
                    writer.flush();
                }
                stop = true;
            } catch (SQLException exc) {
                writer.write("Invalid input , try again.\n");
                writer.flush();
                stop = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findMessageByText() {
        try {
            try {
                String text;

                writer.write("Search : ");
                writer.flush();
                text = reader.readLine();
                text = editMessage(text);

                writer.newLine();

                rs = st.executeQuery("SELECT * FROM messages WHERE message ~~ '%" + text + "%';");
                while (rs.next()) {
                    writer.write(rs.getInt("m_id") + ". " + rs.getString("login") + " :" + "\n" + rs.getString("message") + "\n" + rs.getString("date") + "\n\n");
                    writer.flush();
                }
                stop = true;
            } catch (SQLException e) {
                stop = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean userNotExist(String searchingLogin) {
        try {
            rs = st.executeQuery("SELECT login FROM users;");

            while (rs.next()) {
                if (searchingLogin.equals(rs.getString(1))) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}