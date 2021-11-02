package dao;

public class Main {
    public static void main(String[] args) {
        ExecWorker worker = ExecWorker.GetInstance();
        for (int i = 0; i < 100; i++) {
            final int idx = i;
            worker.pushWork(new Runnable() {
                @Override
                public void run() {
                    System.out.println("hello world " + idx);
                }
            });
        }
    }
}
