package com.czx.run.view;

import android.app.Dialog;
import android.content.Context;
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

import com.czx.run.R;
import com.czx.run.utils.RunAPIService;
import com.czx.run.model.CommonResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by czx on 2016/7/2.
 */
public class RegisterDialog extends Dialog {
    private Context mContext;
    private RegisterDialogEventListener mRegisterListener;
    private TextInputLayout emailInputLayout,passwordInputLayout,usernameInputLayout;
    private EditText emailEditText,passwordEditText,usernameEditText;
    private Button registerBtn,cancelBtn;
    private ProgressBar mProgressBar;


    public interface RegisterDialogEventListener{
        public void registerSuccess();
    }

    protected RegisterDialog(Context context,RegisterDialogEventListener RegisterListener) {
        super(context, R.style.Dialog);
        mContext = context;
        mRegisterListener = RegisterListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_register,null);
        setContentView(layout);
        setUpDialog();
        initView();
    }

    private void initView() {
        emailInputLayout = (TextInputLayout) findViewById(R.id.til_email);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.til_password);
        usernameInputLayout = (TextInputLayout) findViewById(R.id.til_username);

        emailInputLayout.setErrorEnabled(true);
        passwordInputLayout.setErrorEnabled(true);
        usernameInputLayout.setErrorEnabled(true);

        emailEditText = emailInputLayout.getEditText();
        passwordEditText = passwordInputLayout.getEditText();
        usernameEditText = usernameInputLayout.getEditText();

        registerBtn = (Button) findViewById(R.id.btn_register);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_register);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerBtn.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);

                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                Log.i("tag",email+password+username);

                if(!isEmailLeagl(email)){
                    emailInputLayout.setError(mContext.getString(R.string.email_type_error));
                    registerBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }else if(!isPasswordLegal(password)){
                    passwordInputLayout.setError(mContext.getString(R.string.password_type_error));
                    registerBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }else if(!isUsernameLegal(username)){
                    usernameInputLayout.setError(mContext.getString(R.string.username_type_error));
                    registerBtn.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }else{
                    emailInputLayout.setErrorEnabled(false);
                    passwordInputLayout.setErrorEnabled(false);
                    usernameInputLayout.setErrorEnabled(false);
                    doRegister(email,password,username);
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void doRegister(String email, String password, String username) {
        Log.i("tag",email+password+username);
        RunAPIService.getInstance().register(email,password,username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CommonResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("tag",e.getMessage());
                    }

                    @Override
                    public void onNext(CommonResult commonResult) {
                        Log.i("tag",commonResult.getMessage());
                        switch(commonResult.getResultCode()){
                            case 0:
                                dismiss();
                                Toast.makeText(mContext,"注册成功~",Toast.LENGTH_SHORT).show();
                                mRegisterListener.registerSuccess();
                        }

                    }
                });
    }

    private void setUpDialog() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        lp.width = (int) (point.x * 0.8);
        lp.height = (int) (point.y * 0.7);
        dialogWindow.setAttributes(lp);
    }

    private boolean isEmailLeagl(String email){
        String pattern = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(email);
        return m.find();
    }

    private boolean isPasswordLegal(String password){
        String pattern = "^[a-zA-Z]\\w{5,20}$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(password);
        return m.find();
    }

    private boolean isUsernameLegal(String username){
        String pattern = "^[\\u4e00-\\u9fa5]{1,7}$|^[\\dA-Za-z_]{1,14}$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(username);
        return m.find();
    }
}
