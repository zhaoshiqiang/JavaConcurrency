/**
 * 通过轮询与休眠实现阻塞队列
 * Created by zhaoshq on 2017/7/21.
 */
public class SleepyBoundedBuffer<V> {
    private final V[] buf;
    private int tail;
    private int head;
    private int count;

    public SleepyBoundedBuffer(int capacity) {
        this.buf = (V[]) new Object[capacity];
    }

    public boolean isFull(){
        return count == buf.length;
    }

    public boolean isEmpty(){
        return count == 0;
    }

    public void put(V v) throws InterruptedException {
        while (true){
            synchronized (this){
                if (!isFull()){

                    buf[tail] = v;
                    if (++tail == buf.length){
                        tail = 0;
                    }
                    count++;

                    return;
                }
            }
            Thread.sleep(50);
        }
    }

    public V take() throws InterruptedException {
        while (true){
            synchronized (this){
                if (!isEmpty()){

                    V v = buf[head];
                    buf[head] = null;
                    if (++head == buf.length){
                        head = 0;
                    }
                    count--;

                    return v;
                }
            }
            Thread.sleep(50);
        }
    }
}
