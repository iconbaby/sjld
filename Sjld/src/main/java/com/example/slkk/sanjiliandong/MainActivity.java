package com.example.slkk.sanjiliandong;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = "MainActivity";
    private ListView mProvince;
    private ListView mCity;
    private ListView mDistract;
    private Button mBtn_select;
    private List<InfoBean> mProvinceList;
    private List<InfoBean> mCityList;
    private List<InfoBean> mDistractList;
    private File mDir;
    private File mFile;
    private MyAdapter mMyProvinceAdapter;
    private MyAdapter mMyCityAdapter;
    private MyAdapter mMyDistrictAdapter;
    private PopupWindow mPw;
    private int mPriviousPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initDb();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initView();
        ;
        initPopupWindow();
        initProvinceListView();
        initListViewlistener();
    }

    private void initListViewlistener() {
        mProvince.setOnItemClickListener(this);
        mCity.setOnItemClickListener(this);
        mDistract.setOnItemClickListener(this);
    }

    private void initView() {
        mBtn_select = (Button) findViewById(R.id.select);
        mBtn_select.setOnClickListener(this);
    }

    private void initProvinceListView() {
        loadProvinceData();
        if (mMyProvinceAdapter == null) {
            mMyProvinceAdapter = new MyAdapter(this, mProvinceList);
            mProvince.setAdapter(mMyProvinceAdapter);
        } else {
            mMyProvinceAdapter.notifyDataSetChanged();
        }
    }

    private void initPopupWindow() {
        View view = View.inflate(this, R.layout.view_popup, null);
        mPw = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //解决popupwindow焦点占用问题
        mPw.setBackgroundDrawable(new BitmapDrawable());
        mProvince = (ListView) view.findViewById(R.id.province);
        mCity = (ListView) view.findViewById(R.id.city);
        mDistract = (ListView) view.findViewById(R.id.distract);
    }

    private void initDb() throws IOException {
        InputStream is = getResources().openRawResource(R.raw.city);
        mDir = new File("/data/data/" + getPackageName() + "/databases");
        mDir.mkdirs();
        mFile = new File(mDir, "city3.db");

        FileOutputStream fos = null;
        if (!mFile.exists()) {
            try {
                fos = new FileOutputStream(mFile);
                byte[] buffer = new byte[8192];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                is.close();
                fos.close();
            }
        }
        mProvinceList = new ArrayList<>();
        mCityList = new ArrayList<>();
        mDistractList = new ArrayList<>();
    }

    private void loadProvinceData() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(mDir + "/city3.db", null, MODE_PRIVATE);
        Log.d(TAG, "loadProvinceData: " + db.getPath());
        Cursor cursor = db.query("province", new String[]{"id", "code", "name"}, null, null, null, null, null);
        InfoBean info = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                info = new InfoBean();
                int id = cursor.getInt(0);
                int code = cursor.getInt(1);
                String name = null;
                try {
                    name = new String(cursor.getBlob(2), "gbk");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                info.setId(id);
                info.setCode(code);
                info.setName(name);
                mProvinceList.add(info);
            }
        }
        cursor.close();
        db.close();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.province:
                if (mPriviousPosition != -1) {
                    mProvinceList.get(mPriviousPosition).isChecked = false;
                }
                mPriviousPosition = position;
                //Log.d(TAG, "onItemClick: "+position);
                mProvinceList.get(position).setChecked(true);
                //Log.d(TAG, "onItemClick: "+ mProvinceList.get(position).isChecked);
                showCityListView(position);
                mMyProvinceAdapter.notifyDataSetChanged();
                break;
            case R.id.city:
                mCityList.get(position).setChecked(true);
                showDistractListView(position);
                mMyCityAdapter.notifyDataSetChanged();

                break;
            case R.id.distract:
                break;
        }
    }

    private void showDistractListView(int position) {
        loadDistractData(position);
        if (mMyDistrictAdapter == null) {
            mMyDistrictAdapter = new MyAdapter(this, mDistractList);
            mDistract.setAdapter(mMyDistrictAdapter);
        } else {
            mMyDistrictAdapter.notifyDataSetChanged();
        }
    }

    private void loadDistractData(int position) {
        mDistractList.clear();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(mDir + "/city3.db", null, MODE_PRIVATE);
        Cursor cursor = db.query("district", new String[]{"name"}, " pcode = ? ",
                new String[]{String.valueOf(mCityList.get(position).getCode())}, null, null, null);
        Log.d(TAG, "loadDistractData: " + cursor.getCount());
        InfoBean info = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    String name = new String(cursor.getBlob(0), "gbk");
                    info = new InfoBean();
                    info.setName(name);
                    Log.d(TAG, "loadDistractData: " + info.getName());
                    mDistractList.add(info);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        cursor.close();
        db.close();

    }

    private void showCityListView(int position) {
        loadCityData(position);
        if (mMyCityAdapter == null) {

            mMyCityAdapter = new MyAdapter(this, mCityList);
            mCity.setAdapter(mMyCityAdapter);
        } else {
            mMyCityAdapter.notifyDataSetChanged();
        }
    }

    private void loadCityData(int position) {
        mCityList.clear();
        SQLiteDatabase db = SQLiteDatabase.openDatabase(mDir + "/city3.db", null, MODE_PRIVATE);
        Cursor cursor = db.query("city", new String[]{"name", "id", "code"}, " pcode = ?",
                new String[]{String.valueOf(mProvinceList.get(position).getCode())}, null, null, null);
        InfoBean info = null;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = null;
                int code = 0;
                int id = 0;
                try {
                    id = cursor.getInt(1);
                    code = cursor.getInt(2);
                    name = new String(cursor.getBlob(0), "gbk");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                info = new InfoBean();
                info.setName(name);
                info.setId(id);
                info.setCode(code);
                Log.d(TAG, "loadCityData: " + info.getName());
                mCityList.add(info);
            }
        }


    }

    @Override
    public void onClick(View v) {
        showPopWindow();
    }

    private void showPopWindow() {
        mPw.showAsDropDown(mBtn_select);
    }
}
