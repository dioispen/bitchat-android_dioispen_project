import 'dart:async';
import 'package:flutter/material.dart';
import 'bridge/bitchat_bridge.dart';
import 'screens/setup_screen.dart';

// 全局 NavigatorKey 用於在沒有 Context 的情況下顯示 Dialog
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const BitchatFlutterUiApp());
}

class BitchatFlutterUiApp extends StatefulWidget {
  const BitchatFlutterUiApp({super.key});

  @override
  State<BitchatFlutterUiApp> createState() => _BitchatFlutterUiAppState();
}

class _BitchatFlutterUiAppState extends State<BitchatFlutterUiApp> {
  StreamSubscription? _statusSubscription;
  bool _isBluetoothDialogOpen = false;

  @override
  void initState() {
    super.initState();
    _listenToSystemStatus();
  }

  @override
  void dispose() {
    _statusSubscription?.cancel();
    super.dispose();
  }

  void _listenToSystemStatus() {
    _statusSubscription = BitchatBridge.events().listen((event) {
      if (event['type'] == 'system_status') {
        bool bluetoothEnabled = event['bluetoothEnabled'] ?? false;

        if (!bluetoothEnabled) {
          _showBluetoothEnableDialog();
        } else {
          if (_isBluetoothDialogOpen) {
            // 使用 navigatorKey 來 pop，確保能關閉對話框
            navigatorKey.currentState?.pop();
            _isBluetoothDialogOpen = false;
          }
        }
      }
    });
  }

  void _showBluetoothEnableDialog() {
    if (_isBluetoothDialogOpen) return;

    final context = navigatorKey.currentContext;
    if (context == null) return;

    _isBluetoothDialogOpen = true;
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Row(
          children: [
            Icon(Icons.bluetooth_disabled, color: Colors.redAccent),
            SizedBox(width: 12),
            Text('藍牙已關閉'),
          ],
        ),
        content: const Text('Bitchat 需要藍牙功能才能建立 Mesh 網路並傳送訊息。請前往系統設定開啟藍牙。'),
      ),
    ).then((_) => _isBluetoothDialogOpen = false);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey, // 設定全局 Key
      title: 'Bitchat UI',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF5C3D2E)),
        useMaterial3: true,
      ),
      home: const SetupScreen(),
    );
  }
}
