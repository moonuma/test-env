/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.io.Serializable;


        /**
         * １つの問題を示すクラス
         */

public class Question implements Serializable{

    private static final long serialVersionUID = -7084955205189126022L;    //Serializableの生成シリアルID

    private ShuffleWord mWord;    //この問題の単語
    private AnswerResult mAnswerResult;    //解答状態を示す。下記Enum値が入る。ちなみに一度解答された後は、再解答しても解答状態は変更されない。

    /**
     * 解答状態を示すEnum
     */
    public enum AnswerResult{
        /** 正解を解答済み*/
        RIGHT,
        /** 不正解を解答済み*/
        WRONG,
        /** まだ解答されていない */
        UNANSWERED
    }

    /**
     * コンストラクタ
     * @param word
     */
    public Question(ShuffleWord word){
        this.mWord = word;
        this.mAnswerResult = AnswerResult.UNANSWERED;
    }

    /**
     * この問題の答えとなる単語を返すメソッド
     * @return
     */
    public ShuffleWord getRightWord(){
        return mWord;
    }

    /**
     * 解答をチェックするメソッド。Questionインスタンス内部の解答状態も反映される。
     * ただし、一度解答された後は、再解答しても解答状態は変更されない。
     * 例えば不正解の後、再解答で正解してもインスタンスの解答状態は WRONG のまま。
     * @param answer    入力された解答
     * @return    正解かどうか
     */
    public boolean checkAnswer(String answer){
        boolean isRight = (mWord.getOriginalString().equals(answer));

        //解答結果をフィールドに反映。ただし未解答の場合のみ
        if(mAnswerResult == AnswerResult.UNANSWERED){
            this.mAnswerResult = isRight  ?  AnswerResult.RIGHT  :  AnswerResult.WRONG;
        }

        return isRight;
    }

    /**
     * この問題が既に解答済みかどうかを返すメソッド
     * @return
     */
    public boolean isAnswered(){
        return mAnswerResult != AnswerResult.UNANSWERED;
    }

    /**
     * この問題の解答状態を返すメソッド
     * @return
     */
    public AnswerResult getAnswerResult(){
        return mAnswerResult;
    }
}
