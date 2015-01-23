/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jp.ne.docomo.smt.dev.aitalk.AiTalkTextToSpeech;
import jp.ne.docomo.smt.dev.aitalk.data.AiTalkSsml;
import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;


public class TextSpeaker {
    // APIキー(開発者ポータルから取得したAPIキーを設定)
    public static final String APIKEY = 開発者ポータルから取得したAPIキーを設定してください;

    //エラーを通知するリスナーインスタンス
    private OnErrorListener mErrorListener;

    //スレッドプール と 実行中スレッドの制御インスタンス
    private ExecutorService mExecutor;
    private Future<?> mFuture;

    //音声再生インスタンス と そのバッファーサイズ
    private AudioTrack mAudioTrack;
    private int mBufferSize;

    //音声の出力先の種類。AudioManagerの定数が入る。デフォルトは音楽
    private int mStreamType = AudioManager.STREAM_MUSIC;

    //音声合成APIで使用する発話者
    private VoiceType mVoiceType = VoiceType.AKARI;

    //音声合成APIで使用する韻律値
    private float mPitch=1.0F, mRange=1.0F, mRate=1.0F, mVolume=1.0F;

    /**
     * 音声の発話者を示す列挙形。
     */
    public enum VoiceType{
        /**女性ボイス。ただし読み上げが速い。*/
        NOZOMI("nozomi", "のぞみ"),

        /**40～50代 渋めの男性ボイス。*/
        SEIJI("seiji", "せいじ"),

        /**女性ボイス。nozomiより少し柔らかで落ち着いている。*/
        AKARI("akari", "あかり"),

        /**低年齢の女の子ボイス。読み上げはゆっくり目。*/
        ANZU("anzu", "あんず"),

        /**20~30代男性ボイス。少しけだるげ。*/
        HIROSHI("hiroshi", "ひろし"),

        /**若干年齢層が高めの女性ボイス。厳格な感じ。読み上げが速い。*/
        KAHO("kaho", "かほ"),

        /**低年齢の男の子？ボイス。読み上げはとてもゆっくり。*/
        KOUTAROU("koutarou", "こうたろう"),

        /**10代前半？の女の子ボイス。音が高く聞き取りやすい。少し突き放した感じ。*/
        MAKI("maki", "まき"),

        /**ATMから聞こえてきそうな定番女性ボイス。少し音が低め。*/
        NANAKO("nanako", "ななこ"),

        /**30~40代の軽めな男性ボイス。読み上げ速度は速い。*/
        OSAMU("osamu", "おさむ"),

        /**ささやくような女性ボイス。実は聞き取りやすい。*/
        SUMIRE("sumire", "すみれ");

        /** 音声合成APIにおける識別名*/
        public String name;
        /** 各音声の名前をひらがなにしたもの */
        public String hiraganaName;
        private VoiceType(String name, String hiraganaName){
            this.name = name;
            this.hiraganaName = hiraganaName;
        }
    }

    /**
     * 音声合成API・SDKにおける各種エラーを示す列挙形。
     */
    public enum Error{
                INVALID_PARAMETER("001-009-01"),
                AUTHENTICATION_ERROR("001-009-02"),
                SDK_INSIDE_ERROR("001-009-03"),
                SERVER_CONNECTION_ERROR("001-009-04"),
                SERVER_LIMITATION_ERROR("001-009-05"),
                SERVER_OTHER_ERROR("001-009-06"),
                RESPONSE_DATA_ERROR("001-009-07"),
                UNKNOWN_ERROR("");

        /** エラーコード */
        public String code;
        private Error(String code){
            this.code = code;
        }
    };

    /**
     * コンストラクタ
     */
    public TextSpeaker(Context context){
        super();

        //スレッドプール作成
        mExecutor = Executors.newSingleThreadExecutor();

        //音声を再生するAudioTrackインスタンス生成
        mBufferSize = AudioTrack.getMinBufferSize(
                16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack =
                new AudioTrack(
                        mStreamType,
                        16000,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        mBufferSize,
                        AudioTrack.MODE_STREAM );
        mAudioTrack.play();//再生待機状態にしておく
    }

    /**
     * エラーの通知先となるリスナーインスタンスをセットするメソッド
     */
    public void setOnErrorListener(OnErrorListener listener){
        this.mErrorListener = listener;
    }

    /**
     * エラーをリスナークラスへ通知するメソッド
     */
    private void notifyError(Exception exception, String errorCode){
        if(mErrorListener != null){
            mErrorListener.onSpeekError(exception, errorCode);
        }
    }

    /**
     * 発話者の種類をセットするメソッド
     * @param type
     */
    public void setVoiceType(VoiceType type){
        this.mVoiceType = type;
    }

    /**
     * 現在の発話者の種類を返すメソッド
     * @return
     */
    public VoiceType getVoiceType(){
        return mVoiceType;
    }

    /**
     * 引数 name（APIでの識別名） に該当するVoiceType（Enum）を返すメソッド。存在しない場合は null を返す
     * @param name
     * @return
     */
    public static VoiceType findVoiceTypeEnum(String name){
        for(VoiceType v : VoiceType.values()){
            if(v.name.equals(name)){
                return v;
            }
        }
        return null;
    }

    /**
     * テキストを読み上げるベースライン・ピッチをセットするメソッド
     * @param pitch    基準値は1.0 で、0.50～2.00 の範囲で指定できる(小数点第二位まで指定可能)
     */
    public void setPitch(float pitch){
        mPitch = pitch;
    }

    /**
     * 現在の音声のベースライン・ピッチを返すメソッド
     * @return
     */
    public float getPitch(){
        return mPitch;
    }

    /**
     * テキストを読み上げるピッチ・レンジをセットするメソッド
     * @param range 基準値は1.0 で、0.00～2.00の範囲で指定できる(小数点第二位まで指定可能)
     */
    public void setRange(float range){
        this.mRange = range;
    }

    /**
     * テキストを読み上げるピッチ･レンジを返すメソッド
     * @return
     */
    public float getRange(){
        return this.mRange;
    }

    /**
     * テキストを読み上げる速度をセットするメソッド
     * @param rate 基準値は1.0 で、0.50～4.00 の範囲で指定できる(小数点第二位まで指定可能)
     */
    public void setRate(float rate){
        this.mRate = rate;
    }

    /**
     * テキストを読み上げる速度を返すメソッド
     * @return
     */
    public float getRate(){
        return this.mRate;
    }

    /**
     * テキストを読み上げる音量をセットするメソッド。
     * 音声合成APIの値であって、端末の音量値ではない。
     * @param volume 基準値は1.0 で、0.00～2.00 の範囲で指定できる(小数点第二位まで指定可能)
     */
    public void setVolume(float volume){
        this.mVolume = volume;
    }

    /**
     * テキストを読み上げる音量を返すメソッド。
     * 音声合成APIの値であって、端末の音量値ではない。
     * @return
     */
    public float getVolume(){
        return this.mVolume;
    }

    /**
     * 文字列のエラーコードに対応するエラーenumを返すメソッド。該当するものが無ければ UNKNOWN_ERROR を返す
     * @param errorCode
     * @return Error
     */
    public static Error findErrorEnum(String errorCode){
        Error returnValue = null;
        for(Error e : Error.values()){
            if(e.code.equals(errorCode)){
                returnValue = e;
                break;
            }
        }
        return returnValue==null  ?  Error.UNKNOWN_ERROR  :  returnValue;
    }

    /**
     * 現在の音声出力先の種類を返すメソッド。
     * 戻り値はAudioManagerの定数
     * @return
     */
    public int getStreamType(){
        return this.mStreamType;
    }

    /**
     * 音声の出力先の種類をセットするメソッド。
     * @param streamType  AudioManagerの定数
     */
    public void setStreamType(int streamType){
        this.mStreamType = streamType;
    }

    /**
     * 音声の再生を行うメソッド
     */
    public void speak(final String text, final long delay){
        //ネットワーク接続が必要なため別スレッドで実行
        if(mFuture != null){ mFuture.cancel(true) ; }
        mFuture = mExecutor.submit(new Runnable(){
            public void run(){
                //音声合成APIキーの認証
                AuthApiKey.initializeAuth(APIKEY);

                //SSMLテキストの作成
                AiTalkSsml ssml = new AiTalkSsml();
                ssml.startVoice(mVoiceType.name);
                ssml.startProsody(mPitch, mRange, mRate, mVolume);    //引数はおそらく（ピッチ, 抑揚, 速度, 音量）
                ssml.addText(text);
                ssml.endProsody();
                ssml.endVoice();

                try {
                    if(Thread.interrupted()){ throw new InterruptedException(); }

                    //音声の取得
                    AiTalkTextToSpeech tts = new AiTalkTextToSpeech();
                    byte[] resultData = tts.requestAiTalkSsmlToSound(ssml.makeSsml());
                    tts.convertByteOrder16(resultData);

                    //待機時間が指定されていれば一時停止
                    if(delay>0){    Thread.sleep(delay); }

                    if(Thread.interrupted()){ throw new InterruptedException(); }

                    /*    取得した音声データが、AudioTrackの最小バッファーサイズ未満であれば、無音部分を追加して補填。
                     * AudioTrackはwriteされたデータ量が最小バッファーサイズに達するまで再生が開始されないため。*/
                    if(resultData.length < mBufferSize){
                        byte[] src = resultData;
                        resultData = new byte[mBufferSize];
                        System.arraycopy(src, 0, resultData, 0, src.length);
                    }

                    //音声の再生
                    mAudioTrack.write(resultData, 0, resultData.length);
                } catch (SdkException e) {
                    notifyError(e, e.getErrorCode());
                } catch (ServerException e) {
                    notifyError(e, e.getErrorCode());
                } catch (InterruptedException e) {
                    // TODO エラー処理を書く
                }
            }
        });
    }

    /**
     * このクラスインスタンスを破棄する際に呼ぶべきメソッド
     * スレッドプールや音声再生インスタンスの終末処理を行う
     */
    public void dispose(){
        this.mExecutor.shutdownNow();
        mAudioTrack.stop();
        mAudioTrack.release();
    }

    //このクラスのエラーを通知するリスナーインターフェース
    public interface OnErrorListener{
        void onSpeekError(Exception exception, String errorCode);
    }

}
