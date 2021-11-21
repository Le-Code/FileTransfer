package listener;

public interface LogCallback {
    void showSuccessLog(String msg, boolean append);
    void showFailureLog(String msg, boolean append);
}
