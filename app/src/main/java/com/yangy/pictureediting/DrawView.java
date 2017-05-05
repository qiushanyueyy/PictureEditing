package com.yangy.pictureediting;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * Created by yangy on 17/4/21.
 */

/**
 * 自定义图片编辑控件
 */
public class DrawView extends View {
    private static final float MAX_SCALE = 2.0f;//最大缩放倍数
    private static final float MIN_SCALE = 0.5f;//最小缩放倍数
    final public static int DRAG = 1;//偏移
    final public static int ZOOM = 2;//缩放
    final public static int DRAG_ZOOM = 3;//偏移缩放
    final public static int DRAW = 4;//绘画
    final public static int NONE = 0;

    private Paint mPaint = null;        // used to draw line
    private Paint mBitmapPaint = null;    // used to draw bitmap
    private Path mCaPath = null;
    private Path mPath = null;// save the point
    private Bitmap mBitmap = null;        // used as choosing picture
    private Bitmap mBottomBitmap = null;// used as bottom background
    private Canvas mCanvas = null;        // what's it used for
    private float posX, posY;            // used as touched position
    private final float TOUCH_TOLERANCE = 4;//最小编辑操作，操作路线大于4才算有效操作

    private DrawPath mDrawPath = null;

    private int mImageWidth = 480;
    private int mImageHeight = 800;

    private List<DrawPath> mSavePath = new ArrayList<>();//保存绘制的记录
    private List<DrawPath> mDeletePath = new ArrayList<>();//保存清除的记录

    public int mode = 0;
    private Matrix matrix = new Matrix();
    private Matrix matrix1 = new Matrix();//记录原图矩阵
    private Matrix saveMatrix = new Matrix();//记录缩放之后的矩阵
    private float x_down = 0;//记录按下时x轴坐标
    private float y_down = 0;//记录按下时y轴坐标
    private float initDis = 1f;//记录按下时两点的距离
    private float[] values = new float[9];
    private float[] startValues = new float[9];
    public boolean lock = true;//用来判断手势，true代表缩放偏移，false代表编辑图片
    private long mLastTime = 0L;//记录上一次触摸时间
    private long mCurTime = 0L;//记录当前触摸时间
    private IDrawView presenter;

    public DrawView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    public DrawView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public DrawView(Context context) {
        super(context);
        init();
    }

    /**
     * 设置接口回调
     *
     * @param iDrawView 接口对象
     */
    public void setInterfaceCallback(IDrawView iDrawView) {
        presenter = iDrawView;
    }

    /**
     * 用来保存操作信息
     */
    public class DrawPath {
        public Path path;
        public Paint paint;
    }

    private void init() {
        mImageWidth = getMeasuredWidth();
        mImageHeight = getMeasuredHeight();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFCCCCCC);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        matrix = new Matrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mImageWidth = w;
        mImageHeight = h;

        mBottomBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBottomBitmap);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(0x00FFFFFF);
        canvas.drawBitmap(mBottomBitmap, matrix, mBitmapPaint);
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Map<String, Float> map1 = getImageViewIneerSize();
        //仿美图秀秀图片编辑
        int pointCount = event.getPointerCount();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://把画笔移动到触摸处开始绘画
                mode = DRAW;
                mPath = new Path();
                mCaPath = new Path();
                mDrawPath = new DrawPath();
                mDrawPath.paint = new Paint(mPaint);
                mDrawPath.path = mCaPath;
                mPath.moveTo(x, y);
                mCaPath.moveTo(x * map1.get("scaleX"), y * map1.get("scaleY"));
                posX = x;
                posY = y;
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mode == DRAW) {//手指抬起时将手指从触摸到抬起的路线绘制到Canvas上
                    mCaPath.lineTo(posX * map1.get("scaleX"), posY * map1.get("scaleY"));
                    mCaPath.offset(-map1.get("offsetX") * map1.get("scaleX"), -map1.get("offsetY") * map1.get("scaleY"));
                    mCanvas.drawPath(mCaPath, mPaint);
                    mSavePath.add(mDrawPath);
                    boolean back = (mSavePath.size() > 0 ? true : false);
                    presenter.setBack(back);
                    mPath = null;
                    postInvalidate();
                    matrix.getValues(values);
                } else {//记录缩放和偏移后的矩阵
                    matrix.getValues(values);
                    saveMatrix.set(matrix);
                    matrix1.getValues(startValues);
                    if (mBitmap != null) {
                        setEdge();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointCount < 2) {//根据移动路线绘制圆滑曲线，即贝塞尔曲线
                    if (mode == DRAW) {
                        float dx = Math.abs(x - posX);
                        float dy = Math.abs(y - posY);
                        if (dx > TOUCH_TOLERANCE || dy > TOUCH_TOLERANCE) {
                            mPath.quadTo(posX, posY, (x + posX) / 2, (y + posY) / 2);
                            mCaPath.quadTo(posX * map1.get("scaleX"), posY * map1.get("scaleY"), (x + posX) * map1.get("scaleY") / 2, (y + posY) * map1.get("scaleY") / 2);
                            posX = x;
                            posY = y;
                        }
                        postInvalidate();
                    }
                } else {
                    //当一只手指绘画之后没有抬起来再按下第二只手指，会中断绘画操作并进行缩放偏移。 处理：当第二只手指按下时清除绘画
                    mPath = new Path();
                    mCaPath = new Path();
                    postInvalidate();

                    float newDist = spacing(event);//获得当前两点之间的距离
                    matrix.set(saveMatrix);
                    matrix.postTranslate(event.getX() - x_down, event.getY() - y_down);
                    float tScale = newDist / initDis;//根据按下时两点之间的距离和当前两点之间的距离计算缩放比例
                    matrix.postScale(tScale, tScale, mImageWidth / 2, mImageHeight / 2);
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                initDis = spacing(event);//记录按下时两点之间的距离
                matrix.set(saveMatrix);
                x_down = event.getX();
                y_down = event.getY();
                mode = DRAG_ZOOM;
                break;
        }
        return true;
    }

    /**
     * 控制图片缩放的最大值和最小值
     */
    private void setEdge() {
        if ((values[0] / startValues[0]) > MAX_SCALE) {
            matrix.postScale((startValues[0] * MAX_SCALE) / values[0], (startValues[0] * MAX_SCALE) / values[0], mImageWidth / 2, mImageHeight / 2);
        } else if (values[0] < MIN_SCALE) {
            matrix.postScale((startValues[0] * MIN_SCALE) / values[0], (startValues[0] * MIN_SCALE) / values[0], mImageWidth / 2, mImageHeight / 2);
        }
        saveMatrix.set(matrix);
        getImageViewIneerSize();
        postInvalidate();
    }

    /**
     * Matrix{[cosX,-sinX,translateX][sinX,cosX,translateY][0,0,scale]}
     * sinX和cosX，表示旋转角度的cos值和sin值，旋转角度是按顺时针方向计算的。
     * translateX和translateY表示x和y的平移量。scale是缩放的比例，1是不变，2是表示缩放1/2。
     *
     * @return 返回矩阵缩放平移系数
     */
    private Map<String, Float> getImageViewIneerSize() {
        Map<String, Float> size = new HashMap<String, Float>();
        //获得ImageView中Image的变换矩阵
        matrix.getValues(values);
        //Image在绘制过程中的变换矩阵，从中获得x和y方向的缩放系数
        float sx = values[0];
        float sy = values[4];
        //计算Image在屏幕上实际绘制的宽高
        size.put("scaleX", 1 / sx);
        size.put("scaleY", 1 / sy);
        size.put("offsetX", values[2]); //X轴的translate的值
        size.put("offsetY", values[5]);//Y轴的translate的值
        return size;
    }

    //取两点的距离
    private float spacing(MotionEvent event) {

        try {
            float x = event.getX(1) - event.getX(0);
            float y = event.getY(1) - event.getY(0);
            float z = (float) Math.sqrt(x * x + y * y);
            if (z >= x && z >= y) {
                mode = ZOOM;
            } else {
                mode = DRAG;
            }
            return z;
        } catch (IllegalArgumentException ex) {
            Log.v("TAG", ex.getLocalizedMessage());
            return 0;
        }
    }

    /**
     * 显示需要编辑的原图片
     *
     * @param imagePath 原图片bitmap对象
     * @return true代表图片显示成功，false代表图片显示失败
     */
    public boolean setBitmap(Bitmap imagePath) {
        int width = imagePath.getWidth();
        int height = imagePath.getHeight();

        float nxScale;
        float nyScale;
        float tScale = 1.0f;
        if (width != 0 && height != 0) {//如果图片的宽高不等于0代表图片显示成功
            nxScale = (float) width / mImageWidth;
            nyScale = (float) height / mImageHeight;
            if (nxScale >= 1 && nyScale >= 1 || nxScale < 1 && nyScale < 1) {
                if (nxScale > nyScale) {
                    width = (int) (width / nxScale);
                    height = (int) (height / nxScale);
                    tScale = nxScale;
                } else {
                    width = (int) (width / nyScale);
                    height = (int) (height / nyScale);
                    tScale = nyScale;
                }
            }
            if (nxScale >= 1 && nyScale < 1) {
                width = mImageWidth;
                height = (int) (height / nxScale);
                tScale = nxScale;
            }
            if (nxScale <= 1 && nyScale >= 1) {
                height = mImageHeight;
                width = (int) (width / nyScale);
                tScale = nyScale;
            }
            matrix.postScale(1 / tScale, 1 / tScale);
            matrix.postTranslate((mImageWidth - width) / 2, (mImageHeight - height) / 2);//图片居中显示
            matrix1.set(matrix);
            saveMatrix.set(matrix);
            getImageViewIneerSize();
            int tempW = imagePath.getWidth();
            int tempH = imagePath.getHeight();
            if (imagePath.getWidth() > 4000) {
                tempW = tempW / 2;
                tempH = tempH / 2;
            }
            mBitmap = Bitmap.createScaledBitmap(imagePath, tempW, tempH, true);
            mBottomBitmap = Bitmap.createBitmap(tempW, tempH, Bitmap.Config.ARGB_4444);
            if (mBottomBitmap == null) {
                mBitmap.recycle();
                return false;
            }
            mSavePath.clear();
            mDeletePath.clear();
            mCanvas.setBitmap(mBottomBitmap);
            mCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            postInvalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 设置画笔属性
     *
     * @param paint
     */
    public void setPaint(Paint paint) {
        mPaint = paint;
        postInvalidate();
    }

    /**
     * 图片编辑完成之后保存到本地
     *
     * @return 图片本地路径
     */
    public String saveImage() {
        if (mBitmap == null) {
            return null;
        }
        //创建存放缓存的文件夹
        File outfile = new File(Environment.getExternalStorageDirectory().getPath(), "PictureEditing/cache/");
        //如果文件不存在，则创建一个新文件
        if (!outfile.isFile()) {
            outfile.mkdirs();
        }
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "/PictureEditing/cache/tempPic" + new Date().getTime() + ".jpg");
        try {
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            mBottomBitmap.compress(CompressFormat.PNG, 80, out);
            mBottomBitmap.recycle();
            mBitmap.recycle();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    /**
     * 图片编辑完成之后保存到本地   （如果保存失败可能是文件夹不存在 需要先创建文件夹）
     *
     * @param graphPath 想要保存的路径
     * @return 图片本地路径
     */
    public String savePhotoImage(String graphPath) {
        if (mBitmap == null) {
            return null;
        }
        File file = new File(graphPath);
        try {
            if (!file.createNewFile()) {
                file.delete();
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            mBottomBitmap.compress(CompressFormat.PNG, 100, out);
            mBottomBitmap.recycle();
            mBitmap.recycle();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    /**
     * 清除之前编辑的内容
     */
    public void clearImage() {
        mSavePath.clear();
        mDeletePath.clear();
        repaint();
        postInvalidate();
    }

    /**
     * 清除上一步的操作
     */
    public void undo() {
        int nSize = mSavePath.size();
        if (nSize >= 1) {
            mDeletePath.add(0, mSavePath.get(nSize - 1));
            mSavePath.remove(nSize - 1);
        } else {
            return;
        }
        repaint();
        Iterator<DrawPath> iter = mSavePath.iterator();
        DrawPath temp;
        while (iter.hasNext()) {
            temp = iter.next();
            mCanvas.drawPath(temp.path, temp.paint);
        }
        postInvalidate();
    }

    /**
     * 重绘清除的操作
     */
    public void redo() {
        int nSize = mDeletePath.size();
        if (nSize >= 1) {
            mSavePath.add(mDeletePath.get(0));
            mDeletePath.remove(0);
        } else {
            return;
        }
        repaint();
        Iterator<DrawPath> iter = mSavePath.iterator();
        DrawPath temp;
        while (iter.hasNext()) {
            temp = iter.next();
            mCanvas.drawPath(temp.path, temp.paint);
        }
        postInvalidate();
    }

    /**
     * 重绘
     */
    private void repaint() {
        boolean back = (mSavePath.size() > 0 ? true : false);
        presenter.setBack(back);
        boolean next = (mDeletePath.size() > 0 ? true : false);
        presenter.setNext(next);
        if (mBitmap != null) {
            int width = mBitmap.getWidth();
            int height = mBitmap.getHeight();
            mBottomBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas.setBitmap(mBottomBitmap);
            mCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        } else {
            int width = mCanvas.getWidth();
            int height = mCanvas.getHeight();
            mBottomBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas.setBitmap(mBottomBitmap);
        }
    }
}
