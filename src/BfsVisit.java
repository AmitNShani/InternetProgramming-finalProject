import java.util.*;

public class BfsVisit<T> {

    ThreadLocal<LinkedList<Map.Entry<Node<T>, Integer>>> workingQueue = ThreadLocal.withInitial(LinkedList::new);
    Collection<Collection<T>> Paths;
    int shortestSteps;

    public BfsVisit(){
        Paths = new ArrayList<>();
        shortestSteps = 0;
    }


    /**
     * Get all shortest allPaths from source to index
     * @param partOfGraph
     * @param destination
     * @return Collection of Collections of T
     */

    public Collection<Collection<T>> traverse(Traversable<T> partOfGraph,Node<T> destination){
        Node<T> source = partOfGraph.getOrigin();//get source from Traversable origin
        source.setParent(null);
        workingQueue.get().clear();

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
                    if (!reachable.equals(currentKey.getParent()) && (current.getValue() < shortestSteps)) {
                        //adding reachable to working list and increase steps by 1
                        workingQueue.get().add(new AbstractMap.SimpleEntry<>(reachable, current.getValue() + 1));
                    }
                }
                // if we reached the destination cell, this is the shortest allPaths
                else {
                        shortestSteps = current.getValue();//save the shortest Paths length
                    List<T> newPath = new ArrayList<>();//we start a new path
                    //go back to all node parents and add to list
                    Node<T> parent = currentKey.getParent();
                    while (parent != null){//add parents to the list
                        currentKey = parent;
                        newPath.add(currentKey.getData());
                        parent = currentKey.getParent();
                    }
                    newPath.add(currentKey.getData());//adding current to this path
                    newPath.add(reachable.getData());//adding destination to this path

                    //save new Paths in all paths
                    Paths.add(newPath);//save new Paths in all possible paths
                    break;
                }
            }
        }

        return Paths;
    }
}
