package com.czx.run.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.czx.run.activity.ShowDetailActivity;
import com.czx.run.adapter.GroupAdapter;
import com.czx.run.R;
import com.czx.run.adapter.RunRecordAdapter;
import com.czx.run.model.GroupRecord;
import com.czx.run.model.GroupRecordResult;
import com.czx.run.model.MaxIdResult;
import com.czx.run.model.RunRecord;
import com.czx.run.utils.Const;
import com.czx.run.utils.LogUtils;
import com.czx.run.utils.RunAPIService;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class GroupFragment extends Fragment {

//    private SwipeRefreshLayout mSwipeRefreshLayout;
    private XRecyclerView mXRecyclerView;
    private GroupAdapter mGroupAdapter;
    private List<GroupRecord> mList;
    private String token;
    private int currentId;

    public GroupFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        token = preferences.getString(Const.TOKEN,"");
        mList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        initView(view);
        refreshData();
        return view;
    }

    private void initView(View view) {
//        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.srLayout_group);
        mXRecyclerView = (XRecyclerView) view.findViewById(R.id.recyclerView_group);
//        mXRecyclerView.setPullRefreshEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mXRecyclerView.setLayoutManager(linearLayoutManager);
        mGroupAdapter = new GroupAdapter(mList,getActivity());
        mXRecyclerView.setAdapter(mGroupAdapter);
        mXRecyclerView.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }

            @Override
            public void onLoadMore() {
                loadMoreData();
            }
        });
        mGroupAdapter.setOnItemClickListener(new GroupAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, GroupRecord groupRecord) {
//                Toast.makeText(getContext(),runRecord.getEmail(),Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), ShowDetailActivity.class);
                RunRecord runRecord = new RunRecord();
                runRecord.setDistance(groupRecord.getDistance());
                runRecord.setRunTime(groupRecord.getRunTime());
                runRecord.setCalorie(groupRecord.getCalorie());
                runRecord.setDate(groupRecord.getDate());
                runRecord.setPointsKey(groupRecord.getPointsKey());
                runRecord.setAddress(groupRecord.getAddress());
                intent.putExtra("runRecord",runRecord);
                startActivity(intent);
            }
        });

//        mSwipeRefreshLayout.setColorSchemeResources(
//                android.R.color.holo_blue_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light,
//                android.R.color.holo_green_light);
//
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                initData();
//            }
//        });
    }

    private void refreshData() {
        RunAPIService.getInstance().getMaxId()
                .flatMap(new Func1<MaxIdResult, Observable<GroupRecordResult>>() {
                    @Override
                    public Observable<GroupRecordResult> call(MaxIdResult maxIdResult) {
                        if (maxIdResult.getResultCode() == 0){
                            LogUtils.i(getClass(),"get max id success~");
                        }
                        return RunAPIService.getInstance().getRecordsById(maxIdResult.getId(),token);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GroupRecordResult>() {
                    @Override
                    public void call(GroupRecordResult groupRecordResult) {
                        switch (groupRecordResult.getResultCode()){
                            case 0:
                                LogUtils.i(getClass(),"get circle record success~"+"id"+currentId);
//                                mSwipeRefreshLayout.setRefreshing(false);
                                mXRecyclerView.refreshComplete();
                                mXRecyclerView.setLoadingMoreEnabled(true);
                                mGroupAdapter.updateData(groupRecordResult.getRecords());
                                currentId = groupRecordResult.getRecords().get(9).getId()-1;

                        }
                    }
                });
    }

    private void loadMoreData(){
        RunAPIService.getInstance().getRecordsById(currentId,token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GroupRecordResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(GroupRecordResult groupRecordResult) {
                        switch (groupRecordResult.getResultCode()){
                            case 0:
                                mXRecyclerView.loadMoreComplete();
                                mGroupAdapter.addData(groupRecordResult.getRecords());
                                int size = groupRecordResult.getRecords().size();
                                currentId = groupRecordResult.getRecords().get(size-1).getId()-1;
                                LogUtils.i(getClass(),"on LoadMore success~ and id is"+currentId);
                                if(size < 10){
                                    mXRecyclerView.setLoadingMoreEnabled(false);
                                }
                                break;
                        }
                    }
                });
    }
}
