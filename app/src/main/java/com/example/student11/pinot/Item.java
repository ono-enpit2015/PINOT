package com.example.student11.pinot;

/**
 * Created by student11 on 2015/09/25.
 */
//「Item.java」ファイルは、記事タイトルや、記事概要を格納するクラスで、
//前回修正した「item_row.xml（レイアウトファイル）」との関連も強いです。
public class Item {
    // 記事のタイトル
    private CharSequence mTitle;
    // 記事の更新日
    private CharSequence mDate;
    // 記事のリンク
    private CharSequence mLink;

    public Item() {
        mTitle = "";
        mDate = "";
        mLink = "";
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public CharSequence getDate() {
        return mDate;
    }

    public void setDate(CharSequence date) {
        mDate = date;
    }

    public CharSequence getLink() {
        return mLink;
    }

    public void setLink(CharSequence link) {
        mLink = link;
    }
}