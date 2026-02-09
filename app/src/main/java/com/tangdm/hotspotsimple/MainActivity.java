package com.tangdm.hotspotsimple;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "TangDMHotspot";
    
    private TextView statusTextView;
    private Button btnEnableAccessibility;
    private Button btnTestHotspot;
    private Button btnCheckStatus;
    private Button btnExit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "主界面创建");
        
        // 创建主布局
        LinearLayout mainLayout = createMainLayout();
        setContentView(mainLayout);
        
        // 更新状态显示
        updateStatus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "主界面恢复");
        
        // 每次界面显示时更新状态
        updateStatus();
    }
    
    /**
     * 创建主界面布局
     */
    private LinearLayout createMainLayout() {
        // 主垂直布局
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(50, 100, 50, 50);
        
        // 标题
        TextView title = new TextView(this);
        title.setText("🚗 比亚迪唐DM热点助手");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        
        // 版本信息
        TextView version = new TextView(this);
        version.setText("版本 1.0 | 仅供个人使用");
        version.setTextSize(14);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, 0, 0, 20);
        
        // 状态显示区域
        statusTextView = new TextView(this);
        statusTextView.setTextSize(16);
        statusTextView.setPadding(20, 20, 20, 30);
        statusTextView.setGravity(Gravity.CENTER);
        
        // 按钮1：开启无障碍权限
        btnEnableAccessibility = new Button(this);
        btnEnableAccessibility.setText("🔧 开启无障碍权限");
        btnEnableAccessibility.setOnClickListener(v -> enableAccessibility());
        
        // 按钮2：测试热点开启
        btnTestHotspot = new Button(this);
        btnTestHotspot.setText("📡 立即测试热点");
        btnTestHotspot.setOnClickListener(v -> testHotspot());
        
        // 按钮3：检查状态
        btnCheckStatus = new Button(this);
        btnCheckStatus.setText("🔍 检查当前状态");
        btnCheckStatus.setOnClickListener(v -> updateStatus());
        
        // 按钮4：完成配置
        btnExit = new Button(this);
        btnExit.setText("✅ 完成配置（后台运行）");
        btnExit.setOnClickListener(v -> exitApp());
        
        // 说明文字
        TextView instructions = new TextView(this);
        instructions.setText("\n使用说明：\n1. 点击上方按钮开启无障碍权限\n2. 在系统设置中找到'唐DM热点'\n3. 开启无障碍服务开关\n4. 返回应用测试热点功能\n5. 重启车机测试自动开启\n\n应用将在后台自动运行，无需再次打开。");
        instructions.setTextSize(14);
        instructions.setPadding(20, 30, 20, 20);
        
        // 添加到主布局
        mainLayout.addView(title);
        mainLayout.addView(version);
        mainLayout.addView(statusTextView);
        mainLayout.addView(btnEnableAccessibility);
        mainLayout.addView(btnTestHotspot);
        mainLayout.addView(btnCheckStatus);
        mainLayout.addView(btnExit);
        mainLayout.addView(instructions);
        
        return mainLayout;
    }
    
    /**
     * 开启无障碍权限
     */
    private void enableAccessibility() {
        Log.d(TAG, "用户点击开启无障碍权限");
        
        try {
            // 跳转到无障碍设置
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            
            Toast.makeText(this, 
                "请找到'唐DM热点'并开启无障碍服务开关\n然后返回本应用", 
                Toast.LENGTH_LONG).show();
                
        } catch (Exception e) {
            Log.e(TAG, "跳转无障碍设置失败: " + e.getMessage());
            Toast.makeText(this, "跳转设置失败，请手动进入设置", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 测试热点开启
     */
    private void testHotspot() {
        Log.d(TAG, "用户点击测试热点");
        
        // 显示正在开启的提示
        statusTextView.setText("正在尝试开启热点...");
        
        new Thread(() -> {
            try {
                // 在后台线程执行热点开启
                boolean success = HotspotHelper.enableHotspot(MainActivity.this);
                
                // 回到主线程更新UI
                runOnUiThread(() -> {
                    if (success) {
                        String hotspotInfo = HotspotHelper.getHotspotInfo(MainActivity.this);
                        statusTextView.setText("✅ 热点开启成功！\n" + hotspotInfo);
                        Toast.makeText(MainActivity.this, "热点开启成功！", Toast.LENGTH_SHORT).show();
                    } else {
                        statusTextView.setText("❌ 热点开启失败\n请检查系统权限或手动开启");
                        Toast.makeText(MainActivity.this, "热点开启失败，请查看日志", Toast.LENGTH_LONG).show();
                    }
                    
                    // 更新完整状态
                    updateStatus();
                });
                
            } catch (Exception e) {
                Log.e(TAG, "测试热点时发生异常: " + e.getMessage(), e);
                
                runOnUiThread(() -> {
                    statusTextView.setText("⚠️ 发生异常: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "发生异常，请查看日志", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus() {
        Log.d(TAG, "更新状态显示");
        
        new Thread(() -> {
            try {
                // 获取各种状态信息
                boolean isHotspotEnabled = HotspotHelper.isHotspotEnabled(MainActivity.this);
                String hotspotInfo = HotspotHelper.getHotspotInfo(MainActivity.this);
                String accessibilityStatus = SimpleAccessibilityService.getServiceStatus();
                
                // 构建状态文本
                StringBuilder status = new StringBuilder();
                status.append("📊 系统状态\n");
                status.append("────────────\n");
                status.append("热点状态: ").append(isHotspotEnabled ? "✅ 已开启" : "❌ 未开启").append("\n");
                status.append("热点信息: ").append(hotspotInfo).append("\n");
                status.append("无障碍服务: ").append(accessibilityStatus).append("\n");
                status.append("自启动: ✅ 已启用（开机5秒后）\n");
                status.append("车机型号: 比亚迪唐DM 2019\n");
                status.append("系统版本: Android 7.0 (Dlink1.0)");
                
                final String statusText = status.toString();
                
                // 回到主线程更新UI
                runOnUiThread(() -> {
                    statusTextView.setText(statusText);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "更新状态时发生异常: " + e.getMessage());
                
                runOnUiThread(() -> {
                    statusTextView.setText("⚠️ 状态检查失败: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * 退出应用（进入后台运行）
     */
    private void exitApp() {
        Log.d(TAG, "用户点击完成配置，应用将进入后台");
        
        // 显示提示
        Toast.makeText(this, 
            "配置完成！应用将在后台运行\n重启车机测试自动开启功能", 
            Toast.LENGTH_LONG).show();
        
        // 启动后台服务
        try {
            Intent serviceIntent = new Intent(this, HotspotService.class);
            startService(serviceIntent);
            Log.d(TAG, "后台服务已启动");
        } catch (Exception e) {
            Log.e(TAG, "启动后台服务失败: " + e.getMessage());
        }
        
        // 结束当前Activity，应用进入后台
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "主界面销毁");
        
        // 记录应用退出
        Log.d(TAG, "应用进入后台运行模式");
    }
}