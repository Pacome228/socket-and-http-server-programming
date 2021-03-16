
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    /*Properties*/
    public static final int _SPORT= 2213;
    public ArrayList<ClientHandler> _clientHandlers;
    public ExecutorService _pool;

    /*Constructor*/
    public  Server(int nThreads){
        //initialize the server properties
        this._clientHandlers = new ArrayList<>();
        this._pool = Executors.newFixedThreadPool(nThreads);
    }
    /*Methods*/
    //Method that starts returns the server socket
    public void start() throws  IOException, NullPointerException{
        ServerSocket _severSocket = new ServerSocket(_SPORT);
        //BufferedReader _in = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            if(System.in.toString().equals("quit"))
                break;
            else{
                try{
                    display(String.format("Server started listening on PORT %1$s..", _SPORT));
                    //Wait to receive a new client
                    Socket extClientSocket = _severSocket.accept();
                    //Create a thread to handle the connected client
                    ClientHandler _clientThread = new ClientHandler(extClientSocket);
                    //Add the thread to our clientHandlers
                    _clientHandlers.add(_clientThread);
                    //Execute the thread
                    _pool.execute(_clientThread);

                }catch(IOException e){
                    display(e.getMessage());
                }
            }
        }
    }
    //Method that displays me a string in the console
    public static void display(String s){
        System.out.println(s);
    }

}
