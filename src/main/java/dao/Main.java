package dao;

public class Main {
    public static void main(String[] args) {
        ExecWorker worker = ExecWorker.getInstance();
        worker.startWorker();
        worker.startWorker();
        worker.startWorker();
        for (int i = 0; i < 110; i++) {
            final int idx = i;
            worker.pushWork(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName() + " do " + idx);
                }

                @Override
                public String toString() {
                    return "event " + idx;
                }
            });
        }
    }
}
