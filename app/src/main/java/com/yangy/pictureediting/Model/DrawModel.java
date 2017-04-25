package com.yangy.pictureediting.Model;

import com.yangy.pictureediting.Bean.DrawPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangy on 17/4/21.
 */

/**
 * 数据存储的模型层，只需要考虑怎么把数据存起来
 */
public class DrawModel implements IDrawModel {
    private List<DrawPath> mSavePath = new ArrayList<>();//保存绘制的记录
    private List<DrawPath> mDeletePath = new ArrayList<>();//保存清除的记录

    @Override
    public void savePath(DrawPath DrawPath) {
        mSavePath.add(DrawPath);
    }

    @Override
    public int saveSize() {
        return mSavePath.size();
    }

    public DrawPath getSavePath(int position) {
        return mSavePath.get(position);
    }

    @Override
    public List<DrawPath> getSavePathList() {
        return mSavePath;
    }

    @Override
    public void removeSavePath(int position) {
        mSavePath.remove(position);
    }

    @Override
    public void saveClear() {
        mSavePath.clear();
    }

    @Override
    public boolean getSavePathListSize() {
        if (mSavePath.size() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public void deletePath(DrawPath DrawPath) {
        mDeletePath.add(0, DrawPath);
    }

    @Override
    public int deleteSize() {
        return mDeletePath.size();
    }

    public DrawPath getDeletePath(int position) {
        return mDeletePath.get(position);
    }

    @Override
    public void removeDeletePath(int position) {
        mDeletePath.remove(position);
    }

    @Override
    public void deleteClear() {
        mDeletePath.clear();
    }

    @Override
    public boolean getDeletePathListSize() {
        if (mDeletePath.size() != 0) {
            return true;
        }
        return false;
    }
}
