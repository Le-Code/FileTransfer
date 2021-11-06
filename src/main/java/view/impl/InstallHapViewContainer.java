package view.impl;

import dao.ExecWorker;
import listener.LogCallback;
import listener.RuntimeExecListener;
import view.BaseFileView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class InstallHapViewContainer extends BaseFileView {

    private static final String TIPS_DST_PATH = "input dst path";

    private JPanel MainContainer;
    private JButton btn_install;
    private JCheckBox cb_sign;
    private JTextField jtf_dstPath;
    private JPanel panel_baseFile;

    private ExecWorker worker;

    public InstallHapViewContainer(LogCallback logCallback) {
        super(logCallback);
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor.setWorker(worker);
    }

    private void installHap() {
        String dstPath = jtf_dstPath.getText();
        if (dstPath.isEmpty() || dstPath.equals(TIPS_DST_PATH)) {
            logCallback.showLog("dst path is empty......", true);
            return;
        }

        List<String> srcPaths = getSrcPaths();
        if (srcPaths.isEmpty()) {
            logCallback.showLog("please select available file......", true);
            return;
        }
        addRecord(dstPath);
        commandExecutor.installHap(srcPaths, dstPath, cb_sign.isSelected(), new RuntimeExecListener() {
            @Override
            public void onSuccess(String str) {
                logCallback.showLog(str, true);
            }

            @Override
            public void onFailure(String str) {
                logCallback.showLog(str, true);
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
                    btn_install.setEnabled(false);
                } else {
                    btn_install.setEnabled(true);
                }
            }
        });
        btn_install.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                installHap();
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
