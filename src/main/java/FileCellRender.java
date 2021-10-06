import entity.FileEntity;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileCellRender<T extends FileEntity> extends JCheckBox implements ListCellRenderer<T> {

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
        File file = value.getFile();
        setText(file.getName());
        setSelected(value.isSelected());
        return this;
    }
}
