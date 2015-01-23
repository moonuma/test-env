/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

        /**
         * 文字カードをドロップする答えエリアを表現するView
         */


public class AnswerAreaView extends View {

    private View mAnswerView;    //現在、エリア内に収まっている文字カードViewを保持する変数。実際には重ねて表示されているだけで、内包しているわけではない。

    public AnswerAreaView(Context context) {
        super(context);
    }

    public AnswerAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnswerAreaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * このエリアの答えとしてドロップされた文字カードをセットするメソッド
     * @param answerView
     */
    public void setAnswerView(View answerView){
        this.mAnswerView = answerView;
    }

    /**
     * 現在、このエリアの答えとなっている文字カードViewをクリアーするメソッド
     * 文字カードView自体が消去されるわけではない
     */
    public void clearAnswerView(){
        this.mAnswerView = null;
    }

    /**
     * このエリア内に答えとして文字カードが収まっているかどうかを返すメソッド
     * @return
     */
    public boolean hasAnswerView(){
        return mAnswerView != null;
    }

    /**
     * 現在、このエリア内に答えとして収まっている文字カードViewを返すメソッド
     * @return
     */
    public View getAnswerView(){
        return mAnswerView;
    }

}
