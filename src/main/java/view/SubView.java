package view;

import dao.CommandExecutor;
import dao.CommandExecutorFactory;
import dao.ExecWorker;
import entity.ConfigInfo;
import listener.LogCallback;
import listener.RuntimeExecListener;
import utils.FileUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubView implements ViewContainer{
    private JTextPane ta_showLog;
    private JTextField tf_command;
    private JButton btn_exec;
    private JButton btn_clearLog;
    private JPanel mainFrame;

    private List<String> commandHistory;
    private int commandHistoryIdx;
    private CommandExecutor commandExecutor;

    public JPanel getView() {
        return mainFrame;
    }

    private void logAppend(String txt, Color color) {
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, color);
        Document doc = ta_showLog.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), txt, set);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private String generateLogStr(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh::mm::ss");
        String logStr = formatter.format(new Date()) + " " + str + FileUtil.getLineSep();
        return logStr;
    }

    private void execCommand(String command) {
        if (commandHistory.indexOf(command) == -1) {
            commandHistory.add(command);
            commandHistoryIdx = commandHistory.size() - 1;
        }
        logAppend(generateLogStr(command), Color.BLACK);
        tf_command.setText("");
        commandExecutor.executeAsyncString(command, new RuntimeExecListener() {
            @Override
            public void onSuccess(String str) {
                logAppend(generateLogStr(str), Color.BLACK);
            }

            @Override
            public void onFailure(String str) {
                logAppend(generateLogStr(str), Color.RED);
            }
        });
    }

    public SubView(LogCallback logCallback) {
        initInstance();
        initView();
        initEvent();
    }

    private void initInstance() {
        commandHistory = new ArrayList<>();
        ExecWorker worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.HDC);
        commandExecutor.setWorker(worker);
    }

    private void initEvent() {
        btn_exec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = tf_command.getText().trim();
                if (command.isEmpty()) {
                    showFailureLog("please select available file......", true);
                }
                execCommand(command);
            }
        });
        tf_command.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);

                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
                    tf_command.setText(tf_command.getText().trim() + " " + filepath + " ");
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
                for (int i = 0; i < transferFlavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(transferFlavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });
        // 监听方向按键
        tf_command.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP: // key up
                        commandHistoryIdx = Math.max(0, --commandHistoryIdx);
                        tf_command.setText(commandHistory.get(commandHistoryIdx));
                        break;
                    case KeyEvent.VK_DOWN: // key down
                        commandHistoryIdx = Math.min(commandHistory.size() - 1, ++commandHistoryIdx);
                        tf_command.setText(commandHistory.get(commandHistoryIdx));
                        break;
                    case KeyEvent.VK_ENTER: // key enter
                        String command = tf_command.getText().trim();
                        execCommand(command);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        btn_clearLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ta_showLog.setText("");
            }
        });
    }

    private void initView() {
    }

    public void showSuccessLog(String msg, boolean append) {
        if (append) {
            logAppend(generateLogStr(msg), Color.BLACK);
        } else {
            ta_showLog.setText(msg);
        }
    }

    public void showFailureLog(String msg, boolean append) {
        if (append) {
            logAppend(generateLogStr(msg), Color.RED);
        } else {
            ta_showLog.setText(msg);
        }
    }
}
