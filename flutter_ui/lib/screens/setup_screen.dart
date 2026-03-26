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

class _SetupScreenState extends State<SetupScreen> {
  String _statusText = '正在初始化 Mesh 網路...';
  bool _isSearching = false;
  Map<String, String> _nearbyPeers = {};
  Timer? _searchTimer;

  @override
  void initState() {
    super.initState();
    _startSetup();
  }

  @override
  void dispose() {
    _searchTimer?.cancel();
    super.dispose();
  }

  Future<void> _startSetup() async {
    // 1. 啟動 Mesh 服務
    setState(() => _statusText = '啟動藍牙掃描與 Mesh 服務...');
    // 注意：這裡假設權限已在原生端處理，或透過 Flutter 權限套件處理
    bool started = await BitchatBridge.startMesh();
    if (!started) {
      setState(() => _statusText = '啟動失敗，請檢查藍牙權限。');
      return;
    }

    // 2. 開始搜尋附近裝置 (模擬原生檢查 UI)
    setState(() {
      _isSearching = true;
      _statusText = '正在搜尋附近裝置...';
    });

    _searchTimer = Timer.periodic(const Duration(seconds: 2), (timer) async {
      final peers = await BitchatBridge.getNearbyPeers();
      setState(() {
        _nearbyPeers = peers;
      });
      
      // 搜尋 6 秒後繼續下一步，或者發現裝置後讓使用者手動點擊
      if (timer.tick >= 3) {
        timer.cancel();
        _checkRegistration();
      }
    });
  }

  Future<void> _checkRegistration() async {
    setState(() {
      _isSearching = false;
      _statusText = '檢查註冊狀態...';
    });

    await Future.delayed(const Duration(seconds: 1));
    bool registered = await BitchatBridge.isRegistered();

    if (mounted) {
      if (registered) {
        // 已註冊，導向首頁
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const HomeScreen()),
        );
      } else {
        // 未註冊，導向註冊頁面
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
              const Icon(Icons.bluetooth_searching_rounded, size: 80, color: brown),
              const SizedBox(height: 32),
              Text(
                _statusText,
                textAlign: TextAlign.center,
                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: brown),
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
            ],
          ),
        ),
      ),
    );
  }
}
