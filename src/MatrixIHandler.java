import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * This class handles server.Matrix-related tasks
 */
public class MatrixIHandler implements IHandler {
    private Matrix matrix;
    private List<LinkedHashSet<Index>> connectedComponents;
    private Index startIndex;
    private Index endIndex;
    private volatile boolean doWork = true;
    private TraversableMatrix traversableMatrix;
    private ReentrantReadWriteLock lock =  new ReentrantReadWriteLock();

    @Override
    public void resetMembers() {
        this.matrix = null;
        this.startIndex = null;
        this.endIndex = null;
        this.doWork = true;
        this.connectedComponents = null;
    }

    @Override
    public void handle(InputStream fromClient, OutputStream toClient) throws IOException, ClassNotFoundException {
        /*
        Send data as bytes.
        Read data as bytes then transform to meaningful data
        ObjectInputStream and ObjectOutputStream can read and write both primitives and objects
         */
        ObjectInputStream objectInputStream = new ObjectInputStream(fromClient);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(toClient);
        this.resetMembers();

        boolean doWork = true;
        // handle client's tasks
        while(doWork){

            switch (objectInputStream.readObject().toString()){
                case "matrix":{
                    // client will now send a 2d array. handler will create a matrix object
                    int[][] tempArray = (int[][])objectInputStream.readObject();
                    System.out.println("Server: Got 2d array");
                    this.matrix = new Matrix(tempArray);
                    this.matrix.printMatrix();
                    this.traversableMatrix = new TraversableMatrix(this.matrix);
                    break;
                }

                case "getNeighbors":{
                    // handler will receive an index, then compute its neighbors
                    Index tempIndex = (Index)objectInputStream.readObject();
                    if(this.matrix!=null){
                        List<Index> neighbors = new ArrayList<>(this.matrix.getNeighbors(tempIndex));
                        System.out.println("Server: neighbors of "+ tempIndex + ":  " + neighbors);
                        // send to socket's outputstream
                        objectOutputStream.writeObject(neighbors);
                    }
                    break;
                }

                case "getConnectedComponents":{
                    try {
                        List<LinkedHashSet<Index>> allCC = getConnectedComponents();
                        this.connectedComponents = allCC;
                        objectOutputStream.writeObject(allCC);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;



//                    if(this.matrix!=null){
//                        List<Index> reachables = new ArrayList<>(this.matrix.getReachables(tempIndex));
//                        System.out.println("Server: neighbors of "+ tempIndex + ":  " + reachables);
//                        // send to socket's outputstream
//                        objectOutputStream.writeObject(reachables);
//                    }
                }
                case "submarinesBoard":{
                        Submarines submarines = new Submarines();
                        Boolean isValidSubmarine = submarines.checkValidateSubmarines(this.connectedComponents);
                        objectOutputStream.writeObject(isValidSubmarine);
                    break;
                }

                case "stop":{
                    doWork = false;
                    break;
                }
            }
        }





    }

    /**
     * method for returning all the connected components of the graph concurrently
     * this method use a list of callable and future result.
     * it uses local thread so each thread will have his own stack, set and his own origin
     * @result  is a list of connected components order by the number of vertices in each Connected component */
    private List<LinkedHashSet<Index>> getConnectedComponents() throws InterruptedException{
            List<Index> allIndices = this.matrix.getAllAccessibleNodes();
            if (allIndices.size() == 0) return new ArrayList<>();

            LinkedHashSet<Index> foundIndices = new LinkedHashSet<>();

            List<Future<LinkedHashSet<Index>>> futureConnectedComponents = new ArrayList<>();

            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 10, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

            ThreadLocalDFS threadLocalDfsVisit = new ThreadLocalDFS<Index>();
            for (Index oneNode : allIndices){
                Callable<LinkedHashSet<Index>> threadCCSearch = () -> {
                    try {
                        lock.readLock().lock();
                        if (foundIndices.contains(oneNode)){
                            return new LinkedHashSet<>();
                        }
                    } finally {
                        lock.readLock().unlock();
                    }

                    Set<Index> connectedComponent =  threadLocalDfsVisit
                            .traverse(new TraversableMatrix(this.matrix, oneNode));
                    try {
                        lock.writeLock().lock();
                        if (!foundIndices.contains(oneNode)) {
                            foundIndices.addAll(connectedComponent);
                            return (LinkedHashSet<Index>) connectedComponent;
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }
                    return new LinkedHashSet<>();


                };  // end of callable


                futureConnectedComponents.add(threadPool.submit(threadCCSearch));
                }
            List<LinkedHashSet<Index>> finalList = new ArrayList<>();
            for (Future<LinkedHashSet<Index>> futureCC: futureConnectedComponents) {
                try {
                    LinkedHashSet<Index> connectedComponent = futureCC.get();
                    lock.writeLock().lock();
                    if (connectedComponent.size() > 0){

                        finalList.add(connectedComponent);
                    }

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    lock.writeLock().unlock();
                }

            }
            return finalList.stream().sorted(Comparator.comparingInt(HashSet::size)).collect(Collectors.toList());
    }


}