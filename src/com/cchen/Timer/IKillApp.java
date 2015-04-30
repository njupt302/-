/**
 * 
 */
package com.cchen.Timer;

import android.content.pm.ApplicationInfo;
import android.os.Handler;

/**
 * @author: cchen
 * @time: 2014-6-30 ����1:28:24
 */
public interface IKillApp {

	/**
	 * ��ʱ�ر�app
	 * @author: cchen
	 * @time: 2014-6-30 ����2:06:55
	 * @param info ��Ҫ�رյ�app
	 * @param time ���ٷ��Ӻ�ر�app
	 * @param handler �ص���handler msg.what = {@link com.cchen.Timer.Constants.WHAT#NOTIFY_LEFT_TIME} 
	 */
	public void callkillApp(ApplicationInfo info,int time,Handler handler);
	
	/**
	 * ȡ������ʱ
	 * @author: cchen
	 * @time: 2014-6-30 ����5:27:36 
	 * @param handler �ص���handler msg.what = {@link com.cchen.Timer.Constants.WHAT#CANCEL_TIMER} 
	 */
	public void cancelkillApp(Handler handler);
}
