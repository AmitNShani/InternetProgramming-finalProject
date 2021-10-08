package MagenTask;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class Magen<T extends Runnable> {
    private final Function<Runnable, T> defaultFunction;
    protected boolean stop = false;
    protected boolean stopNow= false;
    protected final BlockingQueue<T> taskQueue;
    protected final Thread consumerThread;
    private final ReentrantReadWriteLock readWriteLock=new ReentrantReadWriteLock();
    List<PriorityRunnable> queueTskLeft;

    public Magen(Function<Runnable, T> defaultFunction, BlockingQueue<T> paramBlockingQueue) {
        this.defaultFunction = defaultFunction;
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


    public Magen(BlockingQueue<T> paramBlockingQueue,Function<Runnable,T> runnableTFunction) {

        this.taskQueue = paramBlockingQueue;
        this.defaultFunction = runnableTFunction;

        this.consumerThread = new Thread(
                () -> {
                    while ((!stop || !this.taskQueue.isEmpty()) &&
                            (!stopNow)) {
                        int a = 1;
                        try {
                            taskQueue.take().run();
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                });

        this.consumerThread.start();
    }

    /**
     * Add runnable to queue based on default runnableTFunction
     * Use apply(final Runnable runnable,Function<Runnable,T> runnableTFunction)
     * @param runnable
     * @throws InterruptedException
     */
    public void apply(final Runnable runnable) throws InterruptedException{
        this.apply(runnable,defaultFunction);
    }


    /**
     * Add Callable to queue based on default runnableTFunction
     * Use apply(final Callable<V> callable,Function<Runnable,T> runnableTFunction)
     * @param callable
     * @param <V>
     * @return Future <V>
     * @throws InterruptedException
     */
    public<V> Future<V> apply(final Callable<V> callable) throws InterruptedException{
        return this.apply(callable,defaultFunction);
    }


    /**
     * Add runnable to the queue based on runnableTFunction
     * @param runnable
     * @param runnableTFunction
     * @throws InterruptedException
     */
    public void apply(final Runnable runnable,Function<Runnable,T> runnableTFunction) throws InterruptedException {
        //set default function if didn't get any
        if(runnableTFunction==null){
            runnableTFunction = this.defaultFunction;
        }

        readWriteLock.readLock().lock();
        try {
            if (!stop)
                taskQueue.offer(runnableTFunction.apply(runnable));
        }catch (Exception e){
            throw e;
        }finally {
            readWriteLock.readLock().unlock();
        }
    }


    /**
     * Gets Callable,converts to FutureTask.
     * Add as a runnable to the queue and returns Future
     * @param callable
     * @param runnableTFunction
     * @param <V>
     * @return Future<V>
     * @throws InterruptedException
     */
    public<V> Future<V> apply(final Callable<V> callable, Function<Runnable,T> runnableTFunction) throws InterruptedException {
        FutureTask<V> futureTask = new FutureTask<>(callable);
        this.apply(futureTask, runnableTFunction);
        return futureTask;
    }


    public void submitTask(final Runnable runnable) throws InterruptedException{
        Magen<PriorityRunnable> service = new Magen<>(new PriorityBlockingQueue<>(),
                       aRunnableTask -> new PriorityRunnable(aRunnableTask, 1));

        /*
         submit Runnable tasks to to the queue (as PriorityRunnable objects) using
         the apply methods aboves
         */
        service.apply(() -> System.out.println("There are more than 2 design patterns in this class"),
                runnable1 -> new PriorityRunnable(runnable1,1));

        service.apply(() -> System.out.println("a runnable"));

        service.apply(new Runnable() {
            @Override
            public void run() {
                System.out.println("Fun");
            }
        }, runnable2 -> new PriorityRunnable(runnable2,5));

        Callable<String> stringCallable= () -> {
            try {
                Thread.sleep(5000); // wait until interrupt
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            return "callable string";
        };
        Future<String> futureString = service.apply(stringCallable);
        Future<String> anotherFutureString = service.apply(stringCallable);

        try {
            System.out.println(futureString.get());
            System.out.println(anotherFutureString.get(10000, TimeUnit.MILLISECONDS));
        }catch (TimeoutException | ExecutionException ex){

        }

        service.stop();
        System.out.println("done");

    }

    /**
     * Stop the queue based on wait param.
     * if wait is true, mark as stop and call waitUntilDone
     * if wait is false, mark as stopNow and interrupt the thread
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        if (taskQueue.isEmpty())
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


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        Magen<PriorityRunnable> service =
                new Magen<>(new PriorityBlockingQueue<>(),
                        aRunnableTask -> new PriorityRunnable(aRunnableTask, 1));

        /*
         submit Runnable tasks to to the queue (as PriorityRunnable objects) using
         the apply methods above
         */
        service.apply(() -> System.out.println(
                "There are more than 2 design patterns in this class"),
                runnable -> new PriorityRunnable(runnable,1));

        service.apply(() -> System.out.println("a runnable"));

        service.apply(new Runnable() {
            @Override
            public void run() {
                System.out.println("Fun");
            }
        }, runnable -> new PriorityRunnable(runnable,5));

        Callable<String> stringCallable= () -> {
            try {
                Thread.sleep(5000); // wait until interrupt
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            return "callable string";
        };
        Future<String> futureString = service.apply(stringCallable);
        Future<String> anotherFutureString = service.apply(stringCallable);


        try {
            System.out.println(futureString.get());
            System.out.println(anotherFutureString.get(10000, TimeUnit.MILLISECONDS));
        }catch (TimeoutException ex){

        }

        service.stop();
       // service.stopNow();
        System.out.println("done");


    }
}