package view.impl;

import dao.CommandExecutor;
import dao.CommandExecutorFactory;
import dao.ExecWorker;
import entity.ConfigInfo;
import listener.LogCallback;
import listener.RuntimeExecListener;
import view.ViewContainer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CustomCommandView implements ViewContainer {
    private JPanel main_frame;
    private JButton btn_fresh;
    private JPanel panel_btn;

    private CommandExecutor commandExecutor;
    private ConfigInfo configInfo;
    private LogCallback logCallback;

    public CustomCommandView(LogCallback logCallback) {
        this.logCallback = logCallback;
        initInstance();
        initView();
        initEvent();
    }

    private void initEvent() {
        btn_fresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                configInfo.freshConfigInfo();
                panel_btn.removeAll();
                initCustomCommand();
                panel_btn.updateUI();
            }
        });
    }

    private void addButton(String command_str, String command_name) {
        JButton jButton = new JButton();
        jButton.setText(command_name);
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandExecutor.executeAsyncString(command_str, new RuntimeExecListener() {
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
        });
        panel_btn.add(jButton);
    }

    private void initCustomCommand() {
        List<ConfigInfo.CustomCommandInfo> customCommandInfoList = configInfo.getCustomCommandInfoList();
        if (customCommandInfoList == null) {
            return;
        }
        for (ConfigInfo.CustomCommandInfo customCommandInfo : customCommandInfoList) {
            addButton(customCommandInfo.getCommandStr(), customCommandInfo.getCommandName());
        }
    }

    private void initView() {
        initCustomCommand();
    }

    private void initInstance() {
        configInfo = ConfigInfo.getInstance();
        ExecWorker worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.HDC);
        commandExecutor.setWorker(worker);
    }


    @Override
    public JPanel getView() {
        return main_frame;
    }
}
