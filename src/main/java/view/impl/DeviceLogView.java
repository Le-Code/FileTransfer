package view.impl;

import dao.CommandExecutor;
import dao.CommandExecutorFactory;
import entity.ConfigInfo;
import listener.LogCallback;
import listener.RuntimeExecListener;
import utils.FileUtil;
import view.ViewContainer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceLogView implements ViewContainer, RuntimeExecListener {
    private static final String TIPS_FILTER = "过滤关键字，封号;间隔";

    private JPanel mainContainer;
    private JTextField tf_filter;
    private JButton btn_exec;
    private JTextPane ta_log;
    private JCheckBox cb_hdc;
    private JButton btn_clear;
    private JButton btn_stop;
    private JButton btn_init;
    private JButton btn_clearDeviceLog;

    private List<String> historyFilter;
    private int filterHistoryIdx = 0;

    private LogCallback logCallback;

    private CommandExecutor commandExecutor;

    public DeviceLogView(LogCallback logCallback) {
        this.logCallback = logCallback;
        initInstance();
        initView();
        initEvent();
    }

    private String generateLogStr(String str) {
        String logStr = str + FileUtil.getLineSep();
        return logStr;
    }

    private void initEvent() {
        btn_exec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filter = tf_filter.getText().trim();
                if (filter.equals(TIPS_FILTER)) {
                    filter = "";
                }
                if (!filter.isEmpty() && !historyFilter.contains(filter)) {
                    historyFilter.add(filter);
                    filterHistoryIdx = historyFilter.size();
                }
                if (logCallback != null) {
                    logCallback.showSuccessLog("start get log ......", true);
                }
                getDeviceLog(filter);
            }
        });
        btn_stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                commandExecutor.stopLog();
            }
        });
        cb_hdc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cb_hdc.isSelected()) {
                    commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.HDC);
                } else {
                    commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.ADB);
                }
            }
        });
        btn_clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ta_log.setText("");
            }
        });
        btn_init.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigInfo configInfo = ConfigInfo.getInstance();
                if (!configInfo.checkInitExecInfo()) {
                    if (logCallback != null) {
                        logCallback.showFailureLog("check initExecEnv failure", true);
                    }
                }
                List<String> commands = cb_hdc.isSelected() ?
                        configInfo.getHdcInitExecInfo() : configInfo.getAdbInitExecInfo();
                commandExecutor.executeAsyncCommands(commands, new RuntimeExecListener() {
                    @Override
                    public void onSuccess(String str) {
                        if (logCallback != null) {
                            logCallback.showSuccessLog(str, true);
                        }
                    }

                    @Override
                    public void onFailure(String str) {
                        if (logCallback != null) {
                            logCallback.showFailureLog(str, true);
                        }
                    }
                });
            }
        });

        tf_filter.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                // 获取焦点
                String dstPath = tf_filter.getText();
                if (dstPath.equals(TIPS_FILTER)) {
                    tf_filter.setText("");
                    tf_filter.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // 失去焦点
                String dstPath = tf_filter.getText();
                if (dstPath.isEmpty()) {
                    tf_filter.setForeground(Color.GRAY);
                    tf_filter.setText(TIPS_FILTER);
                }
            }
        });
        // 监听方向按键
        tf_filter.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP: // key up
                        filterHistoryIdx = Math.max(0, --filterHistoryIdx);
                        tf_filter.setText(historyFilter.get(filterHistoryIdx));
                        break;
                    case KeyEvent.VK_DOWN: // key down
                        filterHistoryIdx = Math.min(historyFilter.size() - 1, ++filterHistoryIdx);
                        tf_filter.setText(historyFilter.get(filterHistoryIdx));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        btn_clearDeviceLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigInfo configInfo = ConfigInfo.getInstance();
                String command = cb_hdc.isSelected() ?
                        configInfo.getHdcLogClearCommand() : configInfo.getAdbLogClearCommand();
                commandExecutor.executeAsyncString(command, new RuntimeExecListener() {
                    @Override
                    public void onSuccess(String str) {
                        if (logCallback != null) {
                            logCallback.showSuccessLog(str, true);
                        }
                    }

                    @Override
                    public void onFailure(String str) {
                        if (logCallback != null) {
                            logCallback.showFailureLog(str, true);
                        }
                    }
                });
            }
        });
    }

    private void logAppend(String txt, Color color) {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, color);
        Document doc = ta_log.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), txt, set);
            ta_log.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void getDeviceLog(String filter) {
        commandExecutor.stopLog();
        commandExecutor.log(filter, this);
    }

    private void initView() {
        tf_filter.setForeground(Color.GRAY);
    }

    private void initInstance() {
        commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.ADB);
        historyFilter = new ArrayList<>();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("LogView");
        frame.setContentPane(new DeviceLogView(null).mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public JPanel getView() {
        return mainContainer;
    }

    @Override
    public void onSuccess(String str) {
        logAppend(generateLogStr(str), Color.BLACK);
    }

    @Override
    public void onFailure(String str) {
        logAppend(generateLogStr(str), Color.RED);
    }
}
