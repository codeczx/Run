package com.czx.run.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.czx.run.R;
import com.czx.run.model.RunRecord;
import com.czx.run.model.RunRecordItem;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 运动记录RecyclerView的adapter
 * Created by czx on 2016/3/30.
 */
public class RunRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        View.OnClickListener{

    private OnItemClickListener mListener;
    private List<RunRecordItem> mList;
//    private Context mContext;

    public RunRecordAdapter(List<RunRecordItem> list){
        mList = list;
//        mContext = context;
    }

    public void updateMyList(List<RunRecordItem> records){
        mList = records;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder holder;
        if(viewType == RunRecordItem.TYPE_NARMAL){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
            holder = new MyViewHolder(view);
            view.setOnClickListener(this);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item_month,parent,false);
            holder = new MonthHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MyViewHolder){
            MyViewHolder mHolder = (MyViewHolder)holder;
            DecimalFormat df = new DecimalFormat("#0.00");
            String distance = df.format(mList.get(position).getRunRecord().getDistance()/1000);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            float time = mList.get(position).getRunRecord().getRunTime()*1000-8*1000*3600;
            String runTime = sdf.format(time);
            df = new DecimalFormat("#0.0");
            String calorie = df.format(mList.get(position).getRunRecord().getCalorie());

            mHolder.distance.setText(distance);
            mHolder.runTime.setText(runTime);
            mHolder.calorie.setText(calorie);
            Timestamp timestamp = mList.get(position).getRunRecord().getDate();
            sdf = new SimpleDateFormat("MM月dd日");
            mHolder.date.setText(sdf.format(timestamp));
            holder.itemView.setTag(mList.get(position).getRunRecord());
        }else if(holder instanceof MonthHolder){
            ((MonthHolder) holder).tvMonth.setText(mList.get(position).getRunRecord().getDate().getMonth()+1+"月");
        }
    }



    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public void onClick(View v) {
        if(mListener != null){
            mListener.onItemClick(v, (RunRecord) v.getTag());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getType();
    }

    public interface OnItemClickListener{
        void onItemClick(View view, RunRecord runRecord);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mListener = onItemClickListener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView distance,runTime,calorie,date;

        public MyViewHolder(View itemView) {
            super(itemView);
            distance = (TextView) itemView.findViewById(R.id.tv_distance);
            runTime = (TextView) itemView.findViewById(R.id.tv_runtime);
            calorie = (TextView) itemView.findViewById(R.id.tv_calorie);
            date = (TextView) itemView.findViewById(R.id.tv_date);
        }
    }

    public static class MonthHolder extends RecyclerView.ViewHolder{

        TextView tvMonth;

        public MonthHolder(View itemView) {
            super(itemView);
            tvMonth = (TextView) itemView.findViewById(R.id.tv_card_month);
        }
    }
}
