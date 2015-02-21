package com.iwisdomsky.resflux.dialog;


import android.app.*;
import android.content.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;

public class SetTextDialog
{

	private final Context mContext;
	private final Button mButton;
	private final Button mCButton;
	private AlertDialog mCPD;
	private AlertDialog.Builder mDialog;
	private EditText mEditText;
	
	public SetTextDialog(Context context){
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

		
		
		LinearLayout vll = new LinearLayout(mContext);
		vll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		vll.setOrientation(LinearLayout.HORIZONTAL);
	
		mEditText = new EditText(mContext);
		mEditText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		//mEditText.setBackgroundResource(R.drawable.button_4);
		mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		mEditText.setTextColor(0xFFFFFFFF);
		mEditText.setSingleLine(true);
		
		// Done Button
	 	mButton = new Button(mContext);
		mButton.setLayoutParams(new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		mButton.setVisibility(View.GONE);
		mButton.setGravity(Gravity.CENTER);		
		//mButton.setBackgroundResource(R.drawable.button_2);
		
		// Cancel Button
		mCButton  = new Button(mContext);
		mCButton.setLayoutParams(new TableLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		mCButton.setVisibility(View.GONE);
		mCButton.setGravity(Gravity.CENTER);		
		//mCButton.setBackgroundResource(R.drawable.button_1);
		
				
		

		// Adding Views
		ll.addView(new CPTextView(mContext,"New value:"));	
		ll.addView(mEditText);
		vll.addView(mButton);
		vll.addView(mCButton);
		ll.addView(vll);
		//sv.addView(ll);
		
		// Set the view into the Dialog
		mDialog.setView(ll);
		
	}
	
	public SetTextDialog setTitle(String text){		
		mDialog.setTitle(Html.fromHtml("<font size=\"5\">"+text+"</font>"));		
		return this;
	}
	
	public SetTextDialog setIntegerInputType(boolean bool){
		if ( bool )
			mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
		return this;
	}
	
	public SetTextDialog setText(String text){
		mEditText.setText(text);
		mEditText.setSelection(text.length());
		return this;
	}
	
	public SetTextDialog setPositiveButton(CharSequence text,final SetTextDialog.OnClickListener listener){					
		mButton.setText(text);
		mButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View p1)
			{
				if ( listener!=null ) 
					listener.onClick(mEditText.getText().toString());
				mCPD.dismiss();
			}
		});
		mButton.setVisibility(View.VISIBLE);
		return this;		
	}


	public SetTextDialog setNegativeButton(CharSequence text,final SetTextDialog.OnClickListener listener){					
		mCButton.setText(text);
		mCButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View p1)
				{
					if ( listener!=null )
						listener.onClick(null);
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
		public void onClick(String text); 
	}
	private static class CPTextView extends TextView{
		public CPTextView(Context context,String text){
			super(context);
			setText(text);
			setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		}
	}
	
}
