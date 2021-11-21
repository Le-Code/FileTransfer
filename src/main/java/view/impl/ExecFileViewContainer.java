package view.impl;

import adapter.FileCellRender;
import adapter.FileListMode;
import dao.CommandExecutor;
import dao.CommandExecutorFactory;
import dao.ExecWorker;
import entity.FileEntity;
import entity.RecordEntity;
import listener.LogCallback;
import listener.RuntimeExecListener;
import utils.FileUtil;
import view.ViewContainer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecFileViewContainer implements ViewContainer {
    private JPanel MainContainer;
    private JComboBox comb_frequency;
    private JButton btn_chooseFile;
    private JList fileList;
    private JTextArea ta_showContent;
    private JButton btn_exec;
    private JLabel label_selectPath;
    private JPanel pane_showSelect;
    private LogCallback logCallback;

    private List<FileEntity> files;
    private String selectPath;
    private Set<RecordEntity> records;
    private FileEntity preSelectEntity;
    private ExecWorker worker;

    protected CommandExecutor commandExecutor;

    @Override
    public JPanel getView() {
        return MainContainer;
    }

    public ExecFileViewContainer(LogCallback logCallback) {
        this.logCallback = logCallback;
        initView();
        initEvent();
        initInstance();
    }

    private void initInstance() {
        records = new HashSet<>();
        worker = ExecWorker.getInstance();
        worker.startWorker();
        commandExecutor = CommandExecutorFactory.chooseExecMode(CommandExecutorFactory.Mode.HDC);
        commandExecutor.setWorker(worker);
    }

    private void addRecord() {
        RecordEntity recordEntity = new RecordEntity();
        recordEntity.setSrc(selectPath);
        recordEntity.setSelected(files);
        if (records.add(recordEntity)) {
            comb_frequency.addItem(recordEntity);
        }
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

        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    if (preSelectEntity != null) {
                        preSelectEntity.setSelected(!preSelectEntity.isSelected());
                    }
                    JList currentItem = (JList) e.getSource();
                    preSelectEntity = (FileEntity) currentItem.getSelectedValue();
                    preSelectEntity.setSelected(!preSelectEntity.isSelected());
                    showFileContent(preSelectEntity.getFile());
                } else {
                    fileList.clearSelection();
                }
            }
        });
        btn_exec.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (files == null) {
                    logCallback.showFailureLog("please select available file......", true);
                    return;
                }
                addRecord();
                File file = preSelectEntity.getFile();
                commandExecutor.executeFile(file, new RuntimeExecListener() {
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

    private void showFileContent(File file) {
        List<String> contents = FileUtil.readLineContent(file.getAbsolutePath());
        ta_showContent.setText("");
        for (String content : contents) {
            ta_showContent.append(content + FileUtil.getLineSep());
        }
    }

    private void showDetailRecord(RecordEntity record) {
        selectPath = record.getSrc();
        files = new ArrayList<>(record.getSubFiles());
        FileListMode<FileEntity> fileListMode = new FileListMode<>(files);
        fileList.setModel(fileListMode);
        fileList.setCellRenderer(new FileCellRender());
        label_selectPath.setText("select path: " + selectPath);
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
        label_selectPath.setText("select path: " + selectPath);
    }

    private void initView() {
    }
}
