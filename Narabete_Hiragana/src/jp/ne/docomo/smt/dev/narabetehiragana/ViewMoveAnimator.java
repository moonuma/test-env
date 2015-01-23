/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class ViewMoveAnimator implements AnimatorUpdateListener {

    private View mTargetView;
    private ValueAnimator mXAnimator;
    private ValueAnimator mYAnimator;
    private ValueAnimator mRotateAnimator;

    private int mDuration = 500;

    /**
     * コンストラクタ
     */
    public ViewMoveAnimator(View targetView){
        this.mTargetView = targetView;

        //X・Y移動用Interpolator
        AccelerateInterpolator interpolator = new AccelerateInterpolator(0.19F);

        this.mXAnimator = ValueAnimator.ofFloat(0,0); //初期値は適当
        this.mXAnimator.addUpdateListener(this);
        this.mXAnimator.setDuration(mDuration);
        this.mXAnimator.setInterpolator(interpolator);

        this.mYAnimator = ValueAnimator.ofFloat(0,0);
        this.mYAnimator.addUpdateListener(this);
        this.mYAnimator.setDuration(mDuration);
        this.mYAnimator.setInterpolator(interpolator);

        this.mRotateAnimator = ValueAnimator.ofFloat(0, 0);
        this.mRotateAnimator.addUpdateListener(this);
        this.mRotateAnimator.setDuration(mDuration);
    }

    /**
     * アニメーションを開始するメソッド
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     */
    public void startAnimation(
            float fromX, float fromY, float fromRotation, float toX, float toY, float toRotation){
        stopAnimation();

        mXAnimator.setFloatValues(fromX, toX);
        mYAnimator.setFloatValues(fromY, toY);
        mRotateAnimator.setFloatValues(fromRotation, toRotation);

        mXAnimator.start();
        mYAnimator.start();
        mRotateAnimator.start();
    }

    /**
     * アニメーションを開始するメソッド
     * @param toX
     * @param toY
     * @param toRotation
     */
    public void startAnimation(float toX, float toY, float toRotation){
        startAnimation(
                mTargetView.getX(), mTargetView.getY(),
                mTargetView.getRotation(), toX, toY, toRotation);
    }

    /**
     * アニメーションをキャンセル（中断）するメソッド
     */
    public void stopAnimation(){
        mXAnimator.cancel();
        mYAnimator.cancel();
        mRotateAnimator.cancel();
    }

    /**
     * アニメーション実行中に呼ばれるメソッド。段階的に変化するアニメーション用の値が通知される
     * @param animation
     */
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if(mXAnimator==animation){
            mTargetView.setX((Float)animation.getAnimatedValue());

        }else if(mYAnimator==animation){
            mTargetView.setY((Float)animation.getAnimatedValue());

        }else if(mRotateAnimator==animation){
            mTargetView.setRotation((Float)animation.getAnimatedValue());
        }
        mTargetView.invalidate();
    }
}
