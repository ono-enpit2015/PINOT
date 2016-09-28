package com.example.student11.pinot;

import android.app.ListActivity;
import android.app.ProgressDialog;
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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

//ListViewオブジェクトを既に含んだListActivityを使っているので,新たにListViewのインスタンスを生成して、アクティビティにセットする必要がない
//setAdapter() ではなく setListAdapter() を使う
public class RssReaderActivity extends ListActivity {
    private static final String RSS_FEED_URL =  "http://tshinobu.com/lab/get-multi-rss/?url=http%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fworld%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fentertainment%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fcomputer%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Flocal%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fdomestic%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Feconomy%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fsports%2Frss.xml%0D%0Ahttp%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fscience%2Frss.xml&rss=RSS%E3%83%80%E3%82%A6%E3%83%B3%E3%83%AD%E3%83%BC%E3%83%89"; //http://www.rssmix.com/u/6589813/rss.xml or http://mix.chimpfeedr.com/9f2cd-yahoonews
    private ArrayList<Item> mItems;
    private RssListAdapter mAdapter;
    long start;
    long stop;
    long diff = 0;
    int second;
    int comma;
    final String LOGDIR = Environment.getExternalStorageDirectory().getPath()+"/data/";
    final String SDFILE1 = LOGDIR + "displayed2.txt";
    final String SDFILE2 = LOGDIR + "tmp.txt";
    final String SDFILE3 = LOGDIR + "all2.txt";
    File DISPLAYED = new File(SDFILE1);
    File TMP = new File(SDFILE2);
    File ALL = new File(SDFILE3);
    File DATA = new File(LOGDIR);
    private String line;		//title_info.txtの先頭から１行ずつ取ってきたものを格納
    private int touch;
    private int viewcount;         //視認回数
    private String title_displayed;
    private String link_displayed;
    private String date_displayed;
    ArrayList<String> list;
    static ArrayList<String> list2;
    int x;
    static ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!DATA.exists()) {
            DATA.mkdir();
        }

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList<Item>();
        mAdapter = new RssListAdapter(this, mItems);
        list = new ArrayList<String>();

        // タスクを起動する
        RssParserTask task = new RssParserTask(this, mAdapter);
        task.execute(RSS_FEED_URL);

    }

    // リストの項目を選択した時の処理
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        timer();
        progressDialog = new ProgressDialog(RssReaderActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("記事を読み込み中・・・");
        progressDialog.setCancelable(true);
        progressDialog.show();

        Item item = mItems.get(position);
        list.add(String.valueOf(item.getTitle()));
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("LINK", item.getLink());

        startActivity(intent);
    }

    public void timer(){
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

    public static void distinct(List<String> slist) {       //要素の重複を削除
        list2 = new ArrayList<String>();
        for (Iterator<String> i = slist.iterator(); i.hasNext();) {
            String s = i.next();
            if (list2.contains(s)) {
                i.remove();
            } else {
                list2.add(s);
            }
        }
    }

    public void touchinfo(){
        distinct(list);
        PINOT_FILTER P = new PINOT_FILTER();

        try {
            TMP.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(TMP,true));
                while ((line = br.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    title_displayed = tok.nextToken();
                    link_displayed = tok.nextToken();
                    date_displayed = tok.nextToken();
                    viewcount = Integer.parseInt(tok.nextToken());
                    touch = Integer.parseInt(tok.nextToken());
                    boolean f = true;
                    for ( int i = 0; i < list2.size(); i++ ) {
                        P.Pinot_Filter(list2.get(i),3);
                        if (list2.get(i).equals(title_displayed)) {
                            if (touch == -1) {//今まで未タップの場合
                                pw.write(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + viewcount);
                                pw.newLine();
                            } else if (touch >= 0) {        //今までにタップ済みの場合
                                pw.write(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                                pw.newLine();
                            }
                            f=false;
                            break;
                        }
                    }
                    if(f) {
                        pw.write(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                        pw.newLine();
                    }
                }
                pw.close();
                br.close();
                if(list2.size()>=1) {
                    DISPLAYED.delete();
                    TMP.renameTo(DISPLAYED);
                }
                if(TMP.exists()){
                    TMP.delete();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void viewcount(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                BufferedWriter pw = new BufferedWriter(new FileWriter(TMP, true));
                while ((line = br.readLine()) != null) {
                    StringTokenizer tok = new StringTokenizer(line, "\t");
                    title_displayed = tok.nextToken();
                    link_displayed = tok.nextToken();
                    date_displayed = tok.nextToken();
                    viewcount = Integer.parseInt(tok.nextToken());
                    touch = Integer.parseInt(tok.nextToken());
                    if (x > 0) {
                        viewcount++;
                        x--;
                    }
                    pw.write(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                    pw.newLine();
                }
                pw.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            br.close();
        }catch (IOException e1){
            e1.printStackTrace();
        }
        DISPLAYED.delete();
        TMP.renameTo(DISPLAYED);
    }

    public void UPkousin() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(DISPLAYED));
            try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(TMP));
                try {
                    BufferedWriter bw2 = new BufferedWriter(new FileWriter(ALL,true));
                    while ((line = br.readLine()) != null) {
                        StringTokenizer tok = new StringTokenizer(line, "\t");
                        title_displayed = tok.nextToken();
                        link_displayed = tok.nextToken();
                        date_displayed = tok.nextToken();
                        viewcount = Integer.parseInt(tok.nextToken());
                        touch = Integer.parseInt(tok.nextToken());
                        if (viewcount >= 2 && touch == -1) {
                            PINOT_FILTER P = new PINOT_FILTER();
                            P.Pinot_Filter(title_displayed, 2);        // ユーザプロファイルの更新
                            bw2.write(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                            bw2.newLine();
                            //System.out.println("視認回数：2回以上、未タッチ："+title_displayed);
                        } else {
                            bw.write(title_displayed + "\t" + link_displayed + "\t" + date_displayed + "\t" + viewcount + "\t" + touch);
                            bw.newLine();
                            //System.out.println("次も表示する："+title_displayed);
                        }
                    }
                    bw2.close();
                }catch (IOException e1) {
                    e1.printStackTrace();
                }
                bw.close();
            }catch (IOException e1){
                e1.printStackTrace();
            }
            br.close();
        }catch (IOException e1){
            e1.printStackTrace();
        }
        DISPLAYED.delete();
        TMP.renameTo(DISPLAYED);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){				// 戻るボタンが押された！
            timer();
            x = (int) (diff/2500);
            Log.e("終了", second + "." + comma);
            Log.e("視認件数",""+x);
            start = stop = RssParserTask.start = ItemDetailActivity.start = 0;

            viewcount();
            touchinfo();
            UPkousin();

            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
