import java.util.Collection;

/**
 * This interface defines the functionality required for a traversable graph
 */
public interface Traversable<T> {
    public Node<T> getOrigin();
    public int getValue(T index);
    
    public Collection<Node<T>>  getReachableNodes(Node<T> someNode);
    public Collection<Node<T>>  getNeighborNodes(Node<T> someNode);

}
