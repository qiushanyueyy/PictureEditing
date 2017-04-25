package com.yangy.pictureediting.Model;

import com.yangy.pictureediting.Bean.DrawPath;

import java.util.List;

/**
 * Created by yangy on 17/4/21.
 */
public interface IDrawModel {
    void savePath(DrawPath DrawPath);

    int saveSize();

    DrawPath getSavePath(int position);

    List<DrawPath> getSavePathList();

    void removeSavePath(int position);

    void saveClear();

    boolean getSavePathListSize();

    void deletePath(DrawPath DrawPath);

    int deleteSize();

    DrawPath getDeletePath(int position);

    void removeDeletePath(int position);

    void deleteClear();

    boolean getDeletePathListSize();
}
