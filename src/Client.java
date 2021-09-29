import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Socket socket =new Socket("127.0.0.1",8010);
        System.out.println("client: Created Socket");

        ObjectOutputStream toServer=new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream fromServer=new ObjectInputStream(socket.getInputStream());

        // sending #1 matrix
        int[][] source = {
                {1, 1, 0, 0, 0, 1, 0, 0, 1},
                {1, 1, 0, 1, 0, 1, 0, 0, 1},
                {1, 1, 0, 1, 0, 1, 0, 0, 1},
                {0, 0, 0, 1, 0, 1, 0, 0, 1},

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

        // task 3 send "submarinesBoard" command and get if the board is valid.
        toServer.writeObject("submarinesBoard");
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        boolean isValidRes = (boolean) fromServer.readObject();
        if (isValidRes){
            System.out.println("yayy the Board is valid for submarines game");
        }
        else{
            System.out.println("Booo ain't valid you should rearrange your board game :( ");
        }




        toServer.writeObject("stop");
        System.out.println("client: Close all streams");
        fromServer.close();
        toServer.close();
        socket.close();
        System.out.println("client: Closed operational socket");
    }
}

