package com.example.simpletimecard;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class actTimeCardActivity extends AppCompatActivity {
    private String RequestDate; /* リクエスト日付 */
    private String TargetDate;  /* 表示対象日付   */
    DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /***************************/
        /* DB初期設定               */
        /***************************/
        /* DBHelperの取得 */
        this.helper = new DatabaseHelper(this);
        /* DBファイル未存在時の処理実行 */
        try {
            this.helper.createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_main);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        this.TargetDate = null;       /* 表示対象日を初期化 */

        /* インテントを取得 */
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            if (intent.getExtras().containsKey("TARGET_DAY")) {
                this.TargetDate = intent.getStringExtra("TARGET_DAY");
            }
        }


        /*****************************/
        /* 基本設定情報を取得          */
        /*****************************/
        getConfigParam();
        String cprmStartPageKbn = "1";          /* 起動画面区分 */
        String cprmWorkingPageKbn = "0";        /* 出勤中表示区分 */
        String cprmSelectDayKbn =   "1";        /* 初期表示日区分 */
        int cprmDefaultBreakMinutes = 60;       /* 基本休憩時間 */
        int cprmClosingDate = 1;

        /* 表示日付が指定されていない場合 */
        if(this.TargetDate == null){

            /* 当日を表示の場合 */
            if(cprmSelectDayKbn.equals("1")){
                /* 当日の日付を取得して設定 */
                //"yyyy/MM/dd HH:mm:ss.SSS E"
                this.TargetDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
            /* 翌日を表示の場合 */
            }else if(cprmSelectDayKbn.equals("2")){
                /* 翌日の日付を取得して設定 */
                this.TargetDate = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now().plusDays(1));
            }

            /* 出勤中があれば表示の場合 */
            if(cprmWorkingPageKbn.equals("1")){
                //ここであれば日付を上書く */
            }
        }

        /* 表示対象日付をセット */
        ((TextView) findViewById(R.id.txt_date)).setText(this.TargetDate);


        /* 既に登録された出退社のデータが存在する場合は画面表示 */
        getTimeCardData(this.TargetDate);


    }


    /* 設定情報の取得 */
    private void getConfigParam(){

    }



    public void selector_button_Click(View view) {
    }

    /* 翌日ボタンクリック時 */
    public void NextDate_Click(View view) {
        /* 1日後の日付をセット */
        String IntentParam = addStringDate(this.TargetDate , 1);

        /* 画面遷移 */
        Intent intent = new Intent(this, actTimeCardActivity.class);
        intent.putExtra("TARGET_DAY",IntentParam);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        //overridePendingTransition(android.R.anim.slide_out_right,android.R.anim.slide_in_left);
        this.finish();
    }

    /* 前日ボタンクリック時 */
    public void BeforeDate_Click(View view) {
        /* 1日前の日付をセット */
        String IntentParam = addStringDate(this.TargetDate , -1);

        /* 画面遷移 */
        Intent intent = new Intent(this, actTimeCardActivity.class);
        intent.putExtra("TARGET_DAY",IntentParam);
        startActivity(intent);
        //overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        overridePendingTransition(android.R.anim.slide_out_right,android.R.anim.slide_in_left);
        this.finish();
    }

    public void SaveMemo_Click(View view) {



    }


    /* 文字列型日付けの加減算を行う */
    private String addStringDate(String prmTargetDate , int prmDays){
        String retVal = "";

        /* 次の日付を算出 */
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            /* 現在ページの日付を1日加算 */
            Date date = sdf.parse(prmTargetDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DAY_OF_MONTH , prmDays);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            retVal = dateFormat.format(calendar.getTime());


        } catch(ParseException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**********************************************/
    /* タイムカードテーブルからデータを取得して画面に表示する */
    /*********************************************/
    private void getTimeCardData(String prmTargetDate){
        SQLiteDatabase db = null;
        Cursor cursor = null;

        /* 対象画面項目初期化 */
        ((TextView) findViewById(R.id.txt_IN)).setText("出勤：");
        ((TextView) findViewById(R.id.txt_OUT)).setText("退勤：");
        ((TextView) findViewById(R.id.txt_MEMO)).setText("");

        /* 取得用SQL生成 */
        String sql = "SELECT * FROM TIMECARD \n";
        sql = sql +  "    WHERE TargetDate = '" + prmTargetDate + "'\n";

        /* SQL実行 */
        try {
            db = helper.getReadableDatabase();
            cursor = db.rawQuery(sql, null);

            /* 対象データが存在した場合 */
            if(cursor.getCount() != 0){
                cursor.moveToNext();
                //cursor.getString(cursor.getColumnIndex("OfficeTime"));
                //cursor.getString(cursor.getColumnIndex("LeaveTime"));
                //cursor.getString(cursor.getColumnIndex("Memo"));
                ((TextView) findViewById(R.id.txt_IN)).setText(cursor.getString(cursor.getColumnIndex("OfficeTime")));
                ((TextView) findViewById(R.id.txt_OUT)).setText(cursor.getString(cursor.getColumnIndex("LeaveTime")));
                ((TextView) findViewById(R.id.txt_MEMO)).setText(cursor.getString(cursor.getColumnIndex("Memo")));

            }

        }catch(Exception e){
            throw e;
        } finally {
            if(db != null && db.isOpen() == true){
                db.close();
            }
        }
    }


    private String getDayOfWeek(String prmDay){
        return "";
    }


}
