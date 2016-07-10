package com.czx.run.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidubce.services.bos.model.PutObjectResponse;
import com.czx.run.R;
import com.czx.run.model.CommonResult;
import com.czx.run.utils.BOSUtils;
import com.czx.run.utils.CircleTransformation;
import com.czx.run.utils.Const;
import com.czx.run.utils.LogUtils;
import com.czx.run.utils.RunAPIService;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserProfileActivity extends AppCompatActivity {

    private static final int REQUEST_LOAD_IMAGE = 0;
    private static final int REQUEST_CROP_IMAGE = 1;

    private ImageView mImageView;
    private String photoName,token;
    private int headSize;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            toolbar.setTitle(getResources().getString(R.string.userProfile));
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        TextView tvName,tvEmail,tvWeight;
        tvName = (TextView) findViewById(R.id.tv_userProfile_name);
        tvEmail = (TextView) findViewById(R.id.tv_userProfile_email);
        tvWeight = (TextView) findViewById(R.id.tv_userProfile_weight);
        mImageView = (ImageView) findViewById(R.id.img_userProfile_head);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(albumIntent,REQUEST_LOAD_IMAGE);
            }
        });
        headSize = getResources().getDimensionPixelSize(R.dimen.big_avatar_size);
        SharedPreferences preference = getSharedPreferences(Const.SP_NAME,MODE_PRIVATE);
        String name = preference.getString(Const.NAME,"");
        String email = preference.getString(Const.EMAIL,"");
        String weight = String.valueOf(preference.getFloat(Const.WEIGHT,0));
        LogUtils.i(getClass(),"weight="+weight);
        tvName.setText(name);
        tvEmail.setText(email);
        tvWeight.setText(weight);
        String photoName = preference.getString(Const.PHOTONAME,"");
        File file = null;
        if(!photoName.equals("")){
            file = new File(String.format("%s%s%s.tmp",getExternalCacheDir().getPath(),File.separator,photoName));
        }
        Picasso.with(this)
                .load(file)
                .placeholder(R.drawable.img_circle_placeholder)
                .error(R.mipmap.ic_launcher)
                .resize(headSize,headSize)
                .centerCrop()
                .transform(new CircleTransformation())
                .into(mImageView);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_LOAD_IMAGE:
                    startZoomPhoto(data.getData());
                    break;
                case REQUEST_CROP_IMAGE:
                    Bundle extras = data.getExtras();
                    if(extras != null){
                        try {
                            uploadPhoto(extras);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
    }

    private void uploadPhoto(Bundle extras) throws IOException {
        photoName = String.valueOf(System.currentTimeMillis());
        SharedPreferences preferences = getSharedPreferences("UserData",0);
        token = preferences.getString(Const.TOKEN,"");
        LogUtils.i(UserProfileActivity.class,"token:"+token);
        Bitmap bitmap = extras.getParcelable("data");
//        mImageView.setImageBitmap(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        file = new File(String.format("%s%s%s.tmp",getExternalCacheDir().getPath(),File.separator,photoName));
        if(bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        }
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        FileOutputStream fos = new FileOutputStream(file);
        int count = 0;
        byte[] buffer = new byte[8192];
        while((count = is.read(buffer,0,8192))!= -1){
            fos.write(buffer,0,count);
        }
        fos.close();
        is.close();
//            LogUtils.i(getClass(),"is:"+baos.toByteArray().toString());
        uploadPhotoFile(file,photoName);
    }

    private void uploadPhotoFile(final File file, final String photoName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                LogUtils.i(UserProfileActivity.class,"upLoadPhotoFile inputStream ="+is+"photoName"+photoName);
                PutObjectResponse response = BOSUtils.getInstance().putObject(BOSUtils.BUCKET,photoName,file);
//                LogUtils.i(UserProfileActivity.class,"upLoadPhotoToBos response"+response.getETag());
                Message msg = Message.obtain();
                msg.what = Const.MSG_UPLOAD_PIC;
                mHandler.sendMessage(msg);
            }
        }).start();
    }

    private void uploadPhotoKey(String token, final String photoName) {
        RunAPIService.getInstance().updatePhotoInfo(photoName,token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CommonResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(CommonResult commonResult) {
                        switch(commonResult.getResultCode()){
                            case 0:
                                LogUtils.i(UserProfileActivity.class,"uploadPhotoKey success~"+photoName);
                                SharedPreferences preferences = getSharedPreferences("UserData",MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("photoName",photoName);
                                editor.commit();
                                setUpPhoto(photoName);
                                break;
                        }
                    }
                });
    }

    private void setUpPhoto(String photoKey) {
        LogUtils.i(getClass(),"setupPhoto~"+getExternalCacheDir().getPath());
        downloadPhotoFile(photoKey);
    }

    private void downloadPhotoFile(final String photoKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.i(getClass(),"downloading..");
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




    private void startZoomPhoto(Uri uri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop","true");
        intent.putExtra("aspectX",1);
        intent.putExtra("aspectY",1);
        intent.putExtra("outputX",300);
        intent.putExtra("outputY",300);
        intent.putExtra("return-data",true);
        startActivityForResult(intent,REQUEST_CROP_IMAGE);
    }

    private MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private WeakReference<UserProfileActivity> mActivity;

        public MyHandler(UserProfileActivity activity){
            mActivity = new WeakReference<UserProfileActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            UserProfileActivity activity = mActivity.get();
            if(activity != null){
                switch (msg.what){
                    case Const.MSG_DOWNLOAD_PIC:
                        File file = (File) msg.obj;
                        LogUtils.i(getClass(),"imageView loading bitmap..");
                        Picasso.with(activity)
                                .load(file)
                                .placeholder(R.drawable.img_circle_placeholder)
                                .error(R.mipmap.ic_launcher)
                                .resize(activity.headSize,activity.headSize)
                                .centerCrop()
                                .transform(new CircleTransformation())
                                .into(activity.mImageView);
                        break;
                    case Const.MSG_UPLOAD_PIC:
                        LogUtils.i(getClass(),"const upload pic");
                        activity.uploadPhotoKey(activity.token,activity.photoName);
                        activity.setResult(RESULT_OK);
                        break;
                }
            }
        }
    }
}
