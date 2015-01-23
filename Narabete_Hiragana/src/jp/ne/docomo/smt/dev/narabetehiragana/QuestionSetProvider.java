/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


    /**
     * 問題を順番に提供するクラス。順番はシャッフルされている。
     * クラスが持つ全問題の中から、いくつかをまとめた「問題セット」という単位で順次提供される。
     * クラスがもつ全ての問題が一巡すると再度シャッフルされ、また最初から提供される。
     */

public class QuestionSetProvider implements Serializable{

    //Serializableの生成シリアルID
    private static final long serialVersionUID = -3522944474714869346L;

    private List<ShuffleWord>mAllWordList;    //すべての単語をシャッフルした状態で保持する。このListから順次、問題セットを提供していく
    private int mNextIndex; //次に問題にすべき単語の上記ListにおけるIndex

    /**
     * コンストラクタ
     * @param wordList 問題の元となる単語群
     */
    public QuestionSetProvider(List<ShuffleWord> wordList){
        super();
        this.mAllWordList = wordList;
        resetAllQuestions();
    }

    /**
     * 次の問題セットを返すメソッド
     * @param numOfQuestions
     * @return
     */
    public List<Question> getNextQuestionSet(int numOfQuestions){
        List<Question> list = new ArrayList<Question>(numOfQuestions);
        for(int i=0 ; i<numOfQuestions ; i++){
            list.add( new Question(getNextWord()) );
        }
        return list;
    }

    /**
     * 全ての問題をシャッフルして初期化するメソッド
     * @param context
     */
    public void resetAllQuestions(){
        Collections.shuffle(mAllWordList);
        mNextIndex = 0;
    }

    /**
     * 次の単語を返すメソッド。最後の単語に到達した場合は単語がシャッフルされまた最初から始まる
     * 外部からは使用されない。
     * @return
     */
    private ShuffleWord getNextWord(){
        //全単語が一巡した場合は再シャッフル後、 0 に戻して循環
        if(mNextIndex >= mAllWordList.size()  ||  mNextIndex<0){
            resetAllQuestions();
        }
        return mAllWordList.get(mNextIndex++); //取得後、Indexを1つ進める
    }

}
