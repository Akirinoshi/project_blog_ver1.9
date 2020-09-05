import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;

public interface UserData {
    boolean stop = false;
    boolean goBack = false;
    Statement st = null;
    ResultSet rs = null;
     BufferedReader reader = null;
     BufferedWriter writer = null;

     void start();

    void menuFunctions();

    void choice();

    void callFunction(String int_choice);


}
