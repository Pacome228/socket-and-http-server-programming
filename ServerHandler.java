
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServerHandler implements Runnable {
    private int _SPORT=2213;
    private final URL _URL;
    private Socket _extClientSocket;
    private OutputStream _extClient0ut;

    /*Constructor*/
    public ServerHandler(URL url, Socket extClientSocket) throws IOException {
        this._URL = url;
        this._extClientSocket=extClientSocket;
        this._extClient0ut=extClientSocket.getOutputStream();
    }

    //function that returns a valid request
    private String buildRequest(URL inputURL){

        String host = inputURL.getHost();
        String path = inputURL.getPath();
        //for input that are empty of \r\n only the host yields localhost
        String request = new StringBuilder()
                .append("GET "+ (path.length()==0?"/":path) +" HTTP/1.1\r\n")
                .append("Host: "+(host.length()<=2?"localhost:2213":host)+"\r\n")
                //.append("User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:36.0) Gecko/20100101 Firefox/36.0\r\n")
                .append("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36\r\n")
                .append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n")
                .append("Accept-Language: fr,fr-FR;q=0.8,en-US;q=0.5,en;q=0.3\r\n")
                //.append("Accept-Encoding: gzip, deflate\r\n")
                .append("Connection: keep-alive\r\n")
                .append("\r\n")
                .toString();
        return  request;
    }
    //Method that displays me a string in the console
    public static void display(String s){
        System.out.println(s);
    }
    //Function the writes back data to client Stream
    private void writeDataToClient(String data) throws IOException {
        this._extClient0ut.flush();
        this._extClient0ut.write(data.getBytes());
        this._extClient0ut.flush();
        this._extClient0ut.write("\n***************************************\n".getBytes());
        this._extClient0ut.flush();
    }
    //Returns a response code
    private int getResponseCode(String data){
        String code= data.substring("HTTP/1.1".length()+1,"HTTP/1.1".length()+4);
        return Integer.parseInt(code);
    }
    //Returns a response content as a string
    private String getResponseContent(String data){
        String content = data.substring(data.indexOf("<!doctype html>"));
        return content;
    }
    //Returns a response Location as a string
    private  String getResponseLocation(String data){
        String location = data.substring(data.indexOf("Location:")+
                "Location:".length()+1,data.indexOf("Cache-Control:")-1);
        return location;
    }
    //Function that Redirects to the new link
    private void redirectTo(String link) throws IOException {
        URL _newURL = new URL(link);
        if(_newURL.getProtocol().equals("https")){
            String message = this._URL.toString()+"(Https URL are not allowed, please use HTTP protocol!)";
            writeDataToClient(message);
        }
        else {
            sendGetRequest(_newURL);
        }
    }
    //Function that returns tokens from string
    private ArrayList<String> tokenize(String data){
        ArrayList<String> tokens = new ArrayList<String>();
        String delimiter = " \n\r\t";
        String dataNoHtml= data.replaceAll("\\<.*?>","");
        StringTokenizer _tokenizer= new StringTokenizer(dataNoHtml,delimiter);
        while(_tokenizer.hasMoreTokens())
            tokens.add(_tokenizer.nextToken());
        return tokens;
    }
    //Function that returns number of occurrences
    private int numberOfOccurrences(String word, ArrayList<String> tokens){
        int count = 0;
        for(int i= 0; i<tokens.size();i++){
            if(word.equals(tokens.get(i)))
                count++;
        }
        return count;
    }
    //Returns statistics of tokens:<token, occurrence> as a hash table
    private SortedMap<Integer, String> statistics(ArrayList<String> tokens){
        SortedMap<Integer, String> sm = new TreeMap<Integer, String>();
        for(String token: tokens)
            sm.put(numberOfOccurrences(token, tokens), token);
        return sm;
    }
    //Function that sends request to external server and receive response and send back to external client
    private synchronized void sendGetRequest(URL url) throws IOException {
        //Try sending and reading the response
        try {
                Socket _extServerSocket = new Socket(url.getHost(),url.getDefaultPort());
                String request = buildRequest(url);
                BufferedOutputStream _out = new BufferedOutputStream(_extServerSocket.getOutputStream());

                //Write Request
                _out.write(request.getBytes());
                _out.flush();

                //Read response
                BufferedInputStream _in = new BufferedInputStream(_extServerSocket.getInputStream());
                String data = "";
                int stream;

                byte[] bytes=new byte[1024];
                while ((stream=_in.read(bytes)) !=-1){
                    data+=new String(bytes, 0, stream);
                }
                int code = getResponseCode(data);
                if(code==301 ||code==302 ||code ==303){
                    String location =  getResponseLocation(data);
                    redirectTo(location);
                }
                else{
                    String content = getResponseContent(data);
                    ArrayList<String> tokens = tokenize(content);
                    SortedMap<Integer, String> stats = statistics(tokens);
                    Set _set = stats.entrySet();
                    Iterator _i= _set.iterator();
                    ArrayList<Map.Entry<Integer, String>> listStat = new ArrayList<Map.Entry<Integer,String>>();
                    String result = "";
                    while(_i.hasNext()){
                        Map.Entry me = (Map.Entry)_i.next();
                        listStat.add(me);
                    }
                    for(int i=listStat.size()-1; i>listStat.size()-11; i--){
                        result += listStat.get(i).getValue().toString()+" --> "+listStat.get(i).getKey().toString()+"\r\n";
                        //display(listStat.get(i).getValue().toString()+" --> "+listStat.get(i).getKey().toString());
                    }
                    writeDataToClient(result);
                }
                //writeDataToClient(location);
                //writeDataToClient(String.valueOf(code));
                //Write back to the external client
                //writeDataToClient(data);

                //display(data);
                _out.close();
                _in.close();
                _extServerSocket.close();

        } catch (IOException e) {
            e.getMessage();
        }
    }

    @Override
    public void run() {
        try{
            if(this._URL.getProtocol().equals("https")){
                String message = this._URL.toString()+"(Https URL are not allowed, please use HTTP protocol!)";
                writeDataToClient(message);
            }
            else{
                sendGetRequest(this._URL);
            }
        }catch(IOException e){
            e.getMessage();
        }finally {
            try {
                this._extClient0ut.close();
                this._extClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
