package com.dpower.cintercom.util;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

public class TaskStackUtil {

	public static ActivityManager.RunningAppProcessInfo getRunningAppProcessInfo(Context context, String packageName){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> processList = am.getRunningAppProcesses();
		for(ActivityManager.RunningAppProcessInfo p: processList){
			if(p.processName.equals(packageName)){
				return p;
			}
		}
		return null;
	}
	
	public static boolean isForeground(Context context, String packageName){
		ActivityManager.RunningAppProcessInfo processInfo = getRunningAppProcessInfo(context, packageName);
		if(processInfo != null){
			return ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND == processInfo.importance;
		}
		return false;
	}
	
	public static boolean moveTaskToFront(Activity activity){
		ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
		am.moveTaskToFront(activity.getTaskId(), 0);
		return isForeground(activity, activity.getPackageName());
	}
}
