package com.iwisdomsky.resflux.dialog;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.iwisdomsky.resflux.Constants;

public class SetBooleanDialog
{

	private final Context mContext;
	private final Button mButton;
	private final Button mCButton;	
	private final Button mTButton;
	private final Button mFButton;	
	private AlertDialog mCPD;
	private AlertDialog.Builder mDialog;
	private boolean mValue;
	
	
	public SetBooleanDialog(Context context){
		this.mContext = context;
		this.mDialog = new AlertDialog.Builder(mContext);
		// Scroll View
		ScrollView sv = new ScrollView(mContext);
		sv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));

		// Vertical LinearLayout
		LinearLayout ll = new LinearLayout(mContext);
		ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setPadding(5,0,5,5);
		ll.setMinimumWidth(Constants.DIALOG_MIN_WIDTH);
		
		// true button
	 	mTButton = new Button(mContext);
		mTButton.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		mTButton.setGravity(Gravity.CENTER);
		mTButton.setText("True");	
		mTButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View p1)
				{
					setValue(true);
				}
		});
		// false button
	 	mFButton = new Button(mContext);
		mFButton.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		mFButton.setGravity(Gravity.CENTER);		
		mFButton.setText("False");
		mFButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View p1)
			{
				setValue(false);
			}
		});
		LinearLayout vll = new LinearLayout(mContext);
		vll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		vll.setOrientation(LinearLayout.HORIZONTAL);

		// Done Button
	 	mButton = new Button(mContext);
		mButton.setLayoutParams(new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		mButton.setVisibility(View.GONE);
		mButton.setGravity(Gravity.CENTER);		
	//	mButton.setBackgroundResource(R.drawable.button_2);

		// Cancel Button
		mCButton  = new Button(mContext);
		mCButton.setLayoutParams(new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		mCButton.setVisibility(View.GONE);
		mCButton.setGravity(Gravity.CENTER);		
		//mCButton.setBackgroundResource(R.drawable.button_1);




		// Adding Views
		ll.addView(new CPTextView(mContext,"Select new value:"));	
		ll.addView(mTButton);
		ll.addView(mFButton);
		vll.addView(mButton);
		vll.addView(mCButton);
		ll.addView(vll);
		//sv.addView(ll);
		setValue(false);
		// Set the view into the Dialog
		mDialog.setView(ll);

	}

	public SetBooleanDialog setTitle(String text){		
		mDialog.setTitle(text);		
		return this;
	}
	
	public SetBooleanDialog setValue(boolean value){
		this.mValue = value;
		if ( value ) {
			mTButton.setBackgroundColor(Color.parseColor("#ff444444"));
			mFButton.setBackgroundColor(Color.parseColor("#00000000"));
		} else {
			mTButton.setBackgroundColor(Color.parseColor("#00000000"));
			mFButton.setBackgroundColor(Color.parseColor("#ff444444"));
		}		
		return this;
	}
	
	

	public SetBooleanDialog setPositiveButton(CharSequence text,final SetBooleanDialog.OnClickListener listener){					
		mButton.setText(text);
		mButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View p1)
				{
					if ( listener!=null ) 
						listener.onClick(mValue);
					mCPD.dismiss();
				}
			});
		mButton.setVisibility(View.VISIBLE);
		return this;		
	}


	public SetBooleanDialog setNegativeButton(CharSequence text,final SetBooleanDialog.OnClickListener listener){					
		mCButton.setText(text);
		mCButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View p1)
				{
					if ( listener!=null )
						listener.onClick(false);
					mCPD.dismiss();
				}
			});
		mCButton.setVisibility(View.VISIBLE);
		return this;		
	}



	public void show(){
		mCPD = mDialog.create();
		mCPD.show();
	}


	public static interface OnClickListener {
		public void onClick(boolean bool); 
	}
	private static class CPTextView extends TextView{
		public CPTextView(Context context,String text){
			super(context);
			setText(text);
			setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		}
	}

}
