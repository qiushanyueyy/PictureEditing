package com.yangy.pictureediting.Presenter;

import com.yangy.pictureediting.IDrawView;
import com.yangy.pictureediting.Model.IDrawModel;

/**
 * Created by yangy on 17/4/21.
 */
public class DrawPresenter implements IDrawPresenter {
    private IDrawView userView;
    private IDrawModel userModel;

    public DrawPresenter(IDrawView userView, IDrawModel userModels) {
        this.userView = userView;
        this.userModel = userModels;
    }

    public void loadDraw() {
        userView.setBack(userModel.getSavePathListSize());
        userView.setNext(userModel.getDeletePathListSize());
    }
}
