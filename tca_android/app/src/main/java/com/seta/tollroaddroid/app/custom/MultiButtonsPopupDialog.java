package com.seta.tollroaddroid.app.custom;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.seta.tollroaddroid.app.R;


public class MultiButtonsPopupDialog extends Dialog {
	private Context context = null;
	private  MultiButtonsPopupDialog gMultiButtonsPopupDialog = null;
	TextView mTitle = null;
	TextView mContent = null;
	Button gPositiveButton = null;
	Button gNeutralButton  = null;
	Button gNegativeButton  = null;
	Button gExtraButton  = null;
	View.OnClickListener gPositiveButtonOnClickListener = null;
	View.OnClickListener gNeutralButtonOnClickListener = null;
	View.OnClickListener gNegativeButtonOnClickListener  = null;
	View.OnClickListener gExtraButtonOnClickListener  = null;
	
	public MultiButtonsPopupDialog(Context context){
		super(context);
		this.context = context;
	}
	
	public MultiButtonsPopupDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
        gMultiButtonsPopupDialog = this;
    }
	
    @Override
    public void dismiss() {
    	if(gMultiButtonsPopupDialog != null)
    	{
    		Button mExtraButton  = (Button) gMultiButtonsPopupDialog.findViewById(R.id.btn_extra);
			TextView mExtraButtonPadding  = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.btn_extra_padding);
			
			mExtraButton.setVisibility(View.GONE);
			mExtraButtonPadding.setVisibility(View.GONE);
    	}
    	super.dismiss();
    }
    
	Context getCurContext()
	{
		return this.context;
	}
	
	public  MultiButtonsPopupDialog createDialog(Context context){
		//gMultiButtonsPopupDialog = new MultiButtonsPopupDialog(context,R.style.CustomProgressDialog);
		gMultiButtonsPopupDialog.setContentView(R.layout.dialog_multi_buttons);
		gMultiButtonsPopupDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		gMultiButtonsPopupDialog.setCancelable(false);
		return gMultiButtonsPopupDialog;
	}
	public void setBackground(int color, Drawable background)
	{
		if(gMultiButtonsPopupDialog != null)
		{
			RelativeLayout mPopupLayout = (RelativeLayout) gMultiButtonsPopupDialog.findViewById(R.id.popup_layout);
			if(background != null)
			{
				mPopupLayout.setBackground(background);
			}
			else
			{
				mPopupLayout.setBackgroundColor(color);
			}
		}
	}
	
	public void setContentBackground(int color, Drawable background)
	{
		if(gMultiButtonsPopupDialog != null)
		{
			LinearLayout mPopupContentLayout = (LinearLayout) gMultiButtonsPopupDialog.findViewById(R.id.popup_content_layout);
			if(background != null)
			{
				mPopupContentLayout.setBackground(background);
			}
			else
			{
				mPopupContentLayout.setBackgroundColor(color);
			}
		}
	}
	
	public void CutomizePositiveButton(int visibility, int resid, CharSequence text, View.OnClickListener mOnClickListener)
	{
		if(gMultiButtonsPopupDialog != null)
		{
			gPositiveButton  = (Button) gMultiButtonsPopupDialog.findViewById(R.id.btn_positive);
			TextView gPositiveButtonPadding  = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.btn_positive_padding);
			gPositiveButton.setVisibility(visibility);
			gPositiveButtonPadding.setVisibility(visibility);
			
			if(resid != 0)
			{
				gPositiveButton.setBackgroundResource(resid);
			}
			
			gPositiveButton.setText(text);
	
			gPositiveButtonOnClickListener = mOnClickListener;
			gPositiveButton.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Log.e("MultiButtonsPopupDialog", "gPositiveButton event.getAction():" + event.getAction());
					
					if(event.getAction() == MotionEvent.ACTION_UP)
					{
						if(gPositiveButtonOnClickListener != null)
						{
							gPositiveButtonOnClickListener.onClick(v);
						}
						gMultiButtonsPopupDialog.dismiss();
					}

					return true;
				}			
			});			
		}
	}

	public void CutomizeNegativeButton(int visibility, int resid, CharSequence text, View.OnClickListener mOnClickListener)
	{		
		if(gMultiButtonsPopupDialog != null)
		{		
			gNegativeButton  = (Button) gMultiButtonsPopupDialog.findViewById(R.id.btn_negative);
			gNegativeButton.setVisibility(visibility);
			if(resid != 0)
			{
				gNegativeButton.setBackgroundResource(resid);
			}
			
			gNegativeButton.setText(text);
	
			gNegativeButtonOnClickListener = mOnClickListener;
			gNegativeButton.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Log.e("MultiButtonsPopupDialog", "gNegativeButton event.getAction():" + event.getAction());
					
					if(event.getAction() == MotionEvent.ACTION_UP)
					{
						if(gNegativeButtonOnClickListener != null)
						{
							gNegativeButtonOnClickListener.onClick(v);
						}
						gMultiButtonsPopupDialog.dismiss();
					}

	
					return true;
				}			
			});		
		}
	}	

	public void CutomizeNeutralButton(int visibility, int resid, CharSequence text, View.OnClickListener mOnClickListener)
	{
		if(gMultiButtonsPopupDialog != null)
		{		
			gNeutralButton  = (Button) gMultiButtonsPopupDialog.findViewById(R.id.btn_neutral);
			TextView gNeutralButtonPadding  = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.btn_neutral_padding);
			gNeutralButtonPadding.setVisibility(visibility);
			gNeutralButton.setVisibility(visibility);
			if(resid != 0)
			{
				gNeutralButton.setBackgroundResource(resid);
			}
			
			gNeutralButton.setText(text);
	
			gNeutralButtonOnClickListener = mOnClickListener;
			gNeutralButton.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Log.e("MultiButtonsPopupDialog", "gNeutralButton event.getAction():" + event.getAction());
					
					if(event.getAction() == MotionEvent.ACTION_UP)
					{
						if(gNeutralButtonOnClickListener != null)
						{
							gNeutralButtonOnClickListener.onClick(v);
						}
						gMultiButtonsPopupDialog.dismiss();
					}
	
					return true;
				}			
			});	
		}
	}
	
	public void CutomizeExtraButton(int visibility, int resid, CharSequence text, int textColor, View.OnClickListener mOnClickListener)
	{
		if(gMultiButtonsPopupDialog != null)
		{		
			gExtraButton  = (Button) gMultiButtonsPopupDialog.findViewById(R.id.btn_extra);
			TextView mExtraButtonPadding  = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.btn_extra_padding);
			mExtraButtonPadding.setVisibility(visibility);
			gExtraButton.setVisibility(visibility);
			if(resid != 0)
			{
				gExtraButton.setBackgroundResource(resid);
			}

			gExtraButton.setTextColor(textColor);
            gExtraButton.setText(text);
	
			gExtraButtonOnClickListener = mOnClickListener;
			gExtraButton.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Log.e("MultiButtonsPopupDialog", "gExtraButton event.getAction():" + event.getAction());
					
					if(event.getAction() == MotionEvent.ACTION_UP)
					{
						if(gExtraButtonOnClickListener != null)
						{
							gExtraButtonOnClickListener.onClick(v);
						}
						gMultiButtonsPopupDialog.dismiss();
					}
	
					return true;
				}			
			});	
		}
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && gMultiButtonsPopupDialog != null) {
        	gMultiButtonsPopupDialog.dismiss();
        	gMultiButtonsPopupDialog = null;
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
	public void CutomizeTitle(int visibility, CharSequence text)
	{
		if(gMultiButtonsPopupDialog != null)
		{		
			mTitle = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.popup_title);
			if(text != null)
			{
				mTitle.setText(text);
			}
			mTitle.setVisibility(visibility);
		}
	}	
	
	public void CutomizeContent(int visibility, CharSequence text)
	{
		if(gMultiButtonsPopupDialog != null)
		{
			mContent = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.popup_content);
			if(text != null)
			{
				mContent.setText(text);
			}
			mContent.setVisibility(visibility);
		}
	}

	public void CutomizeContent(int visibility, CharSequence text, int textColor, int gravity)
	{
		if(gMultiButtonsPopupDialog != null)
		{
			mContent = (TextView) gMultiButtonsPopupDialog.findViewById(R.id.popup_content);
			if(text != null)
			{
				mContent.setText(text);
			}
			mContent.setTextColor(textColor);
			mContent.setVisibility(visibility);
			mContent.setGravity(gravity);
		}
	}

    public void onWindowFocusChanged(boolean hasFocus){    	
    	if (gMultiButtonsPopupDialog == null){
    		return;
    	}
    }

    public MultiButtonsPopupDialog setTitle(String strTitle){
    	return gMultiButtonsPopupDialog;
    }
    

    public  MultiButtonsPopupDialog setMessage(String strMessage){
    	TextView tvMsg = (TextView)gMultiButtonsPopupDialog.findViewById(R.id.id_loading_message);
    	
    	if (tvMsg != null){
    		tvMsg.setText(strMessage);
    	}
    	
    	return gMultiButtonsPopupDialog;
    }
}
