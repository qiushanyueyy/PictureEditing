package com.yangy.pictureediting;

/**
 * Created by yangy on 17/4/21.
 */

/**
 * 用于自定义View绘画操作之后通知Activity中回退和前进按钮状态改变
 */
public interface IDrawView {

    void setBack(boolean savePath);
    void setNext(boolean deletePath);

}
