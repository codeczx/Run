package com.czx.run.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.czx.run.R;
import com.czx.run.model.GroupRecord;


import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by czx on 2016/7/5.
 */
public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private List<GroupRecord> recordList;
    private Context mContext;
    private OnItemClickListener mListener;

    public GroupAdapter(List<GroupRecord>list, Context context){
        recordList = list;
        mContext = context;
    }

    @Override
    public void onClick(View v) {
        if(v != null){
            mListener.onItemClick(v, (GroupRecord) v.getTag());
        }
    }

    public interface OnItemClickListener{
        void onItemClick(View view, GroupRecord groupRecord);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mListener = onItemClickListener;
    }

    public void updateData(List<GroupRecord>list){
        recordList = list;
        notifyDataSetChanged();
    }

    public void addData(List<GroupRecord>list){
        recordList.addAll(recordList.size(),list);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_runcircle,parent,false);
        holder = new ItemViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DecimalFormat df = new DecimalFormat("#0.00");
        String distance = df.format(recordList.get(position).getDistance()/1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        float runTime = recordList.get(position).getRunTime()*1000-8*1000*3600;
        String time = sdf.format(runTime);
        df = new DecimalFormat("#0.0");
        String calorie = df.format(recordList.get(position).getCalorie());
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sdf.format(recordList.get(position).getDate());
        ItemViewHolder mHolder = (ItemViewHolder)holder;
        mHolder.name.setText(recordList.get(position).getName());
        mHolder.address.setText(recordList.get(position).getAddress());
        mHolder.distance.setText(distance);
        mHolder.time.setText(time);
        mHolder.calorie.setText(calorie);
        mHolder.date.setText(date);
        holder.itemView.setTag(recordList.get(position));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        public TextView name,address,distance,time,calorie,date;

        public ItemViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_card_name);
            address= (TextView) itemView.findViewById(R.id.tv_card_address);
            distance = (TextView) itemView.findViewById(R.id.tv_card_distance);
            time = (TextView) itemView.findViewById(R.id.tv_card_time);
            calorie = (TextView) itemView.findViewById(R.id.tv_card_calorie);
            date = (TextView) itemView.findViewById(R.id.tv_card_date);
        }
    }

}
