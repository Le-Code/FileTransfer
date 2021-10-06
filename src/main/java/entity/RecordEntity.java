package entity;

import java.util.List;
import java.util.Objects;

public class RecordEntity {
    private String src;
    private String dst;
    private List<String> selected;

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

    public List<String> getSelected() {
        return selected;
    }

    public void setSelected(List<String> selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordEntity that = (RecordEntity) o;
        StringBuilder sb1 = new StringBuilder();
        for (String s : selected) {
            sb1.append(s);
        }

        StringBuilder sb2 = new StringBuilder();
        for (String s : that.getSelected()) {
            sb2.append(s);
        }
        return Objects.equals(src, that.src) &&
                Objects.equals(dst, that.dst) && Objects.equals(sb1.toString(), sb2.toString());
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        for (String s : selected) {
            sb.append(s);
        }
        return Objects.hash(src, dst, sb.toString());
    }

    @Override
    public String toString() {
        return src + ";" + dst;
    }
}
