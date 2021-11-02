package dao;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecWorker {
    private static ExecWorker worker;
    private static final int MAX_EVENT = 100;

    private BlockingQueue<Runnable> events;
    private boolean stop;
    private Lock lock;
    private Condition emptyCondition;
    private Condition fullCondition;

    private Consumer consumer;
    private Producer producer;

    private ExecWorker() {
        events = new ArrayBlockingQueue<Runnable>(MAX_EVENT);
        stop = false;
        lock = new ReentrantLock();
        emptyCondition = lock.newCondition();
        fullCondition = lock.newCondition();
    }

    public static ExecWorker GetInstance() {
        if (worker == null) {
            worker = new ExecWorker();
            worker.startWorker();
        }
        return worker;
    }

    private void startWorker() {
        producer = new Producer();
        consumer = new Consumer();
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.execute(producer);
        threadPool.execute(consumer);
    }

    public void pushWork(Runnable r) {
        producer.pushEvent(r);
    }

    private class Consumer implements Runnable {

        @Override
        public void run() {
            while (!stop) {
                try {
                    if (events.isEmpty()) {
                        emptyCondition.await();
                    }
                    Runnable event = events.take();
                    event.run();
                    fullCondition.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Producer implements Runnable {
        private Runnable event;
        private final Object o = new Object();

        public synchronized void pushEvent(Runnable event) {
            this.event = event;
            o.notify();
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    o.wait();
                    if (events.size() >= MAX_EVENT) {
                        fullCondition.await();
                    }
                    events.offer(event);
                    emptyCondition.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
