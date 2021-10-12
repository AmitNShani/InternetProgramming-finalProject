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
        Socket socket =new Socket("127.0.0.1",8011);
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

        //(0,0) to (2,2)
        int[][] source1 = {
                {1, 1, 1},
                {1, 1, 0},
                {1, 0, 1},
        };

        //(0,0) to (2,4)
        int[][] source2 = {
                {1, 1, 1, 0, 0},
                {1, 0, 0, 0, 0},
                {1, 0, 0, 0, 1},
                {1, 0, 1, 1, 0},
                {1, 1, 1, 1, 0},
        };

        //(9,8) to (0,9)
        int[][] source3 = {
                {1, 1, 1, 0, 0, 1, 0, 0, 1, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 1, 0},
                {1, 0, 0, 0, 0, 1, 0, 0, 1, 1},
                {1, 0, 1, 1, 0, 1, 0, 0, 1, 1},
                {1, 0, 1, 1, 0, 1, 0, 0, 1, 0},
                {1, 1, 1, 0, 0, 1, 0, 0, 1, 1},
                {1, 0, 0, 0, 0, 1, 0, 0, 1, 0},
                {1, 0, 0, 0, 0, 1, 0, 0, 1, 1},
                {1, 0, 1, 1, 0, 1, 0, 0, 1, 1},
                {1, 0, 1, 1, 0, 1, 0, 0, 1, 0},
        };
        //matrix : source1

        //send "matrix" command then write 2d array to socket
        toServer.writeObject("matrix");
        toServer.writeObject(source1); //option1

        //toServer.writeObject(source2);//option2

        //toServer.writeObject(source3);//option3

        // task1- send "getConnectedComponents" command and receive all the connected components
        toServer.writeObject("getConnectedComponents");
        List<LinkedHashSet<Index>> allCC =  new ArrayList<>((List<LinkedHashSet<Index>>) fromServer.readObject());
        System.out.println("Task 1-from client - Connected Components are:");
        if(allCC.size() == 0){
            System.out.println("There are no connected components");
        }else {
            allCC.forEach(System.out::println);
        }

        //task2 -option 1
        toServer.writeObject("start index");
        toServer.writeObject(new Index(0, 0));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(2, 2));
        toServer.writeObject("getShortestPath");
        Collection<List<Index>> shortestPaths = (Collection<List<Index>>) fromServer.readObject();
        System.out.println("Task 2-from client - Shortest path are:");
        if(shortestPaths.size() != 0){
            shortestPaths.forEach(result -> System.out.println(result));
        }else
            System.out.println("There are no available path between 2 indexes");
        //

        //matrix : source2
       /*
        //task2 - option 2
        toServer.writeObject("start index");
        toServer.writeObject(new Index(0, 0));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(2, 4));
        toServer.writeObject("getShortestPath");
        Collection<List<Index>> shortestPaths = (Collection<List<Index>>) fromServer.readObject();
        System.out.println("from client - Shortest path are:");
        if(shortestPaths.size() != 0){
            shortestPaths.forEach(result -> System.out.println(result));
        }else
            System.out.println("There are no available path between 2 indexes");
*/

        //matrix : source3
/*
        //task2 - option 3
        toServer.writeObject("start index");
        toServer.writeObject(new Index(9, 8));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(0, 9));
        toServer.writeObject("getShortestPath");
        Collection<List<Index>> shortestPaths = (Collection<List<Index>>) fromServer.readObject();
        System.out.println("from client - Shortest path are:");
        if(shortestPaths.size() != 0){
            shortestPaths.forEach(result -> System.out.println(result));
        }else
            System.out.println("There are no available path between 2 indexes");
*/

        // task 3 send "submarinesBoard" command and get if the board is valid.
        toServer.writeObject("submarinesBoard");
        int numberOfSubmarines = (int) fromServer.readObject();
        if (numberOfSubmarines == 0 ){
            System.out.println("Boo the board is empty and there is not even a single submarine");
        }
        else{
            System.out.println("Task 3-There are " + numberOfSubmarines + " valid submarines");
        }


        toServer.writeObject("stop");
        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");
    }
}

