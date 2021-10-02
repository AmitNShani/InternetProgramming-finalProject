import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket =new Socket("127.0.0.1",8010);
        System.out.println("client: Created Socket");

        ObjectOutputStream toServer=new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream fromServer=new ObjectInputStream(socket.getInputStream());

        // sending #1 matrix
        int[][] source = {
                {1, 1, 0, 0, 0, 1, 0, 0, 1},
                {1, 1, 1, 1, 0, 1, 0, 0, 1},
                {1, 1, 0, 1, 0, 1, 0, 0, 1},
                {0, 0, 1, 1, 0, 1, 0, 0, 1},

        };


        //send "matrix" command then write 2d array to socket
        toServer.writeObject("matrix");
        toServer.writeObject(source);


        // send "getConnectedComponents" command and receive all the connected components
        toServer.writeObject("getConnectedComponents");
        List<LinkedHashSet<Index>> allCC =  new ArrayList<>((List<LinkedHashSet<Index>>) fromServer.readObject());

        if(allCC.size() == 0){
            System.out.println("There are no connected components");
        }else {
            allCC.forEach(System.out::println);
        }

        //task2
        toServer.writeObject("start index");
        toServer.writeObject(new Index(0, 0));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(1, 2));
        toServer.writeObject("getShortestPath");
        Collection<List<Index>> shortestPaths = (Collection<List<Index>>) fromServer.readObject();
        System.out.println("from client - Shortest path are:");
        if(shortestPaths.size() != 0){
            shortestPaths.forEach(result -> System.out.println(result));
        }else
            System.out.println("There are no available path between 2 indexes");

        // task 3 send "submarinesBoard" command and get if the board is valid.
        toServer.writeObject("submarinesBoard");
        int numberOfSubmarines = (int) fromServer.readObject();
        if (numberOfSubmarines == 0 ){
            System.out.println("Boo the board is empty and there is not even a single submarine");
        }
        else{
            System.out.println("there are " + numberOfSubmarines + " valid submarines");
        }




        toServer.writeObject("stop");
        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");
    }
}

