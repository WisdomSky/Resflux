package com.iwisdomsky.resflux.dialog;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.*;
import com.larswerkman.holocolorpicker.*;

import com.larswerkman.holocolorpicker.R;

public class SetColorDialog
{

	private final Context mContext;
	private final ColorPicker mColorPicker;
	private final Button mButton;
	private final Button mCButton;
	private AlertDialog mCPD;
	private OpacityBar mOpacityBar;
	private AlertDialog.Builder mDialog;
	private EditText et;
	public SetColorDialog(Context context){
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
		
		// Color Hex Value Text View
		et = new EditText(mContext);
		et.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		et.setFocusable(true);
		et.setGravity(Gravity.CENTER);
		et.setShadowLayer(0,0,0,0);
		et.setBackgroundColor(0x00000000);
		et.setOnKeyListener(new View.OnKeyListener(){
			public boolean onKey(View p1, int p2, KeyEvent p3)
			{
				return false;
			}
		});
		
		
		// Color Picker View
		mColorPicker = new ColorPicker(mContext);
		mColorPicker.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		mColorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener(){
			public void onColorChanged(int color)
			{
				et.setText("#"+((Integer.toHexString(color).equals("0"))?"00000001":Integer.toHexString(color)));
				et.setTextColor(Color.WHITE);
				
			}
		});
			
		// Opacity Bar View
		mOpacityBar = new OpacityBar(mContext);
		mOpacityBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		
		// Value Bar View
		ValueBar vb = new ValueBar(mContext);
		vb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		
		// Saturation Bar View
		SaturationBar sb = new SaturationBar(mContext);
		sb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		
		LinearLayout vll = new LinearLayout(mContext);
		vll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		vll.setOrientation(LinearLayout.HORIZONTAL);
		
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
		
				
		
		// Binding Bars
		mColorPicker.addValueBar(vb);
		mColorPicker.addOpacityBar(mOpacityBar);
		mColorPicker.addSaturationBar(sb);

		// Adding Views
		ll.addView(mColorPicker);	
		ll.addView(new CPTextView(mContext,"Saturation:"));	
		ll.addView(sb);
		ll.addView(new CPTextView(mContext,"Value:"));	
		ll.addView(vb);
		ll.addView(new CPTextView(mContext,"Opacity:"));
		ll.addView(mOpacityBar);	
		ll.addView(new CPTextView(mContext,"Color Hex Code:"));
		ll.addView(et);
		vll.addView(mButton);
		vll.addView(mCButton);
		ll.addView(vll);
		sv.addView(ll);
		
		// Set the view into the Dialog
		mDialog.setView(sv);
		
	}
	
	public SetColorDialog setTitle(String text){		
		mDialog.setTitle(text);		
		return this;
	}
	
	
	public SetColorDialog setColor(int color){
		mColorPicker.setNewCenterColor(color);
		mColorPicker.setOldCenterColor(color);
		mColorPicker.setColor(color);
		
		if ( color==0 ) {
			mColorPicker.setNewCenterColor(0x00000001);
			mColorPicker.setOldCenterColor(0x00000001);
			mColorPicker.setColor(0x00000001);
			mOpacityBar.setOpacity(0);
		}
		
		return this;
	} 
	
	public SetColorDialog setPositiveButton(CharSequence text,final SetColorDialog.OnClickListener listener){					
		mButton.setText(text);
		mButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View p1)
			{
				if ( et.getText().toString().matches("^#([0-9a-fA-F]{8}|[0-9a-fA-F]{6})$") 
					&& !et.getText().toString().matches("^#[0]+$")  ) {
					if ( listener!=null ) 
						listener.onClick(et.getEditableText().toString());
					mCPD.dismiss();
				} else 
					Toast.makeText(mContext,"Invalid color hex code format!",Toast.LENGTH_SHORT).show();
			}
		});
		mButton.setVisibility(View.VISIBLE);
		return this;		
	}


	public SetColorDialog setNegativeButton(CharSequence text,final SetColorDialog.OnClickListener listener){					
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
		public void onClick(String color); 
	}

	private static class CPTextView extends TextView{
		public CPTextView(Context context,String text){
			super(context);
			setText(text);
			setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		}
	}
	
}
