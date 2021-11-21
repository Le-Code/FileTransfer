package view.impl;

import dao.ExecWorker;
import listener.LogCallback;
import listener.RuntimeExecListener;
import view.BaseFileView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class TransFileViewContainer extends BaseFileView {

    private static final String TIPS_DST_PATH = "input dst path";

    private JPanel MainContainer;
    private JButton btn_send;
    private JCheckBox cb_reboot;
    private JTextField jtf_dstPath;
    private JPanel panel_baseFile;
    private ExecWorker worker;

    private List<String> dstPathHistory;
    private int dstPathHistoryIdx;

    public TransFileViewContainer(LogCallback logCallback) {
        super(logCallback);
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor.setWorker(worker);
        dstPathHistory = new ArrayList<>();
    }

    private void pushFile() {
        String dstPath = jtf_dstPath.getText();
        if (dstPathHistory.indexOf(dstPath) == -1) {
            dstPathHistory.add(dstPath);
            dstPathHistoryIdx = dstPathHistory.size() - 1;
        }
        if (dstPath.isEmpty() || dstPath.equals(TIPS_DST_PATH)) {
            logCallback.showFailureLog("dst path is empty......", true);
            return;
        }

        List<String> srcPaths = getSrcPaths();
        if (srcPaths.isEmpty()) {
            logCallback.showFailureLog("please select file......", true);
            return;
        }
        addRecord();

        commandExecutor.sendFile(srcPaths, dstPath, cb_reboot.isSelected(), new RuntimeExecListener() {
            @Override
            public void onSuccess(String str) {
                logCallback.showSuccessLog(str, true);
            }

            @Override
            public void onFailure(String str) {
                logCallback.showFailureLog(str, true);
            }
        });

    }

    private void initEvent() {
        jtf_dstPath.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // 获取焦点
                String dstPath = jtf_dstPath.getText();
                if (dstPath.equals(TIPS_DST_PATH)) {
                    jtf_dstPath.setText("");
                    jtf_dstPath.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 失去焦点
                String dstPath = jtf_dstPath.getText();
                if (dstPath.isEmpty()) {
                    jtf_dstPath.setForeground(Color.GRAY);
                    jtf_dstPath.setText(TIPS_DST_PATH);
                    btn_send.setEnabled(false);
                } else {
                    btn_send.setEnabled(true);
                }
            }
        });
        btn_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pushFile();
            }
        });
        // 监听方向按键
        jtf_dstPath.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP: // key up
                        dstPathHistoryIdx = Math.max(0, --dstPathHistoryIdx);
                        jtf_dstPath.setText(dstPathHistory.get(dstPathHistoryIdx));
                        break;
                    case KeyEvent.VK_DOWN: // key down
                        dstPathHistoryIdx = Math.min(dstPathHistory.size() - 1, ++dstPathHistoryIdx);
                        jtf_dstPath.setText(dstPathHistory.get(dstPathHistoryIdx));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private void initView() {
        panel_baseFile.add(super.getView(), BorderLayout.CENTER);
    }

    @Override
    public JPanel getView() {
        return MainContainer;
    }
}
