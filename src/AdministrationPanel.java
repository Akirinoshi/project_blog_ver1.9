import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AdministrationPanel extends Blog {
    private boolean stop;
    private boolean goBack;
    private Statement st;
    private ResultSet rs;
    private String login;
    private BufferedReader reader;
    private BufferedWriter writer;

    AdministrationPanel(Statement st, String login, BufferedReader reader, BufferedWriter writer) {
        this.st = st;
        this.login = login;
        this.reader = reader;
        this.writer = writer;
    }

    public AdministrationPanel() {
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
            writer.write("\n1.Take info about\n");
            writer.write("2.Give privilege\n");
            writer.write("3.Find message/messages\n");
            writer.write("4.Find user(s)\n");
            writer.write("5.Unlock change login\n");
            writer.write("6.Block\n");
            writer.write("7.Unblock\n");
            writer.write("8.Delete message(s)\n");
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
                    takeInfoAbout();
                    break;
                case "2":
                    givePrivilege();
                    break;
                case "3":
                    findMessage();
                    break;
                case "4":
                    findUser();
                    break;
                case "5":
                    unlockChangeLogin();
                    break;
                case "6":
                    block();
                    break;
                case "7":
                    unblock();
                    break;
                case "8":
                    deleteMessage();
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

    private void takeInfoAbout() {
        try {
            String searchingLogin;

            writer.write("\nWrite user login : ");
            writer.flush();
            searchingLogin = reader.readLine();

            if (userNotExist(searchingLogin)) {
                writer.write("No such username in database.\n");
                writer.flush();
                return;
            }

            rs = st.executeQuery("SELECT access_name ,(SELECT COUNT(*) " +
                    "FROM messages WHERE login='" + searchingLogin + "') " +
                    " , (SELECT change_log FROM users WHERE login='" + searchingLogin + "')" +
                    " , (SELECT reg_date FROM users WHERE login='" + searchingLogin + "')" +
                    "FROM access_rights WHERE access_id = (SELECT access_id FROM users WHERE login ='" + searchingLogin + "');");

            while (rs.next()) {
                String changeLog;

                if (Integer.parseInt(rs.getString("change_log")) == 0) {
                    changeLog = "available";
                } else {
                    changeLog = "unavailable";
                }

                writer.write("\nLogin : " + searchingLogin + "\n" + "Status : " + rs.getString("access_name") + "\n" + "Number of messages : " + rs.getString("count") + "\nChange login : " + changeLog +
                        "\nRegistration date : " + rs.getString("reg_date") + "\n");
                writer.flush();

            }
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void givePrivilege() {
        try {
            String username;
            String new_username_access = "0";  //default variable , can be any other number

            writer.write("Write username : ");
            writer.flush();
            username = reader.readLine();

            if (userNotExist(username)) {
                writer.write("No such username in database.\n");
                writer.flush();
                return;
            }

            if (userBlocked(username)) {
                goBack = true;
                return;
            }


            if (notEnoughPrivileges(username)) {
                goBack = true;
            }

            if (goBack) return;

            writer.write("Which privilege you wan't to give?\n");
            writer.flush();
            allRightsList();

            new_username_access = reader.readLine();

            if (AccessIdCondition(new_username_access)) {
                st.executeUpdate("UPDATE users SET access_id = '" + new_username_access + "' WHERE login = '" + username + "';");
                writer.write("Success , privilege update.\n");
            } else {
                writer.write("Invalid input\n");
            }
            writer.flush();

            goBack = true;
        } catch (NumberFormatException e) {
            try {
                writer.write("Invalid input\n");
                writer.flush();
                goBack = true;
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean AccessIdCondition(String new_username_access) {
        String maxAccessId = "";

        try {
            try {
                rs = st.executeQuery("SELECT MAX(access_id) FROM access_rights;");

                while (rs.next()) {
                    maxAccessId = rs.getString(1);
                }

                if (new_username_access != "4" && Integer.parseInt(new_username_access) <= Integer.parseInt(maxAccessId) && Integer.parseInt(new_username_access) > 1) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException e) {
                writer.write("Invalid input");
                writer.flush();
                return false;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void allRightsList() {
        try {
            rs = st.executeQuery("SELECT * FROM access_rights WHERE access_id > 1 AND access_id != 4;");

            while (rs.next()) {
                writer.write(rs.getString("access_id") + ". " + rs.getString("access_name") + "\n");
                writer.flush();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void unlockChangeLogin() {
        try {
            String username;

            writer.write("Write username : ");
            writer.flush();
            username = reader.readLine();

            if (userNotExist(username)) {
                writer.write("No such username in database.\n");
                writer.flush();
                return;
            }

            if (userBlocked(username)) {
                goBack = true;
                return;
            }

            if (notEnoughPrivileges(username)) {
                goBack = true;
                return;
            }

            st.executeUpdate("UPDATE users SET change_log = 0 WHERE login='" + username + "';");

            writer.write(username + " change login function is on.\n");
            writer.flush();
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void block() {
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

            if (userBlocked(username)) {
                goBack = true;
                return;
            }

            if (notEnoughPrivileges(username)) {
                goBack = true;
                return;
            }

            st.executeUpdate("UPDATE users SET access_id = 4 WHERE login='" + username + "';");

            blockSystemNotification(username);

            writer.write(username + " is blocked\n");
            writer.flush();
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void blockSystemNotification(String username) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            st.executeUpdate("INSERT INTO messages(m_id,login,message,date) " +
                    " VALUES ( (SELECT MAX(m_id) FROM messages) + 1 ,'system', '*** user " + username + " was blocked by " + login + "***', '" + sdf.format(timestamp.getTime()) + "');");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void unblock() {
        try {
            String username;

            writer.write("\nWrite username which you wan't to unblock : ");
            username = reader.readLine();

            if (userNotExist(username)) {
                writer.write("No such username in database.\n");
                writer.flush();
                return;
            }

            if (!userBlocked(username)) {
                writer.write("User isn't blocked.\n");
                writer.flush();
                goBack = true;
                return;
            }

            st.executeUpdate("UPDATE users SET access_id = 3 WHERE login='" + username + "';");
            writer.write(username + " is unblocked.\n");
            writer.flush();
            goBack = true;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void findMessage() {
        AdministrationPanelFindMessage findMessage = new AdministrationPanelFindMessage(st, reader, writer);
        findMessage.start();
        goBack = true;
    }

    private void findUser() {
        AdministrationPanelFindUser findUser = new AdministrationPanelFindUser(st, reader, writer);
        findUser.start();
        goBack = true;
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
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean notEnoughPrivileges(String username) {
        try {
            String username_access = "";
            rs = st.executeQuery("SELECT access_id FROM users WHERE login='" + username + "';");

            while (rs.next()) {
                username_access = rs.getString(1);
            }

            rs = st.executeQuery("SELECT access_id FROM users WHERE login='" + login + "';");

            while (rs.next()) {
                String ownAccess;
                ownAccess = rs.getString(1);
                if (Integer.parseInt(ownAccess) >= Integer.parseInt(username_access)) {
                    writer.write("You haven't privilege to do this\n");
                    writer.flush();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteMessage() {
        MessageDeleter messageDeleter = new MessageDeleter(st, reader, writer, login);
        messageDeleter.start();
        goBack = true;
    }
}