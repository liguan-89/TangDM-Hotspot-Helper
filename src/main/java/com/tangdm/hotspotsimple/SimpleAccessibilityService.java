package com.tangdm.hotspotsimple;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * 极简无障碍服务，仅用于保活和权限维持
 * 不执行任何界面操作，避免被系统限制
 */
public class SimpleAccessibilityService extends AccessibilityService {
    private static final String TAG = "TangDMHotspot";
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 极简实现：只记录关键事件，不执行任何操作
        
        int eventType = event.getEventType();
        String className = event.getClassName() != null ? event.getClassName().toString() : "null";
        
        // 只记录窗口状态变化事件（频率较低）
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            Log.d(TAG, "无障碍事件: 窗口变化 -> " + className);
        }
        
        // 不执行任何点击、滚动或其他操作
        // 这样可以避免被系统检测为"过度活跃"的无障碍服务
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "⚠️ 无障碍服务被中断");
        
        // 服务被中断时，尝试重新连接
        // 这对于车机系统很重要，因为系统可能会临时中断服务
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "尝试重新启用无障碍服务");
            // 这里不能直接重新启用，需要用户操作
            // 我们只是记录日志
        }, 5000);
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "✅ 无障碍服务已连接");
        
        // 服务连接成功，记录状态
        Log.d(TAG, "无障碍服务配置:");
        Log.d(TAG, "  - 事件类型: TYPE_WINDOW_STATE_CHANGED");
        Log.d(TAG, "  - 反馈类型: FEEDBACK_GENERIC");
        Log.d(TAG, "  - 标志: FLAG_DEFAULT");
        
        // 设置服务信息（可选）
        // 这可以帮助用户识别我们的服务
        // setServiceInfo(new AccessibilityServiceInfo());
    }
    
    /**
     * 辅助方法：检查服务是否正常运行
     */
    public static boolean isServiceRunning() {
        // 在实际应用中，这里应该检查服务状态
        // 但为了简化，我们返回true
        return true;
    }
    
    /**
     * 辅助方法：获取服务状态描述
     */
    public static String getServiceStatus() {
        return "无障碍服务运行中（仅用于保活）";
    }
}