package com.czx.run.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czx.run.R;
import com.czx.run.activity.RunActivity;
import com.czx.run.utils.RunAPIService;
import com.czx.run.model.SumResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class HomeFragment extends Fragment {

    private TextView distance,runTime,speed,calorie;
    private String token;
    private LinearLayout runLayout;
    public static final String TOKEN = "token";
    public HomeFragment() {
    }

    public static HomeFragment newInstance(String token) {
        HomeFragment homeFragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString("token",token);
        homeFragment.setArguments(args);
        return homeFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initView(view);
        initData();
        return view;
    }

    private void initData() {
        token = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE).getString("token","");
        if(!token.equals("")){
            RunAPIService.getInstance().getSumData(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<SumResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(SumResult sumResult) {
                            switch (sumResult.getResultCode()) {
                                case 0:
                                    Log.i("tag", "成功获取总数据~");
                                    setRunDataUI(sumResult);
                            }
                        }
                    });
        }
    }

    private void initView(View view) {
        distance = (TextView) view.findViewById(R.id.tv_main_distance);
        runTime = (TextView) view.findViewById(R.id.tv_main_runtime);
        speed = (TextView) view.findViewById(R.id.tv_main_time);
        calorie = (TextView) view.findViewById(R.id.tv_main_calorie);
        runLayout = (LinearLayout) view.findViewById(R.id.layout_run);

        runLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RunActivity.class);
                intent.putExtra(TOKEN,token);
                startActivity(intent);
            }
        });

    }


    public void setRunDataUI(SumResult sum){

        DecimalFormat df = new DecimalFormat("#0.00");
        String distanceStr = df.format(sum.getSumDistance()/1000);

        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        float avgtime = (float) ((1.0/((sum.getSumDistance()/1000)/
                sum.getSumTime()))*1000-8*3600*1000);
        String speedStr = sdf.format(avgtime);

        String calorieStr = String.valueOf((int)(sum.getSumCalorie()/sum.getSumRunTime()));
        String runTimeStr = String.valueOf((int)sum.getSumRunTime());

        distance.setText(distanceStr);
        runTime.setText(runTimeStr);
        speed.setText(speedStr);
        calorie.setText(calorieStr);
    }

}
