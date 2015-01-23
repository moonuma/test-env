/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends Activity {

    //Activity間のIntentで使用するExtraキー
    public static final String INTENT_EXTRA_QUESTION_LIST = "question_list";

    //受け取った解答結果
    private List<Question> mQuestionList;

    //View群
    private ImageView mImageViewTop;    //正解率に応じて変化するイラスト「たいへんよくできました」など
    private TextView mTextViewNumOfAllQuestions;
    private TextView mTextViewNumOfRightQuestions;
    private Button mButtonRestart;    //「つぎのもんだいをとく」ボタン

    //画面回転時などに特定の値を保存する際に使用する Bundle savedInstanceState で用いるキー定数
    private static final String STATE_KEY_QUESTION_LIST = "question_list";    //現在の問題List

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //画面回転などから復帰した場合は、受け取った解答結果Listを復元する
        if(savedInstanceState != null){
            Object obj =  savedInstanceState.getSerializable(STATE_KEY_QUESTION_LIST);
            mQuestionList =
                    getQuestionListIfValidValue(obj);   //復元したインスタンスの正当性をチェックする（不正な値は null となる）
        //通常の起動時
        }else{
            Intent intent = getIntent();
            if(intent.hasExtra(INTENT_EXTRA_QUESTION_LIST)){
                Object obj = intent.getSerializableExtra(INTENT_EXTRA_QUESTION_LIST);
                mQuestionList =
                        getQuestionListIfValidValue(obj);  //受け取ったインスタンスの正当性をチェックする（不正な値は null となる）
            }
        }

        mImageViewTop = (ImageView) findViewById(R.id.activity_result_imgView_top);
        mTextViewNumOfAllQuestions =
                (TextView) findViewById(R.id.activity_result_textView_numOfAllQuestion);
        mTextViewNumOfRightQuestions =
                (TextView) findViewById(R.id.activity_result_textView_numOfRightQuestion);
        mButtonRestart = (Button) findViewById(R.id.activity_result_btn_oneMoreGame);
        mButtonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //次の問題セットを開始する要求を呼び出し元Activityへ返して、このActivityを閉じる
                setResult(Activity.RESULT_OK, new Intent().putExtra(
                        MainActivity.INTENT_EXTRA_IS_NEXT_QUESTION_SET_REQESTED, true));
                finish();
            }
        });

        //受け取った値を表示
        if(mQuestionList != null){
            int numOfRights = getNumOfRightQuestions(mQuestionList);
            int numOfQuestions = mQuestionList.size();
            mImageViewTop.setImageResource(this.getTopImageResourceId(numOfRights, numOfQuestions));
            mTextViewNumOfAllQuestions.setText(
                    ZenkakuUtil.toZenkakuNumber(numOfQuestions, true)
                    + getString(R.string.mon_chu) );  //数字は全角に変換して表示。ただし日本語環境外では半角
            mTextViewNumOfRightQuestions.setText(
                    ZenkakuUtil.toZenkakuNumber(numOfRights, true) + getString(R.string.mon) );
        }else{
            //正しく解答結果を受け取れなかった場合はエラーであることを表示して、このActivityを閉じる
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_KEY_QUESTION_LIST, (Serializable)mQuestionList);
    }

    /**
     * 引数のインスタンスが有効な List＜Question＞ であればそれを返すメソッド。
     * 不正な値であれば null を返す
     * @param obj
     * @return    null または List＜Question＞
     */
    @SuppressWarnings("unchecked")
    private List<Question> getQuestionListIfValidValue(Object obj){
        if(obj != null  &&  obj instanceof List){
            List<?> list = (List<?>) obj;
            if(list.size()>0){
                Object listObject = list.get(0);
                if(listObject != null  &&  listObject instanceof Question){
                    return (List<Question>) list;
                }
            }
        }
        return null;
    }

    /**
     * 解答結果から正解の数をカウントして返すメソッド
     * @param list
     * @return
     */
    private int getNumOfRightQuestions(List<Question> list){
        int count = 0;
        for(Question q : list){
            if(q.getAnswerResult()==Question.AnswerResult.RIGHT){
                count++;
            }
        }
        return count;
    }

    /**
     * 正解率にあわせたトップ画像のリソースIDを返すメソッド
     * @param numOfRights
     * @param numOfQuestions
     */
    private int getTopImageResourceId(int numOfRights, int numOfQuestions){
        int resourceId = R.drawable.stamp_yokudekimasita;

        int percentage = Math.round( (float)numOfRights/numOfQuestions*100 );

        if(percentage>=90){
            resourceId = R.drawable.stamp_taihenyokudekimasita;//たいへんよくできました

        }else if(percentage>=70){
            resourceId = R.drawable.stamp_yokudekimasita;//よくできました

        }else if(percentage>=50){
            resourceId = R.drawable.stamp_ganbarimasita;//がんばりました

        }else if(percentage>=30){
            resourceId = R.drawable.stamp_mousukosiganbarimasyou;//もうすこしがんばりましょう

        }else{
            resourceId = R.drawable.stamp_ganbarimasyou;//がんばりましょう
        }
        return resourceId;
    }

}
