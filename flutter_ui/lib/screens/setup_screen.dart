import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/user.dart';
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
    _startSetup();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _stopTasks();
    super.dispose();
  }

  void _stopTasks() {
    _searchTimer?.cancel();
    _searchTimer = null;
    _isSearching = false;
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
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

    // 1. 檢查權限
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

    // 3. 開始搜尋附近裝置並進行註冊檢查
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
      
      // 搜尋滿 2 秒（即第一次 Tick）後，就開始執行註冊狀態檢查，不用等待太久
      if (timer.tick >= 1) {
        _checkRegistration();
      }
    });
  }

  Future<void> _checkRegistration() async {
    if (!mounted) return;
    
    // 導航前停止 Timer
    _stopTasks();
    
    setState(() {
      _statusText = '驗證身分中...';
    });

    bool isRegisteredInCloud = false;
    bool hasLocalData = false;

    try {
      // Step A: 檢查原生層密鑰
      bool nativeRegistered = await BitchatBridge.isRegistered();
      debugPrint('DEBUG: Native registration status: $nativeRegistered');

      if (nativeRegistered) {
        // Step B: 檢查本地 SharedPreferences 資料
        final prefs = await SharedPreferences.getInstance();
        final userJson = prefs.getString('app_user');
        
        if (userJson != null) {
          hasLocalData = true;
          final user = AppUser.fromJson(jsonDecode(userJson));
          debugPrint('DEBUG: Local user found: ${user.id}');

          // Step C: 比對 Cloud Firestore
          // 設定超時，避免網路不穩時卡死
          final cloudDoc = await FirebaseFirestore.instance
              .collection('users')
              .doc(user.id)
              .get()
              .timeout(const Duration(seconds: 5));

          if (cloudDoc.exists) {
            isRegisteredInCloud = true;
            debugPrint('DEBUG: Cloud registration verified.');
          } else {
            debugPrint('DEBUG: User ID ${user.id} not found in Firestore.');
          }
        }
      }
    } catch (e) {
      debugPrint('DEBUG: Registration check failed or timed out: $e');
      // 如果是網路超時，但本地已有資料，我們採取寬鬆策略允許進入首頁 (離線模式)
      if (hasLocalData) {
        debugPrint('DEBUG: Proceeding in offline mode with local data.');
        isRegisteredInCloud = true; 
      }
    }

    if (mounted) {
      if (isRegisteredInCloud) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const HomeScreen()),
        );
      } else {
        // 若完全沒有雲端紀錄且非離線寬鬆情況，導向註冊頁
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
