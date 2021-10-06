package listener;

public interface RuntimeExecListener {
    void onSuccess(String str);
    void onFailure(String str);
}
