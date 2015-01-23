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
         * ある単語の文字順をシャッフルして扱うためのクラス
         */

public class ShuffleWord implements Serializable{

    /**
     * Serializableの生成シリアルID
     */
    private static final long serialVersionUID = -5853971314511823249L;

    private String mOriginalString;        //正しい順番の文字列
    private String mAccentString;    //合成音声を正しいアクセントで発音させるため漢字などで表現した文字列。 例:「おかし」の場合は「お菓子」など...

    /**
     * コンストラクタ
     * @param wordString 単語
     */
    public ShuffleWord(String wordString){
        this.mOriginalString = wordString;
    }

    /**
     * コンストラクタ
     * @param wordString    単語
     * @param accentString  合成音声を正しいアクセントで発音させるため漢字などで表現した文字列。例:「おかし」の場合は「お菓子」など
     */
    public ShuffleWord(String wordString, String accentString){
        this(wordString);
        this.mAccentString = accentString;
    }

    /**
     * 単語を正しい文字順のString型で返すメソッド
     */
    public String getOriginalString(){
        return mOriginalString;
    }

    /**
     * 単語を正しい文字順で1文字ずつ細切れにしたListを返すメソッド
     * @return
     */
    private List<String> getOriginalList(){
        int length = mOriginalString.length();
        List<String> list = new ArrayList<String>(length);
        for(int i=0 ; i<length ; i++){
            list.add(mOriginalString.substring(i, i+1));
        }
        return list;
    }

    /**
     * 単語をランダムな文字順で1文字ずつ細切れにしたListを返すメソッド
     * @return
     */
    public List<String> getShuffledList(){
        List<String> orgList = getOriginalList();
        List<String> shuffledList = getOriginalList();

        if(orgList.size() > 1){
            do{
                Collections.shuffle(shuffledList);
            }while(isEqualList(orgList, shuffledList));
        }
        return shuffledList;
    }

    /**
     * 2つの細切れ文字Listが合致しているかを返すメソッド
     * @param listA
     * @param listB
     * @return
     */
    private boolean isEqualList(List<String> listA, List<String> listB){
        boolean isEqual = true;

        if(listA.size()==listB.size()){
            int size = listA.size();
            for(int i=0 ; i<size ; i++){
                if( ! listA.get(i).equals(listB.get(i)) ){
                    isEqual = false;
                    break;
                }
            }
        }else{
            isEqual = false;
        }
        return isEqual;
    }

    /**
     * 音声合成用の文字列を返すメソッド
     * 発音アクセント補助用の文字列があればそれを、無ければ単語そのものを返す。
     * @return
     */
    public String getVoiceString(){
        if(mAccentString != null){
            return mAccentString;
        }
        return this.getOriginalString();
    }
}
