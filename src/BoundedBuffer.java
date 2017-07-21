/**
 * 使用内置锁实现条件队列
 * 内置锁的标准形式为：
 * <code>
 *     //必须通过一个锁来保护条件队列
 * synchronized(TestLock){
 *      //这里必须要用while，这是为了防止过早唤醒
 *     while( !conditionPredicate() ){
 *         TestLock.wait();
 *     }
 *     //现在对象处于合适状态
 *
 * }
 * <code/>
 * Created by zhaoshq on 2017/7/20.
 */
public class BoundedBuffer<V> {
    private final V[] buf;
    private int tail;
    private int head;
    private int count;

    public BoundedBuffer(int capacity) {
        this.buf = (V[]) new Object[capacity];
    }

    public synchronized final boolean isFull(){
        return count == buf.length;
    }

    public synchronized final boolean isEmpty(){
        return count == 0;
    }

    public synchronized V take() throws InterruptedException{
        //条件谓词：not-empty
        while (isEmpty()){
            wait();
        }
        boolean wasFull = isFull();

        V item = buf[head];
        /*
        * 任何持有或管理其他对象（如线程，套接字，文件句柄，数据库连接等有限资源）的对象，
        * 都应该在不需要这些对象时，销毁对它们的引用，使内存被gc回收，防止资源耗尽，以及程序失败
        * 这是为数不多需要显示置空情况之一，大多数情况下，这样做会有负面效果
        * */
        buf[head] = null;
        if (++head == buf.length){
            head = 0;
        }
        count--;

        if (wasFull){
            //通知条件队列有元素出列
            notifyAll();
        }
        return item;
    }

    public synchronized void put(V item) throws InterruptedException {
        //条件谓词：not-full
        while (isFull()){
            wait();
        }
        boolean wasEmpty = isEmpty();

        buf[tail] = item;
        if (++tail == buf.length){
            tail = 0;
        }
        count++;
        /*
        * 这里作了一个优化，当缓存从空变为非空时，才发通知，这叫做 “条件通知”
        * 因为notifyAll是一个重的方法，它会带来大量的上下文切换和锁竞争。
        * 而notify只有在单进单出，且所有等待线程的类型都相同时才能用，否则会出现丢失信号的问题
        * */
        if (wasEmpty){
            //通知条件队列有元素入列
            notifyAll();
        }
    }
}

