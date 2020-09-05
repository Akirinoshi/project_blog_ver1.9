import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class PrivateMessagesMenu extends Blog {
    private boolean stop, goBack;
    private Statement st;
    private ResultSet rs;
    private String login;
    private BufferedReader reader;
    private BufferedWriter writer;

    PrivateMessagesMenu(Statement st, String login, BufferedReader reader, BufferedWriter writer) {
        this.st = st;
        this.login = login;
        this.reader = reader;
        this.writer = writer;
    }

    PrivateMessagesMenu() {

    }

    @Override
    public void start() {
        stop = false;
        for (; ; ) {
            if (stop) return;
            goBack = false;

            showDialogs();

            menuFunctions();

            choice();
        }
    }

    @Override
    public void menuFunctions() {
        try {
            writer.write("\nMain menu : \n");
            writer.write("1.Create new dialog\n");
            writer.write("2.Choose dialog\n");
            writer.write("9.Exit\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void choice() {
        String choice;

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
                    createDialog();
                    break;
                case "2":
                    openPrivateChat();
                    break;
                case "9":
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

    private void showDialogs() {
        int i = 1;

        try {
            writer.write("\nYour chats : \n");

            rs = st.executeQuery("SELECT *, (SELECT MAX(date) FROM p_messages WHERE chats.ch_id = p_messages.ch_id) " +
                    "FROM chats " +
                    "WHERE first_login='" + login + "' OR second_login='" + login + "' " +
                    "ORDER BY max DESC;");
            while (rs.next()) {
                if (rs.getString("first_login").equals(login)) {
                    writer.write(i++ + ". " + rs.getString("second_login") + "\n");
                } else {
                    writer.write(i++ + ". " + rs.getString("first_login") + "\n");
                }
            }
            writer.flush();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void createDialog() {
        String username, message;

        try {
            writer.write("Who do you want to write : ");
            writer.flush();

            username = reader.readLine();

            if (userNotExist(username)) {
                return;
            }

            if (userBlocked(username)) {
                return;
            }

            if (chatExist(username)) {
                return;
            }

            writer.write("Write the message : \n");
            writer.flush();

            message = reader.readLine();

            message = editMessage(message);

            if (messageCheck(message)) {
                return;
            }

            insertingData(username, message);

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
            writer.write("No such username in database.\n");
            writer.flush();
            return true;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean userBlocked(String username) {
        try {
            String username_access = "";
            rs = st.executeQuery("SELECT access_id FROM users WHERE login='" + username + "';");

            while (rs.next()) {
                username_access = rs.getString(1);
            }

            if (Integer.parseInt(username_access) == 4) {
                writer.write("User is blocked\n");
                writer.flush();
                return true;
            }
            return false;
        } catch (IOException| SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean chatExist(String username) {
        try {
            rs = st.executeQuery("SELECT ch_id FROM chats WHERE first_login='" + login + "'AND second_login='" + username + "' " +
                    "OR first_login='" + username + "' AND second_login='" + login + "';");
            while (rs.next()) {
                if (!rs.getString(1).equals("")) {
                    writer.write("Chat already exist.\n");
                    writer.flush();
                    return true;
                }
            }
            return false;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean messageCheck(String message) {
        if (message == " " | message.length() > 100) {
            return true;
        }
        return false;
    }

    private void insertingData(String username, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String ch_id = "";

        try {
            st.executeUpdate("INSERT INTO chats (first_login,second_login) " +
                    "VALUES ('" + login + "','" + username + "')");

            rs = st.executeQuery("SELECT ch_id FROM chats WHERE first_login='" + login + "' AND second_login='" + username + "';");
            while (rs.next()) {
                ch_id = rs.getString(1);
            }
            st.executeUpdate("INSERT INTO p_messages (ch_id,sender_login,message,date) " +
                    "VALUES (" + ch_id + ",'" + login + "','" + message + "','" + sdf.format(timestamp.getTime()) + "');");
            writer.write("\nSuccess)\n");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void openPrivateChat() {
        String chat_id;
        chat_id = chooseChat();

        if (goBack) return;

        PrivateChat privateChat = new PrivateChat(st, login, reader, writer, chat_id);
        privateChat.start();
    }

    private String chooseChat() {
        String chat_number;
        int chat_n, i = 0;

        try {
            writer.write("Choose the chat (number): ");
            writer.flush();

            chat_number = reader.readLine();
            chat_n = Integer.parseInt(chat_number);

            writer.newLine();

            if (goBack) return "";

            rs = st.executeQuery("SELECT *, (SELECT MAX(date) FROM p_messages WHERE chats.ch_id = p_messages.ch_id) " +
                    "FROM chats " +
                    "WHERE first_login='" + login + "' OR second_login='" + login + "' " +
                    "ORDER BY max DESC;");
            while (rs.next()) {
                i++;
                if (i == chat_n) {
                    return rs.getString(1);
                }
            }

            writer.write("No such chat.\n");
            writer.flush();
            goBack = true;
        } catch (IOException | SQLException e) {
            goBack = true;
            e.printStackTrace();
        } catch (NumberFormatException e) {
            goBack = true;
        }
        return "";
    }
}