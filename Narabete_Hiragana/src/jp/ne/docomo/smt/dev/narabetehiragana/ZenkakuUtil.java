/*
 * 2014 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.narabetehiragana;

import java.util.Locale;

public final class ZenkakuUtil {
    private ZenkakuUtil() { }

    /**
     * 数値を全角の文字列に変換して返すメソッド。
     * @param num
     * @param japanLocaleOnly     trueの場合は日本語環境の場合のみ全角、それ以外の環境では半角の文字列で返る
     * @return
     */
    public static String toZenkakuNumber(int num, boolean japanLocaleOnly){
        String str = String.valueOf(num);

        if(japanLocaleOnly){

            Locale defalut = Locale.getDefault();
            if(Locale.JAPAN.equals(defalut) || Locale.JAPANESE.equals(defalut)){
                str = toZenkakuNumber(num);
            }

        }else{
            str = toZenkakuNumber(num);
        }

        return str;
    }

    /**
     * 数値を全角の文字列に変換して返すメソッド。
     * こちらは基本的に外部から使用しない。
     * @param num
     * @return
     */
    private static String toZenkakuNumber(int num){
        String str = String.valueOf(num);
        StringBuilder sb = new StringBuilder();
        int strLength = str.length();
        for(int i=0 ; i<strLength ; i++){
            char c = str.charAt(i);
            if(c >= '0'  &&  c<='9'){
                sb.append((char)(c - '0' + '０'));
            }else{
                sb.append(c);
            }
        }//end of for
        return sb.toString();
    }
}
