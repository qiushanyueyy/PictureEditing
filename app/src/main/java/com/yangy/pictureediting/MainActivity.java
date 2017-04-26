package com.yangy.pictureediting;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yangy.pictureediting.Model.IDrawModel;
import com.yangy.pictureediting.Model.DrawModel;
import com.yangy.pictureediting.Presenter.IDrawPresenter;
import com.yangy.pictureediting.Presenter.DrawPresenter;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener ,IDrawView ,PermissionListener {
    private Toast mToast;//提示用的短时间显示Toast
    private LoadingDialog loadingDialog;//图片加载处理时显示dialog

    private View rectRedPaint;//画笔红色点击边框
    private View rectYellowPaint;//画笔黄色点击边框
    private View rectBluePaint;//画笔蓝色点击边框

    private View rectClearDraw;//清除按钮
    private TextView lockDraw;//锁定按钮
    private View rectCompleteDraw;//完成按钮

    private View rectRedPaintSelected;//画笔红色选中状态
    private View rectYellowPaintSelected;//画笔黄色选中状态
    private View recttBluePaintSelected;//画笔蓝色选中状态

    private View line1;//线条1点击边框
    private View line2;//线条2点击边框
    private View line3;//线条3点击边框
    private View line1Selected;//线条1选中状态
    private View line2Selected;//线条2选中状态
    private View line3Selected;//线条3选中状态
    private View line1_p;//线条1
    private View line2_p;//线条2
    private View line3_p;//线条3

    private TextView tv_back;
    private TextView tv_next;

    private DrawView touchView;

    private Paint mPaint = null;

    private String graphPath = "";//需要编辑的原图片路径（可以是本地图片路径，也可以是网络图片URL）
    private String picPath = "";//图片编辑完成之后保存到本地的路径
    private IDrawModel userModel;
    IDrawPresenter presenter;
    // 原图片bitmap对象
    private Bitmap bitmapNet;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (bitmapNet != null) {
                    if (!touchView.setBitmap(bitmapNet)) {
                        showShort("显示图片失败");
                    }
                    dismissLoading();
                } else {
                    dismissLoading();
                    showShort("图片已损坏！");
                }
            } else if (msg.what == 1) {
                dismissLoading();
                if (picPath != null) {
                    //将编辑后的图片路径返回到上一页面
//                    Intent intent = new Intent();
//                    intent.putExtra("picPath", picPath);
//                    setResult(10, intent);
//                    finish();
                    showShort("图片编辑成功！");
                } else {
                    showShort("图片已损坏！");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadingDialog = new LoadingDialog(this);
        userModel = new DrawModel();
        presenter=new DrawPresenter(this,userModel);
        //本地图片路径
//        File file = new File(Environment.getExternalStorageDirectory().getPath(),
//                "QuanlityCs" + File.separator + "18" + File.separator + "graphList" + File.separator + "1" + ".jpg");
//        if (file.exists()) {
//            graphPath = file.getAbsolutePath();
//        }
        //网络图片路径
        graphPath = "http://pic2.ooopic.com/12/62/16/24bOOOPIC57_1024.jpg";
        initView();
        initDates();
        initListener();
    }

    private void initView() {
        touchView = (DrawView) findViewById(R.id.myView);
        //传递用来存储操作的Model和Presenter对象
        touchView.setMvp(userModel,presenter);

        rectRedPaint = findViewById(R.id.rectRedPaint);
        rectYellowPaint = findViewById(R.id.rectYellowPaint);
        rectBluePaint = findViewById(R.id.rectBluePaint);

        rectClearDraw = findViewById(R.id.rectClearDraw);
        lockDraw = (TextView) findViewById(R.id.lock);
        rectCompleteDraw = findViewById(R.id.rectCompleteDraw);

        rectRedPaintSelected = findViewById(R.id.rectRedPaintSelected);
        rectYellowPaintSelected = findViewById(R.id.rectYellowPaintSelected);
        recttBluePaintSelected = findViewById(R.id.recttBluePaintSelected);

        line1Selected = findViewById(R.id.line1Selected);
        line2Selected = findViewById(R.id.line2Selected);
        line3Selected = findViewById(R.id.line3Selected);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        line1_p = findViewById(R.id.line1_p);
        line2_p = findViewById(R.id.line2_p);
        line3_p = findViewById(R.id.line3_p);

        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_next = (TextView) findViewById(R.id.tv_next);
    }

    private void initDates() {
        andPermissions(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);//适配6.0权限
        initPaint();
        touchView.setPaint(mPaint);
        showLoading();
        loadImageNet();
    }

    /**
     * 获取原图片bitmap对象
     */
    private void loadImageNet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                bitmapNet = BitmapUtils.getBitmap(graphPath);
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF4D4B);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);
    }

    private void initListener() {
        rectRedPaint.setOnClickListener(this);
        rectYellowPaint.setOnClickListener(this);
        rectBluePaint.setOnClickListener(this);

        rectClearDraw.setOnClickListener(this);
        lockDraw.setOnClickListener(this);
        rectCompleteDraw.setOnClickListener(this);

        line1.setOnClickListener(this);
        line2.setOnClickListener(this);
        line3.setOnClickListener(this);

        tv_back.setOnClickListener(this);
        tv_next.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rectRedPaint://选择红色
                doSelectedPainterColor(0);
                break;
            case R.id.rectYellowPaint://选择黄色
                doSelectedPainterColor(1);
                break;
            case R.id.rectBluePaint://选择蓝色
                doSelectedPainterColor(2);
                break;
            case R.id.line1://选择细线条
                doSelectedLineSize(0);
                break;
            case R.id.line2://选择中线条
                doSelectedLineSize(1);
                break;
            case R.id.line3://选择粗线条
                doSelectedLineSize(2);
                break;
            case R.id.rectClearDraw://清除之前编辑的内容
                touchView.clearImage();
                break;
            case R.id.lock://用来判断手势，（文本显示锁定）true代表缩放偏移，（文本显示解锁）false代表编辑图片
                touchView.lock = !touchView.lock;
                lockDraw.setText(touchView.lock ? "锁定" : "解锁");
                break;
            case R.id.tv_back://回退
                touchView.undo();
                break;
            case R.id.tv_next://前进
                touchView.redo();
                break;
            case R.id.rectCompleteDraw://完成按钮，保存编辑后的图片到本地
                saveImage();
                break;
        }
    }

    /**
     * 保存编辑后的图片到本地，获得编辑后的图片路径
     */
    private void saveImage() {
        showLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                picPath = touchView.saveImage();
                handler.sendEmptyMessage(1);
            }
        }).start();
    }

    /**
     * 选择画笔颜色
     *
     * @param position 0、1、2分别代表红、黄、蓝
     */
    private void doSelectedPainterColor(int position) {
        rectRedPaintSelected.setVisibility(View.INVISIBLE);
        rectYellowPaintSelected.setVisibility(View.INVISIBLE);
        recttBluePaintSelected.setVisibility(View.INVISIBLE);
        setLineColor(position);
        switch (position) {
            case 0:
                rectRedPaintSelected.setVisibility(View.VISIBLE);
                setPaiterColor(0xFFFF4D4B);
                break;
            case 1:
                rectYellowPaintSelected.setVisibility(View.VISIBLE);
                setPaiterColor(0xFFffbb00);
                break;
            case 2:
                recttBluePaintSelected.setVisibility(View.VISIBLE);
                setPaiterColor(0xFF00b8ee);
                break;
        }
    }

    /**
     * 设置线条颜色
     *
     * @param position 0、1、2分别代表红、黄、蓝
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setLineColor(int position) {
        switch (position) {
            case 0:
                line1_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_red));
                line2_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_red));
                line3_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_red));
                break;
            case 1:
                line1_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_yellow));
                line2_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_yellow));
                line3_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_yellow));
                break;
            case 2:
                line1_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_blue));
                line2_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_blue));
                line3_p.setBackground(getResources().getDrawable(R.drawable.shape_circle_blue));
                break;
        }
    }

    /**
     * 选择线条粗细
     *
     * @param position 0、1、2分别代表线条从细到粗
     */
    private void doSelectedLineSize(int position) {
        line1Selected.setVisibility(View.INVISIBLE);
        line2Selected.setVisibility(View.INVISIBLE);
        line3Selected.setVisibility(View.INVISIBLE);
        switch (position) {
            case 0:
                line1Selected.setVisibility(View.VISIBLE);
                setPaiterLine(2);
                break;
            case 1:
                line2Selected.setVisibility(View.VISIBLE);
                setPaiterLine(5);
                break;
            case 2:
                line3Selected.setVisibility(View.VISIBLE);
                setPaiterLine(8);
                break;
        }
    }

    /**
     * 重新设置画笔宽度
     *
     * @param strong 画笔宽度
     */
    private void setPaiterLine(int strong) {
        mPaint.setStrokeWidth(strong);
        touchView.setPaint(mPaint);
    }

    /**
     * 重新设置画笔颜色
     *
     * @param color 画笔颜色
     */
    private void setPaiterColor(int color) {
        mPaint.setColor(color);
        touchView.setPaint(mPaint);
    }

    /**
     * Toast短时间显示
     *
     * @param message 显示内容
     */
    public void showShort(CharSequence message) {
        if (mToast == null) {
            mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }


    protected void showLoading() {
        loadingDialog.show();
    }

    protected void dismissLoading() {
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void setBack(boolean savePath) {
        if (savePath){
            tv_back.setClickable(savePath);
            tv_back.setText("回退");
        }else {
            tv_back.setClickable(savePath);
            tv_back.setText("无绘画");
        }
    }

    @Override
    public void setNext(boolean deletePath) {
        if (deletePath){
            tv_next.setClickable(deletePath);
            tv_next.setText("前进");
        }else {
            tv_next.setClickable(deletePath);
            tv_next.setText("无记录");
        }
    }

    /**
     * 适配6.0权限
     * 复制下面所有内容 build里配置远程库compile 'com.yanzhenjie:permission:1.0.5'
     * implements PermissionListener  实现接口
     **/
    private static final int REQUEST_CODE_PERMISSION_LOCATION = 100;
    private static final int REQUEST_CODE_SETTING = 300;
    /**
     * 申请权限
     */
    public static void andPermissions(Activity activity, @NonNull String... permissions){
        if (Build.VERSION.SDK_INT >= 23) {
            // 先判断是否有权限。
            if(AndPermission.hasPermission(activity, permissions)) {
                // 有权限，直接do anything.
                return;
            } else {
                // 申请权限。
                AndPermission.with(activity)
                        .requestCode(REQUEST_CODE_PERMISSION_LOCATION)
                        .permission(permissions)
                        // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                        .rationale(rationaleListener)
                        .send();
            }
        }
    }
    private static RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode,Rationale rationale) {
            rationale.resume();
        }
    };
    @Override
    public void onSucceed(int requestCode, List<String> grantPermissions) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION: {
                showShort("获取权限成功");
                break;
            }
        }
    }

    @Override
    public void onFailed(int requestCode, List<String> deniedPermissions) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION: {
                showShort("获取权限失败");
                break;
            }
        }

        // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            // 第一种：用默认的提示语。
            AndPermission.defaultSettingDialog(this, REQUEST_CODE_SETTING).show();

            // 第二种：用自定义的提示语。
//             AndPermission.defaultSettingDialog(this, REQUEST_CODE_SETTING)
//                     .setTitle("权限申请失败")
//                     .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
//                     .setPositiveButton("好，去设置")
//                     .show();

//            第三种：自定义dialog样式。
//            SettingService settingHandle = AndPermission.defineSettingDialog(this, REQUEST_CODE_SETTING);
//            你的dialog点击了确定调用：
//            settingHandle.execute();
//            你的dialog点击了取消调用：
//            settingHandle.cancel();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // listener方式，最后一个参数是PermissionListener。
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
