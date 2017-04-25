package com.yangy.pictureediting.Bean;
import android.graphics.Paint;
import android.graphics.Path;
/**
 * Created by yangy on 17/4/21.
 */
/**
 * 用来保存操作信息
 */
public class DrawPath {

    public Path path;
    public Paint paint;

    public void setPath(Path path) {
        this.path = path;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }
    public Path getPath() {
        return path;
    }

    public Paint getPaint() {
        return paint;
    }
}
