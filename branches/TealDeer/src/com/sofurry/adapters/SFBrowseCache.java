package com.sofurry.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import com.sofurry.mobileapi.ApiFactory.ContentType;
import com.sofurry.mobileapi.ApiFactory.ViewSource;
import com.sofurry.model.NetworkList;
import com.sofurry.model.Submission;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SFBrowseCache {
	
    private static final String DATABASE_NAME    = "sfbrowsecache";
    private static final int    DATABASE_VERSION = 1;

    private SQLiteDatabase db = null;
	
	public SFBrowseCache(Context context) {
		db = (new SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
			
			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
				db.execSQL("DROP TABLE sfbrowsecache;");
				onCreate(db);
			}
			
			@Override
			public void onCreate(SQLiteDatabase db) {
				db.execSQL(	"CREATE TABLE sfbrowsecache ( "+
							"'id' INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL, "+
							"'ViewSource' INT(1) NOT NULL DEFAULT 0, "+
							"'ContentType' INT(1) NOT NULL DEFAULT 0, "+
							"'Extra' CHAR(64) NOT NULL DEFAULT '', "+
							"'data' BLOB NOT NULL DEFAULT '', "+
							"UNIQUE ('ViewSource', 'ContentType', 'Extra') "+
							");");
			}
		}).getWritableDatabase();
		
		db.execSQL("DELETE FROM 'sfbrowsecache' WHERE id NOT IN (SELECT id FROM 'sfbrowsecache' ORDER BY id DESC LIMIT 50);");
	}

	public ArrayList<Submission> getCache(ViewSource viewSource, ContentType contentType, String extra) {
		ArrayList<Submission> list = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		
		Cursor cursor =   db.rawQuery("SELECT data FROM sfbrowsecache WHERE ViewSource=? AND ContentType=? AND Extra=?",
										new String[] {""+viewSource.value, ""+contentType.value, ""+extra});

		if ((cursor != null) && (cursor.moveToFirst())) {
			try {
				byte[] data = cursor.getBlob(0);
				bis = new ByteArrayInputStream(data);
				is = new ObjectInputStream(bis);
				list = new ArrayList<Submission>();
				Submission s = null;
				do {
					s = (Submission) is.readObject();
					list.add(s);
				} while (s != null);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				if (bis != null)
					bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        cursor.close();
	    }
		
		return list;
	}

	public void putCache(ViewSource viewSource, ContentType contentType, String extra, ArrayList<Submission> list, ArrayList<Submission> cachelist) {
		if (list == null)
			return;
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(bos);
		
			int i = 0;
			
			if (list instanceof NetworkList) {
				Submission s = ((NetworkList<Submission>) list).get(i, false, false);
				while ( s != null) {
					os.writeObject(s);
					i++;
					s = ((NetworkList<Submission>) list).get(i, false, false);
				}
				
			} else {
				while (i < list.size()) {
					os.writeObject(list.get(i));
					i++;
				}
			}
			
			if (cachelist != null)
				if (cachelist instanceof NetworkList) {
					Submission s = ((NetworkList<Submission>) cachelist).get(i, false, false);
					while ( s != null) {
						os.writeObject(s);
						i++;
						s = ((NetworkList<Submission>) cachelist).get(i, false, false);
					}
					
				} else {
					while (i < cachelist.size()) {
						os.writeObject(cachelist.get(i));
						i++;
					}
				}
			
			Cursor cursor = db.rawQuery("SELECT id FROM sfbrowsecache WHERE ViewSource=? AND ContentType=? AND Extra=?",
					new String[] {""+viewSource.value, ""+contentType.value, ""+extra});
			if ((cursor != null) && (cursor.moveToFirst())) {
//				db.rawQuery("DELETE FROM sfbrowsecache WHERE ViewSource=? AND ContentType=? AND Extra=?",
//						new String[] {""+viewSource.value, ""+contentType.value, ""+extra});
				db.delete("sfbrowsecache", "ViewSource=? AND ContentType=? AND Extra=?", 
						new String[] {""+viewSource.value, ""+contentType.value, ""+extra});
			}

			SQLiteStatement insertStmt =   db.compileStatement("INSERT INTO sfbrowsecache (ViewSource, ContentType, Extra, data) VALUES (?,?,?,?)");
		    insertStmt.clearBindings();
		    insertStmt.bindLong(1, viewSource.value);
		    insertStmt.bindLong(2, contentType.value);
		    insertStmt.bindString(3, ""+extra);
		    insertStmt.bindBlob(4, bos.toByteArray());
		    insertStmt.executeInsert();
			
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (db != null)
			db.close();
		
		super.finalize();
	}
	
	
}

