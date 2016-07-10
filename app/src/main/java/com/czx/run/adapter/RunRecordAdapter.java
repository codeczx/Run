package com.czx.run.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.czx.run.R;
import com.czx.run.model.RunRecord;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by czx on 2016/3/30.
 */
public class RunRecordAdapter extends RecyclerView.Adapter<RunRecordAdapter.MyViewHolder> implements
        View.OnClickListener{


    private OnItemClickListener mListener;
    private List<RunRecord> mList;
//    private Context mContext;

    public RunRecordAdapter(List<RunRecord> list){
        mList = list;
//        mContext = context;
    }

    public void updateMyList(List<RunRecord> records){
        mList = records;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        view.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        DecimalFormat df = new DecimalFormat("#0.00");
        String distance = df.format(mList.get(position).getDistance()/1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        float time = mList.get(position).getRunTime()*1000-8*1000*3600;
        String runTime = sdf.format(time);
        df = new DecimalFormat("#0.0");
        String calorie = df.format(mList.get(position).getCalorie());

        holder.distance.setText(distance);
        holder.runTime.setText(runTime);
        holder.calorie.setText(calorie);
        Timestamp timestamp = mList.get(position).getDate();
        sdf = new SimpleDateFormat("MM月dd日");
        holder.date.setText(sdf.format(timestamp));
        holder.itemView.setTag(mList.get(position));
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
        return super.getItemViewType(position);
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

        TextView distance,runTime,calorie,date;

        public MonthHolder(View itemView) {
            super(itemView);
            distance = (TextView) itemView.findViewById(R.id.tv_distance);
            runTime = (TextView) itemView.findViewById(R.id.tv_runtime);
            calorie = (TextView) itemView.findViewById(R.id.tv_calorie);
            date = (TextView) itemView.findViewById(R.id.tv_date);
        }
    }
}
