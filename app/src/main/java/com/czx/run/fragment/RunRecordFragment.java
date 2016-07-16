package com.czx.run.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.czx.run.R;
import com.czx.run.model.RecordResult;
import com.czx.run.model.RunRecord;
import com.czx.run.adapter.RunRecordAdapter;
import com.czx.run.activity.ShowDetailActivity;
import com.czx.run.model.RunRecordItem;
import com.czx.run.utils.LogUtils;
import com.czx.run.utils.RunAPIService;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by czx on 2016/7/3.
 */
public class RunRecordFragment extends Fragment {

    public static final String TOKEN = "token";
    private String token;
    private List<RunRecordItem> records = new ArrayList<>();
    private RunRecordAdapter adapter;

    public RunRecordFragment(){}

//    public RunRecordFragment newInstance(String token){
//        RunRecordFragment fragment = new RunRecordFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString(TOKEN,token);
//        fragment.setArguments(bundle);
//        return fragment;
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        token = getArguments().getString(TOKEN);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_run_record,container,false);
        Log.i(""+this.getClass(),"onCreateView~");
        initView(view);
//        initData();
        initData();
        return view;
    }

    private void initData() {
        SharedPreferences sp = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String token = sp.getString("token","");
        Log.i(""+this.getClass(),"token="+token);
        if(!token.equals("")){
            RunAPIService.getInstance().findAll(token)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<RecordResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.i(""+this.getClass(),"获取记录失败~"+e.getCause());
                        }

                        @Override
                        public void onNext(RecordResult recordResult) {
                            if(recordResult.getResultCode() == 0){
                                Log.i(""+this.getClass(),"获取记录成功~");
                                reBuildList(recordResult.getRecords());
                                LogUtils.i(getClass(),recordResult.getRecords().size()+"");
//                                adapter.updateMyList(recordResult.getRecords());
                                adapter.updateMyList(reBuildList(recordResult.getRecords()));
                                Log.i(""+this.getClass(),recordResult.getRecords().get(0).getDistance()+"");
                            }
                        }
                    });
        }
    }

    private List<RunRecordItem> reBuildList(List<RunRecord> records) {
        List<RunRecordItem> itemList = new ArrayList<>();
        int month = 0 ,temp;
        for(int i = 0;i<records.size();i++){
            temp = records.get(i).getDate().getMonth();
            if(month != temp){
                month = temp;
                Log.i(""+this.getClass(),"temp="+temp);
                itemList.add(new RunRecordItem(records.get(i),RunRecordItem.TYPE_MONTH));
            }
            itemList.add(new RunRecordItem(records.get(i),RunRecordItem.TYPE_NARMAL));
        }
        LogUtils.i(getClass(),"itemlist size"+itemList.size());
        return itemList;
    }

    private void initView(View view) {
        RecyclerView recyclerview = (RecyclerView) view.findViewById(R.id.recyclerview_record);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RunRecordAdapter(records);
        recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener(new RunRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RunRecord runRecord) {
//                Toast.makeText(getContext(),runRecord.getEmail(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), ShowDetailActivity.class);
                intent.putExtra("runRecord",runRecord);
                Log.i(""+getClass(),"getCalorie"+runRecord.getCalorie()+"time"+runRecord.getRunTime()+"");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
