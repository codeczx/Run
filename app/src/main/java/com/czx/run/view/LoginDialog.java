package com.czx.run.view;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.czx.run.model.LoginResult;
import com.czx.run.R;
import com.czx.run.utils.LogUtils;
import com.czx.run.utils.RunAPIService;
import com.czx.run.model.User;
import com.czx.run.model.UserResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by czx on 2016/7/1.
 */
public class LoginDialog extends Dialog {

    private Context mContext;

    private LoginDialogEventListener mLoginDialogEventListener;

    private Button loginBtn,registerBtn;
    private EditText emailEditText,passwordEditText;
    private TextInputLayout emailLayout,passwordLayout;
    private ProgressBar mProgressBar;


    public interface LoginDialogEventListener{
        void setUserData();
    }

    public LoginDialog(Context context,LoginDialogEventListener loginDialogEventListener) {
        super(context, R.style.Dialog);
        mContext = context;
        this.mLoginDialogEventListener = loginDialogEventListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_login,null);
        this.setContentView(layout);
        setUpDialog();
        initView();
    }

    private void initView() {
        loginBtn = (Button) findViewById(R.id.btn_login);
        registerBtn = (Button) findViewById(R.id.btn_register);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_login);
        emailLayout = (TextInputLayout) findViewById(R.id.til_email);
        passwordLayout = (TextInputLayout) findViewById(R.id.til_password);

        emailEditText = emailLayout.getEditText();
        passwordEditText = passwordLayout.getEditText();

        emailLayout.setErrorEnabled(true);
        passwordLayout.setErrorEnabled(true);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBtn.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if(!isEmailLegal(email)){
                    emailLayout.setError(mContext.getString(R.string.email_type_error));
                    loginBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    return;
                }else if(!isPasswordLegal(password)){
                    passwordLayout.setError(mContext.getString(R.string.password_type_error));
                    loginBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    return;
                }else{
                    emailLayout.setErrorEnabled(false);
                    passwordLayout.setErrorEnabled(false);
                }
                doLogin(email,password);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterDialog registerDialog = new RegisterDialog(mContext, new RegisterDialog.RegisterDialogEventListener() {
                    @Override
                    public void registerSuccess() {
                        show();
                    }
                });
                registerDialog.show();
            }
        });
    }

    private boolean isEmailLegal(String email) {
        String pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(email);
        return m.find();
    }

    private boolean isPasswordLegal(String password) {
        String pattern = "^[a-zA-Z]\\w{5,20}$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(password);
        return m.find();
    }

    private void doLogin(String email,String password){
        RunAPIService.getInstance().login(email,password).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LoginResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(LoginResult loginResult) {
                        switch(loginResult.getResultCode()){
                            case 0:
                                Toast.makeText(mContext,loginResult.getMessage(),Toast.LENGTH_SHORT).show();
                                getUser(loginResult.getToken());
                                break;
                        }

                    }
                });
    }

    public void getUser(String token) {
        RunAPIService.getInstance().getUser(token).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(UserResult userResult) {
                        Log.i("tag",userResult.getResultCode());
                        if(userResult.getResultCode().equals("0")){
                            dismiss();
                            SharedPreferences sharedPreferences = mContext.getSharedPreferences("UserData",mContext.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username",userResult.getUser().getName());
                            LogUtils.i(getClass(),userResult.getUser().getPhoto());
                            editor.putString("photoName",userResult.getUser().getPhoto());
                            editor.putString("token",userResult.getUser().getToken());
                            editor.putString("email",userResult.getUser().getEmail());
                            editor.putFloat("weight",userResult.getUser().getWeight());
                            editor.commit();
                            mLoginDialogEventListener.setUserData();
                        }

                    }
                });
    }

    private void setUpDialog() {
        setTitle(R.string.login);
        //获取dialog的参数，宽和高
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        //获取屏幕的宽高
        WindowManager m = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = m.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        //设置dialog的宽高
        lp.height = (int) (point.y * 0.5);
        lp.width = (int) (point.x * 0.8);
        dialogWindow.setAttributes(lp);
    }
}
