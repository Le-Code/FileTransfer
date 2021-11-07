package view;

import adapter.FileCellRender;
import adapter.FileListMode;
import dao.CommandExecutor;
import dao.CommandExecutorFactory;
import entity.ConfigInfo;
import entity.FileEntity;
import entity.RecordEntity;
import listener.LogCallback;
import listener.RuntimeExecListener;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseFileView implements ViewContainer {

    protected LogCallback logCallback;
    protected CommandExecutor commandExecutor;

    private JPanel MainContainer;
    private JComboBox comb_frequency;
    private JButton btn_chooseFile;
    private JButton btn_initEnv;
    private JCheckBox cb_hdcMode;
    private JPanel pane_showSelect;
    private JLabel label_selectPath;
    private JList fileList;
    private JCheckBox cb_selectAllFiles;
    private String selectPath;
    private Set<RecordEntity> records;
    private List<FileEntity> files;

    @Override
    public JPanel getView() {
        return MainContainer;
    }

    public BaseFileView(LogCallback logCallback) {
        this.logCallback = logCallback;
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        // init executor is adb
        commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.ADB);
        records = new HashSet<>();
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

        cb_hdcMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cb_selectAllFiles.isSelected()) {
                    commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.HDC);
                } else {
                    commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.ADB);
                }
            }
        });
        btn_initEnv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigInfo configInfo = ConfigInfo.getInstance();
                if (!configInfo.checkInitExecInfo()) {
                    logCallback.showLog("check initExecEnv failure", true);
                }
                List<String> commands = cb_hdcMode.isSelected() ?
                        configInfo.getHdcInitExecInfo() : configInfo.getAdbInitExecInfo();
                commandExecutor.executeAsyncCommands(commands, new RuntimeExecListener() {
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
        });
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

    private void initView() {
    }

    protected void addRecord() {
        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setSrc(selectPath);
        recordEntity.setSelected(files);
        if (records.add(recordEntity)) {
            comb_frequency.addItem(recordEntity);
        }
    }

    protected List<String> getSrcPaths() {
        List<String> srcPaths = new ArrayList<>();
        for (FileEntity fileEntity : files) {
            if (!fileEntity.isSelected()) {
                continue;
            }
            srcPaths.add(fileEntity.getFile().getAbsolutePath());
        }
        return srcPaths;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("BaseFileView");
        frame.setContentPane(new BaseFileView(null).MainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
