package fesb.papac.marin.augmented_reality_poi;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

/**
 * Created by Marin on 19.4.2017..
 */

/**
 * Here i have my settings for painting on canvas. I use .getResources().getDisplayMetrics().density
 * for text size because i want the text to be the same on every phone, no mater the resolution of the screen
 */
public class PaintUtils extends Paint {

    private TextPaint contentPaint;
    private TextPaint textPaint;
    private Paint targetPaint;
    private Paint roundRec;
    private Paint borderRec;

    public PaintUtils(ViewMain context) {
        super();

        // paint for text
        contentPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        contentPaint.setTextAlign(Align.LEFT);
        contentPaint.setTextSize(8 * context.getResources().getDisplayMetrics().density );
        contentPaint.setColor(Color.RED);

        // paint for target
        targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setTextSize(5 * context.getResources().getDisplayMetrics().density);
        targetPaint.setColor(Color.GREEN);


        // paint for text
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setTextSize(10 * context.getResources().getDisplayMetrics().density);
        textPaint.setColor(Color.BLACK);

        // paint for white rectangle
        roundRec = new Paint(Paint.ANTI_ALIAS_FLAG);
        roundRec.setColor(Color.WHITE);
        roundRec.setStrokeWidth(20);

        // paint for border of the rectangle
        int myColor = context.getResources().getColor(R.color.mygreen);
        borderRec = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderRec.setStrokeWidth(10);
        borderRec.setColor(myColor);
        borderRec.setStyle(Style.STROKE);
    }

    public TextPaint getContentPaint(){ return contentPaint;}
    public TextPaint getTextPaint() { return textPaint;}
    public Paint getTargetPaint() { return targetPaint;}
    public Paint getRoundRec() { return roundRec;}
    public Paint getBorderRec() { return borderRec;}

}
