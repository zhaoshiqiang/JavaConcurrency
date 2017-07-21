import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用显示条件变量Condition实现阻塞队列
 *
 * Created by zhaoshq on 2017/7/21.
 */
public class ConditionBoundedBuffer<V> {
    private final V[] buf;
    private int tail;
    private int head;
    private int count;

    protected final Lock lock = new ReentrantLock();
    //条件谓词：notFull（count < buf.length）
    private final Condition notFull = lock.newCondition();
    //条件谓词：notEmpty（count > 0）
    private final Condition notEmpty = lock.newCondition();

    public ConditionBoundedBuffer(int capacity) {
        this.buf = (V[]) new Object[capacity];
    }

    public void put(V v) throws InterruptedException {

        lock.lock();
        try {
            while (count == buf.length){
                notFull.await();
            }

            buf[tail] = v;
            if (++tail == buf.length){
                tail=0;
            }
            count++;

            notEmpty.signal();
        }finally {
            lock.unlock();
        }
    }

    public V take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0){
                notEmpty.await();
            }

            V v = buf[head];
            buf[head] = null;
            if (++head == buf.length){
                head=0;
            }
            count--;

            notFull.signal();

            return v;
        }finally {
            lock.unlock();
        }
    }
}
