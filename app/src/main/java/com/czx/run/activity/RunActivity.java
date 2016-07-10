package com.czx.run.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.baidubce.services.bos.model.PutObjectResponse;
import com.czx.run.R;
import com.czx.run.model.RunRecord;
import com.czx.run.model.CommonResult;
import com.czx.run.utils.BOSUtils;
import com.czx.run.utils.Const;
import com.czx.run.utils.RunAPIService;
import com.google.gson.Gson;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class RunActivity extends AppCompatActivity implements AMapLocationListener,LocationSource, AMap.OnMapLoadedListener {


    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationClientOption;
    private AMap aMap;
    private Polyline polyline;
    private PolylineOptions polylineOptions;
    //处理定位更新的接口
    private LocationSource.OnLocationChangedListener mListener;

    private MapView mapView;
    private TextView showTime,showDistance;
    private Button start,stop;
    public ProgressDialog progressDialog;


    //标识状态
    private boolean isStart = false;
    private boolean isFirstStart = true;
    private boolean isFirstLoc = true;
    //轨迹点集合
    private List<LatLng> points = new ArrayList<>();
    private float distance;
    //计时器变量
    private long firstStartTime=0;
    private long currentTime=0;
    private long startTime=0;
    private long sumTime=0;
    private static RunRecord runRecord =new RunRecord();
    private String token;

    public static final String TOKEN = "token";
    private static final float weight = 60;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        Log.i("tag","firstStartTime"+firstStartTime+",currentTime"+currentTime+",sumTime"+sumTime);
        //获取定位权限
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }

        initView(savedInstanceState);
        token = getIntent().getStringExtra(TOKEN);
        setupLocation();
    }

    /**
     * 设置定位参数以及启动定位
     */
    private void setupLocation() {
        //初始化定位客户端
        locationClient = new AMapLocationClient(getApplicationContext());
        locationClient.setLocationListener(this);
        locationClientOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationClientOption.setNeedAddress(true);
        //设置定位间隔,单位毫秒,默认为2000ms
        locationClientOption.setInterval(2000);
        locationClient.setLocationOption(locationClientOption);
        //启动定位，调用onLocationChange
        locationClient.startLocation();
    }

    /**
     * 设置AMap
     */
    private void setUpMap() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.alpha(Color.WHITE));
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);

        //设置定位监听
        aMap.setLocationSource(this);
        //设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        //设置为定位模式
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
        aMap.setOnMapLoadedListener(this);
        polylineOptions = new PolylineOptions();
        polylineOptions.width(30);
        polylineOptions.color(ContextCompat.getColor(this,R.color.colorPrimaryDark));
    }


    private void initView(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map);
        showTime = (TextView) findViewById(R.id.show_time);
        showDistance = (TextView) findViewById(R.id.show_distance);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);

        //设置开始按钮监听，主要是记录时间
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = !isStart;
                if(isStart){
                    stop.setEnabled(false);
                    startTimer();
                    if(isFirstStart){
                        //记录初次开始毫秒数
                        firstStartTime = System.currentTimeMillis();
                        isFirstStart = false;
                        runRecord.setDate(new Timestamp(new Date().getTime()));
                    }else{
                        //每次暂停加起来的毫秒数
                        sumTime =  sumTime + System.currentTimeMillis()-startTime;
                    }
                    start.setText(getResources().getString(R.string.stop_locate));
                }else{
                    stop.setEnabled(true);
                    //记录本次暂停开始时的毫秒数
                    startTime = System.currentTimeMillis();
                    start.setText(getResources().getString(R.string.start_locate));
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialog();
            }
        });

        mapView.onCreate(savedInstanceState);
        if(aMap == null){
            aMap = mapView.getMap();
            setUpMap();
        }
    }

    /**
     * 启动计时器的方法
     */
    public void startTimer(){
        new Thread(new MyTimerRunnable(this)).start();
    }

    private static class MyTimerRunnable implements Runnable {

        private WeakReference<RunActivity> mActivity;

        public MyTimerRunnable(RunActivity activity){
            mActivity = new WeakReference<RunActivity>(activity);
        }

        @Override
        public void run() {
            RunActivity activity = mActivity.get();
            while(activity.isStart){
                Message msg = Message.obtain();
                msg.what = Const.MSG_TIMER;
                msg.obj = System.currentTimeMillis();
                activity.mHandler.sendMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * 是否结束本次运动的dialog
     */
    private void startDialog() {
        if(distance < 10){
//        if(false){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("本次运动距离太短将不会保存，是否结束运动");
            builder.setNegativeButton("继续运动", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(RunActivity.this,"继续运动",Toast.LENGTH_SHORT).show();
                }
            });
            builder.setPositiveButton("结束运动", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (locationClient != null) {
                        locationClient.stopLocation();
                        locationClient.onDestroy();
                        locationClient = null;
                        locationClientOption = null;
                    }
                    finish();
//                    Intent intent = new Intent(RunActivity.this,RunRecordActivity.class);
//                    intent.putExtra(TOKEN,token);
//                    startActivity(intent);
                }
            });
            builder.show();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("保存本次运动");
            builder.setNegativeButton("取消",null);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Gson gson = new Gson();
                    String pointsStr = gson.toJson(points);
                    String pointsKey = UUID.randomUUID().toString();
                    runRecord.setPointsKey(pointsKey);
                    progressDialog = new ProgressDialog(RunActivity.this);
                    progressDialog.setTitle("正在上传数据");
                    progressDialog.show();
                    getAddress();
                    uploadPoints(pointsStr,pointsKey);
                }
            });
            builder.show();
        }
    }

    /**
     * 通过坐标点得到地址
     */
    public void getAddress(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                GeocodeSearch geocodeSearch = new GeocodeSearch(RunActivity.this);
                LatLonPoint point =new LatLonPoint(points.get(0).latitude, points.get(0).longitude);
                RegeocodeQuery regeocodeQuery = new RegeocodeQuery(point, 1000,GeocodeSearch.AMAP);
                RegeocodeAddress regeocodeAddress = null;
                String address = null;
                try {
                    regeocodeAddress = geocodeSearch.getFromLocation(regeocodeQuery);
                } catch (com.amap.api.services.core.AMapException e) {
                    e.printStackTrace();
                }
                if(null != regeocodeAddress){
                    address = regeocodeAddress.getCity()+regeocodeAddress.getDistrict();
                }
                Message msg = Message.obtain();
                msg.what = Const.MSG_GET_ADDRESS;
                msg.obj = address;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private static class GetAddressRunnable implements Runnable{

        @Override
        public void run() {

        }
    }

    /**
     * 上传点集合
     * @param pointsStr
     */
    private void uploadPoints(final String pointsStr,final String pointskey) {
        new Thread(new Runnable(){
            @Override
            public void run() {
                PutObjectResponse response = BOSUtils.getInstance()
                        .putObject(BOSUtils.BUCKET,pointskey,pointsStr);
//                Toast.makeText(RunActivity.this,response.getETag(),Toast.LENGTH_SHORT).show();
//                Message msg = Message.obtain();
//                msg.what = Const.MSG_UPLOAD_POINT;
//                msg.obj = response;
//                mHandler.sendMessage(msg);
            }
        }).start();
    }


    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler{

        private final WeakReference<RunActivity> mActivity;

        public MyHandler(RunActivity activity){
            mActivity = new WeakReference<RunActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final RunActivity activity = mActivity.get();
            if(activity != null){
                switch(msg.what){
                    case Const.MSG_TIMER:
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                        activity.currentTime = (long) msg.obj;
                        activity.currentTime = activity.currentTime - activity.firstStartTime - 8*60*60*1000 - activity.sumTime;
                        String formatCurrTime = simpleDateFormat.format(activity.currentTime);
                        activity.showTime.setText(formatCurrTime);
                        break;
                    case Const.MSG_DOWNLOAD_POINTS:
                        PutObjectResponse response = (PutObjectResponse) msg.obj;
                        Toast.makeText(activity,"上传点成功 reponse ="+response.getETag(),Toast.LENGTH_SHORT).show();

                    case Const.MSG_GET_ADDRESS:
                        Toast.makeText(activity,"获取地址成功",Toast.LENGTH_SHORT).show();
                        String address = (String) msg.obj;
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String date = sdf.format(runRecord.getDate());

                        float runTime = (activity.currentTime+8*60*60*1000)/1000;
                        //速度 米/秒
                        float speed = activity.distance/runTime;
                        //速度 分钟/400米
                        speed = 400/speed/60;
                        //跑步热量（kcal）＝体重（kg）×运动时间（hour）×指数K  指数K＝30÷速度（分钟/400米）
                        float calorie = weight*(runTime/3600)*(30/speed);

                        String token = activity.getIntent().getStringExtra(TOKEN);

                        RunAPIService.getInstance().add("add",token,date, String.valueOf(activity.distance),
                                String.valueOf(calorie),String.valueOf(runTime),runRecord.getPointsKey(),address)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<CommonResult>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        activity.progressDialog.dismiss();
                                    }

                                    @Override
                                    public void onNext(CommonResult commonResult) {
                                        if(commonResult.getResultCode()==0){
                                            activity.progressDialog.dismiss();
                                            Toast.makeText(activity,"上传用户数据成功",Toast.LENGTH_SHORT).show();
                                            if (activity.locationClient != null) {
                                                Log.i(""+getClass(),activity.distance+"");
                                                activity.mListener = null;
//                                                activity.points = null;
                                                activity.locationClient.stopLocation();
                                                activity.locationClient.onDestroy();
                                                activity.locationClient = null;
                                                activity.locationClientOption = null;
                                            }
                                            activity.finish();
                                        }
                                    }
                                });
                        break;
                }
            }
        }
    }






    /**
     * 地图加载完成后调用此方法
     */
    @Override
    public void onMapLoaded() {
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
        if(points != null){
            aMap.addPolyline(polylineOptions);
        }
    }

    /**
     * 定位回调监听，当定位完成后调用此方法
     * @param aMapLocation 定位数据
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(mListener != null && aMapLocation != null){
//            Log.i("tag",aMapLocation.getErrorCode()+"");
            if(aMapLocation != null && aMapLocation.getErrorCode() == 0){
                //显示系统小蓝点
                mListener.onLocationChanged(aMapLocation);
                LatLng currentLatLng = new LatLng(aMapLocation.getLatitude(),aMapLocation.getLongitude());
                if(isFirstLoc){
                    isFirstLoc = false;
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(currentLatLng));
                }
                //精度小于预定值且开始运动
                if(aMapLocation.getAccuracy()<15 && isStart){
                    aMap.addPolyline(polylineOptions.add(currentLatLng));
                    points.add(currentLatLng);
                    if(points.size() >= 2){
                        distance += AMapUtils.calculateLineDistance(points.get(points.size()-1),points.get(points.size()-2));
                        DecimalFormat decimalFormat = new DecimalFormat(".00");
                        String disStr = decimalFormat.format(distance);
                        showDistance.setText(disStr);
                    }
                }
            }else{
                Log.i("info",aMapLocation.getErrorInfo());
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
        if(locationClient != null){
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(aMap != null){
            aMap.clear();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        deactivate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isStart = false;
        locationClientOption = null;
//        points = null;
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }



//    /**
//     * 对地图截屏
//     */
//    private void getMapScreenShot() {
//        aMap.getMapScreenShot(this);
//    }
//
//    @Override
//    public void onMapScreenShot(Bitmap bitmap) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        try {
//            // 保存在SD卡根目录下，图片为png格式。
//            FileOutputStream fos = new FileOutputStream(
//                    Environment.getExternalStorageDirectory() + "/test_"
//                            + sdf.format(new Date()) + ".png");
//            boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            try {
//                fos.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void onMapScreenShot(Bitmap bitmap, int i) {
//
//    }

}
