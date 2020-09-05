import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Main {
    public static int onLine = 0;

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/blog",
                "app", "p@ssw0rd");
        Statement st = conn.createStatement();

        try {
            ServerSocket server = new ServerSocket(3345);
            while (true) {
                Socket socket = server.accept();

                onLine++;
                System.out.println("Client connected.");
                System.out.println("Current online :" + onLine);
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Runnable runnable = new ThreadEchoHandler(socket, st, reader, writer);
                Thread thread = new Thread(runnable);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}