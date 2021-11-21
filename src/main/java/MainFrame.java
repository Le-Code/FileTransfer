import dao.CommandExecutor;
import dao.CommandExecutorFactory;
import dao.ExecWorker;
import entity.ConfigInfo;
import listener.LogCallback;
import listener.RuntimeExecListener;
import utils.FileUtil;
import view.impl.ExecFileViewContainer;
import view.impl.InstallHapViewContainer;
import view.impl.TransFileViewContainer;
import view.ViewContainer;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainFrame implements LogCallback {
    private ExecWorker worker;
    private CommandExecutor commandExecutor;
    private List<String> commandHistory;
    private int commandHistoryIdx;

    private String generateLogStr(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh::mm::ss");
        String logStr = formatter.format(new Date()) + " " + str + FileUtil.getLineSep();
        return logStr;
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

    public MainFrame() {
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.ADB);
        commandExecutor.setWorker(worker);
        ConfigInfo.getInstance();
        commandHistory = new ArrayList<>();
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
        // set System UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // init table view
        ViewContainer transFileViewContainer = new TransFileViewContainer(this);
        contentTabbedPane.add(transFileViewContainer.getView(), "传输文件");
        ViewContainer installHapViewContainer = new InstallHapViewContainer(this);
        contentTabbedPane.add(installHapViewContainer.getView(), "安装hap包");
        ViewContainer execFileViewContainer = new ExecFileViewContainer(this);
        contentTabbedPane.add(execFileViewContainer.getView(), "执行脚本");
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        MainFrame mainFrame = new MainFrame();
        frame.setContentPane(mainFrame.mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(frame);
        frame.setVisible(true);
    }

    private JPanel mainContainer;
    private JTextPane ta_showLog;
    private JTextField tf_command;
    private JTabbedPane contentTabbedPane;
    private JButton btn_exec;
    private JSplitPane jsp_main;
    private JButton btn_clearLog;

    @Override
    public void showSuccessLog(String msg, boolean append) {
        if (append) {
            logAppend(generateLogStr(msg), Color.BLACK);
        } else {
            ta_showLog.setText(msg);
        }
    }

    @Override
    public void showFailureLog(String msg, boolean append) {
        if (append) {
            logAppend(generateLogStr(msg), Color.RED);
        } else {
            ta_showLog.setText(msg);
        }
    }
}
