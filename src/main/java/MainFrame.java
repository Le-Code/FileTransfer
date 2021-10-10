import entity.FileEntity;
import entity.RecordEntity;
import entity.SignBean;
import listener.RuntimeExecListener;
import utils.CommandHelp;
import utils.JsonUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainFrame{

    private static final String TIPS_DST_PATH = "input dst path";

    private String generateLogStr(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh::mm::ss");
        String logStr = formatter.format(new Date()) + " " + str + "\n";
        return logStr;
    }

    private void showFileDetail(File selectFile) {
        selectPath = selectFile.getAbsolutePath();
        files = new ArrayList<>();
        if (selectFile.isFile()) {
            files.add(new FileEntity(selectFile));
        } else {
            File[] listFiles = selectFile.listFiles();
            for (File f : listFiles) {
                if (f.isFile()) {
                    files.add(new FileEntity(f));
                }
            }
        }
        FileListMode<FileEntity> fileListMode = new FileListMode<>(files);
        fileList.setModel(fileListMode);
        fileList.setCellRenderer(new FileCellRender());
        cb_selectAllFiles.setSelected(false);
        label_selectPath.setText("select path: " + selectPath);
    }

    private void showDetailRecord(RecordEntity record) {
        selectPath = record.getSrc();
        files = new ArrayList<>(record.getSubFiles());
        FileListMode<FileEntity> fileListMode = new FileListMode<>(files);
        fileList.setModel(fileListMode);
        fileList.setCellRenderer(new FileCellRender());
        cb_selectAllFiles.setSelected(false);
        label_selectPath.setText("select path: " + selectPath);
    }

    private void execCommand(String command) {
        ta_showLog.append(generateLogStr(command));
        tf_command.setText("");
        CommandHelp.Exec(command, new RuntimeExecListener() {
            @Override
            public void onSuccess(String str) {
                ta_showLog.append(generateLogStr(str));
            }

            @Override
            public void onFailure(String str) {
                ta_showLog.append("exec command \"" + command + "\" error");
            }
        });
    }

    private void pushFile() {
        String dstPath = jtf_dstPath.getText();
        if (dstPath.isEmpty() || dstPath.equals(TIPS_DST_PATH)) {
            ta_showLog.append(generateLogStr("dst path is empty......"));
            return;
        }
        boolean executed = false;
        for (FileEntity fileEntity : files) {
            if (!fileEntity.isSelected()) {
                continue;
            }
            executed = true;
            String command = "adb push " +
                    fileEntity.getFile().getAbsolutePath() + " " + dstPath;
            CommandHelp.Exec("adb remount", null);
            CommandHelp.Exec(command, new RuntimeExecListener() {
                @Override
                public void onSuccess(String str) {
                    ta_showLog.append(generateLogStr(str));
                    ta_showLog.append(generateLogStr("exec command " + command + " success"));
                }

                @Override
                public void onFailure(String str) {
                    ta_showLog.append(generateLogStr(str));
                }
            });
        }
        if (!executed) {
            ta_showLog.append(generateLogStr("please select file......"));
            return;
        }
        if (cb_reboot.isSelected()) {
            CommandHelp.Exec("adb reboot", null);
        }
        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setSrc(selectPath);
        recordEntity.setDst(dstPath);

        recordEntity.setSelected(files);
        if (records.add(recordEntity)) {
            comb_frequency.addItem(recordEntity);
        }
    }

    private void installHap() {
        String dstPath = jtf_dstPath.getText();
        if (dstPath.isEmpty() || dstPath.equals(TIPS_DST_PATH)) {
            ta_showLog.append(generateLogStr("dst path is empty......"));
            return;
        }
        boolean executed = false;
        for (FileEntity fileEntity : files) {
            if (!fileEntity.isSelected()) {
                continue;
            }
            String path = fileEntity.getFile().getAbsolutePath();
            if (!(path.endsWith(".apk") || path.endsWith(".hap"))) {
                continue;
            }
            executed = true;
            if (cb_reboot.isSelected() ) { // need sign
                // 1. sign hap
                String dir = path.substring(0, path.lastIndexOf(File.separator));
                String fileName = fileEntity.getFile().getName();
                int lastDotIdx = fileName.lastIndexOf(".");
                String suffix = fileName.substring(lastDotIdx);
                String signedName = fileName.substring(0, fileName.lastIndexOf(".")) + "_signed" + suffix;
                String signedOutFile = dir + File.separator + signedName;
                String command = "jar " + signBean.getJarPath() + " username " + signBean.getUserName() + " " +
                        " password " + signBean.getPassword() + " input " + path + " output " + signedOutFile;
                CommandHelp.Exec(command, null);
                path = signedOutFile;
            }
            String command = "adb push " + path + " " + dstPath;
            CommandHelp.Exec("adb remount", null);
            CommandHelp.Exec(command, new RuntimeExecListener() {
                @Override
                public void onSuccess(String str) {
                    ta_showLog.append(generateLogStr(str));
                    ta_showLog.append(generateLogStr("exec command " + command + " success"));
                }

                @Override
                public void onFailure(String str) {
                    ta_showLog.append(generateLogStr(str));
                }
            });
        }
        if (!executed) {
            ta_showLog.append(generateLogStr("please select available file......"));
            return;
        }

        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setSrc(selectPath);
        recordEntity.setDst(dstPath);

        recordEntity.setSelected(files);
        if (records.add(recordEntity)) {
            comb_frequency.addItem(recordEntity);
        }
    }

    public MainFrame() {
        btn_chooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int retCode = jFileChooser.showOpenDialog(jFileChooser);
                if (retCode == JFileChooser.APPROVE_OPTION) {
                    File selectFile = jFileChooser.getSelectedFile();
                    comb_frequency.setSelectedIndex(-1);
                    showFileDetail(selectFile);
                }
            }
        });
        cb_selectAllFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (files == null || files.isEmpty()) {
                    return;
                }
                for (FileEntity fileEntity : files) {
                    fileEntity.setSelected(cb_selectAllFiles.isSelected());
                }
                if (cb_selectAllFiles.isSelected()) {
                    for (FileEntity f : files) {
                        f.setSelected(true);
                    }
                } else {
                    for (FileEntity f : files) {
                        f.setSelected(false);
                    }
                }
                fileList.repaint();
            }
        });
        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    JList currentItem = (JList) e.getSource();
                    FileEntity file = (FileEntity) currentItem.getSelectedValue();
                    file.setSelected(!file.isSelected());
                } else {
                    fileList.clearSelection();
                }
            }
        });
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
                if (rb_pushFile.isSelected()) {
                    pushFile();
                } else {
                    installHap();
                }
            }
        });
        btn_clearLog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ta_showLog.setText("");
            }
        });
        comb_frequency.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                RecordEntity recordEntity = (RecordEntity) e.getItem();
                showDetailRecord(recordEntity);
            }
        });
        pane_showSelect.setTransferHandler(new TransferHandler() {
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
                    File selectFile = new File(filepath);
                    if (!selectFile.exists()) {
                        return false;
                    }
                    comb_frequency.setSelectedIndex(-1);
                    showFileDetail(selectFile);
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
        btn_rmRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecordEntity recordEntity = (RecordEntity)comb_frequency.getSelectedItem();
                int idx = comb_frequency.getSelectedIndex();
                records.remove(recordEntity);
                comb_frequency.removeItemAt(idx);
            }
        });
        btn_exec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = tf_command.getText().trim();
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
        tf_command.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String command = tf_command.getText().trim();
                execCommand(command);
            }
        });

        rb_installHap.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (rb_installHap.isSelected()) {
                    btn_send.setText("安装");
                    cb_reboot.setText("签名");
                    if (signBean == null) {
                        signBean = JsonUtil.readSignInfo("signConfig.json");
                    }
                }
            }
        });

        rb_pushFile.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (rb_pushFile.isSelected()) {
                    btn_send.setText("推送");
                    cb_reboot.setText("重启");
                }
            }
        });
        records = new HashSet<>();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainFrame");
        frame.setContentPane(new MainFrame().mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(frame);
        frame.setVisible(true);
    }

    private JPanel mainContainer;

    private JButton btn_chooseFile;
    private JCheckBox cb_selectAllFiles;
    private JButton btn_send;
    private JList fileList;
    private JTextField jtf_dstPath;
    private JTextArea ta_showLog;
    private JButton btn_clearLog;
    private JRadioButton rb_pushFile;
    private JRadioButton rb_installHap;
    private JComboBox comb_frequency;
    private JCheckBox cb_reboot;
    private JLabel label_selectPath;
    private JPanel pane_showSelect;
    private JButton btn_rmRecord;
    private JTextField tf_command;
    private JButton btn_exec;

    private List<FileEntity> files;
    private String selectPath;
    private Set<RecordEntity> records;
    private SignBean signBean;
}
