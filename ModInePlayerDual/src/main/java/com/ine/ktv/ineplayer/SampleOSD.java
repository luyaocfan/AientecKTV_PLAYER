package com.ine.ktv.ineplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class SampleOSD extends OSDBase{

    private ImageView leftUPImage;
    private OutlinedTextView rightUPText;
    private ImageView rightUPAlwaysOnImage;
    private OutlinedTextView rightUPAlwaysOnText;
    private LinearLayout centerLayout;
    private ImageView centerImage;
    private OutlinedTextView centerText;
    private Bitmap play_bmp, cut_bmp, replay_bmp, pause_bmp, original_bmp, musicOnly_bmp;
    private int centerStatus = 0;
    final int keepTime  = 2000;

    static public class UpdateData extends OSDBase.UpdateData {
        String centerString = null;
    }
    public SampleOSD(FrameLayout odsRoot, Context context) {
        super(odsRoot, context);

        play_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.play);
        pause_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.pause);
        cut_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.cut);
        replay_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.replay);
        original_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.original);
        musicOnly_bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_only);

        ConstraintLayout.LayoutParams layoutParams;
        Typeface t = Typeface.createFromAsset(context.getAssets(), "ine_tc.otf");

        leftUPImage=new ImageView(context);
        layoutParams = new ConstraintLayout.LayoutParams( 256,256);
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.setMargins(20,20,0,0);
        leftUPImage.setId(View.generateViewId());
        leftUPImage.setLayoutParams(layoutParams);
        leftUPImage.setVisibility(View.GONE);
        parentLayout.addView(leftUPImage);

        rightUPAlwaysOnImage = new ImageView(context);
        layoutParams = new ConstraintLayout.LayoutParams( 64,64);
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        layoutParams.setMargins(0,15,10,0);
        rightUPAlwaysOnImage.setId(View.generateViewId());
        rightUPAlwaysOnImage.setLayoutParams(layoutParams);
        rightUPAlwaysOnImage.setVisibility(View.GONE);
        parentLayout.addView(rightUPAlwaysOnImage);

        rightUPAlwaysOnText = new OutlinedTextView(context, false);
        rightUPAlwaysOnText.setTypeface(t);
        rightUPAlwaysOnText.setTextColor(Color.WHITE);
        rightUPAlwaysOnText.setOutlineTextColor(Color.BLUE);
        rightUPAlwaysOnText.setTextSize(30);
        rightUPAlwaysOnText.setId(View.generateViewId());

        layoutParams = new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        layoutParams.setMargins(0,10,90,0);  //hack for otf font

        rightUPAlwaysOnText.setLayoutParams(layoutParams);
        rightUPAlwaysOnText.setVisibility(View.GONE);
        parentLayout.addView(rightUPAlwaysOnText);

        rightUPText = new OutlinedTextView(context, false);
        rightUPText.setTypeface(t);
        rightUPText.setTextColor(0xff0020df);
        rightUPText.setOutlineTextColor(Color.WHITE);
        rightUPText.setTextSize(30);
        rightUPText.setId(View.generateViewId());

        layoutParams = new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        layoutParams.endToStart = rightUPAlwaysOnText.getId();
        layoutParams.setMarginEnd(10);

        rightUPText.setLayoutParams(layoutParams);
        rightUPText.setVisibility(View.GONE);
        parentLayout.addView(rightUPText);

        //layout_gravity="center_horizontal"
        centerLayout = new LinearLayout(context);
        centerLayout.setOrientation(LinearLayout.VERTICAL);

        centerImage = new ImageView(context);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams( 256,256);
        linearLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        centerImage.setLayoutParams(linearLayoutParams);
        centerLayout.addView(centerImage);

        centerText = new OutlinedTextView(context, false);
        centerText.setTypeface(t);
        centerText.setTextColor(Color.WHITE);
        centerText.setOutlineTextColor(Color.BLUE);
        centerText.setTextSize(100);
        centerText.setId(View.generateViewId());
        linearLayoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        centerText.setLayoutParams(linearLayoutParams);
        centerLayout.addView(centerText);

        layoutParams = new ConstraintLayout.LayoutParams( ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.topToTop = ConstraintSet.PARENT_ID;
        centerLayout.setLayoutParams(layoutParams);
        centerLayout.setVisibility(View.GONE);
        parentLayout.addView(centerLayout);

    }
    void ClearRightUPText(){
        rightUPText.setVisibility(View.GONE);
    }
    void ClearRightUPAlwaysOn(){
        rightUPAlwaysOnImage.setVisibility(View.GONE);
        rightUPAlwaysOnText.setVisibility(View.GONE);
    }
    void ClearCenter(){
        centerLayout.setVisibility(View.GONE);
    }
    @Override
    public void onUpdate(OSDBase.UpdateData updateData) {
        if(updateData instanceof SampleOSD.UpdateData) {
            SampleOSD.UpdateData data = (SampleOSD.UpdateData)updateData;
            switch (data.type) {
                case OSDBase.UpdateData.Type_Broadcast:
                case OSDBase.UpdateData.Type_PlayListChange:
                case OSDBase.UpdateData.Type_Stop:
                    break;
                case OSDBase.UpdateData.Type_Next:
                    rightUPText.setText("準備播放:"+data.message);
                    rightUPText.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{ClearRightUPText();}, keepTime);
                    if(data.centerString!=null) {
                        centerImage.setVisibility(View.GONE);
                        centerText.setText(data.centerString);
                        centerLayout.setVisibility(View.VISIBLE);
                        handler.postDelayed(()->{ClearCenter();}, keepTime);
                    }
                    break;
                case OSDBase.UpdateData.Type_OrderSongFinish:
                    centerImage.setVisibility(View.GONE);
                    centerText.setText("所有歌曲已播畢，請點歌!");
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{ClearCenter();}, keepTime);
                    break;
                case OSDBase.UpdateData.Type_LoadingError:
                    centerImage.setVisibility(View.GONE);
                    centerText.setText("載入錯誤:" + data.message);
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{ClearCenter();}, keepTime);
                    break;
                case OSDBase.UpdateData.Type_PlayingError:
                    centerImage.setVisibility(View.GONE);
                    centerText.setText(data.message);
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{ClearCenter();}, keepTime);
                    break;

                case OSDBase.UpdateData.Type_AudioOriginal:
                    leftUPImage.setImageBitmap(original_bmp);
                    leftUPImage.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{leftUPImage.setVisibility(View.GONE);}, keepTime);
                    break;
                case OSDBase.UpdateData.Type_AudioMusic:
                    leftUPImage.setImageBitmap(musicOnly_bmp);
                    leftUPImage.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{leftUPImage.setVisibility(View.GONE);}, keepTime);
                    break;
                case OSDBase.UpdateData.Type_Play:
                    centerStatus = OSDBase.UpdateData.Type_Play;
                    ClearRightUPAlwaysOn();
                    centerImage.setImageBitmap(play_bmp);
                    centerImage.setVisibility(View.VISIBLE);
                    centerText.setText(data.centerString);
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{
                        if(centerStatus == OSDBase.UpdateData.Type_Play)
                            ClearCenter();
                        }, keepTime);
                    break;
                case OSDBase.UpdateData.Type_Pause:
                    centerStatus = OSDBase.UpdateData.Type_Pause;
                    centerImage.setImageBitmap(pause_bmp);
                    centerImage.setVisibility(View.VISIBLE);
                    centerText.setText(data.centerString);
                    rightUPAlwaysOnText.setText(data.centerString);
                    rightUPAlwaysOnImage.setImageBitmap(pause_bmp);
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{
                        if(centerStatus == OSDBase.UpdateData.Type_Pause) {
                            ClearCenter();
                            rightUPAlwaysOnImage.setVisibility(View.VISIBLE);
                            rightUPAlwaysOnText.setVisibility(View.VISIBLE);
                        }
                    }, keepTime);
                    break;
                case OSDBase.UpdateData.Type_Cut:
                    centerStatus = OSDBase.UpdateData.Type_Cut;
                    ClearRightUPAlwaysOn();
                    centerImage.setImageBitmap(cut_bmp);
                    centerImage.setVisibility(View.VISIBLE);
                    centerText.setText(data.centerString);
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{
                        if(centerStatus == OSDBase.UpdateData.Type_Cut)
                            ClearCenter();
                        }, keepTime);
                    break;
                case OSDBase.UpdateData.Type_Replay:
                    centerStatus = OSDBase.UpdateData.Type_Replay;
                    ClearRightUPAlwaysOn();
                    centerImage.setImageBitmap(replay_bmp);
                    centerImage.setVisibility(View.VISIBLE);
                    centerText.setText(data.centerString);
                    centerLayout.setVisibility(View.VISIBLE);
                    handler.postDelayed(()->{
                        if(centerStatus == OSDBase.UpdateData.Type_Replay)
                            ClearCenter();
                    }, keepTime);
                    break;
            }
        }

    }


}
