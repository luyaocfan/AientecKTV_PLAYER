package com.ine.ktv.ineplayer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.ine.ktv.playerengine.InePlayerController;

public abstract class OSDBase {
    static public class UpdateData{
        static public final int Type_Broadcast = 0;
        static public final int Type_PlayListChange = 1;
        static public final int Type_OrderSongFinish = 2;
        static public final int Type_Play = 0x20;
        static public final int Type_Pause = 0x21;
        static public final int Type_Stop = 0x22;
        static public final int Type_Next = 0x23;
        static public final int Type_Cut = 0x24;
        static public final int Type_Replay = 0x25;
        static public final int Type_LoadingError = 0x70;
        static public final int Type_PlayingError = 0x71;
        static public final int Type_AudioOriginal = 0x80;
        static public final int Type_AudioMusic = 0x81;
        public int type;
        String message = null;
        }
    protected ConstraintLayout parentLayout;
    protected static final Handler handler = new Handler(Looper.getMainLooper());
    protected DisplayMetrics metrics;
    public OSDBase(FrameLayout odsRoot, Context context) {
        ConstraintLayout parentLayout = new ConstraintLayout(context);
        parentLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        odsRoot.addView(parentLayout);

        metrics = context.getResources().getDisplayMetrics();
        this.parentLayout = parentLayout;
    }
    abstract public void onUpdate(UpdateData data);
}
