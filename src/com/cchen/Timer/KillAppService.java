/**
 * 
 */
package com.cchen.Timer;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

/**
 * @author: cchen
 * @time: 2014-6-30 下午1:24:55
 */
public class KillAppService extends Service {
	
	private static Timer mTimer,mTimer1;
	private static TimerTask mTimerTask,mTimerTask1;
	private static int count = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return new KillApp();
	}
	
	@Override
	public void onCreate() {
		System.out.println("=======onCreate===========");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("======onStartCommand============");
		count = 0;
		return START_STICKY;//保证不被杀死
	}
	
	@Override
	public void onDestroy() {
		System.out.println("=======onDestroy===========");
		super.onDestroy();
	}
	
	@Override
	public void onRebind(Intent intent) {
		System.out.println("=======onRebind===========");
		super.onRebind(intent);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		System.out.println("=======onUnbind===========");
		return super.onUnbind(intent);
	}
	
	private void killApp(final ApplicationInfo info,int time,final Handler handler){
		if(time != 0){
			if(handler != null){
				//用来更改前台时间
				mTimerTask = new TimerTask() {
					
					@Override
					public void run() {
						handler.sendEmptyMessage(Constants.WHAT.NOTIFY_LEFT_TIME);
					}
				};
				getTimer().schedule(mTimerTask,0,1000);
			}
			//用来计时
			mTimerTask1 = new TimerTask() {
				
				@Override
				public void run() {
					killApp(info);
					mTimer.cancel();
					mTimer = null;
					mTimer1 = null;
					handler.sendEmptyMessage(Constants.WHAT.TIMER_FINISHED);
				}
			};
			getTimer1().schedule(mTimerTask1, time*60*1000);
			
		}else{
			killApp(info);
		}
	}
	
	private void killApp(ApplicationInfo info){
		if(info == null){
			return;
		}
		System.out.println("开始杀进程========");
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runingAppList = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : runingAppList) {
			if(runningAppProcessInfo.processName.contains(info.processName)){
				System.out.println("===kill==="+runningAppProcessInfo.processName);
				am.killBackgroundProcesses(info.packageName);
			}
		}
		count++;
		if(count>10){
			count = 0;//预防死循环
			return;
		}
		if(checkProcess(info.packageName)){
			killApp(info);
		}
		System.out.println("杀进程结束========");
	}
	
	private boolean checkProcess(String processName){
		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runingAppList = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningAppProcessInfo : runingAppList) {
			if(runningAppProcessInfo.processName.contains(processName)){
				return true;
			}
		}
		return false;
	}
	
	private class KillApp extends Binder implements IKillApp{

		@Override
		public void callkillApp(ApplicationInfo info,int time,Handler handler) {
			count = 0;
			killApp(info,time,handler);
		}

		@Override
		public void cancelkillApp(Handler handler) {
			count = 0;
			getTimer().cancel();
			getTimer1().cancel();
			mTimer = null;
			mTimer1 = null;
			handler.sendEmptyMessage(Constants.WHAT.CANCEL_TIMER);
		}
		
	}
	
	private Timer getTimer1(){
		if(mTimer1 == null){
			mTimer1 = new Timer();
		}
		return mTimer1;
	}
	
	private Timer getTimer(){
		if(mTimer == null){
			mTimer = new Timer();
		}
		return mTimer;
	}
	
}
