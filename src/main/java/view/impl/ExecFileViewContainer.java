package view.impl;

import adapter.FileCellRender;
import adapter.FileListMode;
import entity.FileEntity;
import entity.RecordEntity;
import listener.LogCallback;
import utils.FileUtil;
import view.ViewContainer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
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
    private LogCallback logCallback;

    private List<FileEntity> files;
    private String selectPath;
    private Set<RecordEntity> records;
    private FileEntity preSelectEntity;

    @Override
    public JPanel getView() {
        return MainContainer;
    }

    public ExecFileViewContainer(LogCallback logCallback) {
        this.logCallback = logCallback;
        initView();
        initEvent();
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
                String text = ta_showContent.getText();
                System.out.println(text);
            }
        });
    }

    private void showFileContent(File file) {
        List<String> contents = FileUtil.readLineContent(file.getAbsolutePath());
        ta_showContent.setText("");
        for (String content : contents) {
            ta_showContent.append(content + "\n");
        }
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
        comb_frequency.setPrototypeDisplayValue("xxxxxxxx");
    }
}
