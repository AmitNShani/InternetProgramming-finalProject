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
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 20, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>());


    @Override
    public void resetMembers() {
        this.matrix = null;
        this.startIndex = null;
        this.endIndex = null;
        this.doWork = true;
        this.connectedComponents = null;
    }

    @Override
    public void handle(InputStream fromClient, OutputStream toClient) throws Exception {
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
                    lock.writeLock().lock();
                    // client will now send a 2d array. handler will create a matrix object
                    int[][] tempArray = (int[][])objectInputStream.readObject();
                    System.out.println("Server: Got 2d array");
                    this.matrix = new Matrix(tempArray);
                    this.matrix.printMatrix();
                    this.traversableMatrix = new TraversableMatrix(this.matrix);
                    lock.writeLock().unlock();
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

                case "getConnectedComponents":{ //task1
                    try {
                        List<LinkedHashSet<Index>> allCC = getConnectedComponents();
                        this.connectedComponents = allCC;
                        objectOutputStream.writeObject(allCC);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                }
                case "start index": {
                    try {
                        this.startIndex = (Index) objectInputStream.readObject();
                    } catch (ClassCastException e) {
                        throw new Exception("Invalid source");
                    }
                    break;
                }

                case "end index": {
                    try {
                        this.endIndex = (Index) objectInputStream.readObject();
                    } catch (ClassCastException e) {
                        throw new Exception("Invalid dest");
                    }
                    break;
                }
                case "getShortestPath":{ //task2
                    Collection<Collection<Index>> path = getShortestPath();
                    objectOutputStream.writeObject(path); //return to client
                    break;
                }
                case "submarinesBoard":{ //task3
                        Submarines submarines = new Submarines();
                        int isValidSubmarine = submarines.checkValidateSubmarines(this.connectedComponents);
                        objectOutputStream.writeObject(isValidSubmarine);
                    break;
                }
                //Task 4
                case "getLightestPath":{
                    Collection<Collection<Index>> lightestPaths = getLightestPaths();
                    //return to client
                    objectOutputStream.writeObject(lightestPaths);
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

    /**
     * get shortest path of matrix from source to destination indexes.
     * @return Collection<Collection<Index>> of the shortest path
     * @throws Exception
     */
    private Collection<Collection<Index>> getShortestPath()throws Exception {
        validateMatrix();
        TraversableMatrix traversableMatrix = new TraversableMatrix(this.matrix);
        validateIndexes(traversableMatrix);//validate end and start indexes
        ThreadLocalBfsVisit<Index> threadLocalBfsVisit;
        Collection<Collection<Index>> traverseCollection;
        try {// to avoid the deadlock situations
            lock.writeLock().lock();
            threadLocalBfsVisit = new ThreadLocalBfsVisit<>();
            traversableMatrix.setStartIndex(this.startIndex);
            traverseCollection = threadLocalBfsVisit.traverse(traversableMatrix, new Node(this.endIndex));
        } finally {
            lock.writeLock().unlock();
        }
        return traverseCollection;
    }

    /**
     * validate that the matrix is not null
     * @throws NullPointerException
     */
    private void validateMatrix() throws NullPointerException{
        if(matrix == null){
            throw new NullPointerException("Matrix not found");
        }
    }

    private Collection<Collection<Index>> getLightestPaths() throws Exception {
        validateMatrix();
        TraversableMatrix traversableMatrix = new TraversableMatrix(matrix);
        validateIndexes(traversableMatrix);

        DijkstraVisit<Index> DijkstraVisit = new DijkstraVisit<>();

        Collection<Map.Entry<List<Index>, Integer>> pairs = DijkstraVisit.getLightestPaths(traversableMatrix, new ArrayList<>(), new Node<>(startIndex), new Node<>(endIndex));
        return pairs.stream().map(p -> reverse(p.getKey())).collect(Collectors.toList());
    }

    private List<Index> reverse(List<Index> list){
        Collections.reverse(list);
        return list;
    }

    /**
     * validate start and end indexes - make sure it's between matrix boarders
     * @param traversableMatrix
     * @throws IndexOutOfBoundsException
     */
    private void validateIndexes(TraversableMatrix traversableMatrix) throws IndexOutOfBoundsException{
        if((startIndex == null) || (endIndex == null))
        {
            throw new IndexOutOfBoundsException("Index not found");
        }
        if ((traversableMatrix.isValidIndex(this.startIndex)==false) || (traversableMatrix.isValidIndex(this.endIndex)==false)){
            throw new IndexOutOfBoundsException("Source is out of matrix");
        }
    }
}
