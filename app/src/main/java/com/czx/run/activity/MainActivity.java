package com.czx.run.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.czx.run.R;
import com.czx.run.fragment.GroupFragment;
import com.czx.run.fragment.RunRecordFragment;
import com.czx.run.model.SumResult;
import com.czx.run.model.User;
import com.czx.run.fragment.HomeFragment;
import com.czx.run.utils.BOSUtils;
import com.czx.run.utils.CircleTransformation;
import com.czx.run.utils.Const;
import com.czx.run.utils.LogUtils;
import com.czx.run.view.LoginDialog;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private User user;
    private HomeFragment homeFragment;
    private TextView tvHeaderName;
    private ImageView ivHeader;
    private int headSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDefaultFragment();

        initView();
        startLogin();
    }

    private void setDefaultFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft= fm.beginTransaction();
        ft.replace(R.id.container,new HomeFragment()).commit();
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,drawer,toolbar,R.string.app_name,R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        tvHeaderName = (TextView) headerView.findViewById(R.id.tv_header_username);
        ivHeader = (ImageView) headerView.findViewById(R.id.img_header_pic);

        ivHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUserProfile();
            }
        });
    }

    /**
     * 判断是否有缓存，若有则从sharedPreferences里取出数据，没有则通过网络请求获取
     * 在获取数据后改变header和fragment里显示的数据
     */
    private void startLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData",MODE_PRIVATE);
        String email = sharedPreferences.getString("email","");
        if(email.equals("")){
            Log.i("tag","get data from internet");
            LoginDialog loginDialog = new LoginDialog(MainActivity.this, new LoginDialog.LoginDialogEventListener() {
                @Override
                public void setUserData() {
                    SharedPreferences preferences = getSharedPreferences(Const.SP_NAME,MODE_PRIVATE);
                    downloadPhotoFile(preferences.getString(Const.PHOTONAME,""));
                    setDefaultFragment();
                }
            });
            loginDialog.show();
        }else{
            setUpHeader();
            SumResult sum = new SumResult();
            sum.setSumDistance(sharedPreferences.getFloat("distance",0));
            sum.setSumRunTime(sharedPreferences.getFloat("runTime",0));
            sum.setSumTime(sharedPreferences.getFloat("time",0));
            sum.setSumCalorie(sharedPreferences.getFloat("calorie",0));
        }
    }

    private void downloadPhotoFile(final String photoKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(getClass(),"downloading..");
                File file = new File(String.format("%s%s%s.tmp",getExternalCacheDir().getPath(),File.separator,photoKey));
                InputStream is = BOSUtils.getInstance()
                        .getObject(BOSUtils.BUCKET,photoKey)
                        .getObjectContent();
                try {
                    OutputStream os = new FileOutputStream(file);
                    int count = 0;
                    byte[] buffer = new byte[8192];
                    while((count = is.read(buffer,0,8192))!= -1){
                        os.write(buffer,0,count);
                    }
                    is.close();
                    os.close();
                    Message msg = Message.obtain();
                    msg.what = Const.MSG_DOWNLOAD_PIC;
                    msg.obj = file;
                    mHandler.sendMessage(msg);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler{

        private WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
            switch(msg.what){
                case Const.MSG_DOWNLOAD_PIC:
                    activity.setUpHeader();
                    break;
            }
        }
    }

//    private static class MyRunnable implements Runnable{
//
//        @Override
//        public void run() {
//
//        }
//    }

    private void setUpHeader() {
        SharedPreferences preference = getSharedPreferences(Const.SP_NAME,MODE_PRIVATE);
        String name = preference.getString(Const.NAME,"");
        String photoName = preference.getString(Const.PHOTONAME,"");
        tvHeaderName.setText(name);
        File file = new File(String.format("%s%s%s.tmp",getExternalCacheDir().getPath(),File.separator,photoName));
        headSize = getResources().getDimensionPixelSize(R.dimen.normal_avatar_size);
        Picasso.with(this)
                .load(file)
                .placeholder(R.drawable.img_circle_placeholder)
                .error(R.mipmap.ic_launcher)
                .resize(headSize,headSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(ivHeader);
    }




    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        switch(item.getItemId()){
            case R.id.homePage:
                HomeFragment homeFragment = new HomeFragment();
                ft.replace(R.id.container,homeFragment)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.exercise:
                RunRecordFragment runRecordFragment = new RunRecordFragment();
                ft.replace(R.id.container,runRecordFragment).commit();
                break;
            case R.id.group:
                GroupFragment groupFragment = new GroupFragment();
                ft.replace(R.id.container,groupFragment).commit();
                break;
            case R.id.set_profile:
                setUserProfile();
                break;
            case R.id.login_out:
                SharedPreferences sp = getSharedPreferences("UserData",MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear().commit();
                setDefaultFragment();
                startLogin();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUserProfile() {
        Intent intent = new Intent(this,UserProfileActivity.class);
        startActivityForResult(intent, Const.REQUEST_CHANGE_USERPROFILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == Const.REQUEST_CHANGE_USERPROFILE){
           setUpHeader();
        }
    }
}
