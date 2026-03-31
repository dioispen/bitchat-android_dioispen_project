import 'dart:async';
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';
import 'package:flutter/material.dart';
import 'bridge/bitchat_bridge.dart';
import 'screens/setup_screen.dart';

// 全局 NavigatorKey 用於在沒有 Context 的情況下顯示 Dialog
final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  try {
    if (Firebase.apps.isEmpty) {
      await Firebase.initializeApp(
        options: DefaultFirebaseOptions.currentPlatform,
      );
    }
  } catch (e) {
    debugPrint('Firebase initialization warning: $e');
  }

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
    // 確保在第一幀渲染後才開始監聽，此時 navigatorKey.currentContext 才有效
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _startListeningToSystemStatus();
    });
  }

  @override
  void dispose() {
    _statusSubscription?.cancel();
    super.dispose();
  }

  void _startListeningToSystemStatus() {
    // 1. 先主動檢查一次初始狀態
    BitchatBridge.getSystemStatus().then((status) {
      if (status != null) _handleStatusUpdate(status);
    });

    // 2. 持續監聽來自原生端的狀態變更事件
    _statusSubscription = BitchatBridge.events().listen((event) {
      if (event['type'] == 'system_status') {
        _handleStatusUpdate(event);
      }
    });
  }

  void _handleStatusUpdate(Map<String, dynamic> status) {
    // 從原生端傳回的 Map 中讀取藍牙狀態
    bool bluetoothEnabled = status['bluetoothEnabled'] ?? true;

    if (!bluetoothEnabled) {
      _showBluetoothEnableDialog();
    } else {
      // 如果藍牙已開啟且對話框正開著，則自動關閉它
      if (_isBluetoothDialogOpen) {
        navigatorKey.currentState?.pop();
        _isBluetoothDialogOpen = false;
      }
    }
  }

  void _showBluetoothEnableDialog() {
    if (_isBluetoothDialogOpen) return;

    final context = navigatorKey.currentContext;
    if (context == null) return;

    _isBluetoothDialogOpen = true;
    showDialog(
      context: context,
      barrierDismissible: false, // 強制使用者必須處理，不能點擊空白處取消
      builder: (context) => PopScope(
        canPop: false, // 禁用實體返回鍵取消
        child: AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Row(
            children: [
              Icon(Icons.bluetooth_disabled, color: Colors.redAccent, size: 28),
              SizedBox(width: 12),
              Text('藍牙未開啟', style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
          content: const Text(
            'Bitchat 需要藍牙功能才能建立 Mesh 網路並在無網路環境下傳送訊息。\n\n請前往系統設定開啟藍牙。',
            style: TextStyle(fontSize: 15, height: 1.5),
          ),
          actions: [
            TextButton(
              onPressed: () {
                // 原生端通常會彈出系統權限請求，這裡提供一個了解按鈕
                // 如果藍牙開啟了，_handleStatusUpdate 會自動關閉此 Dialog
              },
              child: const Text('了解'),
            ),
          ],
        ),
      ),
    ).then((_) => _isBluetoothDialogOpen = false);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      navigatorKey: navigatorKey,
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
