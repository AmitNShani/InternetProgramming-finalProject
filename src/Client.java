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


        //(0,0) to (1,1)
        int[][] source1 = {
                {1, 1, 0},
                {1, 1, 0},
                {1, 1, 0},
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

        int[][] source4 = {
                {100, 100, 100},
                {300, 900, 500},
                {100, 100, 100},
        };

        //send "matrix" command then write 2d array to socket
        toServer.writeObject("matrix");
        toServer.writeObject(source1); //option1

        //toServer.writeObject(source2);//option2

        //toServer.writeObject(source3);//option3

// task1- send "getConnectedComponents" command and receive all the connected components
        toServer.writeObject("getConnectedComponents");
        List<LinkedHashSet<Index>> allCC =  new ArrayList<>((List<LinkedHashSet<Index>>) fromServer.readObject());

        if(allCC.size() == 0){
            System.out.println("There are no connected components");
        }else {
            System.out.println(String.format("\nTask 1: \nThere are %d connected components and they are " +
                    "sorted in ascending order. The components are:", allCC.size()));
            allCC.forEach(System.out::println);
            System.out.println("");
        }

        //task2 -option 1
        toServer.writeObject("start index");
        toServer.writeObject(new Index(0, 0));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(1, 1));
        toServer.writeObject("getShortestPath");
        Collection<List<Index>> shortestPaths = (Collection<List<Index>>) fromServer.readObject();
        System.out.println("Task 2:\nShortest path are:");
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
        System.out.println("\nTask 3(submarines):");
        if (numberOfSubmarines == 0 ){
            System.out.println("Boo the board is empty and there is not even a single submarine");
        }
        else{
            System.out.println("There are " + numberOfSubmarines + " valid submarines");
        }
        System.out.println("");


        // task 4
        toServer.writeObject("matrix");
        toServer.writeObject(source4);
        toServer.writeObject("start index");
        toServer.writeObject(new Index(1, 0));
        toServer.writeObject("end index");
        toServer.writeObject(new Index(1, 2));

        toServer.writeObject("getLightestPath");
        Collection<List<Index>> lightestPaths = (Collection<List<Index>>) fromServer.readObject();
        System.out.println("Task 4:\nLightest paths are: ");
        if(lightestPaths.size() == 0) {
            System.out.println("There are no available allPaths between 2 indexes");
        }else{
            lightestPaths.forEach(System.out::println);
        }

        toServer.writeObject("stop");
        System.out.println("\nclient: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");
    }
}

