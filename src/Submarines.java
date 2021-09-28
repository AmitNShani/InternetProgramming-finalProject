import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Submarines {
    List<Set<Index>> connectedComponent;
    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 10, 10,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    List<Future<Boolean>> futures = new ArrayList<>();

    public Submarines() {

    }
    public boolean checkValidateSubmarines(List<LinkedHashSet<Index>> connectedComponents){
        if (connectedComponents.isEmpty()) return  false;
        for (LinkedHashSet<Index> connectedComponent : connectedComponents){
            //Callable<Boolean> taskThread =
            futures.add(threadPool.submit(() -> {
                if (connectedComponent != null && connectedComponent.size() > 1){
                    int top = Integer.MAX_VALUE,bottom = -1, left = Integer.MAX_VALUE, right = -1;
                    for (Index vertex : connectedComponent){
                        int row = vertex.getRow(), col = vertex.getColumn();
                        if (col > right)
                            right = col;
                        if (col < left)
                            left = col;
                        if (row < top)
                            top = row;
                        if (row > bottom)
                            bottom = row;
                    }
                    return connectedComponent.size() == ((right - left + 1) * (bottom - top + 1));
                }
                return false;
            }));

        }
        for (Future<Boolean> future : futures){
            try {
                if (!future.get()) return false;
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
