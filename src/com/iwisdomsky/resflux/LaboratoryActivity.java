package com.iwisdomsky.resflux;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.iwisdomsky.resflux.adapter.*;
import java.util.*;



public class LaboratoryActivity extends Activity
{
    

	private ProgressBar mProgress;
	private ListView mListView;
	private ArrayList<String> mPackagesList;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laboratory);
		mProgress = (ProgressBar)findViewById(R.id.loading);
		mListView = (ListView)findViewById(R.id.packagelist);		
		mListView.setFastScrollEnabled(true);
		new Thread(loadPackagesList()).start();
				
	}
	
	
	private Runnable loadPackagesList(){
		return new Runnable(){
			@Override
			public void run(){				
				PackageManager pm = getPackageManager();
				List<ApplicationInfo> apps = pm.getInstalledApplications(0);
				Collections.sort(apps,new ApplicationInfo.DisplayNameComparator(pm));
				mPackagesList = new ArrayList<String>();
				for ( int i=0; i<apps.size(); i++ ) {
					mPackagesList.add(apps.get(i).packageName);
				}				
				runOnUiThread(new Runnable(){
					public void run(){		
						loadPackagesCallback();
					}
				});
			}	
		};	
	}


	private void loadPackagesCallback(){
		mProgress.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		PackageListAdapter adapter = new PackageListAdapter(LaboratoryActivity.this,mPackagesList);		
		mListView.setAdapter(adapter);	
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
			{				
				Intent experiment = new Intent(LaboratoryActivity.this,ExperimentActivity.class);
				experiment.putExtra("package_name",mPackagesList.get(p3));
				startActivity(experiment);
			}
		});
	}


	
}
