# 接入方法
- 编译本工程的 tapsdk 模块
- 项目 -> 安装 Android 构建模板 -> 管理 -> 下载最新
- 项目 -> 导出 -> Android
- 复制本工程的 tapsdk/libs 中的所有 aar 文件到 godot 工程中的 res://android/plugins
- 复制本工程的 tapsdk/build/output/aar/TapSDK.aar 到 godot 工程中的 res://android/plugins
- 复制本工程的 TapSDK.gdap 到 godot 工程中的 res://android/plugins
- 项目 -> 导出 -> Android -> Plugin 中勾选 TapSDK

# 接入代码
```python
func _ready():
	if Engine.has_singleton("TapSDK"):
		tapsdk = Engine.get_singleton("TapSDK")
		tapsdk.tap_login_success.connect(tap_login_success)
		tapsdk.tap_login_failed.connect(tap_login_failed)
		tapsdk.startLogin()
	else:
		print("No tapsdk")
			
			
func tap_login_success(user_name, user_id, openid):
	print("Login success: %s", user_name)


func tap_login_failed(message, detail_message):
	print("Login failed: %s", message)	
```