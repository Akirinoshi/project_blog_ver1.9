import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;

public class AdministrationPanelFindUser extends AdministrationPanel {
    private boolean stop;
    private Statement st;
    private ResultSet rs;
    private BufferedReader reader;
    private BufferedWriter writer;

    AdministrationPanelFindUser(Statement st, BufferedReader reader, BufferedWriter writer) {
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
            writer.write("\n1.By registration date (from/to)\n");
            writer.write("2.By access rights\n");
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
                    findUserByRegistrationDate();
                    break;
                case "2":
                    findUserByAccessRights();
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

    private void findUserByRegistrationDate() {
        try {
            try {
                String dateFrom, dateTo;

                writer.write("Write registration date in format ****-**-** and time if need **:**:**\n");
                writer.write("From : ");
                writer.flush();
                dateFrom = reader.readLine();
                writer.write("\nTo : ");
                writer.flush();
                dateTo = reader.readLine();

                writer.write("\nLogin\tRegistration time\n");
                writer.flush();

                rs = st.executeQuery("SELECT login , reg_date " +
                        "FROM users WHERE reg_date " +
                        "between '" + dateFrom + "'::timestamp and '" + dateTo + "'::timestamp order by reg_date;");

                while (rs.next()) {
                    writer.write(rs.getString("login") + "\t\t" + rs.getString("reg_date\n"));
                    writer.flush();
                }
                stop = true;
            } catch (SQLException e) {
                writer.write("Invalid input");
                writer.flush();
                stop = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            stop = true;
        }
    }

    private void findUserByAccessRights() {
        try {
            String access_right;

            writer.write("\nWhich group you're want to choose?\n");
            writer.write("Access_rights :\n");
            writer.flush();
            allRightsList();

            access_right = reader.readLine();
            writer.newLine();

            rs = st.executeQuery("SELECT login FROM users WHERE access_id ='" + Integer.parseInt(access_right) + "';");

            while (rs.next()) {
                writer.write(rs.getString("login") +"\n");
                writer.flush();
                stop = true;
            }
        } catch(SQLException | NumberFormatException| IOException exc) {
            try {
                writer.write("Invalid input\n");
                writer.flush();
                stop = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
    }

    private void allRightsList() {
        try {
            rs = st.executeQuery("SELECT * FROM access_rights WHERE access_id > 0;");

            while (rs.next()) {
                writer.write(rs.getString("access_id") + ". " + rs.getString("access_name") + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}