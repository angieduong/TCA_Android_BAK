package com.seta.tollroaddroid.app.custom;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.seta.tollroaddroid.app.R;

public class MyProgressDialog extends Dialog {
	private Context context = null;
	private static MyProgressDialog myProgressDialog = null;

	public MyProgressDialog(Context context){
		super(context);
		this.context = context;
	}
	
	public MyProgressDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }
	
	public static MyProgressDialog createDialog(Context mContext){
		myProgressDialog = new MyProgressDialog(mContext, R.style.CustomProgressDialog);
		myProgressDialog.setContentView(R.layout.dialog_custom_progress);

		myProgressDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		myProgressDialog.setCancelable(false);

		return myProgressDialog;
	}
	
	@Override
    protected void onStop() {
		super.onStop();
        if(myProgressDialog != null) {
            ImageView ivLoading = (ImageView) myProgressDialog.findViewById(R.id.iv_loading);
            ivLoading.clearAnimation();
            myProgressDialog = null;
        }

    }
	private static ImageView mLoadingLogo;
    public static MyProgressDialog show(Context mContext, CharSequence message) {
        createDialog(mContext);
        setMessage(message.toString());
        TextView tvMsg = (TextView)myProgressDialog.findViewById(R.id.id_loading_message);
        ImageView ivLoading = (ImageView)myProgressDialog.findViewById(R.id.iv_loading);

        tvMsg.setText(message);

        Animation a = AnimationUtils.loadAnimation(mContext, R.anim.progress_anim);
        a.setDuration(500);
//        a.setInterpolator(new Interpolator() {
//            private final int frameCount = 30;
//
//            @Override
//            public float getInterpolation(float input) {
//                return (float) Math.floor(input * frameCount) / frameCount;
//            }
//        });

        ivLoading.startAnimation(a);
        myProgressDialog.show();
        return myProgressDialog;
    }

    public static MyProgressDialog setMessage(String strMessage){
    	TextView tvMsg = (TextView)myProgressDialog.findViewById(R.id.id_loading_message);
    	
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    	
    	return myProgressDialog;
    }
}
