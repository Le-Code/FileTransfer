package view.impl;

import adapter.FileCellRender;
import adapter.FileListMode;
import entity.FileEntity;
import entity.RecordEntity;
import listener.LogCallback;
import listener.RuntimeExecListener;
import utils.CommandHelp;
import view.ViewContainer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransFileViewContainer implements ViewContainer {

    private static final String TIPS_DST_PATH = "input dst path";

    private JPanel MainContainer;
    private JCheckBox cb_selectAllFiles;
    private JButton btn_send;
    private JCheckBox cb_reboot;
    private JTextField jtf_dstPath;
    private JPanel pane_showSelect;
    private JLabel label_selectPath;
    private JList fileList;
    private JComboBox comb_frequency;
    private JButton btn_chooseFile;
    private JCheckBox cb_execMode;
    private List<FileEntity> files;
    private String selectPath;
    private Set<RecordEntity> records;
    private LogCallback logCallback;

    public TransFileViewContainer(LogCallback logCallback) {
        this.logCallback = logCallback;
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        records = new HashSet<>();
    }

    private void pushFile() {
        String dstPath = jtf_dstPath.getText();
        if (dstPath.isEmpty() || dstPath.equals(TIPS_DST_PATH)) {
            logCallback.showLog("dst path is empty......", true);
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
                    logCallback.showLog(str, true);
                    logCallback.showLog("exec command " + command + " success", true);
                }

                @Override
                public void onFailure(String str) {
                    logCallback.showLog(str, true);
                }
            });
        }
        if (!executed) {
            logCallback.showLog("please select file......", true);
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

    private void showDetailRecord(RecordEntity record) {
        selectPath = record.getSrc();
        files = new ArrayList<>(record.getSubFiles());
        FileListMode<FileEntity> fileListMode = new FileListMode<>(files);
        fileList.setModel(fileListMode);
        fileList.setCellRenderer(new FileCellRender());
        cb_selectAllFiles.setSelected(false);
        label_selectPath.setText("select path: " + selectPath);
    }

    private void initEvent() {
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
                pushFile();
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
    }

    private void initView() {
        comb_frequency.setPrototypeDisplayValue("xxxxxxxx");
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

    @Override
    public JPanel getView() {
        return MainContainer;
    }
}
