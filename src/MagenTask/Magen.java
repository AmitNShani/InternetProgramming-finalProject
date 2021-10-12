package MagenTask;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Magen<T extends Runnable> {
    protected boolean stop = false;
    protected boolean stopNow= false;
    protected final BlockingQueue<PriorityRunnable> taskQueue;
    protected final Thread consumerThread;
    private final ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    List<PriorityRunnable> queueTaskLeft=new ArrayList<>();

    /**
     * ctor of Magen class
     * @param paramBlockingQueue
     */
    public Magen(BlockingQueue<PriorityRunnable> paramBlockingQueue) {
        this.taskQueue = paramBlockingQueue;
        this.consumerThread = new Thread(
                () -> {
                    while ((!stop || !this.taskQueue.isEmpty())){
                        try {
                            taskQueue.take().run();
                        } catch (InterruptedException e) {

                        }
                    }
                });
        this.consumerThread.start();
    }


    /**
     * Submit runnable to queue as priority runnable
     * @param runnable
     */
    public void submitTask(final Runnable runnable) {
        readWriteLock.readLock().lock();
        try { // to avoid deadlock
            if (!stop && !stopNow) // if we don't want to stop now
                taskQueue.offer((PriorityRunnable) runnable);//add runnable to queue
        }finally {
            readWriteLock.readLock().unlock();
        }
    }


    /**
     * Stop the queue :
     * mark as stop and check if the task is not empty and thread is still alive -
     * if so wait till the thread is terminate and then interrupt him
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
            //Lock and check if stop didn't changed while waiting
            readWriteLock.writeLock().lock();
        try {
            if (!stop) {
                stop = true;
                //Check if thread is still alive and Wait until thread finish
                if (this.consumerThread.isAlive() && !taskQueue.isEmpty()) {
                    this.consumerThread.join();//if not empty and still alive wait to terminate
                }
                this.consumerThread.interrupt();
            }
        } catch (InterruptedException interruptedException) {
            throw interruptedException;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * StopNow : stop the queue now and add task that was left in the queue to queueTaskLeft
     * @return queueTaskLeft (tasks that wasn't done)
     * @throws InterruptedException
     */
    public List<PriorityRunnable> stopNow() throws InterruptedException {
        queueTaskLeft=new List<PriorityRunnable>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @Override
            public Iterator<PriorityRunnable> iterator() {
                return null;
            }

            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @Override
            public <T> T[] toArray(T[] a) {
                return null;
            }

            @Override
            public boolean add(PriorityRunnable priorityRunnable) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(Collection<? extends PriorityRunnable> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends PriorityRunnable> c) {
                return false;
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public PriorityRunnable get(int index) {
                return null;
            }

            @Override
            public PriorityRunnable set(int index, PriorityRunnable element) {
                return null;
            }

            @Override
            public void add(int index, PriorityRunnable element) {

            }

            @Override
            public PriorityRunnable remove(int index) {
                return null;
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @Override
            public ListIterator<PriorityRunnable> listIterator() {
                return null;
            }

            @Override
            public ListIterator<PriorityRunnable> listIterator(int index) {
                return null;
            }

            @Override
            public List<PriorityRunnable> subList(int fromIndex, int toIndex) {
                return null;
            }
        };
            //Lock and check if stop didn't changed while waiting
            readWriteLock.writeLock().lock();
            try {
                if (!stopNow) {
                    stopNow = true;
                    // if there is any task left add them to the queueTaskLeft
                    if(!taskQueue.isEmpty())
                        queueTaskLeft.add((PriorityRunnable) taskQueue.remove());
                    this.consumerThread.interrupt();
                }
            } catch (Exception e) {
                throw e;
            } finally {
                readWriteLock.writeLock().unlock();
            }
        return queueTaskLeft;
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        Magen<PriorityRunnable> service = new Magen<>(new PriorityBlockingQueue<>(1));

        service.submitTask(new PriorityRunnable(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task1-priority 1");
            }
        }));

        /*
         submit Runnable tasks to to the queue (as PriorityRunnable objects) using
         the apply methods above
         */
        service.submitTask(new PriorityRunnable(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task2-priority 3");
            }
        },3));


        service.submitTask(new PriorityRunnable(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task3-priority -2");
            }
        },-2));


        service.stop();
        //service.stopNow();
        System.out.println("done");


    }
}