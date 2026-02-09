package com.tangdm.hotspotsimple;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class HotspotService extends Service {
    private static final String TAG = "TangDMHotspot";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "🔧 后台保活服务创建");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🚀 后台保活服务启动");
        
        // 这里可以添加定期检查热点状态的逻辑
        // 但为了简化，我们只做保活，不主动干预
        
        // 记录当前热点状态（用于调试）
        try {
            String hotspotInfo = HotspotHelper.getHotspotInfo(this);
            Log.d(TAG, "当前热点状态: " + hotspotInfo);
        } catch (Exception e) {
            Log.e(TAG, "获取热点状态失败: " + e.getMessage());
        }
        
        // 返回START_STICKY让服务被系统杀死后自动重启
        // 这对于车机系统很重要，因为系统可能会清理后台服务
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // 我们不提供绑定接口
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 后台保活服务销毁");
        
        // 服务被销毁时记录日志
        Log.d(TAG, "服务被系统销毁，将在需要时自动重启");
    }
    
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "📱 应用从最近任务中移除");
        
        // 当用户从最近任务中移除应用时，我们重新启动服务
        // 这样可以确保服务持续运行
        Intent restartService = new Intent(getApplicationContext(), HotspotService.class);
        restartService.setPackage(getPackageName());
        startService(restartService);
    }
}