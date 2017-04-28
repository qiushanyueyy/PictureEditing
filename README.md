# PictureEditing
图片编辑控件

重写onTouchEvent()方法监听触摸事件：                                    
当只有一个手指触摸时设置mode状态为DRAW，代表绘画操作。
MotionEvent.ACTION_DOWN:把画笔移动到触摸处开始绘画，
MotionEvent.ACTION_MOVE:根据移动路线绘制圆滑曲线，即贝塞尔曲线，
MotionEvent.ACTION_UP:手指抬起时将手指从触摸到抬起的路线绘制到Canvas上，保存绘制信息到List。 
当两只手指触摸时设置mode状态为DRAG_ZOOM，代表缩放和偏移操作。
MotionEvent.ACTION_POINTER_DOWN:记录按下时两点的距离和Matri信息以及第一只手指的XY坐标（通过spacing()方法获得按下时两点的距离），
MotionEvent.ACTION_MOVE:根据按下时记录的XY坐标和当前XY坐标计算偏移量，根据按下时两点之间的距离和当前两点之间的距离计算缩放比例，
MotionEvent.ACTION_UP:记录缩放偏移之后的Matri信息，通过setEdge()方法判断缩放比例有么有超过最大值或最小值。

LoadingDialog
使用progress第三方控件，处理耗时操作时弹出的圆形加载框，具体实现方法：
build文件中引用compile 'com.pnikosis:materialish-progress:1.7'
dialog_loading.xml中引用com.pnikosis.materialishprogress.ProgressWheel

LoadingDialog耗时操作时显示的dialog
Drawview   自定义画图控件
Drawbean 储存操作的实体类
BitmapUtils 通过图片路径获取图片的bitmap对象（支持本地路径和网络路径）

适配Android6.0权限
build文件中引用compile 'com.yanzhenjie:permission:1.0.5'
MainActivity中实现PermissionListener接口 
调用andPermissions（）方法添加6.0权限
