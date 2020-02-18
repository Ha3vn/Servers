import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Michael Liu
 */

public class Server {

    private static final int PORT_NUMBER = 8080;
    private static final int TIMEOUT = 15;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    private static final ConcurrentHashMap<File, String> FILE = new ConcurrentHashMap<>();

    public static void main(String[] args){
        try {
            ServerSocket sSocket = new ServerSocket(PORT_NUMBER);
            while(true){
                Socket s = sSocket.accept();
                new Thread(() -> clientInstance(s)).start();
            }
        } catch(IOException ignored){}
    }

    private static void clientInstance(Socket clientSocket){
        try {
            clientSocket.setSoTimeout(TIMEOUT * 1000);
            HashMap<String, String> headers = new HashMap<>();
            Scanner scanner = new Scanner(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            String next; String temp; String request = "";
            if(scanner.hasNextLine() && !(temp = scanner.nextLine()).equals(""))
                request = temp;
            else
                respond(dos, 400);
            while(scanner.hasNextLine()){
                next = scanner.nextLine();
                if(next.equals("")) break;
                int index = next.indexOf(":");
                headers.put(next.substring(0, index).replaceAll("\\s+",""), next.substring(index + 1).trim());
            }
            String[] r = request.split(" ");
            if(!r[2].equals("HTTP/1.1"))
                respond(dos, 400);
            handleRequests(dos, scanner, headers, r[0], r[1]);
            clientSocket.close();
        }catch(IOException | ParseException ignored) {}
    }

    private static void writeHeader(StringBuilder sb, int responseCode){
        sb.append("HTTP/1.1 ").append(responseCode);
        switch(responseCode){
            case 200:
                sb.append("OK").append("\r\n"); break;
            case 304:
                sb.append("Not Modified").append("\r\n"); break;
            case 400:
                sb.append("Bad Request").append("\r\n"); break;
            case 404:
                sb.append("Not Found").append("\r\n"); break;
        }
        sb.append("Date: ").append(DATE_FORMAT.format(new Date())).append("\r\n");
        sb.append("Server: M/1.0 (MacOS)\r\n");
    }

    private static void writeFile(DataOutputStream dos, File file, String dateModified) throws ParseException, IOException {
        StringBuilder sb = new StringBuilder();
        if(dateModified != null && file.lastModified() < DATE_FORMAT.parse(dateModified).getTime()) {
            writeHeader(sb, 304);
            sb.append("Content-Length: ").append(file.length()).append("\r\n");
            sb.append("\r\n");
        }
        else{
            writeHeader(sb, 200);
            sb.append("Content-Length: ").append(file.length()).append("\r\n");
            sb.append("\r\n");
            if(FILE.containsKey(file))
                sb.append(FILE.get(file));
            else{
                try{
                    FILE.put(file, new String(Files.readAllBytes(file.toPath())));
                    sb.append(FILE.get(file));
                }
                catch(IOException | OutOfMemoryError | SecurityException ignored){}
            }
        }
        dos.write(sb.toString().getBytes());
    }

    private static void respond(DataOutputStream dos, int responseCode) throws IOException {
        StringBuilder sb = new StringBuilder();
        writeHeader(sb, responseCode);
        sb.append("Content-Length: ").append(0).append("\r\n").append("\r\n");
        dos.write(sb.toString().getBytes());
    }

    private static void handleRequests(DataOutputStream dos, Scanner scanner, HashMap<String, String> headers, String req, String fileName) throws IOException, ParseException {
        File file;
        if(req.equals("GET")){
            String pathname = "." + (headers.get("Host") == null ? "" : "/" + headers.get("Host") + "/") + fileName;
            System.out.println("Get: " + pathname);
            file = new File(pathname);
            if(file.exists())
                writeFile(dos, file, headers.get("If-Modified-Since"));
            else
                respond(dos, 404);
        }else if(req.equals("POST")){
            respond(dos, 200);
            StringBuilder sb = new StringBuilder();
            scanner.useDelimiter("");
            for(int i = 0; i < Integer.parseInt(headers.get("Content-Length")); i++){
                sb.append(scanner.next());
            }
            String pathname = "." + (headers.getOrDefault("Host", null) == null ? "" : "/" + headers.get("Host") + "/") + fileName;
            file = new File(pathname);
            System.out.println("Post: " + pathname);
            Files.write(file.toPath(), sb.toString().getBytes());
        }else
            respond(dos, 400);
    }
}