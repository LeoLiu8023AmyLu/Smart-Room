package com.dreammaker.smartroom;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmartRoomDB extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "LEO.db"; // ���ݿ�����
	private final static int DATABASE_VERSION = 1; // ���ݿ�汾
	private final static String TABLE_NAME = "room_table"; // ��ǩ���ƣ�
	public final static String ROOM_ID = "room_id"; // ���ID
	public final static String ROOM_TIME = "room_time"; // ʱ��
	public final static String ROOM_T = "room_t"; // �¶�
	public final static String ROOM_H = "room_h"; // ʪ��
	public final static String ROOM_PM = "room_pm"; // PM2.5
	public final static String ROOM_L = "room_l"; // ��ǿ
	public final static String ROOM_SMO = "room_smo"; // ����
	public final static String ROOM_Vibration = "room_vibration"; // ����
	public final static String ROOM_R = "room_r"; // �ȸ�Ӧ

	public SmartRoomDB(Context context) {
		// TODO Auto-generated constructor stub
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// ����table
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + ROOM_ID
				+ " INTEGER primary key autoincrement, " + ROOM_TIME
				+ " text, " + ROOM_T + " text, " + ROOM_H + " text, " + ROOM_PM
				+ " text, " + ROOM_L + " text, " + ROOM_Vibration + " text, "+ ROOM_SMO + " text, "
				+ ROOM_R + " text);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}

	public Cursor select() // ����ʲô��
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}

	// ���Ӳ���
	public long insert(String roomtime, String roomt, String roomh,
			String roompm, String rooml, String roomsmo, String roomvib ,String roomr) {
		SQLiteDatabase db = this.getWritableDatabase();
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(ROOM_TIME, roomtime);
		cv.put(ROOM_T, roomt);
		cv.put(ROOM_H, roomh);
		cv.put(ROOM_PM, roompm);
		cv.put(ROOM_L, rooml);
		cv.put(ROOM_SMO, roomsmo);
		cv.put(ROOM_Vibration, roomvib);
		cv.put(ROOM_R, roomr);
		long row = db.insert(TABLE_NAME, null, cv);
		return row;
	}

	// ɾ������

	public void delete(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ROOM_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
	}

	// �޸Ĳ���

	public void update(int id, String roomtime, String roomt, String roomh,
			String roompm, String rooml, String roomsmo, String roomvib , String roomr) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = ROOM_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };
		ContentValues cv = new ContentValues();
		cv.put(ROOM_TIME, roomtime);
		cv.put(ROOM_T, roomt);
		cv.put(ROOM_H, roomh);
		cv.put(ROOM_PM, roompm);
		cv.put(ROOM_L, rooml);
		cv.put(ROOM_SMO, roomsmo);
		cv.put(ROOM_Vibration, roomvib);
		cv.put(ROOM_R, roomr);
		db.update(TABLE_NAME, cv, where, whereValue);
	}

}
