package org.godotengine.godot.plugin;

import android.util.ArraySet;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tapsdk.bootstrap.Callback;
import com.tapsdk.bootstrap.TapBootstrap;
import com.tapsdk.bootstrap.account.TDSUser;
import com.tapsdk.bootstrap.exceptions.TapError;
import com.taptap.sdk.TapLoginHelper;
import com.tds.common.entities.TapConfig;
import com.tds.common.entities.TapDBConfig;
import com.tds.common.models.TapRegionType;

import org.godotengine.godot.Dictionary;
import org.godotengine.godot.Godot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.leancloud.LCLogger;
import cn.leancloud.core.LeanCloud;
import cn.leancloud.json.JSON;

public class TapSDK extends GodotPlugin {

    private static final String TAG = "TapSDK";

    // 开发者中心后台应用配置信息
    public static final String TDS_ClientID = "0RiAlMny7jiz086FaU";
    public static final String TDS_ClientToken = "8V8wemqkpkxmAN7qKhvlh6v0pXc8JJzEZe3JFUnU";
    public static final String TDS_ServerUrl = "https://0rialmny.cloud.tds1.tapapis.cn";

    String userID = "";
    String accessToken = "";

    public TapSDK(Godot godot) {
        super(godot);
        Init();
    }

    public void Init() {
        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
        WebView.setWebContentsDebuggingEnabled(true);
        // 内建账户方式登陆 SDK 初始化
        // TapDB 初始化
        TapDBConfig tapDBConfig = new TapDBConfig();
        tapDBConfig.setEnable(true);
        tapDBConfig.setChannel("gameChannel");
        tapDBConfig.setGameVersion("gameVersion");
        // TapSDK 初始化
        TapConfig tapConfig = new TapConfig.Builder()
                .withAppContext(getGodot().getContext())
                .withRegionType(TapRegionType.CN) // TapRegionType.CN: 国内  TapRegionType.IO: 国外
                // 自己账号
                .withClientId(TDS_ClientID)
                .withClientToken(TDS_ClientToken)
                /* 如果使用 单独 TapTap 授权，则不需要配置自定义域名 */
                .withServerUrl(TDS_ServerUrl)
                .withTapDBConfig(tapDBConfig)
                .build();
        TapBootstrap.init(getActivity(), tapConfig);
    }

    @UsedByGodot
    public void startLogin() {
        TDSUser currentUser = TDSUser.currentUser();

        // 未登录用户会返回 null
        if (currentUser == null) {
            // 用户未登录过
            TDSUser.loginWithTapTap(getActivity(), new Callback<TDSUser>() {

                @Override
                public void onSuccess(TDSUser resultUser) {
                    // 开发者可以调用 resultUser 的方法获取更多属性。
                    userID = resultUser.getObjectId();

                    String userName = resultUser.getUsername();
                    String avatar = (String) resultUser.get("avatar");
                    Log.d(TAG, "userID: " + userID);
                    Log.d(TAG, "userName: " + userName);
                    Log.d(TAG, "avatar: " + avatar);
                    Map<String, Object> authData = (Map<String, Object>) resultUser.get("authData");
                    Map<String, Object> taptapAuthData = (Map<String, Object>) authData.get("taptap");
                    Log.d(TAG, "authData:" + JSON.toJSONString(authData));
                    Map<String, Object> authDataResult = (Map<String, Object>) ((Map<String, Object>) resultUser.get("authData")).get("taptap");
                    Log.d(TAG, "unionid:" + taptapAuthData.get("unionid").toString());
                    Log.d(TAG, "openid:" + taptapAuthData.get("openid").toString());
                    Toast.makeText(getActivity(), "succeed to login with Taptap.", Toast.LENGTH_SHORT).show();
                    Dictionary returnValue = new Dictionary();
                    returnValue.put("user_name", userName);
                    returnValue.put("user_id", userID);
                    returnValue.put("openid", taptapAuthData.get("unionid").toString());
                    emitSignal("tap_login_success", (Object) returnValue);
                }

                @Override
                public void onFail(TapError error) {
                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, error.detailMessage);
                    Log.d(TAG, error.getMessage());
                    Log.d(TAG, error.toJSON());
                    Dictionary returnValue = new Dictionary();
                    returnValue.put("message", error.getMessage());
                    returnValue.put("detail_message", error.detailMessage);
                    emitSignal("tap_login_failed", (Object) returnValue);
                }
            }, TapLoginHelper.SCOPE_PUBLIC_PROFILE);
        } else {
            // 用户已经登录过
            Toast.makeText(getActivity(), "已经登陆，执行程序业务逻辑", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "TapSDK";
    }

    @NonNull
    @Override
    public List<String> getPluginMethods() {
        return Arrays.asList(new String[]{"startLogin"});
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Set<SignalInfo> signals = new HashSet<>();
        signals.add(new SignalInfo("tap_login_success"));
        signals.add(new SignalInfo("tap_login_failed"));
        return signals;
    }
}
