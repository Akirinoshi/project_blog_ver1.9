import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Scanner;


public class Initialization implements UserData {
    private boolean stop = false;
    private boolean goBack = false;
    private Statement st;
    private ResultSet rs;
    private BufferedReader reader;
    private BufferedWriter writer;


    Initialization(Statement st, BufferedReader reader, BufferedWriter writer) {
        this.st = st;
        this.reader = reader;
        this.writer = writer;
    }

    Initialization() {
    }

    public void start() {
        for (; ; ) {
            goBack = false;

            if (stop) return;

            menuFunctions();

            choice();
        }
    }

    public void menuFunctions() {
        try {
            writer.write("\n1.Sign in\n");
            writer.write("2.Register\n");
            writer.write("9.Exit\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

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

    public void callFunction(String int_choice) {
        if (goBack) {
            return;
        }
        switch (int_choice) {
            case "1":
                logIn();
                break;
            case "2":
                registration();
                break;
            case "9":
                try {
                    writer.write("\nGoodbye!)\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stop = true;
                break;
            default:
                try {
                    writer.write("No such variant\n");
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void logIn() {
        String login, password;

        try {
            writer.write("\nPlease write your login : ");
            writer.flush();
            login = reader.readLine();

            writer.write("Write password : ");
            writer.flush();
            password = reader.readLine();

            login = editMessage(login);
            password = editMessage(password);

            if (userExist(login, password)) {
                writer.write("Success!\n\n\n");
                writer.flush();
                Blog blog = new Blog(st, login, reader, writer);
                blog.start();
            } else {
                writer.write("Invalid input\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        goBack = true;
        return;
    }

    private boolean userExist(String login, String password) {
        boolean rc = false;
        try {
            rs = st.executeQuery("SELECT login FROM users WHERE login='" + login + "' AND password='" + password + "';");
            rc = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rc;
    }

    private void registration() {
        Scanner scannerL = new Scanner(System.in);
        Scanner scannerP = new Scanner(System.in);
        String login, password;

        try {
            writer.write("\nWrite login : ");
            writer.flush();
            login = reader.readLine();

            checkLoginLength(login);

            checkLoginCharacters(login);

            if (goBack) return;

            writer.write("Write password :");
            writer.flush();
            password = reader.readLine();

            checkPasswordLength(password);

            checkPasswordCharacters(password);

            checkUsersForSameLogin(login);

            if (goBack) return;
            rs = null;
            createUser(login, password);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void checkLoginLength(String login) {
        try {
            if (login.length() > 12 || login.length() < 4) {
                writer.write("Login must be with size between 4 and 12 characters.\n");
                writer.flush();
                goBack = true;
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkLoginCharacters(String login) {
        try {
            for (int i = 0; i < login.length(); i++) {
                if (login.charAt(i) >= (char) 48 && login.charAt(i) <= (char) 122) {
                    if (login.charAt(i) > (char) 57 && login.charAt(i) < (char) 65) {
                        writer.write("\n" + "Login contains invalid characters.\n");
                        writer.flush();
                        goBack = true;
                        return;
                    }
                } else {
                    writer.write("\n" + "Login contains invalid characters.");
                    writer.flush();
                    goBack = true;
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkUsersForSameLogin(String login) {
        try {
            rs = st.executeQuery("SELECT login FROM users;");
            while (rs.next()) {
                String logCheck;
                logCheck = rs.getString(1);

                if (logCheck.equalsIgnoreCase(login)) {
                    writer.write("Such user exist.\n");
                    writer.flush();
                    goBack = true;
                    break;
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkPasswordLength(String password) {
        try {
            if (password.length() > 16 || password.length() < 4) {
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

    private void checkPasswordCharacters(String password) {
        try {
            for (int i = 0; i < password.length(); i++) {
                if (password.charAt(i) >= (char) 48 && password.charAt(i) <= (char) 122) {
                    if (password.charAt(i) > (char) 57 && password.charAt(i) < (char) 65) {
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

    private void createUser(String login, String password) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

            st.executeUpdate("INSERT INTO users (login , password, access_id , reg_date) " +
                                  "VALUES ('" + login + "','" + password + "',3 , '" + sdf.format(timestamp.getTime()) + "');");

            writer.write("\nUser " + login + " successfully created.\n");
            writer.write("Log in in your account.\n\n");
            goBack = true;
            return;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    synchronized String editMessage(String message) {
        message.replaceAll("\\s+", " ");
        StringBuffer string = new StringBuffer(message);

        for (int i = 0; i < string.length(); i++) {
            if (message.charAt(i) == (char) 39) {
                string.insert(i, '\'');
            }
        }

        return string.toString();
    }
}