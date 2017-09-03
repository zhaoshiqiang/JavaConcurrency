import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程池类，线程池管理器：创建线程，执行任务，销毁线程，获取线程基本信息
 * 线程池主要由：执行任务的线程数组WorkThread[] 和 任务等待队列组成BlockingQueue<Runnable>
 *     任务来时直接扔进队列，执行任务的线程阻塞等待任务队列中的任务
 * Create by zhaoshiqiang on 16:31 2017/9/3
 */
public final class ThreadPool {

    private static int worker_num = 5;
    private WorkThread[] workThreads;
    private static AtomicLong finished_task = new AtomicLong(0);
    private BlockingQueue<Runnable> taskQueue = new LinkedBlockingDeque<>();
    //这是单例模式
    private static volatile ThreadPool threadPool;
    private volatile boolean close = false;

    //私有化构造函数
    private ThreadPool() {
        this(5);
    }
    // 创建线程池,worker_num为线程池中工作线程的个数
    private ThreadPool(int worker_num){
        ThreadPool.worker_num = worker_num;
        workThreads = new WorkThread[worker_num];
        for (int i = 0; i < worker_num; i++) {
            workThreads[i] = new WorkThread();
            //开启线程池中的线程
            workThreads[i].start();
        }
    }
    // 单例模式，获得一个默认线程个数的线程池
    public static ThreadPool getThreadPool(){
        return getThreadPool(ThreadPool.worker_num);
    }
    //单态模式，获得一个指定线程个数的线程池,worker_num(>0)为线程池中工作线程的个数
    //worker_num<=0创建默认的工作线程个数
    private static ThreadPool getThreadPool(int worker_num) {
        if (worker_num <= 0){
            worker_num = ThreadPool.worker_num;
        }
        if (threadPool == null){
            synchronized (ThreadPool.class){
                if (threadPool == null){
                    threadPool = new ThreadPool(worker_num);
                }
            }
        }
        return threadPool;
    }

    /**
     * 执行任务,其实只是把任务加入任务队列，什么时候执行有线程池管理器决定
     * 若线程池关闭，则直接退出，不添加任务
     * @param task
     */
    public boolean execute(Runnable task){
        if (close){
            return false;
        }
        taskQueue.offer(task);
        return true;
    }

    /**
     * 批量执行任务,其实只是把任务加入任务队列，什么时候执行有线程池管理器决定
     * @param task
     */
    public boolean execute(Runnable[] task){
        if (close){
            return false;
        }
        for (Runnable t : task){
            taskQueue.offer(t);
        }
        return true;
    }

    /**
     * 批量执行任务,其实只是把任务加入任务队列，什么时候执行有线程池管理器决定
     * @param task
     */
    public boolean execute(List<Runnable> task){
        if (close){
            return false;
        }
        for (Runnable t : task){
            taskQueue.offer(t);
        }
        return true;
    }

    /**
     * 销毁线程池,该方法保证在所有任务都完成的情况下才销毁所有线程，否则等待任务完成才销毁
     */
    public void destroy(){
        //先将线程池关闭
        close = true;
        //如果还有任务没执行完成，就先睡会吧
        while (!taskQueue.isEmpty()){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 工作线程停止工作，且置为null
        for (int i = 0; i < worker_num; i++) {
            workThreads[i].stopWorker();
            //将线程池的引用置为null，方便gc
            workThreads[i] = null;
        }
        threadPool =null;
        //清空任务队列
        taskQueue.clear();
    }

    /**
     * 返回工作线程的个数
     * @return
     */
    public int getWorkThreadNumber(){
        return worker_num;
    }

    /**
     * 返回已完成任务的个数,这里的已完成是指已完成和正在执行的任务总和
     * @return
     */
    public int getFinishedTasknumber() {
        return finished_task.intValue();
    }

    /**
     * 返回任务队列的长度，即还没处理的任务个数
     * @return
     */
    public int getWaitTasknumber() {
        return taskQueue.size();
    }

    // 覆盖toString方法，返回线程池信息：工作线程个数和已完成任务个数
    @Override
    public String toString() {
        return "WorkThread number:" + worker_num + "  finished task number:"
                + finished_task + "  wait task number:" + getWaitTasknumber();
    }

    /**
     * 内部类，工作线程
     */
    private class WorkThread extends Thread{
        //该工作线程是否有效，用于结束该工作线程
        private boolean isRunning = true;

        /**
         * 关键所在，如果任务队列不为空，则取出任务执行，若任务队列空，则等待
         */
        @Override
        public void run(){
            Runnable r = null;
            // 若线程无效则自然结束run方法，该线程就没用了
            while (isRunning){
                //取出任务，若队列为空，则阻塞等待
                r = taskQueue.poll();
                if (r!=null){
                    //执行任务
                    r.run();
                }
                finished_task.addAndGet(1);
                r = null;
            }
        }

        /**
         * 停止工作，让该线程自然执行完run方法，自然结束
         */
        public void stopWorker(){
            isRunning = false;
        }
    }

    public static void main(String[] args) {
        // 创建3个线程的线程池
        ThreadPool t = ThreadPool.getThreadPool(3);
        for (int i = 0; i < 10000; i++) {
            t.execute(new Task());
//            t.execute(new Runnable[] { new Task(), new Task(), new Task() });
        }
        System.out.println(t);
        t.destroy();// 所有线程都执行完成才destory
        System.out.println(t);
    }

    static class Task implements Runnable{

        private static AtomicLong tasknum = new AtomicLong(0);
//        private static volatile int tasknum = 0;

        @Override
        public void run() {
            int sleeptime = new Random().nextInt(10);
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("任务 " + tasknum.addAndGet(1) + " 完成");
//            System.out.println("任务 " + (++tasknum) + " 完成");
        }
    }
}
