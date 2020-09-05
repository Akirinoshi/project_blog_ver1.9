import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class MessageDeleter extends AdministrationPanel {
    private boolean stop;
    private Statement st;
    private ResultSet rs;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String login;

    MessageDeleter(Statement st, BufferedReader reader, BufferedWriter writer, String login) {
        this.st = st;
        this.reader = reader;
        this.writer = writer;
        this.login = login;
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
            writer.write("\n1.By message id\n");
            writer.write("2.By login (ALL)\n");
            writer.write("3.By text\n");
            writer.write("9.Exit\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            stop = true;
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
            stop = true;
        }
    }

    @Override
    public void callFunction(String choice) {
        try {
            switch (choice) {
                case "1":
                    deleteMessageById();
                    break;
                case "2":
                    deleteMessageByLogin();
                    break;
                case "3":
                    deleteMessagesByText();
                    break;
                case "9":
                    writer.write("Exit\n");
                    writer.flush();
                    stop = true;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            stop = true;
        }
    }

    private void deleteMessageById() {
        String ID;

        try {
            writer.write("\nWrite message id : ");
            writer.flush();

            ID = reader.readLine();

            idCheck(ID);

            if (stop) return;

            st.executeUpdate("UPDATE messages " +
                    "SET message = 'Message deleted by administration : " + login + "' " +
                    "WHERE m_id = " + ID + ";");

            writer.write("\nMessage successfully deleted.\n");

            stop = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {

        }
    }

    private void idCheck(String ID) {
        int id;

        try {
            try {
                id = Integer.parseInt(ID);

                if (!st.execute("SELECT m_id FROM messages WHERE m_id = " + id + ";")) {
                    writer.write("\nNo such id.\n");
                    writer.flush();
                    stop = true;
                }

            } catch (NumberFormatException e) {
                e.printStackTrace();
                writer.write("Invalid input\n");
                stop = true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteMessageByLogin() {
        String username;

        try {
            writer.write("\nWrite login : ");
            writer.flush();

            username = reader.readLine();

            if (userNotExist(username)) {
                writer.write("No such username in database.\n");
                writer.flush();
                return;
            }

            st.executeUpdate("UPDATE messages " +
                    "SET message = 'Message deleted by administration : " + login + "' " +
                    "WHERE login = '" + username + "';");

            writer.write("\nMessages successfully deleted.\n");
            writer.flush();

            stop = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean userNotExist(String searchingLogin) throws SQLException {
        rs = st.executeQuery("SELECT login FROM users;");

        while (rs.next()) {
            if (searchingLogin.equals(rs.getString(1))) {
                return false;
            }
        }
        return true;
    }

    private void deleteMessagesByText() {
        String text;

        try {
            try {
                writer.write("\nWith what text messages should be deleted : ");
                writer.flush();

                text = reader.readLine();
                text = editMessage(text);

                st.executeUpdate("UPDATE messages " +
                        "SET message = 'Message deleted by administration : " + login + "' " +
                        "WHERE message ~~ '%" + text + "%';");

                writer.write("\nSuccess\n");
                writer.flush();

                stop = true;
            } catch (SQLException e) {
                e.printStackTrace();

                writer.write("Invalid input.");
                writer.flush();
                stop = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}