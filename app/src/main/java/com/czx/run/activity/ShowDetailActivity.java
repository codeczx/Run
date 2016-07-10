package com.czx.run.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.czx.run.R;
import com.czx.run.model.RunRecord;
import com.czx.run.utils.BOSUtils;
import com.czx.run.utils.Const;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by czx on 2016/7/4.
 */
public class ShowDetailActivity extends Activity implements LocationSource, AMap.OnMapLoadedListener {

    public TextView distance,time,date,speed,calorie;
    public AMap aMap;
    public MapView mapView = null;
    public PolylineOptions polylineOptions;

    private RunRecord runRecord;
    private List<LatLng> points;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);
        runRecord= (RunRecord) getIntent().getSerializableExtra("runRecord");
        Log.i(""+getClass(),"getCalorie"+runRecord.getCalorie()+"time"+runRecord.getRunTime()+"");
        initView();
        initData();
        initMap(savedInstanceState);
        uploadPoints();
    }

    private void initData() {
        DecimalFormat df = new DecimalFormat("#0.00");
        String distanceStr = df.format(runRecord.getDistance() / 1000);

        float runTime = runRecord.getRunTime() * 1000 - 8 * 1000 * 3600;
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.format_time), Locale.getDefault());
        String timeStr = sdf.format(runTime);

        df = new DecimalFormat("#0.0");
        String calorieStr = df.format(runRecord.getCalorie());

        sdf = new SimpleDateFormat(getString(R.string.format_min),Locale.getDefault());
        float avgtime = (float) ((1.0 / ((runRecord.getDistance() / 1000) /
                runRecord.getRunTime())) * 1000 - 8 * 3600 * 1000);
        String speedStr = sdf.format(avgtime);

        sdf = new SimpleDateFormat(getString(R.string.format_date_hour),Locale.getDefault());
        String dateStr = sdf.format(runRecord.getDate());

        Log.i(""+getClass(),runRecord.getDate().toString());

        distance.setText(distanceStr);
        time.setText(timeStr);
        calorie.setText(calorieStr);
        speed.setText(speedStr);
        date.setText(dateStr);

    }

    private void initView() {
        distance = (TextView) findViewById(R.id.tv_show_distance);
        time = (TextView) findViewById(R.id.tv_show_time);
        date = (TextView) findViewById(R.id.tv_show_date);
        speed = (TextView) findViewById(R.id.tv_show_speed);
        calorie = (TextView) findViewById(R.id.tv_show_calorie);
    }

    private void initMap(Bundle savedInstanceState) {
        mapView = (MapView) findViewById(R.id.map_item);
        Log.i(""+getClass(),"mapView = "+ mapView.toString());

        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
            aMap.setOnMapLoadedListener(this);
            polylineOptions = new PolylineOptions();
            polylineOptions.width(30);
            polylineOptions.color(ContextCompat.getColor(this, R.color.yellow));
        }
    }

    private void setUpMap() {
        //将系统蓝点设置为透明
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.alpha(Color.WHITE));
        aMap.setMyLocationStyle(myLocationStyle);

        // 设置定位监听,activate & deactivate
        aMap.setLocationSource(this);
        // 设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
    }

    private void uploadPoints(){

        new Thread(new Runnable(){
            @Override
            public void run() {
                InputStream is = BOSUtils.getInstance().getObject(BOSUtils.BUCKET,runRecord.getPointsKey()).getObjectContent();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line = null;
                try {
                    while((line = br.readLine()) != null){
                        sb.append(line);
                    }
                    Gson gson = new Gson();
                    points = gson.fromJson(sb.toString(),new TypeToken<List<LatLng>>(){}.getType());
                    Message msg = Message.obtain();
                    msg.what = Const.MSG_DOWNLOAD_POINTS;
                    msg.obj = points;
                    mHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                Toast.makeText(RunActivity.this,putObjectResponseFromString.getETag(),Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case Const.MSG_DOWNLOAD_POINTS:
                    aMap.addPolyline(polylineOptions.addAll(points));
                    double maxLat=points.get(0).latitude,minLat=points.get(0).latitude,
                            maxLong=points.get(0).longitude,minLong=points.get(0).longitude;
                    for(LatLng point : points){
                        if(point.latitude > maxLat){
                            maxLat = point.latitude;
                        }
                        if(point.latitude < minLat){
                            minLat = point.latitude;
                        }
                        if(point.longitude > maxLong){
                            maxLong = point.longitude;
                        }
                        if(point.longitude < minLat){
                            minLong = point.longitude;
                        }
                    }
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng((maxLat+minLat)/2,(maxLong+minLong)/2)));
            }
        }
    };

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(aMap != null)
            aMap.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        points = null;
    }

    @Override
    public void onMapLoaded() {
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
    }
}
