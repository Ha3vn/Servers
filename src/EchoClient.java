import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class EchoClient {

    public static void main(String[] args) throws IOException {

        if(args.length != 2){
            System.err.println("Invalid Host Name/Port Number");
            System.exit(1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        try(
            Socket echoSocket = new Socket(host, port);
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            ){
                String input;
                while((input = stdIn.readLine()) != null){
                    out.println(input);
                    System.out.println("Echo: " + in.readLine());
                }
        } catch(UnknownHostException e){
            System.err.println("Invalid Host: " + host);
            System.exit(1);
        } catch(IOException e){
            System.err.println("IO connection error to: " + host);
            System.exit(1);
        }
    }
}
