
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Client {
    /*Properties*/
    private final int _SPORT=2213;
    private String _URL;
    private ArrayList<ServerHandler> _serverHandlers;
    private ExecutorService _pool;
    private Socket _extClientSocket;
    /*Constructor*/
    public Client(String URL, Socket extClientSocket) {
        this._URL=URL;
        this._serverHandlers = new ArrayList<>();
        this._extClientSocket = extClientSocket;

    }

    //Function to check is URL is valid
    private boolean isURLValid(String URL){
        Pattern urlPattern = Pattern.compile("\"^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\"");
        Matcher urlMach = urlPattern.matcher(URL);
        return urlMach.matches();
    }

    //Function that returns separate URLs
    private ArrayList<String> getURLs(String request){
        ArrayList<String> URLs = new ArrayList<>();
        String urlsWithReplace =  request.replace("\\r\\n","#");
        Scanner urlScanner = new Scanner(urlsWithReplace);
        urlScanner.useDelimiter("[\r\n]");
        String str = "";
        while(urlScanner.hasNext())
            str += urlScanner.next();
        String[] separatedURLs=str.split("#");
        for(String url:separatedURLs)
            URLs.add(url+"\r\n");
        //String urlsWithReplace =  request.replace("\\r\\n","#");
        //String[] arrOfUrls = urlsWithReplace.split("#");
        URLs.add("\r\n");
        return URLs;
    }
    private ArrayList<URL> makeURLs(ArrayList<String> arr) throws MalformedURLException {
        ArrayList<URL> URLs = new ArrayList<>();
        for(String url: arr){
            if(url.equals("\r\n"))
                continue;
            URLs.add(new URL(url));
        }
        return URLs;
    }
    public void start() throws IOException {

        ArrayList<String> arrURLs = getURLs(this._URL);
        ArrayList<URL> URLs = makeURLs(arrURLs);
        int nThreads = URLs.toArray().length;
        this._pool = Executors.newFixedThreadPool(nThreads);
        try{
            display("Waiting for Response..");
            for(URL _url:URLs){
                ServerHandler _serverThread = new ServerHandler(_url,this._extClientSocket);
                this._serverHandlers.add(_serverThread);
                this._pool.execute(_serverThread);
            }

        }catch(IOException e){
            e.getMessage();
        }
    }
    //Method that displays me a string in the console
    public static void display(String s){
        System.out.println(s);
    }
}
