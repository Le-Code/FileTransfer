package dao;

import entity.WorkerEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecWorker {
    private static ExecWorker worker;
    private static int MAX_EVENT = 100;
    private static int count = 0;

    private LinkedList<Runnable> events;
    private boolean stop;
    private Lock lock;
    private Condition emptyCondition;
    private Condition fullCondition;
    private ExecutorService produceThreadPool;
    private ExecutorService consumerThreadPool;
    private List<Consumer> consumers;

    private ExecWorker() {
        events = new LinkedList<>();
        stop = false;
        lock = new ReentrantLock();
        emptyCondition = lock.newCondition();
        fullCondition = lock.newCondition();
    }

    public static ExecWorker getInstance() {
        if (worker == null) {
            worker = new ExecWorker();
            worker.init();
        }
        return worker;
    }

    private void init() {
        consumerThreadPool = Executors.newFixedThreadPool(4);
        produceThreadPool = Executors.newFixedThreadPool(4);
        consumers = new ArrayList<>();
    }

    public void startWorker() {
        Consumer consumer = new Consumer();
        consumerThreadPool.execute(consumer);
        consumers.add(consumer);
    }

    public void pushWork(Runnable r) {
        produceThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    if (events.size() >= MAX_EVENT) {
                        fullCondition.await();
                    }
                    events.offer(r);
                    System.out.println(Thread.currentThread().getName() + " receive " + r.toString() + ", size " + events.size());
                    emptyCondition.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    public void stop() {
        for (Consumer c : consumers) {
            c.stop();
        }
        produceThreadPool.shutdown();
        consumerThreadPool.shutdown();
    }

    private class Consumer implements Runnable {

        @Override
        public void run() {
            while (!stop) {
                try {
                    lock.lock();
                    if (events.isEmpty()) {
                        emptyCondition.await();
                    }
                    Runnable event = events.poll();
                    fullCondition.signal();
                    lock.unlock();
                    if (event != null) {
                        event.run();
                        System.out.println("do thing " + (++count));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (lock.tryLock()) {
                        lock.unlock();
                    }
                }
            }
        }

        public void stop() {
            stop = true;
            try {
                lock.lock();
                emptyCondition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
