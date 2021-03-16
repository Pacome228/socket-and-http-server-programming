
import java.io.*;
import java.net.Socket;
import java.io.BufferedReader;
import java.util.Scanner;

public class ClientHandler implements Runnable{
    /*Properties*/
    private final Socket _extClientSocket;
    private Client _client;
    private String _URL="";
    /*Constructor*/
    public ClientHandler(Socket clientSocket) throws IOException {
        this._extClientSocket = clientSocket;
    }
    /*Methods*/
    public static void display(String s){
        System.out.println(s);
    }
    //Function that returns data read from the socket stream
    private String readData(Socket _socket) throws IOException {
        InputStream _in = _socket.getInputStream();
        byte[] dataBytes=new byte[1024];
        String dataString = "";
        while(dataString.indexOf("\r\n")==-1){
            int bytesRead = _in.read(dataBytes);
            if(bytesRead > 0)
                dataString+=new String(dataBytes,0,bytesRead);
        }
        return dataString;
    }
    //Function the writes on the socket stream
    private void writeData(String data, Socket _socket) throws IOException {
        OutputStream _out = _socket.getOutputStream();

        for (char c:data.toCharArray()) {
            _out.write((byte)c);
        }
        _out.close();
    }

    @Override
    public void run() {
        display("Client connected!"+_extClientSocket);
        try{
            this._URL= readData(this._extClientSocket);

            if(this._URL.equals("QUIT") || this._URL.equals("quit")
                    ||this._URL.equals("q")||this._URL.equals("Q")){
                this._extClientSocket.close();
            }
            else if(this._URL.contains("http")){
                this.display(String.format("You requested for : %1$s ",this._URL));
                this._client = new Client(this._URL, this._extClientSocket); // Pass the URL read to the client side of Bridge
                this._client.start();//Start the the client
            }
            else
                this.writeData("Not valid URL\n",this._extClientSocket);
        }catch (IOException e){
            System.err.println(e.getStackTrace());
        }

    }
}
