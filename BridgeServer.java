
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BridgeServer {

    public static Server _server;
    public static Client _client;

    /*Constructor*/
    public BridgeServer(){}
    /*Class methods*/
    //function that will start the server at static port given
    public static void  startBridge(int nThreads) throws IOException {
        _server = new Server(nThreads);
        _server.start();

    }
    //Function that displays me a string in the console
    public static void display(String s){
        System.out.println(s);
    }
    public static void main(String[] args) {
        int nThreads = Integer.parseInt(args[0]);//getting the first parsed arg
        try{
            startBridge(nThreads);// starting the server with maximum threads

        }catch (IOException e){
            display(e.getMessage());
        }
    }
}
