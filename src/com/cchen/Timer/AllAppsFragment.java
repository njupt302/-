/**
 * 
 */
package com.cchen.Timer;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ab.view.pullview.AbPullToRefreshView;
import com.ab.view.pullview.AbPullToRefreshView.OnHeaderRefreshListener;

/**
 * @author: cchen
 * @time: 2014-7-1 上午10:10:39
 */
public class AllAppsFragment extends Fragment {

	private AbPullToRefreshView mAbPullToRefreshView = null;
	private ListView mListView = null;
	private List<ApplicationInfo> dataList;
	private AppListAdapter mAdapter;
	private IFragment mIFragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataList = new ArrayList<ApplicationInfo>();
		mAdapter = new AppListAdapter(this.getActivity().getApplicationContext(), dataList);
		mIFragment = (IFragment) getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pull_to_refresh_list, null);
		findViews(view);
		setListener();
		initViews();
		return view;
	}
	
	/**
	 * @author: cchen
	 * @time: 2014-7-1 上午10:47:13
	 */
	@Override
	public void onStart() {
		refresh();
		super.onStart();
	}
	
	private void findViews(View view){
		 //获取ListView对象
        mAbPullToRefreshView = (AbPullToRefreshView)view.findViewById(R.id.mPullRefreshView);
        mListView = (ListView)view.findViewById(R.id.mListView);
	}
	
	private void setListener(){
		mAbPullToRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {
			
			@Override
			public void onHeaderRefresh(AbPullToRefreshView view) {
				refresh();
			}
		});
	}
	
	private void initViews(){
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new LvOnItemClickListener());
        //设置进度条的样式
        mAbPullToRefreshView.getHeaderView().setHeaderProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
        mAbPullToRefreshView.getFooterView().setFooterProgressBarDrawable(this.getResources().getDrawable(R.drawable.progress_circular));
	}
	
	private void refresh(){
		dataList.clear();
		PackageManager pm = getActivity().getPackageManager();
		List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		dataList.addAll(list);
		mAdapter.notifyDataSetChanged();
	}
	
	private class LvOnItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			AppListAdapter adapter = (AppListAdapter) parent.getAdapter();
			mIFragment.changeView((ApplicationInfo) adapter.getItem(position));
		}
		
	}
}
