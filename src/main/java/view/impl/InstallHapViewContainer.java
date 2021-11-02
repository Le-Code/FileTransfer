package view.impl;

import adapter.FileCellRender;
import adapter.FileListMode;
import entity.FileEntity;
import entity.RecordEntity;
import entity.SignBean;
import listener.LogCallback;
import listener.RuntimeExecListener;
import utils.CommandHelp;
import utils.JsonUtil;
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

public class InstallHapViewContainer implements ViewContainer {

    private static final String TIPS_DST_PATH = "input dst path";

    private JPanel MainContainer;
    private JCheckBox cb_selectAllFiles;
    private JButton btn_install;
    private JCheckBox cb_sign;
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
    private SignBean signBean;

    public InstallHapViewContainer(LogCallback logCallback) {
        this.logCallback = logCallback;
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        records = new HashSet<>();
    }

    private void installHap() {
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
            String path = fileEntity.getFile().getAbsolutePath();
            if (!(path.endsWith(".apk") || path.endsWith(".hap"))) {
                continue;
            }
            executed = true;
            if (cb_sign.isSelected() ) { // need sign
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
            logCallback.showLog("please select available file......", true);
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
        cb_sign.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cb_selectAllFiles.isSelected()) {
                    if (signBean == null) {
                        signBean = JsonUtil.readSignInfo("signConfig.json");
                    }
                }
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
