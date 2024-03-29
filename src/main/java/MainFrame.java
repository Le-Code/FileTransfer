import dao.ExecWorker;
import entity.ConfigInfo;
import entity.ConstantValue;
import listener.LogCallback;
import view.SubView;
import view.impl.*;
import view.ViewContainer;

import javax.swing.*;

public class MainFrame implements LogCallback {
    public static String configPath;

    private JPanel mainContainer;
    private JTabbedPane contentTabbedPane;
    private JPanel panel_subView;
    private SubView subView;

    public MainFrame() {
        initInstance();
        initView();
    }

    private void initInstance() {
        ExecWorker.getInstance();
        ConfigInfo.getInstance();
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
        ViewContainer customCommandContainer = new CustomCommandView(this);
        contentTabbedPane.add(customCommandContainer.getView(), "自定义按钮");
        ViewContainer logContainer = new DeviceLogView((this));
        contentTabbedPane.add(logContainer.getView(), "查看log");
        subView = new SubView(this);
        panel_subView.add(subView.getView());
    }

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            ConstantValue.configPath = args[0];
            System.out.println(ConstantValue.configPath);
        }
        JFrame frame = new JFrame("MainFrame");
        MainFrame mainFrame = new MainFrame();
        frame.setContentPane(mainFrame.mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(frame);
        frame.setVisible(true);
    }

    @Override
    public void showSuccessLog(String msg, boolean append) {
        subView.showSuccessLog(msg, append);
    }

    @Override
    public void showFailureLog(String msg, boolean append) {
        subView.showFailureLog(msg, append);
    }
}
