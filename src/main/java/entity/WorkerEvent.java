package entity;

import listener.RuntimeExecListener;

public class WorkerEvent {
    private Runnable event;
    private RuntimeExecListener execListener;

    public WorkerEvent(Runnable r, RuntimeExecListener listener) {
        this.event = r;
        this.execListener = listener;
    }

    public Runnable getEvent() {
        return event;
    }

    public void setEvent(Runnable event) {
        this.event = event;
    }

    public RuntimeExecListener getExecListener() {
        return execListener;
    }

    public void setExecListener(RuntimeExecListener execListener) {
        this.execListener = execListener;
    }

    public void callSuccess(String str) {
        if (execListener != null) {
            execListener.onSuccess(str);
        }
    }

    public void callFailure(String str) {
        if (execListener != null) {
            execListener.onFailure(str);
        }
    }

    public void exec() {
        if (event != null) {
            event.run();
        }
    }

    @Override
    public String toString() {
        return "WorkerEvent{" +
                "event=" + event +
                ", execListener=" + execListener +
                '}';
    }
}
