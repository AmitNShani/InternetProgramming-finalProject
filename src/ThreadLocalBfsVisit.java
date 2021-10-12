import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ThreadLocalBfsVisit<T> {

    ThreadLocal<LinkedBlockingQueue<Map.Entry<Node<T>, Integer>>> workingQueue = ThreadLocal.withInitial(LinkedBlockingQueue::new);
    Collection<Collection<T>> Paths;
    int shortestPath;

    public ThreadLocalBfsVisit(){
        Paths = new ArrayList<>();
        shortestPath =Integer.MAX_VALUE;;
    }

/**
     * Get all shortest Paths from source to index
     * @param partOfGraph
     * @param destination
     * @return Collection of Collections of T
     */


    public Collection<Collection<T>> traverse(Traversable<T> partOfGraph,Node<T> destination){
        Node<T> source = partOfGraph.getOrigin();//get source from Traversable origin
        source.setParent(null);
        workingQueue.get().clear();

        //if start or end are not 1 we return empty path
        if(partOfGraph.getValue(source.getData()) == 0 || partOfGraph.getValue(destination.getData()) == 0){
            return Paths;
        }

        // Create a linked list for BFS and the Distance of source cell is 0
        workingQueue.get().add(new AbstractMap.SimpleEntry<>(source,0));

        while (!workingQueue.get().isEmpty())// bfs and start with the source node
        {
            //Remove first item in queue and save as current
            Map.Entry<Node<T>,Integer> current = workingQueue.get().remove();//remove first
            Node<T> currentKey = current.getKey();//get the current key

            //Finding reachable nodes from current node incloding the diagonal
            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(currentKey);

            for (Node<T> reachable : reachableNodes)
            {
            //check if not visited (as parent) && current path lower then shortest path
                if (!reachable.getData().equals(destination.getData())) {
                    if (!reachable.equals(currentKey.getParent()) && (current.getValue() < shortestPath)) {
                        //adding reachable to working list and increase steps by 1
                        workingQueue.get().add(new AbstractMap.SimpleEntry<>(reachable, current.getValue() + 1));
                    }
                }
                // if we reached the destination cell, this is the shortest allPaths
                else {
                    shortestPath = current.getValue();//save the shortest Paths length
                    List<T> newPath = new ArrayList<>();//we start a new path
                    newPath.add(reachable.getData());//adding destination to this path
                    newPath.add(currentKey.getData());//adding current to this path
                    //go back to all node parents and add to list
                    Node<T> parent = currentKey.getParent();
                    while (parent != null){//add parents to the list
                        currentKey = parent;
                        newPath.add(currentKey.getData());
                        parent = currentKey.getParent();
                    }
                    Collections.reverse(newPath);
                    //save new Paths in all paths
                    Paths.add(newPath);//save new Paths in all possible paths
                    break;
                }
            }
        }

        return getMinWithStreams(Paths.toArray(),shortestPath+1);
    }

    /**
     * get all paths that equals to the shortest path length
     * @param objects
     * @param shortestPath
     * @return Collection of Collections of T
     */
    public Collection<Collection<T>> getMinWithStreams(Object[] objects,int shortestPath ) {
        return this.Paths.stream()
                .filter(index -> index.size() == shortestPath)
                .collect(Collectors.toList());
    }
}

