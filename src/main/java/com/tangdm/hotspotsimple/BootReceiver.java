package com.tangdm.hotspotsimple;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "TangDMHotspot";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "收到系统广播: " + action);
        
        // 处理开机完成广播
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "系统启动完成，准备开启热点");
            
            // 延迟5秒执行（避免系统未完全就绪）
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "延迟5秒后开始执行热点开启流程");
                
                // 第一步：尝试直接开启热点
                boolean success = HotspotHelper.enableHotspot(context);
                
                if (success) {
                    Log.d(TAG, "✅ 热点开启成功！");
                    
                    // 记录成功日志
                    Log.d(TAG, "热点信息: " + HotspotHelper.getHotspotInfo(context));
                    
                } else {
                    Log.e(TAG, "❌ 热点开启失败，将尝试重试...");
                    
                    // 失败后等待3秒重试一次
                    new Handler().postDelayed(() -> {
                        Log.d(TAG, "开始重试热点开启...");
                        boolean retrySuccess = HotspotHelper.enableHotspot(context);
                        
                        if (retrySuccess) {
                            Log.d(TAG, "✅ 重试成功！热点已开启");
                        } else {
                            Log.e(TAG, "❌ 重试也失败，请检查系统权限或手动开启");
                        }
                    }, 3000);
                }
                
                // 启动后台服务保活（即使热点开启失败也启动，用于后续监控）
                startBackgroundService(context);
                
            }, 5000); // 5秒延迟
        }
    }
    
    /**
     * 启动后台服务
     */
    private void startBackgroundService(Context context) {
        try {
            Log.d(TAG, "启动后台保活服务...");
            Intent serviceIntent = new Intent(context, HotspotService.class);
            context.startService(serviceIntent);
            Log.d(TAG, "后台服务启动完成");
        } catch (Exception e) {
            Log.e(TAG, "启动后台服务失败: " + e.getMessage());
        }
    }
}