/**
 * 
 */
package com.cchen.Timer;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author: cchen
 * @time: 2014-6-30 上午11:08:22
 */
public class AppListAdapter extends BaseAdapter {
	
	private Context context;
	private List<ApplicationInfo> list;
	
	 /**
	 * @author: cchen
	 * @time: 2014-6-30 上午11:08:29
	 */
	public AppListAdapter(Context context,List<ApplicationInfo> list) {
		this.context = context;
		this.list = list;
	}

	/**
	 * @author: cchen
	 * @time: 2014-6-30 上午11:08:22
	 * @return
	 */
	@Override
	public int getCount() {
		return list.size();
	}

	/**
	 * @author: cchen
	 * @time: 2014-6-30 上午11:08:22
	 * @param position
	 * @return
	 */
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	/**
	 * @author: cchen
	 * @time: 2014-6-30 上午11:08:22
	 * @param position
	 * @return
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * @author: cchen
	 * @time: 2014-6-30 上午11:08:22
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.app_list_item, null);
			holder.ivIcon = (ImageView) convertView.findViewById(R.id.ivAppIcon);
			holder.tvName = (TextView) convertView.findViewById(R.id.tvAppName);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		ApplicationInfo appInfo = list.get(position);
		holder.ivIcon.setImageDrawable(appInfo.loadIcon(context.getPackageManager()));
		holder.tvName.setText(appInfo.loadLabel(context.getPackageManager()));
		holder.tvName.setTextColor(Color.BLACK);
		
		return convertView;
	}

	static class ViewHolder{
		ImageView ivIcon;
		TextView tvName;
	}
}
