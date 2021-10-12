package MagenTask;

/**
 * this class define a runnable task and assign it a priority
 *implements Runnable to run the task and Comparable to sort the tasks  */

public class PriorityRunnable implements Runnable, Comparable<PriorityRunnable>{
    private int priority;
    private final Runnable runnable;

    public PriorityRunnable(Runnable runnable, int priority) {
        this.priority = priority;
        this.runnable = runnable;
    }
    public PriorityRunnable(Runnable runnable) {
        this(runnable, 1);
    }

    public PriorityRunnable(Object aRunnableTask, int priority){
        this.runnable= (Runnable) aRunnableTask;
        this.priority = priority;
    }


    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }



    @Override
    public int compareTo(PriorityRunnable second) {
        return Integer.compare(this.priority, second.getPriority());
    }

    @Override
    public void run() {
        if (runnable != null){
            this.runnable.run();
        }
    }
}
