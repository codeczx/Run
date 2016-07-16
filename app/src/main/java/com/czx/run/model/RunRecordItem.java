package com.czx.run.model;

/**
 * Created by czx on 2016/7/15.
 */
public class RunRecordItem {

    public static final int TYPE_NARMAL = 0;
    public static final int TYPE_MONTH = 1;

    private int type;
    private RunRecord runRecord;

    public RunRecordItem(RunRecord runRecord,int type){
        this.type = type;
        this.runRecord = runRecord;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RunRecord getRunRecord() {
        return runRecord;
    }

    public void setRunRecord(RunRecord runRecord) {
        this.runRecord = runRecord;
    }
}
