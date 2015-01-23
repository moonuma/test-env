/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.ne.docomo.smt.dev.narabetehiragana.TextSpeaker.OnErrorListener;

public class MainActivity
        extends FragmentActivity
        implements OnTouchListener, OnClickListener, OnErrorListener {

    //ひらがなの数を示す定数
    private static final int NUM_OF_CHARACTERS = 3;

    //一度に出題される問題の数（問題セットの数）を示す定数。この問題数ごとに一区切りとなり結果が表示される。
    private static final int NUM_OF_QUESTION_SET = 20;

    //使用する単語群（問題用）の配列リソースのIDを示す定数
    private static final int WORD_ARRAY_RES_ID = R.array.hiragana_words;


    //画面回転時などに特定の値を保存する際に使用する Bundle savedInstanceState で用いるキー定数
    private static final String STATE_KEY_INDEX = "index";        //現在の問題のIndex
    private static final String STATE_KEY_QUESTION_LIST = "question_list";    //現在の問題List
    private static final String STATE_KEY_QUESTION_PROVIDER = "question_provider"; //問題セットの提供インスタンス

    //結果を表示するActivityとのやり取りに使用する定数
    private static final int REQUEST_CODE_SHOW_RESULT = 100;        // onActivityResultでの識別に使用する
    public static final String INTENT_EXTRA_IS_NEXT_QUESTION_SET_REQESTED =
            "is_next_question_set_requested"; //次の問題セットの出題を要求されたかどうか（boolean）を引き出すキー

    //設定値などを永続的に保存するプリファレンスで用いるキー定数
    private static final String PREFERENCE_KEY_VOICE_TYPE_NAME = "voice_type_name"; //話者のキー

    //View群
    private Button[] mCardViewArray;     //文字カードButtonクラスが格納される。一括処理することが多いため配列として保持している。
    private AnswerAreaView[] mAnswerAreaViewArray;    //答えエリアのView群が入る。一括処理することが多いため配列として保持している。
    private TextView mTextMessageboard; //画面最上部のTextView。「もじをならびかえてみよう」など各種メッセージを表示する
    private ImageView mImageViewOnMessageboard; //上記mTextMessageboardの右端にワンポイントで表示するイラストのImageView。
                                                //単なるにぎやかしで特に意味はない。
    private ImageView mImageViewCenter; //「よくできました」等のイラストを表示するImageView
    private Button mButtonNextQuestion; //次の問題へ進むボタン
    private Button mButtonAllQuestionsResult; //全問を解答した後、全体の結果を表示するためのボタン
    private RelativeLayout mCardParentLayout; //カード類を内包するレイアウト
    private ImageButton mImageButtonVoice; //話者選択ダイアログを表示するためのボタン

    //その他
    private TextSpeaker mTextSpeaker;    //音声再生インスタンス
    private Random mRandom = new Random(); //ランダム値生成インスタンス
    private Handler mHandler = new Handler();
    private AnimalPictureProvider mAnimalPictureProvider =
            new AnimalPictureProvider();    //ワンポイントの動物イラストの画像をランダムに取得するためのクラス
    private SharedPreferences mPreferences;    //話者などの設定を永続的に保存するためのインスタンス

    private Map<View, Point> mLastTouchPointMap =
            new HashMap<View, Point>(); //文字カードの直前のタッチ座標を保存しておくMap。
                                        //複数のカードを同時にドラッグできるようViewをキーとして複数の座標を保存している。

    private QuestionSetProvider mQuestionSetProvider;   //アプリが持つ全ての問題から、いくつかをセットにして（問題セット）
                                                        //順次提供するためのインスタンス。問題はシャッフルされる。
    private List<Question> mQuestionList;   //現在の問題セット。ここから１問ずつ出題され、全て完了すると一旦結果を表示。
                                            //その後次の問題セットを開始する。
    private int mCurrentQuestionIndex = -1; //上記 mQuestionList における現在出題されている問題のIndex。
                                            //初期値は-1

    private TextSpeaker.Error mLastSpeakError;  //直前に発生した音声合成APIに関するエラー。
                                                //同じ種類のエラーダイアログが連続して表示されないために用いる。

     //「つぎのもんだいへ」ボタンのアニメーション専用リスナーインスタンス
    private AnimatorListener mButtonAnimatorListener =
            new AnimatorListener(){
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    mButtonNextQuestion.setTextColor(
                            getResources().getColor(R.color.text_white_base_message));
                }
                @Override
                public void onAnimationRepeat(Animator animation) {}
                @Override
                public void onAnimationStart(Animator animation) {}
            };

    //全カードを初期位置（ランダム）へアニメーション移動するRunnable。不正解のときに、ワンテンポ置いてから移動させるためRunnableを使用する
    private Runnable mInitAllCardLocationRun = new Runnable(){
        public void run(){ initAllCardViewLocation(); }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //プリファレンスの取得
        mPreferences = getPreferences(MODE_PRIVATE);

        //現在の話者設定をプリファレンスから取得
        String voiceTypeName = mPreferences.getString(PREFERENCE_KEY_VOICE_TYPE_NAME, "nothing");
        TextSpeaker.VoiceType voiceType = TextSpeaker.findVoiceTypeEnum(voiceTypeName);
        if(voiceType==null){ voiceType = TextSpeaker.VoiceType.NOZOMI; } //デフォルトは nozomi

        //音声再生インスタンスの作成
        mTextSpeaker = new TextSpeaker(this);
        mTextSpeaker.setPitch(1.0F);
        mTextSpeaker.setRange(1.5F);
        mTextSpeaker.setRate(0.75F);
        mTextSpeaker.setVolume(1.8F);
        mTextSpeaker.setVoiceType( voiceType );
        mTextSpeaker.setOnErrorListener(this);
        this.setAudioStreamType(AudioManager.STREAM_MUSIC);    //音声の出力先は音楽用ストリームに指定

        //View類の取得
        mTextMessageboard = (TextView) findViewById(R.id.activity_main_textView_messageBoard);
        mImageViewOnMessageboard =
                (ImageView) findViewById(R.id.activity_main_imgView_onMessageBoard);
        mImageViewCenter = (ImageView) findViewById(R.id.activity_main_imgView_center);
        mButtonNextQuestion = (Button) findViewById(R.id.activity_main_btn_nextQuestion);
        mButtonAllQuestionsResult =
                (Button) findViewById(R.id.activity_main_btn_showAllQuestionsResult);
        mCardParentLayout = (RelativeLayout) findViewById(R.id.activity_main_relativeLayout_root);
        mImageButtonVoice = (ImageButton) findViewById(R.id.activity_main_imgBtn_voice);

        mCardViewArray = new Button[NUM_OF_CHARACTERS];
        mCardViewArray[0] = (Button) findViewById(R.id.activity_main_btn_cardA);
        mCardViewArray[1] = (Button) findViewById(R.id.activity_main_btn_cardB);
        mCardViewArray[2] = (Button) findViewById(R.id.activity_main_btn_cardC);

        mAnswerAreaViewArray = new AnswerAreaView[NUM_OF_CHARACTERS];
        mAnswerAreaViewArray[0] = (AnswerAreaView) findViewById(R.id.activity_main_answerArea_1);
        mAnswerAreaViewArray[1] = (AnswerAreaView) findViewById(R.id.activity_main_answerArea_2);
        mAnswerAreaViewArray[2] = (AnswerAreaView) findViewById(R.id.activity_main_answerArea_3);

        //Viewにイベントリスナーをセット
        mButtonNextQuestion.setOnClickListener(this);
        mButtonAllQuestionsResult.setOnClickListener(this);
        mImageButtonVoice.setOnClickListener(this);

        for(Button card : mCardViewArray){
            card.setOnTouchListener(this);
        }

        //通常の起動時
        if(savedInstanceState==null){
            mQuestionSetProvider =
                    new QuestionSetProvider( getAllWordFromResource() );    //問題セットの順次提供インスタンス作成
            mQuestionList =
                    mQuestionSetProvider.getNextQuestionSet(NUM_OF_QUESTION_SET);   //問題セットを取得
            mCurrentQuestionIndex = 0;
        }else{    //画面回転からの復帰時などは問題系のフィールド値を復元
            mQuestionSetProvider =
                    (QuestionSetProvider) savedInstanceState.getSerializable(
                            STATE_KEY_QUESTION_PROVIDER);
            mQuestionList =
                    (List<Question>) savedInstanceState.getSerializable(STATE_KEY_QUESTION_LIST);
            mCurrentQuestionIndex = savedInstanceState.getInt(STATE_KEY_INDEX);
        }

        //文字カードに最初の単語をセットしておく。これは初期状態の（カードがアニメーションでバラける前の）見栄えのための処理。
        setTextToCards(mQuestionList.get(mCurrentQuestionIndex).getRightWord());

        //文字カードサイズの調整とカードの配置。
        //これらの処理には各Viewのサイズ取得が必要だが、onCreateViewの段階ではサイズを取得できないのでView.postメソッドを使用する
        mCardParentLayout.postDelayed(new Runnable(){
            public void run(){
                initAllCardViewSize();

                //該当問題が既に正解されていれば（画面回転での復元時など）、その正解のView状態を再現する
                if(getCurrentQuestion().getAnswerResult()==Question.AnswerResult.RIGHT){
                    updateViewForRightAnswer();
                    moveAllCardsToRightAnswerArea();

                }else{
                    --mCurrentQuestionIndex; //下記 startNextQuestion() メソッドでIndexが1つ進んでしまうので予め１を引いておく
                    startNextQuestion();
                }
            }
        }, 500);
    }//end of <onCreate>

    @Override
    public void onStart(){
        super.onStart();

        //端末の音量が０であれば音量の増加を促すダイアログを表示する
        if(getDeviceVolumeOfVoice() <= 0){
            SimpleDialogFragment fragment =
                    SimpleDialogFragment.getInstance(
                            getString(R.string.volume_zero),
                            getString(R.string.desc_volume_zero));
            fragment.show(getSupportFragmentManager(), "volume_zero");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        //画面回転などでActivityがリセットされる際に、現在の問題ListとIndexを保存して再度復元する
        outState.putSerializable(STATE_KEY_QUESTION_PROVIDER, (Serializable)mQuestionSetProvider);
        outState.putSerializable(STATE_KEY_QUESTION_LIST, (Serializable)mQuestionList);
        outState.putInt(STATE_KEY_INDEX, mCurrentQuestionIndex);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        //結果を表示するアクティビティから出題を最初からやり直すよう要求されていれば
        if(requestCode==REQUEST_CODE_SHOW_RESULT){
            if(data != null  &&
                    data.getBooleanExtra(INTENT_EXTRA_IS_NEXT_QUESTION_SET_REQESTED, false)){
                startNextQuestionSet();    //次の問題セットを出題開始
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPreferences.edit().putString(
                PREFERENCE_KEY_VOICE_TYPE_NAME,
                mTextSpeaker.getVoiceType().name
                ).commit();        //現在の話者設定をプリファレンスに永続保存
        mHandler.removeCallbacks(mInitAllCardLocationRun);
        mTextSpeaker.dispose();
    }

    /**
     * リソースから問題用の単語を全て取得して返すメソッド
     * @return
     */
    private List<ShuffleWord> getAllWordFromResource(){
        List<ShuffleWord> list = new ArrayList<ShuffleWord>();
        String[] stringArray = getResources().getStringArray(WORD_ARRAY_RES_ID);
        for(String str : stringArray){

            //発音アクセント補助用の文字列が含まれていれば、ひらがなと補助文字列を分離してからセット（ @が区切り文字 ）
            int atmarkIndex = str.indexOf("@");
            if(atmarkIndex>0){
                try{
                    String hiragana = str.substring(0, atmarkIndex);
                    String accent = str.substring(atmarkIndex+1, str.length());
                    list.add(new ShuffleWord(hiragana, accent) );
                }catch(IndexOutOfBoundsException e){
                    // TODO エラー処理を書く
                }
            }else{
                list.add( new ShuffleWord(str) ) ;
            }
        }
        return list;
    }

    /**
     * TextSpeekerによる音声合成のエラーが通知されるメソッド
     * @param exception
     * @param errorCode
     */
    @Override
    public void onSpeekError(Exception exception, String errorCode) {
        //エラーコード（文字列）から該当するエラーEnumを取得
        TextSpeaker.Error error = TextSpeaker.findErrorEnum(errorCode);

        //今回のエラーと直近に発生したエラーが違う場合のみエラーダイアログを表示する。
        //これは同種のエラーが何度もダイアログ表示されないようにするため。（やむなく音声無しでアプリを使用する場合など）
        if(error != mLastSpeakError){
            SimpleDialogFragment dialog = null;
            switch(error){
                case SERVER_CONNECTION_ERROR:
                    dialog = SimpleDialogFragment.getInstance(
                            getString(R.string.error),
                            getString(R.string.desc_of_connection_error));
                    break;
                case SERVER_OTHER_ERROR:
                case SERVER_LIMITATION_ERROR:
                    dialog = SimpleDialogFragment.getInstance(
                            getString(R.string.error),
                            getString(R.string.desc_of_server_limited_error));
                    break;
                case INVALID_PARAMETER:
                case AUTHENTICATION_ERROR:
                case SDK_INSIDE_ERROR:
                case RESPONSE_DATA_ERROR:
                default:
                    dialog = SimpleDialogFragment.getInstance(
                            getString(R.string.error),
                            getString(R.string.desc_of_fatal_error));
                    break;
            }
            //エラーダイアログの表示
            if(dialog!=null){
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), "error");
            }

        }//end of if

        mLastSpeakError = error;    //今回のエラーをフィールドに保存しておく
    }

    /**
     * Viewのタッチイベントが通知されるメソッド
     * このクラスでは文字カードViewのイベントのみ受け取る
     */
    @Override
    public boolean onTouch(View cardView, MotionEvent event) {
        //前回のタッチポイントを取得。複数のViewを同時にドラッグできるようにViewをキーとしたMapに保存してある。ちなみに初回は null なので注意
        Point lastPoint = mLastTouchPointMap.get(cardView);

        float rawX = event.getRawX();
        float rawY = event.getRawY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:    //タッチ開始時
                //カードが重なった際に不自然なタッチ優先度にならないよう、ドラッグしたカードは最前面にする
                cardView.bringToFront();

                //ドラッグされたカードがどの答えエリアにも収まっていないことを示しておく
                for(AnswerAreaView area : mAnswerAreaViewArray){
                    if(cardView == area.getAnswerView()){
                        area.clearAnswerView();
                        break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:    //タッチ移動中
                //文字カードViewのドラッグ移動。初回タッチ時は lastPoint が null なので念のため try~catch
                try{
                    float moveX = rawX - lastPoint.x;
                    float moveY = rawY - lastPoint.y;
                    moveView(cardView, (cardView.getX()+moveX), (cardView.getY()+moveY));
                }catch(NullPointerException e){
                    // TODO エラー処理を書く
                }
                break;
            case MotionEvent.ACTION_UP:            //タッチ終了時
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                //カードが画面外に出てしまった場合は、自動で画面内に移動させる
                if( ! isInsideView(cardView, mCardParentLayout) ){
                    initCardViewLocation(cardView);
                }else{
                    //カードが答えのエリアへドロップされた場合は吸い寄せるようにエリア内の中央に移動して、そのカードを暫定的な答えとして保持しておく
                    for(AnswerAreaView area : mAnswerAreaViewArray){
                        if( isInsidePoint(area, rawX, rawY) ){
                            //既にそのエリアに収まっているカードがあればエリア外へ移動させる
                            if(area.hasAnswerView()){
                                View oldView = area.getAnswerView();
                                if(oldView != cardView) {
                                    animateView(oldView, oldView.getX(),
                                            (oldView.getY()-Math.round(oldView.getHeight()*1.2F)),
                                            getRandomCardRotation());
                                }
                            }

                            //今回ドロップしたカードをエリア内へ移動
                            moveViewCenter(cardView, area);
                            cardView.setRotation(0); //角度を0に強制
                            area.setAnswerView(cardView); //エリアに答えのカードをセット

                            //現在の答えをチェック
                            checkAnswer();
                            break;
                        }
                    }//end of <for>
                }
                break;
            default:
                break;
        }//end of switch

        //タッチ座標をフィールドに保存しておく
        if(lastPoint==null){
            lastPoint = new Point(rawX, rawY);
            mLastTouchPointMap.put(cardView, lastPoint);
        }else{
            lastPoint.x = rawX;
            lastPoint.y = rawY;
        }
        return true;
    }

    /**
     * Viewのクリックで呼ばれるメソッド
     */
    @Override
    public void onClick(View v) {
        if(v == mButtonNextQuestion){        //次の問題へ進むボタン
            startNextQuestion();

        }else if(v == mButtonAllQuestionsResult){    //全問解答後の結果を表示するボタン
            showResultActivity();

        }else if(v == mImageButtonVoice){        //話者選択ボタン
            showVoiceSelectDialog();
        }
    }

    /**
     * 合成音声の出力先ストリームをセットするメソッド。
     * TextSpeakerにセットされるだけでなく、Activity#{@link #setVolumeControlStream(int)}が呼ばれることで、
     * 物理音量キーによって該当ストリームの音量が変化するようになる
     * @param streamType AudioManagerの定数
     */
    private void setAudioStreamType(int streamType){
        this.mTextSpeaker.setStreamType(streamType);
        this.setVolumeControlStream(streamType);
    }

    /**
     * 現在の音声出力先の端末音量を返すメソッド
     * もし音声出力用のインスタンスがnullの場合は {@link AudioManager#STREAM_MUSIC}の音量を返す
     * @return
     */
    private int getDeviceVolumeOfVoice(){
        int volume = 0;
        AudioManager audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if(mTextSpeaker==null){
            volume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        }else{
            volume = audioMgr.getStreamVolume(mTextSpeaker.getStreamType());
        }
        return volume;
    }

    /**
     * 合成音声の話者をセットするメソッド
     * @param type
     */
    public void setVoiceType(TextSpeaker.VoiceType type){
        if(mTextSpeaker != null){ mTextSpeaker.setVoiceType(type); }
    }

    /**
     * 最後の問題かどうかを返すメソッド
     * @return
     */
    private boolean isLastQuestion(){
        return this.mCurrentQuestionIndex==(mQuestionList.size()-1);
    }

    /**
     * 現在の答えの正解・不正解をチェックするメソッド
     */
    private void checkAnswer(){

        mHandler.removeCallbacks(mInitAllCardLocationRun);    //もし不正解によるカード移動が待機中であればキャンセルしておく

        if(isAnswerFull()){
            String userAnswer = getCurrentUserAnswer();    //ユーザーの入力した答えを取得

            //正解のとき
            if( getCurrentQuestion().checkAnswer(userAnswer) ){
                mTextSpeaker.speak(getCurrentQuestion().getRightWord().getVoiceString(), 0);
                updateViewForRightAnswer();    //各Viewを正解後の状態へ変更

            }else{    //不正解のとき
                mTextSpeaker.speak(getString(R.string.zan_nen_mou_ichido_for_accent)
                        + getCurrentQuestion().getRightWord().getVoiceString(), 0);
                updateViewForWrongAnswer(); //各Viewを不正解後の状態へ変更
                mHandler.postDelayed(mInitAllCardLocationRun, 500);    //ワンテンポ置いてからカードを初期位置へ移動させる
            }
        }
    }

    /**
     * 解答エリアが全て埋まっているかどうかを返すメソッド
     * @return
     */
    private boolean isAnswerFull(){
        for(AnswerAreaView area : mAnswerAreaViewArray){
            if( ! area.hasAnswerView() ){
                return false;
            }
        }
        return true;
    }

    /**
     * 現在、解答エリアに納められた文字の並び順を返すメソッド
     * @return
     */
    private String getCurrentUserAnswer(){
        StringBuilder sb = new StringBuilder();
        for(AnswerAreaView area : mAnswerAreaViewArray){
            if(area.hasAnswerView()){
                View answerView = area.getAnswerView();
                if(answerView instanceof Button){
                    sb.append( ((Button)answerView).getText().toString() );
                }
            }else{
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * 結果表示アクティビティを表示するメソッド。現在の問題セットを渡す。
     */
    private void showResultActivity(){
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.INTENT_EXTRA_QUESTION_LIST, (Serializable) mQuestionList);
        startActivityForResult(intent, REQUEST_CODE_SHOW_RESULT);
    }

    /**
     * 話者選択ダイアログを表示するメソッド
     */
    private void showVoiceSelectDialog(){
        VoiceSelectDialogFragment fragment =
                VoiceSelectDialogFragment.getInstance(mTextSpeaker.getVoiceType());
        fragment.show(getSupportFragmentManager(), "voice");
    }

    /**
     * Viewを移動させるメソッド
     * @param view
     * @param moveX        X座標
     * @param moveY      Y座標
     */
    private void moveView(View view, float x, float y){
        view.setX(x);
        view.setY(y);
        view.invalidate();
    }

    /**
     * Viewをアニメーション移動させるメソッド
     * @param view
     * @param x        X座標
     * @param y   Y座標
     */
     private void animateView(View view, float x, float y, float rotation){
         ViewMoveAnimator anim = new ViewMoveAnimator(view);
         anim.startAnimation(x, y, rotation);
     }

    /**
     * Viewを他のViewと同じ位置（同じ中央点）へ移動させるメソッド
     * @param view  移動するView
     * @param backView
     */
    private void moveViewCenter(View view, View backView){
        float centerX = backView.getX() + (backView.getWidth()/2);
        float centerY = backView.getY() + (backView.getHeight()/2);
        float targetX = centerX - (view.getWidth()/2);
        float targetY = centerY - (view.getHeight()/2);
        view.setX(targetX);
        view.setY(targetY);
        view.invalidate();
    }

    /**
     * 全てのカードを強制的に正解の解答エリアへ配置するメソッド
     */
    private void moveAllCardsToRightAnswerArea(){
        String rightString = this.getCurrentQuestion().getRightWord().getOriginalString();

        //解答エリアの答えを一旦すべてリセット
        for(AnswerAreaView area : mAnswerAreaViewArray){     area.clearAnswerView(); }

        //カードの配置と解答エリアの答えフィールド値のセット
        for(Button card : mCardViewArray){
            String cs = card.getText().toString();

            for(int i=0 ; i<rightString.length() ; i++){
                if(cs.equals(rightString.substring(i, i+1))){
                    if( ! mAnswerAreaViewArray[i].hasAnswerView() ){
                        mAnswerAreaViewArray[i].setAnswerView(card);
                        moveViewCenter(card, mAnswerAreaViewArray[i]);
                        card.setRotation(0); //角度を0に強制
                        break;
                    }
                }
            }
        }
    }

    /**
     * 引数のタッチRaw座標が対象Viewの範囲内かどうかを返すメソッド
     * @param view
     * @param rawX
     * @param rawY
     * @return
     */
    private boolean isInsidePoint(View view, float rawX, float rawY){
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return (rect.left<=rawX) && (rect.right>=rawX) && (rect.top<=rawY) && (rect.bottom>=rawY);
    }

    /**
     * 引数のViewが親Viewの領域内に収まっているかを返すメソッド
     * @param view
     * @return
     */
    private boolean isInsideView(View view, View parent){

        //View#getLeft()系はView#setX()系でのドラッグ移動で変化しないので座標とサイズから各辺を算出する
        float left = view.getX();
        float right = left+view.getWidth();
        float top = view.getY();
        float bottom = top + view.getHeight();

        float parentW = parent.getWidth();
        float parentH = parent.getHeight();

        return (right>0) && (left<parentW) && (bottom>0) && (top<parentH);
    }

    /**
     * 画面上部のメッセージボードのテキストをセットするメソッド。
     * 文字が変わった際はついでにボード右下のワンポイントイラストも変更される。
     * @param text
     */
    private void setMessageboardText(String text){
        //直前のテキストから変更されたかどうかを取得
        String oldText = mTextMessageboard.getText().toString();
        boolean isTextChanged = (oldText!=null)  &&  (!oldText.equals(text));

        //テキストとワンポイント画像のセット
        mTextMessageboard.setText(text);
        if(isTextChanged){
            mImageViewOnMessageboard.setImageResource( mAnimalPictureProvider.getNextPictureId() );
        }

        //右下のイラストによってテキストが左にずれてしまうのをpaddingによって補正する
        int textAreaWidth = mTextMessageboard.getWidth();
        int imgWidth = mImageViewOnMessageboard.getWidth();
        if(  textAreaWidth>0  &&  imgWidth>0  ){
            Paint p = new Paint();
            p.setTextSize(mTextMessageboard.getTextSize());
            int space = Math.round(textAreaWidth - p.measureText(text) );
            space = space>imgWidth  ?   (imgWidth)  :  (space<0 ? 0 : space);
            mTextMessageboard.setPadding(
                    space, mTextMessageboard.getPaddingTop(),
                    mTextMessageboard.getPaddingRight(),
                    mTextMessageboard.getPaddingBottom());
        }
    }

    /**
     * 「よくできました」等のイラスト用Viewを表示するメソッド
     */
    private void showCenterImageView(){
        if(mImageViewCenter.getVisibility() != View.VISIBLE){
            mImageViewCenter.setAlpha(0.0F);
            mImageViewCenter.setVisibility(View.VISIBLE);
            ObjectAnimator animater = ObjectAnimator.ofFloat(mImageViewCenter, "alpha", 0.0F, 1.0F);
            animater.setDuration(500);
            animater.start();
        }
    }

    /**
     * 「よくできました」等のイラスト用Viewを非表示にするメソッド
     */
    private void hideCenterImageView(){
        if(mImageViewCenter.getVisibility() != View.GONE) {
            mImageViewCenter.setVisibility(View.GONE);
        }
    }

    /**
     * 「つぎのもんだいへ」ボタンを表示するメソッド
     */
    private void showNextQuestionButton(){
        mButtonNextQuestion.bringToFront();
        if(mButtonNextQuestion.getVisibility() != View.VISIBLE){
            mButtonNextQuestion.setTextColor(Color.TRANSPARENT);
            mButtonNextQuestion.setVisibility(View.VISIBLE);
            ObjectAnimator animator =
                    ObjectAnimator.ofFloat(mButtonNextQuestion, "scaleX", 0.0F, 1.0F);
            animator.setDuration(600);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(mButtonAnimatorListener);
            animator.start();
        }
    }

    /**
     * 「つぎのもんだいへ」ボタンを非表示にするメソッド
     */
    private void hideNextQuestionButton(){
        if(mButtonNextQuestion.getVisibility()==View.VISIBLE){
            mButtonNextQuestion.setVisibility(View.GONE);
        }
    }

    /**
     * 「これでもんだいはおわりです！ けっかをみる」ボタンを表示するメソッド
     */
    private void showAllQuestionsResultButton()    {
        mButtonAllQuestionsResult.bringToFront();
        if(mButtonAllQuestionsResult.getVisibility() != View.VISIBLE){
            mButtonAllQuestionsResult.setTextColor(Color.TRANSPARENT);
            mButtonAllQuestionsResult.setVisibility(View.VISIBLE);
            ObjectAnimator animator =
                    ObjectAnimator.ofFloat(mButtonAllQuestionsResult, "scaleX", 0.0F, 1.0F);
            animator.setDuration(600);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener(){
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    mButtonAllQuestionsResult.setTextColor(
                            getResources().getColor(R.color.text_white_base_message));
                }
                @Override
                public void onAnimationRepeat(Animator animation) {}
                @Override
                public void onAnimationStart(Animator animation) {    }
            });
            animator.start();
        }
    }

    /**
     * 「これでもんだいはおわりです！ けっかをみる」ボタンを非表示にするメソッド
     */
    private void hideAllQuestionsResultButton(){
        if(mButtonAllQuestionsResult.getVisibility() != View.GONE){
            mButtonAllQuestionsResult.setVisibility(View.GONE);
        }
    }

    /**
     * 各Viewのテキストや表示状態を正解後の状態へ変更するメソッド
     */
    private void updateViewForRightAnswer(){
        setMessageboardText(getString(R.string.seikai));
        showCenterImageView();
        if( isLastQuestion() ){    //最後の問題であれば、「これでもんだいはおわりです けっかをみる」ボタンを表示。そうでなければ「つぎのもんだいへ」ボタンを表示
            showAllQuestionsResultButton();
        }else{
            showNextQuestionButton();
        }
    }

    /**
     * 各Viewのテキストや表示状態を不正解後の状態へ変更するメソッド
     */
    private void updateViewForWrongAnswer(){
        setMessageboardText(getString(R.string.zan_nen_mou_ichido));
        hideCenterImageView();
        hideAllQuestionsResultButton();
        hideNextQuestionButton();
    }

    /**
     * 現在、出題されている問題を返すメソッド
     * @return
     */
    private Question getCurrentQuestion(){
        return mQuestionList.get(mCurrentQuestionIndex);
    }

    /**
     * 次の【問題セット】の出題を開始するメソッド
     */
    public void startNextQuestionSet(){
        mCardParentLayout.post(new Runnable(){
            public void run(){
                mQuestionList =
                        mQuestionSetProvider.getNextQuestionSet(NUM_OF_QUESTION_SET); //次の問題セットを取得
                mCurrentQuestionIndex = -1; //下記 startNextQuestion()メソッドで index は１つ進んでしまうので -1
                startNextQuestion();
            }
        });
    }

    /**
     * 次の問題を開始するメソッド
     */
    private void startNextQuestion(){
        hideCenterImageView();
        hideNextQuestionButton();
        hideAllQuestionsResultButton();
        Question newQuestion = getNextQuestion();
        initAllCardViewLocation();
        setTextToCards(newQuestion.getRightWord());

        //最初の問題は「もじをならびかえてみよう」それ以降は「～もんめ」とメッセージボードに表示
        String message = null;
        if(mCurrentQuestionIndex<=0){
            message = getString(R.string.moji_wo_narabi_kaete_miyou);
        }else{
            message = ZenkakuUtil.toZenkakuNumber(mCurrentQuestionIndex+1, true)
                    + getString(R.string.mon_me);
        }
        setMessageboardText(message);
    }

    /**
     * 次の問題を返すメソッド
     * @return
     */
    private Question getNextQuestion(){
        //万が一、全単語が一巡したらシャッフルして次の問題セットを取得してその最初から。ただしこれは正常であれば実行されない処理
        if(++mCurrentQuestionIndex>=mQuestionList.size() || mCurrentQuestionIndex<0){
            mQuestionList =
                    mQuestionSetProvider.getNextQuestionSet(NUM_OF_QUESTION_SET);   //次の問題セットを取得
            mCurrentQuestionIndex = 0;
        }
        return mQuestionList.get(mCurrentQuestionIndex);
    }

    /**
     * 全ての文字カードViewをランダムな初期位置に移動させるメソッド
     */
    private void initAllCardViewLocation(){
        hideNextQuestionButton();
        int length = mCardViewArray.length;
        for(int i=0 ; i<length ; i++){
            initCardViewLocation(mCardViewArray[i]);
        }
        //現在の答えの状態もリセット
        for(AnswerAreaView area: mAnswerAreaViewArray){
            area.clearAnswerView();
        }
    }

    /**
     * 1枚の文字カードViewをランダムな初期位置に移動させるメソッド
     * @param card
     */
    private void initCardViewLocation(View card){
        animateView(card, getRandomCardX(card), getRandomCardY(card), getRandomCardRotation());
    }

    /**
     * 全ての文字カードのサイズを親レイアウトのサイズに合わせて調整するメソッド。
     * ただし、規定サイズ以上にはならない
     * @param card
     */
    private void initAllCardViewSize(){
        if(mCardViewArray == null  ||  mCardViewArray.length<1){ return; }

        Button card = mCardViewArray[0];
        int orgW = card.getWidth();
        int orgH = card.getHeight();
        int parentW = mCardParentLayout.getWidth();
        int parentH = mCardParentLayout.getHeight();
        int messageboardBottom = this.mTextMessageboard.getBottom();

        //計算に使用するサイズ値のどれか1つでも0pxであればメソッドを中断
        if(orgW * orgH * parentW * parentH * messageboardBottom == 0){
            return;
        }

        int targetW = Math.round(parentW / mCardViewArray.length * 0.75F);
        int targetH = Math.round((parentH-messageboardBottom) / 2 * 0.75F);

        float scaleW = (float)targetW / orgW;
        float scaleH = (float)targetH / orgH;
        float targetScale = Math.min(scaleW, scaleH);

        //規定サイズ以下にする場合のみサイズ変更
        if(targetScale < 1){
            for(Button c : mCardViewArray){
                ViewGroup.LayoutParams params = c.getLayoutParams();
                params.width = Math.round(params.width * targetScale);
                params.height = Math.round(params.height * targetScale);
                mCardParentLayout.updateViewLayout(c, params);
            }

            //答えのドロップ領域のサイズもカードに合わせて（点線の幅の半分大きく）調整
            ViewGroup.LayoutParams cardParams = card.getLayoutParams();
            int scaledCardW = cardParams.width;
            int scaledCardH = cardParams.height;
            float strokeWidth = this.getResources().getDisplayMetrics().density*3;
            for(AnswerAreaView a : mAnswerAreaViewArray){
                ViewGroup.LayoutParams params = a.getLayoutParams();
                params.width = Math.round(scaledCardW + strokeWidth);
                params.height = Math.round(scaledCardH + strokeWidth);
                mCardParentLayout.updateViewLayout(a, params);
            }
            mCardParentLayout.requestLayout();
        }
    }

    /**
     * 各カードに新しい文字をセットするメソッド
     * @param word
     */
    private void setTextToCards(ShuffleWord word){
        List<String> strList = word.getShuffledList();
        Iterator<String> strIte = strList.iterator();
        for(Button card : mCardViewArray){
            if(strIte.hasNext()){ card.setText(strIte.next()); }
        }
    }

    /**
     * ランダムな整数を返すメソッド
     * @param min       最小値
     * @param max   最大値
     * @return
     */
    private int getRandomInt(int min, int max){
        return min + mRandom.nextInt(max-min);
    }

    /**
     * 文字カードView用のランダムな初期回転角度を返すメソッド
     * @return
     */
    private int getRandomCardRotation(){
        return getRandomInt(-15, 15);
    }

    /**
     * 文字カードView用のランダムな初期X座標を返すメソッド
     * @param card
     * @return
     */
    private int getRandomCardX(View card){
        int length = mCardViewArray.length;
        int index = 0;
        for( ; index<length ; index++){
            if(mCardViewArray[index]==card){ break; }
        }
        float partWidth = (mCardParentLayout.getWidth()/(float)length);
        int partStart = Math.round(partWidth*index);
        int partEnd = partStart + Math.round(partWidth-card.getWidth());
        if(partEnd<=partStart){ partEnd = partStart+1; }
        int x = getRandomInt(partStart, partEnd);
        return x;
    }

    /**
     * 文字カードView用のランダムな初期Y座標を返すメソッド
     * @param card
     * @return
     */
    private int getRandomCardY(View card){
        int y = 0;
        int partHeight =
                mCardParentLayout.getHeight()
                - mAnswerAreaViewArray[0].getHeight()
                - mTextMessageboard.getBottom()
                - card.getHeight();
        if(partHeight>0){
            int partStart = partHeight/5 + mTextMessageboard.getBottom();
            int partEnd = partStart*2;
            y = getRandomInt(partStart, partEnd);
        }
        return y;
    }
}
