import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class Settings extends Blog {
    private boolean stop;
    private boolean goBack;
    private Statement st;
    private ResultSet rs;
    private String login;
    private BufferedReader reader;
    private BufferedWriter writer;

    Settings(Statement st, String login, BufferedReader reader, BufferedWriter writer) {
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
            writer.write("\n1.Change login\n");
            writer.write("2.Change password\n");
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
                    changeLogin();
                    break;
                case "2":
                    changePassword();
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

    private void changeLogin() {
        try {
            String loginNew;

            checkChangeLogin();
            if (goBack) return;

            for (; ; ) {

                changeLoginWarning();
                if (goBack) return;

                writer.write("\nPlease write your new login : ");
                writer.flush();
                loginNew = reader.readLine();

                checkLoginLength(loginNew);

                checkLoginCharacters(loginNew);

                checkUsersForSameLogin(loginNew);

                if (goBack) return;

                st.executeUpdate("UPDATE users SET login='" + loginNew + "' , change_log='1' WHERE login='" + login + "';");
                writer.write("Success , you're new login is : " + loginNew + "\n\n");
                writer.flush();
                login = loginNew;
                goBack = true;
                return;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkLoginLength(String loginNew) {
        try {
            if (loginNew.length() > 12 || loginNew.length() < 4) {
                writer.write("Login must be with size between 4 and 12 characters.\n");
                writer.flush();
                goBack = true;
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkLoginCharacters(String loginNew) {
        try {
            for (int i = 0; i < loginNew.length(); i++) {
                if (loginNew.charAt(i) >= (char) 48 && loginNew.charAt(i) <= (char) 122) {
                    if (loginNew.charAt(i) > (char) 57 && loginNew.charAt(i) < (char) 65) {
                        writer.write("\n" + "Login contains invalid characters.\n");
                        writer.flush();
                        goBack = true;
                        return;
                    }
                } else {
                    writer.write("\n" + "Login contains invalid characters.\n");
                    writer.flush();
                    goBack = true;
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkUsersForSameLogin(String loginNew) {
        try {
            for (; ; ) {
                if (loginNew.equalsIgnoreCase(login)) {
                    break;
                }

                rs = st.executeQuery("SELECT login FROM users;");
                while (rs.next()) {
                    String logCheck;
                    logCheck = rs.getString(1);

                    if (logCheck.equalsIgnoreCase(loginNew)) {
                        writer.write("Such user exist.\n\n");
                        writer.flush();
                        goBack = true;
                        return;
                    }
                }
                break;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void changeLoginWarning() {
        try {
            for (; ; ) {
                Scanner scanner = new Scanner(System.in);
                writer.write("\nYou can change login only one time , are you sure that you want to take new?\n");
                writer.write("1: Yes , 2: No : \n");
                writer.flush();
                int choice;

                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    switch (choice) {
                        case 1:
                            return;
                        case 2:
                            writer.write("Operation canceled.\n");
                            writer.flush();
                            goBack = true;
                            return;
                        default:
                            writer.write("Invalid input.\n");
                            writer.flush();
                            scanner = null;
                            break;
                    }
                } else {
                    writer.write("Invalid input.\n");
                    writer.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkChangeLogin() {
        try {
            String checkFlag = "";

            rs = st.executeQuery("SELECT change_log FROM users WHERE login='" + login + "';");
            while (rs.next()) {
                checkFlag = rs.getString(1);
            }

            if (Integer.parseInt(checkFlag) == 1) {
                writer.write("\nYou can't change your login anymore.\n\n");
                writer.flush();
                goBack = true;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void changePassword() {
        try {
            for (; ; ) {
                String password, newPassword;

                writer.write("\nChanging password operation is started.\n");
                writer.flush();

                writer.write("Write your current password : ");
                writer.flush();
                password = reader.readLine();

                writer.write("\nWrite new password : ");
                writer.flush();
                newPassword = reader.readLine();

                checkPasswordLength(newPassword);

                checkPasswordCharacters(newPassword);

                if (goBack) return;

                passwordsReconciliation(password);

                if (goBack) return;

                st.executeUpdate("UPDATE users SET password = '" + newPassword + "' WHERE login='" + login + "';");

                writer.write("Password changed successfully.\n\n");
                goBack = true;
                return;
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkPasswordLength(String newPassword) {
        try {
            if (newPassword.length() > 16 || newPassword.length() < 4) {
                writer.write("\nPassword must be with size between 4 and 16 characters.\n");
                writer.write("Try again next time.\n");
                writer.flush();
                goBack = true;
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPasswordCharacters(String newPassword) {
        try {
            for (int i = 0; i < newPassword.length(); i++) {
                if (newPassword.charAt(i) >= (char) 48 && newPassword.charAt(i) <= (char) 122) {
                    if (newPassword.charAt(i) > (char) 57 && newPassword.charAt(i) < (char) 65) {
                        writer.write("\n" + "Password contains invalid characters.\n");
                        writer.flush();
                        goBack = true;
                        return;
                    }
                } else {
                    writer.write("\n" + "Password contains invalid characters.\n");
                    writer.flush();
                    goBack = true;
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void passwordsReconciliation(String password) {
        try {
            rs = st.executeQuery("SELECT password FROM users WHERE login='" + login + "';");
            while (rs.next()) {
                String str = rs.getString(1);
                if (!str.equals(password)) {
                    writer.write("Invalid old password entered.\n");
                    writer.flush();
                    goBack = true;
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    String getLoginNew() {
        return login;
    }
}
