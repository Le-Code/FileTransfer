package adapter;

import entity.FileEntity;

import javax.swing.*;
import java.util.List;

public class FileListMode<T extends FileEntity> extends AbstractListModel<T> {
    private List<T> files;

    public FileListMode(List<T> files) {
        this.files = files;
    }

    @Override
    public int getSize() {
        return files.size();
    }

    @Override
    public T getElementAt(int index) {
        return files.get(index);
    }
}
