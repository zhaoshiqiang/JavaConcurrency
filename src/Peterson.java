
/**
 * Peterson 算法实现互斥锁
 * Created by zhaoshq on 2017/7/20.
 */
public class Peterson {
    //用于表示轮到哪个进程
    private int turn;
    //用于表示进程进入临界区的意愿，下标对应进程号
    private boolean[] interested = new boolean[]{false,false};

    public void entreRegion(int process){
        // 另一个进程的进程号
        int other = 1 - process;
        // 进程process想进入临界区
        interested[process] = true;
        // 设置轮到自己进入临界区了
        turn = process;
        /**
         * 当两个进程同时想进入临界区时，它们的interested[process]都
         * 为true，但后一个进程设置的turn值会覆盖前一个进程设置的turn值，
         * 这样后一个进程的turn == process条件为真，它会在该while循环
         * 中持续等待，而前一个进程的turn == process条件为假，能顺利进入
         * 临界区；等前一个进程离开临界区后，后一个进程也能进入临界区
         */
        while (turn == process && interested[other] == true){

        }
    }

    public void leaveRegion(int process){
        interested[process] = false;
    }
}
