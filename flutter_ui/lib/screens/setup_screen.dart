import 'dart:async';
import 'package:flutter/material.dart';
import '../bridge/bitchat_bridge.dart';
import 'register_screen.dart';
import 'home_screen.dart';

class SetupScreen extends StatefulWidget {
  const SetupScreen({super.key});

  @override
  State<SetupScreen> createState() => _SetupScreenState();
}

class _SetupScreenState extends State<SetupScreen> with WidgetsBindingObserver {
  String _statusText = '正在初始化...';
  bool _isSearching = false;
  bool _hasError = false;
  bool _needsPermissions = false;
  Map<String, String> _nearbyPeers = {};
  Timer? _searchTimer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    // 移除本地監聽器，改由 main.dart 的全局監聽處理彈窗
    _startSetup();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _stopTasks(); // 確保銷毀時停止所有任務
    super.dispose();
  }

  void _stopTasks() {
    _searchTimer?.cancel();
    _searchTimer = null;
    _isSearching = false;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    // 當使用者從系統設定授權回來時，自動重新檢查
    if (state == AppLifecycleState.resumed && _hasError) {
      _startSetup();
    }
  }

  Future<void> _startSetup() async {
    if (_isSearching) return;

    setState(() {
      _hasError = false;
      _needsPermissions = false;
      _statusText = '檢查必要權限與服務狀態...';
    });

    // 1. 檢查權限 (通知、藍牙相關、位置)
    bool hasPermissions = await BitchatBridge.checkPermissions();
    if (!hasPermissions) {
      setState(() {
        _hasError = true;
        _needsPermissions = true;
        _statusText = '權限未開啟。\n請確保已授權相關權限以維持運行。';
      });
      await BitchatBridge.requestPermissions();
      return;
    }

    // 2. 啟動 Mesh 服務
    setState(() => _statusText = '啟動藍牙掃描與 Mesh 服務...');
    bool started = await BitchatBridge.startMesh();
    if (!started) {
      setState(() {
        _hasError = true;
        _statusText = '啟動 Mesh 失敗。\n請確認藍牙已開啟，且通知權限已允許。';
      });
      return;
    }

    // 3. 開始搜尋附近裝置
    setState(() {
      _isSearching = true;
      _statusText = '正在搜尋附近裝置...';
    });

    _searchTimer?.cancel();
    _searchTimer = Timer.periodic(const Duration(seconds: 2), (timer) async {
      final peers = await BitchatBridge.getNearbyPeers();
      if (mounted) {
        setState(() {
          _nearbyPeers = peers;
        });
      }
      
      // 搜尋 6 秒後繼續
      if (timer.tick >= 3) {
        _checkRegistration();
      }
    });
  }

  Future<void> _checkRegistration() async {
    if (!mounted) return;
    
    // 重要：在導航前立即停止所有背景任務，解決 BLASTBufferQueue 問題
    _stopTasks();
    
    setState(() {
      _statusText = '檢查註冊狀態...';
    });

    // 給予渲染引擎短暫緩衝時間處理狀態變更
    await Future.delayed(const Duration(milliseconds: 500));
    bool registered = await BitchatBridge.isRegistered();

    if (mounted) {
      if (registered) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const HomeScreen()),
        );
      } else {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const RegisterScreen()),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    const brown = Color(0xFF5C3D2E);
    const bg = Color(0xFFF7F3EC);

    return Scaffold(
      backgroundColor: bg,
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                _hasError ? Icons.error_outline_rounded : Icons.bluetooth_searching_rounded, 
                size: 80, 
                color: _hasError ? Colors.redAccent : brown
              ),
              const SizedBox(height: 32),
              Text(
                _statusText,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 18, 
                  fontWeight: FontWeight.bold, 
                  color: _hasError ? Colors.redAccent : brown
                ),
              ),
              const SizedBox(height: 24),
              if (_isSearching) ...[
                const CircularProgressIndicator(color: brown),
                const SizedBox(height: 32),
                if (_nearbyPeers.isNotEmpty) ...[
                  const Text('附近已發現裝置：', style: TextStyle(color: Colors.grey, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 12),
                  Container(
                    constraints: const BoxConstraints(maxHeight: 200),
                    child: ListView.builder(
                      shrinkWrap: true,
                      itemCount: _nearbyPeers.length,
                      itemBuilder: (context, index) {
                        final entry = _nearbyPeers.entries.elementAt(index);
                        return ListTile(
                          leading: const Icon(Icons.devices, color: brown),
                          title: Text(entry.value),
                          subtitle: Text('ID: ${entry.key.substring(0, 8)}'),
                        );
                      },
                    ),
                  ),
                ] else ...[
                  const Text('正在尋找其他 Bitchat 節點...', style: TextStyle(fontStyle: FontStyle.italic, color: Colors.grey)),
                ],
              ],
              if (_hasError) ...[
                const SizedBox(height: 32),
                ElevatedButton(
                  onPressed: () {
                    if (_needsPermissions) {
                      BitchatBridge.requestPermissions();
                    } else {
                      _startSetup();
                    }
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: brown,
                    foregroundColor: bg,
                    padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 12),
                  ),
                  child: Text(_needsPermissions ? '授權權限' : '重新檢查狀態'),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
