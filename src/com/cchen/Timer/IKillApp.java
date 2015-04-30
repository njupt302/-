/**
 * 
 */
package com.cchen.Timer;

import android.content.pm.ApplicationInfo;
import android.os.Handler;

/**
 * @author: cchen
 * @time: 2014-6-30 下午1:28:24
 */
public interface IKillApp {

	/**
	 * 定时关闭app
	 * @author: cchen
	 * @time: 2014-6-30 下午2:06:55
	 * @param info 需要关闭的app
	 * @param time 多少分钟后关闭app
	 * @param handler 回调的handler msg.what = {@link com.cchen.Timer.Constants.WHAT#NOTIFY_LEFT_TIME} 
	 */
	public void callkillApp(ApplicationInfo info,int time,Handler handler);
	
	/**
	 * 取消倒计时
	 * @author: cchen
	 * @time: 2014-6-30 下午5:27:36 
	 * @param handler 回调的handler msg.what = {@link com.cchen.Timer.Constants.WHAT#CANCEL_TIMER} 
	 */
	public void cancelkillApp(Handler handler);
}
