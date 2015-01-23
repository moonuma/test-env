/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

    /**
     * 動物イラストのリソースIDをランダムな順番で提供するためのクラス
     */

public class AnimalPictureProvider {

    //動物イラスト画像ファイルのリソースID配列。これはシャッフル前のベース。
    private static Integer[] sPictureIdArray = new Integer[]    {
                                                                    R.drawable.animal_ashika,
                                                                    R.drawable.animal_hamster,
                                                                    R.drawable.animal_harinezumi,
                                                                    R.drawable.animal_iruka,
                                                                    R.drawable.animal_kaeru,
                                                                    R.drawable.animal_kujira,
                                                                    R.drawable.animal_kuma,
                                                                    R.drawable.animal_neko,
                                                                    R.drawable.animal_risu
                                                                    };

    private Integer[] mShuffledPictureIdArray;    //シャッフル後のリソースID配列
    private int mCurrentIndex;    //シャッフル後のリソースID配列における現在のイラストのIndex

    /**
     * コンストラクタ
     * @param context
     */
    public AnimalPictureProvider(){
        super();

        //イラストのリソースID配列の順番をシャッフル
        shufflePictures();
        this.mCurrentIndex = -1;
    }

    /**
     * 次のイラストのリソースIDを返すメソッド
     * @return
     */
    public int getNextPictureId(){

        //一巡したら再度シャッフル
        if(++mCurrentIndex>=mShuffledPictureIdArray.length  ||  mCurrentIndex<0){
            shufflePictures();
            mCurrentIndex = 0;
        }
        return mShuffledPictureIdArray[mCurrentIndex];
    }

    /**
     * リソースID配列の順番をシャッフルするメソッド
     */
    private void shufflePictures(){
        List<Integer> list = Arrays.asList(sPictureIdArray);
        Collections.shuffle(list);
        this.mShuffledPictureIdArray = list.toArray(new Integer[0]);
    }

}
