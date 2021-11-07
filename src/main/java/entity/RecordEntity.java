package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecordEntity {
    private String src;
    private String dst;
    private List<FileEntity> subFiles;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public void setSelected(List<FileEntity> subFiles) {
        this.subFiles = new ArrayList<>(subFiles);
    }

    public List<FileEntity> getSubFiles() {
        return subFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordEntity that = (RecordEntity) o;
        StringBuilder sb1 = new StringBuilder();
        for (FileEntity s : subFiles) {
            sb1.append(s);
        }

        StringBuilder sb2 = new StringBuilder();
        for (FileEntity s : that.getSubFiles()) {
            sb2.append(s);
        }
        return Objects.equals(src, that.src) && Objects.equals(sb1.toString(), sb2.toString());
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        for (FileEntity s : subFiles) {
            sb.append(s);
        }
        return Objects.hash(src, sb.toString());
    }

    @Override
    public String toString() {
        return src;
    }
}
