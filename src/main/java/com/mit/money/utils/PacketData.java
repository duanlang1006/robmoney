package com.mit.money.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by langduan on 16/3/29.
 */
public class PacketData {

    private DbHelper helper;

    public PacketData(Context context) {
        helper = new DbHelper(context);
    }

    /**
     * 查找指定的序号是否存在
     *
     * @param no
     * @return
     */
    public boolean find(String no) {
        boolean result = false;

        SQLiteDatabase db = helper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from record where no=?", new String[]{no});
            if (cursor.moveToFirst()) {
                result = true;
            }
            cursor.close();
            db.close();
        }
        return result;
    }

    /**
     * 向数据库中添加一条记录，对这个序号进行锁定
     *
     * @param no
     * @return
     */
    public boolean add(String no, String type, String name, String time, String size) {
        if (find(no)) {
            return false;
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("insert into record (no,type,name,time,size) values (?,?,?,?,?)",
                    new String[]{no, type, name, time, size});
            db.close();
        }

        return find(no);
    }

    /**
     * 删除某条记录，下次不再对其锁定
     *
     * @param no
     */
    public void delete(String no) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if (db.isOpen()) {
            db.execSQL("delete from record where no=?", new String[]{no});
            db.close();
        }
    }

    /**
     * 删除数据库，清空所有记录
     *
     * @param context
     */
    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase("record.db");
    }

    public List<PacketInfo> getAll() {
        List<PacketInfo> recordInfos = new ArrayList<PacketInfo>();
        SQLiteDatabase db = helper.getReadableDatabase();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("SELECT * FROM record", null);
            while (cursor.moveToNext()) {
                PacketInfo recordInfo = new PacketInfo();
                recordInfo.setNo(cursor.getString(cursor.getColumnIndex("no")));
                recordInfo.setType(cursor.getString(cursor.getColumnIndex("type")));
                recordInfo.setName(cursor.getString(cursor.getColumnIndex("name")));
                recordInfo.setTime(cursor.getString(cursor.getColumnIndex("time")));
                recordInfo.setSize(cursor.getString(cursor.getColumnIndex("size")));

                recordInfos.add(recordInfo);
            }
            cursor.close();
            db.close();
        }
        return recordInfos;
    }

}
