/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SimpleDialogFragment extends DialogFragment {

    //setArguments()・getArguments()で扱うBundleで使用するキー
    private static final String ARG_KEY_TITLE = "title";
    private static final String ARG_KEY_MESSAGE = "message";

    /**
     * 新しいインスタンスを作成して返すメソッド。コンストラクタの代わり
     * @param title    ダイアログのタイトル
     * @param message  ダイアログの本文
     * @return
     */
    public static SimpleDialogFragment getInstance(String title, String message){
        SimpleDialogFragment f = new SimpleDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY_TITLE, title);
        args.putString(ARG_KEY_MESSAGE, message);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = this.getArguments();
        String title = args.getString(ARG_KEY_TITLE);
        String message = args.getString(ARG_KEY_MESSAGE);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.close, null);
        return builder.create();
    }
}
