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
    private JPanel MainContainer;
    private JButton btn_install;
    private JCheckBox cb_sign;
    private JPanel panel_baseFile;

    public InstallHapViewContainer(LogCallback logCallback) {
        super(logCallback);
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        ExecWorker worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor.setWorker(worker);
    }

    private void installHap() {
        List<String> srcPaths = getSrcPaths();
        if (srcPaths.isEmpty()) {
            logCallback.showFailureLog("please select available file......", true);
            return;
        }
        addRecord();
        commandExecutor.installHap(srcPaths, "", cb_sign.isSelected(), new RuntimeExecListener() {
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
