import java.util.*;

public class DfsVisit<T> {
    Stack<Node<T>> workingStack; // for discovered nodes
    Set<Node<T>> finished;

    public DfsVisit(){
        workingStack = new Stack<>();
        finished = new LinkedHashSet<>(); // maintains insertion order
    }

    public List<T> traverse(Traversable<T> someGraph){
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
        workingStack.push(someGraph.getOrigin());
        while(!workingStack.isEmpty()) {
            Node<T> popped = workingStack.pop();
            finished.add(popped);
            Collection<Node<T>> reachableNodes = someGraph.getReachableNodes(popped);
            for (Node<T> singleReachableNode : reachableNodes) {
                if (!finished.contains(singleReachableNode) && !workingStack.contains(singleReachableNode)) {
                    workingStack.push(singleReachableNode);
                }
            }
        }
            List<T> connectedComponent = new ArrayList<>();
            for(Node<T> node: finished) connectedComponent.add(node.getData());
        return connectedComponent;
        /*
    [1,0,0,1]
    [1,1,0,1]
    [0,1,1,0]
    Stack:
    Finished:
    (0,0)
    (1,0)
    (1,1)
    (2,1)
    (2,2)

    reachableList:

         */

    }
}
