package entity;

import listener.RuntimeExecListener;
import utils.CloseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InputStreamEvent implements Runnable{
    private InputStream is;
    private RuntimeExecListener listener;
    private String type;

    public InputStreamEvent(InputStream is, String type, RuntimeExecListener listener) {
        this.is = is;
        this.type = type;
        this.listener = listener;
    }

    private void callOnSuccess(RuntimeExecListener listener, String txt) {
        if (listener != null) {
            listener.onSuccess(txt);
        }
    }

    private void callOnFailure(RuntimeExecListener listener, String txt) {
        if (listener != null) {
            listener.onFailure(txt);
        }
    }

    @Override
    public void run() {
        if (is == null) {
            return;
        }
        Thread thread = Thread.currentThread();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null && !thread.isInterrupted()) {
                callOnSuccess(listener, "type: " + type + " res: " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            callOnFailure(listener, e.getMessage());
        } finally {
            CloseUtil.close(br);
        }
    }
}
