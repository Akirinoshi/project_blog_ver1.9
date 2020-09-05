import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class PrivateChat extends PrivateMessagesMenu {
    private boolean stop, goBack;
    private Statement st;
    private ResultSet rs;
    private String login, chat_id;
    private BufferedReader reader;
    private BufferedWriter writer;

    PrivateChat(Statement st, String login, BufferedReader reader, BufferedWriter writer, String chat_id) {
        this.st = st;
        this.login = login;
        this.reader = reader;
        this.writer = writer;
        this.chat_id = chat_id;
    }

    @Override
    public void start() {
        stop = false;
        for (; ; ) {
            if (stop) return;

            showDialog();

            menuFunctions();

            choice();
        }
    }

    @Override
    public void menuFunctions() {
        try {
            writer.write("\n1.Write message\n");
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
        goBack = false;

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
                    writeMessage();
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

    private void showDialog() {
        try {
            rs = st.executeQuery("SELECT * FROM p_messages WHERE ch_id='" + chat_id + "' ORDER BY date LIMIT 15 OFFSET 0;");
            while (rs.next()) {
                if (login.equals(rs.getString(2))) {
                    writer.write("\n"+rs.getString(4) + " : " + rs.getString(3) + "\n");
                } else {
                    writer.write(rs.getString(2) + "\n" + rs.getString(4) + " : " + rs.getString(3) + "\n");
                }
                writer.flush();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void writeMessage() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String message;

        try {
           writer.write("Write the message : \n");
           writer.flush();

           message = reader.readLine();

           message = editMessage(message);

           if (goBack) return;

           st.executeUpdate("INSERT INTO p_messages (ch_id,sender_login,message,date) " +
                   "VALUES (" + chat_id + ",'" + login + "','" + message + "','" + sdf.format(timestamp.getTime()) + "');");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
