import java.util.*;

/**
 * When we need to make sure that every thread has its own local data structures  - synchronization
 * is not the solution.
 * TLS- Thread Local Storage
 */
public class ThreadLocalDFS<T> {
    // ForkJoinPool
    // SparkRDD
    final ThreadLocal<Stack<Node<T>>> threadLocalStack = ThreadLocal.withInitial(() -> new Stack<Node<T>>()); // lambda expression
//    final ThreadLocal<Stack<Node<T>>> threadLocalStack2 = ThreadLocal.withInitial(Stack::new); // method reference
    final ThreadLocal<Set<Node<T>>> threadLocalSet = ThreadLocal.withInitial(LinkedHashSet::new);
    final ThreadLocal<Node<T>> threadLocalOrigin = new ThreadLocal<>();
    public Set<T> traverse(Traversable<T> someGraph) {
        /*
        push origin to the Stack V
        while stack is not empty: V
            removed = pop operation V
            insert to finished V
            invoke getReachableNodes method on removed node V
            for each reachableNode: V
                if current reachableNode is NOT in stack (just discovered)
                &&  current reachableNode is NOT in finished
                push to stack
         */
        threadLocalSet.get().clear();
        threadLocalOrigin.set(someGraph.getOrigin());
        threadLocalStack.get().push(threadLocalOrigin.get());
        while (!threadLocalStack.get().isEmpty()) {
            Node<T> popped = threadLocalStack.get().pop();
            threadLocalSet.get().add(popped);
            Collection<Node<T>> reachableNodes = someGraph.getReachableNodes(popped);
            for (Node<T> singleReachableNode : reachableNodes) {
                if (!threadLocalSet.get().contains(singleReachableNode) && !threadLocalStack.get().contains(singleReachableNode)) {
                    threadLocalStack.get().push(singleReachableNode);
                }
            }
        }
        Set<T> connectedComponent = new LinkedHashSet<>();
        for (Node<T> node : threadLocalSet.get()) connectedComponent.add(node.getData());
        return connectedComponent;
    }
}

