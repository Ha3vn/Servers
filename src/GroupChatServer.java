import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupChatServer {

    public static void main(String [] args) throws Exception {

        if(args.length != 1){
            System.err.println("Invalid port");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        ServerSocket serverSocket = new ServerSocket(port);
        Socket clientSocket = serverSocket.accept();
        Set<PrintWriter> outputs = ConcurrentHashMap.newKeySet();
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        outputs.add(out);
        Scanner scanner = new Scanner(System.in);
        new Thread(() -> serverInput(outputs, scanner)).start();

        while(true) {
            Socket copySocket = clientSocket;
            PrintWriter copyOut = out;
            new Thread(() -> {
                try {chatClient(copySocket, copyOut, outputs);}
                catch (IOException e){e.printStackTrace();}
            }).start();
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            outputs.add(out);
        }
    }

    public static void chatClient(Socket s, PrintWriter out, Set<PrintWriter> outputs) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        new Thread(() -> clientInput(in, out, outputs)).start();
        System.out.println("Done");
    }

    public static void clientInput(BufferedReader in, PrintWriter out, Set<PrintWriter> outputs){
        try {
            String input;
            while ((input = in.readLine()) != null) {
                System.out.println(input);
            }
            outputs.remove(out);
        } catch(IOException e){
            System.out.println("Exception caught trying to listen on port #" + 7 + " or listening for a connection.");
            e.printStackTrace();
        }
    }

    public static void serverInput(Set<PrintWriter> outputs, Scanner scanner){
        while(scanner.hasNextLine()) {
            String s = scanner.nextLine();
            for(PrintWriter out : outputs)
                out.println(s);
        }
    }
}
