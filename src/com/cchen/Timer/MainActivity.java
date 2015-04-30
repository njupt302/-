package com.cchen.Timer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ab.util.AbViewUtil;
import com.ab.view.wheel.AbNumericWheelAdapter;
import com.ab.view.wheel.AbWheelAdapter;
import com.ab.view.wheel.AbWheelView;

public class MainActivity extends Activity implements OnClickListener,IFragment{
	
	private int mLeftTime;
	private ImageView mIvIcon;
	private TextView mTvAppName,mTvTime;
	private AbWheelView mWheelViewH,mWheelViewM;
	private Button mBtnPick,mBtnOk;
	private List<ApplicationInfo> dataList = new ArrayList<ApplicationInfo>();
	private List<ApplicationInfo> appList = new ArrayList<ApplicationInfo>();
	private Handler mHandler;
	private ApplicationInfo mSelectedApplicationInfo;
	private IKillApp mIKillApp;
	private ServiceConnection mCon = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mIKillApp = (IKillApp) service;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		initHandler();
		findViews();
		setListener();
		startService();
		bindService();
	}

	private void findViews(){
		mIvIcon = (ImageView) findViewById(R.id.ivIcon);
		mTvAppName = (TextView) findViewById(R.id.tvAppName);
		mTvTime = (TextView) findViewById(R.id.tvTime);
		mWheelViewH = (AbWheelView) findViewById(R.id.wheelViewH);
		mWheelViewM = (AbWheelView) findViewById(R.id.wheelViewM);
		mBtnPick = (Button) findViewById(R.id.btnPick);
		mBtnOk = (Button) findViewById(R.id.btnOk);
		initWheelView(mWheelViewH,"小时",new AbNumericWheelAdapter(0, 23),0);
		initWheelView(mWheelViewM,"分钟",new AbNumericWheelAdapter(0, 59),30);
	}
	
	private void initWheelView(AbWheelView wheelView,String label,AbWheelAdapter adapter,int currentItem){
		wheelView.setAdapter(adapter);
		// 可循环滚动
		wheelView.setCyclic(true);
		// 添加文字
		wheelView.setLabel(label);
		// 初始化时显示的数据
		wheelView.setCurrentItem(currentItem);
		wheelView.setValueTextSize(35);
		wheelView.setLabelTextSize(35);
		wheelView.setLabelTextColor(0x80000000);
		wheelView.setCenterSelectDrawable(this.getResources().getDrawable(R.drawable.wheel_select));
	}
	
	private void pickApp(){
		showAppList();
		refreshRunning();
		
	}

	private void refreshAll() {
		showProgressDialog();
		new Thread(new Runnable() {
			@Override
			public void run() {
				appList.clear();
				PackageManager pm = getPackageManager();
				List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
				for (ApplicationInfo applicationInfo : list) {
					if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) ==0
							&& (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0){
						appList.add(applicationInfo);
					}
				}
				mHandler.sendEmptyMessage(Constants.WHAT.INIT_APP_LIST);
			}
		}).start();
	}
	
	private void refreshRunning() {
		showProgressDialog();
		new Thread(new Runnable() {
			@Override
			public void run() {
				appList.clear();
				PackageManager pm = getPackageManager();
				List<ApplicationInfo> list = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
				ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningAppProcessInfo> runList = am.getRunningAppProcesses();
				for (ApplicationInfo applicationInfo : list) {
					if((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) ==0
							&& (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0){
						for (RunningAppProcessInfo runningAppProcessInfo : runList) {
							if(runningAppProcessInfo.processName.contains(applicationInfo.processName)){
								appList.add(applicationInfo);
								break;
							}
						}
					}
				}
				mHandler.sendEmptyMessage(Constants.WHAT.INIT_APP_LIST);
			}
		}).start();
	}
	
	private PopupWindow mPop;
	private AppListAdapter mAdapter;
	private Button btnAll,btnRunning;
	
	private void initAppList(){
		if(mPop == null){
			View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.app_list_pop, null);
			ListView lv = (ListView) view.findViewById(R.id.lvApps);
			mAdapter = new AppListAdapter(getApplicationContext(), dataList);
			lv.setAdapter(mAdapter);
			lv.setOnItemClickListener(new LvOnItemClickListener());
			ProgressBar bar = new ProgressBar(this);
			bar.setLayoutParams(new LayoutParams(100,100));
			lv.setEmptyView(bar);
			btnAll = (Button) view.findViewById(R.id.btnAll);
			btnRunning = (Button) view.findViewById(R.id.btnRunning);
			btnAll.setOnClickListener(MainActivity.this);
			btnRunning.setOnClickListener(MainActivity.this);
			btnRunning.setTextColor(Color.BLUE);
			mPop = new PopupWindow(view, AbViewUtil.dip2px(this, 320), LayoutParams.WRAP_CONTENT);
			mPop.setOutsideTouchable(true);
			mPop.setFocusable(true);
			mPop.setBackgroundDrawable(new BitmapDrawable());
			mPop.setAnimationStyle(R.style.PopupAnimation);
		}
	}
	
	private class LvOnItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			AppListAdapter adapter = (AppListAdapter) parent.getAdapter();
			mSelectedApplicationInfo = (ApplicationInfo) adapter.getItem(position);
			mTvAppName.setText(mSelectedApplicationInfo.loadLabel(getPackageManager()));
			mIvIcon.setImageDrawable(mSelectedApplicationInfo.loadIcon(getPackageManager()));
			hideAppList();
		}
		
	}
	
	private void showAppList(){
		initAppList();
		if(mPop != null){
			mPop.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
		}
	}
	
	private void hideAppList(){
		if(mPop != null){
			mPop.dismiss();
		}
	}
	
	private void setListener(){
		mBtnOk.setOnClickListener(this);
		mBtnPick.setOnClickListener(this);
	}
	
	private void initHandler(){
		mHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				if(msg.what == Constants.WHAT.INIT_APP_LIST){
					if(mAdapter != null){
						dataList.clear();
						dataList.addAll(appList);
						mAdapter.notifyDataSetChanged();
					}
					hideProgressDialog();
				}else if(msg.what == Constants.WHAT.NOTIFY_LEFT_TIME){
					mTvTime.setText(calcTime(mLeftTime--));
					if(mLeftTime<8){
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addCategory(Intent.CATEGORY_HOME);
						startActivity(intent);
					}
				}else if(msg.what == Constants.WHAT.CANCEL_TIMER){
					mTvTime.setText("00:00:00");
				}else if(msg.what == Constants.WHAT.TIMER_FINISHED){
					mTvTime.setText("00:00:00");
					mBtnOk.setTag("1");
					mBtnOk.setText("开始倒计时");
				}
			}
		};
	}
	
//	private class MyHandler extends Handler{
//		
//	}
	
	private String calcTime(int time){
		if(time<0){
			return "";
		}
		if(time > 60*60*24){
			time = time%(60*60*24);
		}
		String hS,mS,sS;
		int h = time/3600;
		if(h<10){
			hS = "0"+h;
		}else{
			hS = h+"";
		}
		int m = (time - h*3600)/60;
		if(m<10){
			mS = "0"+m;
		}else{
			mS = m+"";
		}
		int s = time%60;
		if(s<10){
			sS = "0"+s;
		}else{
			sS = s+"";
		}
		
		return hS+":"+mS+":"+sS;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			exit();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @author: cchen
	 * @time: 2014-6-30 上午10:26:22
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnPick:
				pickApp();
				break;
			case R.id.btnOk:
				if(mSelectedApplicationInfo == null){
					Toast.makeText(getApplicationContext(), "请先选择App", Toast.LENGTH_LONG).show();
					return ;
				}
				if(v.getTag() == null || "1".equals(v.getTag().toString())){
					String h = mWheelViewH.getAdapter().getItem(mWheelViewH.getCurrentItem());
					String m = mWheelViewM.getAdapter().getItem(mWheelViewM.getCurrentItem());
					int time = Integer.parseInt(h)*60 + Integer.parseInt(m);
					mLeftTime = time*60;
					mIKillApp.callkillApp(mSelectedApplicationInfo,time,mHandler);
					if(time > 0){
						mBtnOk.setTag("2");
						mBtnOk.setText("取消倒计时");
					}
				}else{
					mBtnOk.setTag("1");
					mBtnOk.setText("开始倒计时");
					mIKillApp.cancelkillApp(mHandler);
				}
				break;
			case R.id.btnAll:
				btnAll.setTextColor(Color.BLUE);
				btnRunning.setTextColor(Color.BLACK);
				refreshAll();
				break;
			case R.id.btnRunning:
				btnAll.setTextColor(Color.BLACK);
				btnRunning.setTextColor(Color.BLUE);
				refreshRunning();
				break;
			default:
				break;
		}
	}

	private void startService(){
		Intent service = new Intent(this, KillAppService.class);
		startService(service);
	}
	
	private void stopService(){
		Intent service = new Intent(this, KillAppService.class);
		stopService(service);
	}
	
	private void bindService(){
		Intent service = new Intent(this, KillAppService.class);
		bindService(service, mCon,BIND_AUTO_CREATE);
	}
	
	private void unBindService(){
		if(mCon!=null){
			unbindService(mCon);
		}
	}
	
	private void exit(){
		unBindService();
		stopService();
		android.os.Process.killProcess(Process.myPid());
	}

	private ProgressDialog mProgressDialog;
	private void showProgressDialog(){
		if(mProgressDialog == null){
			mProgressDialog = new ProgressDialog(this);
		}
		mProgressDialog.show();
	}
	
	private void hideProgressDialog(){
		if(mProgressDialog != null){
			mProgressDialog.dismiss();
		}
	}
	
	/**
	 * @author: cchen
	 * @time: 2014-7-1 上午10:45:00
	 * @param info
	 */
	@Override
	public void changeView(ApplicationInfo info) {
		
	}
	
}
