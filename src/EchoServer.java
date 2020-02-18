import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

// How to make a Thread - Nathan Dias
//        Runnable r = () -> {
//            System.out.println("yeet");
//        };
//        Thread th = new Thread(r);
//        th.start();
//
//        Thread.sleep(1000);
//        System.out.println("yoot");

public class EchoServer {

    public static void main(String [] args) throws Exception {

        if(args.length != 1){
            System.err.println("Invalid port");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        ServerSocket serverSocket = new ServerSocket(port);
        while(true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> newClient(clientSocket)).start();
        }
    }

    public static void newClient(Socket s){
        try(
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        ){
            String input;
            while((input = in.readLine()) != null)
                out.println("boomer said " + input);
        } catch(IOException e){
            System.out.println("Exception caught trying to listen on port #" + 7 + " or listening for a connection.");
            e.printStackTrace();
        }
        System.out.println("Done");
    }
}
