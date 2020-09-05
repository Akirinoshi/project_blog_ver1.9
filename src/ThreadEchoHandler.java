import java.io.*;
import java.net.Socket;
import java.sql.Statement;

class ThreadEchoHandler extends Main implements Runnable {
    private Socket socket;
    private Statement st;
    private BufferedReader reader;
    private BufferedWriter writer;

    public ThreadEchoHandler(Socket socket, Statement st, BufferedReader reader, BufferedWriter writer) {
        this.socket = socket;
        this.st = st;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void run() {
        try {
            writer.write("\nHello , this is blog created by Dmitriy Belkov.\n");
            writer.flush();
            UserData initialization = new Initialization(st, reader, writer);
            initialization.start();
            socket.close();
            Main.onLine--;
            System.out.println("Client disconnected.");
            System.out.println("Current online :" + Main.onLine);
        } catch (IOException e) {
            Main.onLine--;
            e.printStackTrace();
        }
    }
}