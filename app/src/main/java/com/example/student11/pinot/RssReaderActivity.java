package com.example.student11.pinot;

import android.app.ListActivity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

//ListViewオブジェクトを既に含んだListActivityを使っているので,新たにListViewのインスタンスを生成して、アクティビティにセットする必要がない
//setAdapter() ではなく setListAdapter() を使う
public class RssReaderActivity extends ListActivity {
    private static final String RSS_FEED_URL =  "http://www.rssmix.com/u/7592172/rss.xml"; //http://www.rssmix.com/u/6589813/rss.xml
    private ArrayList<ClipData.Item> mItems;
    private RssListAdapter mAdapter;
    long start;
    long stop;
    long diff = 0;
    int second;
    int comma;
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE3 = LOGDIR + "title_info.txt";
    final String SDFILE4 = LOGDIR + "title_info_new.txt";
    File Title = new File(SDFILE3);
    File Title_w = new File(SDFILE4);
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private int count;			//count=-1ならば既読、０以上なら未読で見たと判断した回数を表示
    private int count_line;
    private String title_line;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList<ClipData.Item>();
        mAdapter = new RssListAdapter(this, mItems);

        // タスクを起動する
        RssParserTask task = new RssParserTask(this, mAdapter);
        task.execute(RSS_FEED_URL);

    }

    // リストの項目を選択した時の処理
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        ClipData.Item item = (ClipData.Item) mItems.get(position);
        Intent intent = new Intent(this, ItemDetailActivity.class);

        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("LINK", item.getLink());

        startActivity(intent);

        if(ItemDetailActivity.start == 0){
            start = RssParserTask.start;
        }else{
            start = ItemDetailActivity.start;
        }
        stop = System.currentTimeMillis();
        diff = diff + (stop - start);
        second = (int) (diff / 1000);
        comma = (int) (diff % 1000);
        Log.e("一時停止", second + "." + comma);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){				// 戻るボタンが押された！
            if(ItemDetailActivity.start == 0){
                start = RssParserTask.start;
            }else{
                start = ItemDetailActivity.start;
            }
            stop = System.currentTimeMillis();
            diff = diff + (stop - start);
            int x = (int) (diff/1570);
            Log.i("a",""+x);
            second = (int) (diff/1000);
            comma = (int) (diff % 1000);
            Log.e("終了",second+"."+comma);
            start = stop = RssParserTask.start = ItemDetailActivity.start = 0;

            try {
                Title.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedReader br = new BufferedReader(new FileReader(Title));
                try {
                    BufferedWriter pw = new BufferedWriter(new FileWriter(Title_w,true));
                    PINOT_FILTER P = new PINOT_FILTER();
                    try {
                        while((line = br.readLine()) != null){
                            StringTokenizer tok = new StringTokenizer(line,"\t\t");
                            title_line = tok.nextToken();
                            count_line = Integer.parseInt(tok.nextToken());
                            if(count_line != -1){				//未読見出し文
                                if(x>0){
                                    count_line++;
                                    pw.write(title_line+"\t\t"+count_line);
                                    pw.newLine();
                                    x--;
                                }else{
                                    pw.write(title_line+"\t\t"+count_line);
                                    pw.newLine();
                                }
                                if(count_line >= 2){
                                    P.Pinot_Filter(title_line,2);
                                }
                            }else{								//count_line=-1(既読見出し文)
                                pw.write(title_line+"\t\t"+count_line);
                                pw.newLine();
                                x--;
                            }

                        }

                        pw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    br.close();
                    Title.delete();
                    Title_w.renameTo(Title);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
