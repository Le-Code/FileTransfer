package entity;

import java.io.File;

public class FileEntity {
    private File file;
    private boolean selected;

    public FileEntity(File file) {
        this.file = file;
        this.selected = false;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
