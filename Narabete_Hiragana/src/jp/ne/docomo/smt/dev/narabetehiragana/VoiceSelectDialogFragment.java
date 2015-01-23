/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import jp.ne.docomo.smt.dev.narabetehiragana.TextSpeaker.VoiceType;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class VoiceSelectDialogFragment extends DialogFragment {

    private static final String ARG_KEY_CURRENT_TYPE = "current_type";

    public static VoiceSelectDialogFragment getInstance(TextSpeaker.VoiceType currentType){
        VoiceSelectDialogFragment fragment = new VoiceSelectDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_KEY_CURRENT_TYPE, currentType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.koe_wo_kaeru);

        //現在の話者を取得
        TextSpeaker.VoiceType currentType =
                (VoiceType) getArguments().getSerializable(ARG_KEY_CURRENT_TYPE);

        final TextSpeaker.VoiceType[] voiceTypes = TextSpeaker.VoiceType.values();
        int length = voiceTypes.length;
        int checkedItem = 0;    //初期状態で選択されるアイテムのIndex
        final String[] items = new String[length];
        for(int i=0 ; i<length ; i++){
            TextSpeaker.VoiceType v = voiceTypes[i];
            items[i] = v.hiraganaName;
            if(v==currentType){ checkedItem = i; }
        }

        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MainActivity)getActivity()).setVoiceType(voiceTypes[which]);
                dismissAllowingStateLoss();
            }
        });

        return builder.create();
    }

}
