import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;

public class Blog extends Initialization {
    private boolean stop;
    private Statement st;
    private ResultSet rs;
    private String login;
    private BufferedReader reader;
    private BufferedWriter writer;

    Blog(Statement st, String login, BufferedReader reader, BufferedWriter writer) {
        this.st = st;
        this.login = login;
        this.reader = reader;
        this.writer = writer;
    }

    Blog() {
    }

    @Override
    public void start() {
        stop = false;
        for (; ; ) {
            if (stop) return;

            menuFunctions();

            choice();
        }
    }

    @Override
    public void menuFunctions() {
        try {
            writer.write("\nMain menu : \n");
            writer.write("1.Blog\n");
            writer.write("2.Messages\n");
            writer.write("3.Statics\n");
            writer.write("4.Settings\n");
            writer.write("9.Exit\n\n");
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
                    blogPageCall();
                    break;
                case "2":
                    callPrivateMessageMenu();
                    break;
                case "3":
                    Statics statics = new Statics(st, login, reader, writer);
                    statics.start();
                    break;
                case "4":
                    writer.write("\nSettings:\n");
                    writer.flush();
                    Settings settings = new Settings(st, login, reader, writer);
                    settings.start();
                    loginUpdate(settings);
                    break;
                case "9":
                    System.out.println("");
                    stop = true;
                    break;
                case "666":
                    checkRightsToCallAdministrationPanel();
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

    private void blogPageCall() {
        BlogPage blogPage = new BlogPage(st, login, reader, writer);
        blogPage.start();
    }

    private void checkRightsToCallAdministrationPanel() {
        try {
            String access_rightsString = "";

            rs = st.executeQuery("SELECT access_id FROM users WHERE login='" + login + "';");
            while (rs.next()) {
                access_rightsString = rs.getString(1);
            }

            if (Integer.parseInt(access_rightsString) < 3) {
                callAdministrationPanel();
            } else {
                writer.write("No such variant\n");
                writer.flush();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void callAdministrationPanel() {
        AdministrationPanel administrationPanel = new AdministrationPanel(st, login, reader, writer);
        administrationPanel.start();
    }

    private void callPrivateMessageMenu() {
        PrivateMessagesMenu privateMessagesMenu = new PrivateMessagesMenu(st, login, reader, writer);
        privateMessagesMenu.start();
    }

    //this method we need if user change login to avoid program error
    private void loginUpdate(Settings settings) {
        login = settings.getLoginNew();
    }
}