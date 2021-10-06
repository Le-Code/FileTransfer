import entity.FileEntity;
import entity.RecordEntity;
import listener.RuntimeExecListener;
import utils.CommandHelp;
import utils.FileUtil;
import utils.JsonUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MainFrame{

    private static final String TIPS_DST_PATH = "input dst path";

    private String generateLogStr(String str) {
        SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD hh::mm::ss");
        String logStr = formatter.format(new Date()) + " " + str + "\n";
        return logStr;
    }

    private void initRecord() {
        String jsonData = FileUtil.readContent("record.json");
        List<RecordEntity> recordEntities = JsonUtil.Json2Array(jsonData, RecordEntity.class);
        records = new HashSet<>();
        if (recordEntities == null) {
            return;
        }
        records.addAll(recordEntities);
        for (RecordEntity recordEntity : records) {
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
                    selectPath = selectFile.getAbsolutePath();
                    files = new ArrayList<>();
                    selectedFiles = new ArrayList<>();
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
                    selectedFiles.addAll(files);
                } else {
                    selectedFiles.clear();
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
                    if (file.isSelected()) {
                        selectedFiles.add(file);
                    } else {
                        selectedFiles.remove(file);
                    }
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
                System.out.println("btn_send onclick");
                String dstPath = jtf_dstPath.getText();
                if (dstPath.isEmpty() || dstPath.equals(TIPS_DST_PATH)) {
                    ta_showLog.append(generateLogStr("dst path is empty......"));
                    return;
                }
                if (selectedFiles == null || selectedFiles.isEmpty()) {
                    ta_showLog.append(generateLogStr("please select file......"));
                    return;
                }
                RecordEntity recordEntity = new RecordEntity();
                recordEntity.setSrc(selectPath);
                recordEntity.setDst(dstPath);
                List<String> tmpRecords = new ArrayList<>();
                for (FileEntity fileEntity : selectedFiles) {
                    tmpRecords.add(fileEntity.getFile().getAbsolutePath());
                    String command = "adb push " +
                            fileEntity.getFile().getAbsolutePath() + " " + dstPath;
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
                recordEntity.setSelected(tmpRecords);
                if (records.add(recordEntity)) {
                    comb_frequency.addItem(recordEntity);
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
                File srcFile = new File(recordEntity.getSrc());
                if (!srcFile.exists()) {
                    return;
                }
                selectPath = recordEntity.getSrc();
                files = new ArrayList<>();
                selectedFiles = new ArrayList<>();
                List<String> selectedRecords = recordEntity.getSelected();
                if (srcFile.isFile()) {
                    FileEntity entity = new FileEntity(srcFile);
                    if (selectedRecords.contains(srcFile.getAbsolutePath())) {
                        entity.setSelected(true);
                        selectedFiles.add(entity);
                    }
                    files.add(entity);
                } else {
                    File[] listFiles = srcFile.listFiles();
                    for (File f : listFiles) {
                        if (f.isFile()) {
                            FileEntity entity = new FileEntity(f);
                            if (selectedRecords.contains(f.getAbsolutePath())) {
                                entity.setSelected(true);
                                selectedFiles.add(entity);
                            }
                            files.add(entity);
                        }
                    }
                }
                FileListMode<FileEntity> fileListMode = new FileListMode<>(files);
                fileList.setModel(fileListMode);
                fileList.setCellRenderer(new FileCellRender());
                jtf_dstPath.setText(recordEntity.getDst());
                cb_selectAllFiles.setSelected(false);
            }
        });
        btn_saveRecord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String jsonStr = JsonUtil.Array2Json(new ArrayList<>(records));
                System.out.println(jsonStr);
                FileUtil.writeContent(jsonStr, "record.json");
            }
        });
        initRecord();
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
    private JPanel contentContainer;

    private JButton btn_chooseFile;
    private JPanel logPanel;
    private JPanel operatePanel;
    private JPanel fileContentsPanel;
    private JPanel fileOperatePanel;
    private JCheckBox cb_selectAllFiles;
    private JButton btn_send;
    private JScrollPane fileListPanel;
    private JList fileList;
    private JTextField jtf_dstPath;
    private JPanel fileFindPanel;
    private JTextArea ta_showLog;
    private JButton btn_clearLog;
    private JRadioButton rb_pushFile;
    private JRadioButton rb_installHap;
    private JComboBox comb_frequency;
    private JButton btn_saveRecord;

    private List<FileEntity> files;
    private List<FileEntity> selectedFiles;
    private String selectPath;
    private Set<RecordEntity> records;
}
