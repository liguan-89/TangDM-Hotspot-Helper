package com.tangdm.hotspotsimple;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.lang.reflect.Method;

public class HotspotHelper {
    private static final String TAG = "TangDMHotspot";
    
    /**
     * 直接开启热点（使用系统API）
     */
    public static boolean enableHotspot(Context context) {
        try {
            Log.d(TAG, "开始尝试开启热点...");
            
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                Log.e(TAG, "获取WifiManager失败");
                return false;
            }
            
            // 检查WiFi状态，如果开启则先关闭（热点和WiFi不能同时开启）
            if (wifiManager.isWifiEnabled()) {
                Log.d(TAG, "WiFi已开启，正在关闭WiFi...");
                wifiManager.setWifiEnabled(false);
                Thread.sleep(1500); // 等待1.5秒让WiFi完全关闭
            }
            
            // 使用反射调用隐藏API
            Method setWifiApEnabled = wifiManager.getClass()
                .getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            
            // 配置热点参数
            WifiConfiguration wifiConfig = createHotspotConfig(context);
            
            Log.d(TAG, "正在开启热点，SSID: " + wifiConfig.SSID);
            
            // 开启热点
            boolean result = (boolean) setWifiApEnabled.invoke(wifiManager, wifiConfig, true);
            
            Log.d(TAG, "热点开启API调用结果: " + result);
            
            // 验证热点是否真的开启
            Thread.sleep(2000); // 等待2秒
            boolean isEnabled = isHotspotEnabled(context);
            Log.d(TAG, "热点状态验证: " + isEnabled);
            
            return isEnabled;
            
        } catch (Exception e) {
            Log.e(TAG, "开启热点失败: " + e.getMessage(), e);
            
            // 记录详细错误信息
            e.printStackTrace();
            
            // 尝试备用方案
            return tryAlternativeMethods(context);
        }
    }
    
    /**
     * 创建热点配置
     */
    private static WifiConfiguration createHotspotConfig(Context context) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        
        // 热点名称：唐DM_设备后4位
        String deviceId = getDeviceId(context);
        wifiConfig.SSID = "TangDM_" + deviceId;
        
        // 密码：12345678（简单易记）
        wifiConfig.preSharedKey = "12345678";
        
        // 安全设置：WPA2 PSK
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        
        // 其他设置
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        
        return wifiConfig;
    }
    
    /**
     * 尝试备用方法
     */
    private static boolean tryAlternativeMethods(Context context) {
        Log.d(TAG, "尝试备用方法...");
        
        try {
            // 方法1：尝试不同的方法名
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            
            // 尝试其他可能的方法名
            String[] methodNames = {
                "setWifiApEnabled",
                "enableWifiAp",
                "startWifiAp",
                "startTethering"
            };
            
            for (String methodName : methodNames) {
                try {
                    Log.d(TAG, "尝试方法: " + methodName);
                    Method method = wifiManager.getClass().getMethod(methodName, WifiConfiguration.class, boolean.class);
                    WifiConfiguration config = createHotspotConfig(context);
                    boolean result = (boolean) method.invoke(wifiManager, config, true);
                    
                    if (result) {
                        Log.d(TAG, "备用方法 " + methodName + " 成功");
                        return true;
                    }
                } catch (NoSuchMethodException e) {
                    // 方法不存在，继续尝试下一个
                    continue;
                } catch (Exception e) {
                    Log.e(TAG, "方法 " + methodName + " 调用失败: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "所有备用方法都失败: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 检查热点状态
     */
    public static boolean isHotspotEnabled(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            
            // 尝试不同的检查方法
            String[] methodNames = {
                "isWifiApEnabled",
                "getWifiApState"
            };
            
            for (String methodName : methodNames) {
                try {
                    Method method = wifiManager.getClass().getMethod(methodName);
                    Object result = method.invoke(wifiManager);
                    
                    if (result instanceof Boolean) {
                        return (boolean) result;
                    } else if (result instanceof Integer) {
                        int state = (int) result;
                        // WIFI_AP_STATE_ENABLED 通常是 13
                        return state == 13 || state == 3;
                    }
                } catch (NoSuchMethodException e) {
                    continue;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "检查热点状态失败: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 关闭热点
     */
    public static boolean disableHotspot(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method setWifiApEnabled = wifiManager.getClass()
                .getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            
            return (boolean) setWifiApEnabled.invoke(wifiManager, null, false);
            
        } catch (Exception e) {
            Log.e(TAG, "关闭热点失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成设备唯一标识（用于热点名称）
     */
    private static String getDeviceId(Context context) {
        try {
            String deviceId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
            );
            
            if (deviceId != null && deviceId.length() >= 4) {
                return deviceId.substring(deviceId.length() - 4); // 取后4位
            }
        } catch (Exception e) {
            Log.e(TAG, "获取设备ID失败: " + e.getMessage());
        }
        
        return "8888"; // 默认值
    }
    
    /**
     * 获取当前热点配置
     */
    public static String getHotspotInfo(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method getWifiApConfiguration = wifiManager.getClass()
                .getMethod("getWifiApConfiguration");
            
            WifiConfiguration config = (WifiConfiguration) getWifiApConfiguration.invoke(wifiManager);
            if (config != null) {
                return "SSID: " + config.SSID + ", 状态: " + (isHotspotEnabled(context) ? "已开启" : "已关闭");
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        return "无法获取热点信息";
    }
}