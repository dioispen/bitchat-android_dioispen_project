import 'dart:async';
import 'package:flutter/material.dart';
import '../bridge/bitchat_bridge.dart';

class OnboardingScreen extends StatefulWidget {
  final Map<String, dynamic> initialState;
  const OnboardingScreen({super.key, required this.initialState});

  @override
  State<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  late Map<String, dynamic> _state;
  StreamSubscription? _subscription;

  @override
  void initState() {
    super.initState();
    _state = widget.initialState;
    _subscription = BitchatBridge.events().listen((event) {
      if (event['type'] == 'onboardingStateChanged') {
        setState(() {
          _state = event;
        });
      }
    });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final stateStr = _state['state'] ?? 'CHECKING';
    
    // 如果狀態是 COMPLETE，則不顯示 Onboarding (由 main.dart 切換)
    if (stateStr == 'COMPLETE') {
      return const SizedBox.shrink();
    }

    return Scaffold(
      body: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.security, size: 80, color: Colors.blue),
            const SizedBox(height: 24),
            Text(
              _getTitle(stateStr),
              style: Theme.of(context).textTheme.headlineMedium,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            Text(
              _getMessage(stateStr),
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 16, color: Colors.grey),
            ),
            const SizedBox(height: 40),
            _buildActionArea(stateStr),
            if (_state['errorMessage'] != null && _state['errorMessage'].toString().isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 20),
                child: Text(
                  _state['errorMessage'],
                  style: const TextStyle(color: Colors.red),
                ),
              ),
          ],
        ),
      ),
    );
  }

  String _getTitle(String state) {
    switch (state) {
      case 'PERMISSION_EXPLANATION': return '歡迎使用 Bitchat';
      case 'BLUETOOTH_CHECK': return '藍牙未開啟';
      case 'LOCATION_CHECK': return '定位服務未開啟';
      case 'BATTERY_OPTIMIZATION_CHECK': return '電池最佳化';
      case 'ERROR': return '發生錯誤';
      case 'INITIALIZING': return '正在初始化...';
      default: return '檢查中...';
    }
  }

  String _getMessage(String state) {
    switch (state) {
      case 'PERMISSION_EXPLANATION': return '我們需要藍牙與定位權限來建立去中心化網路。';
      case 'BLUETOOTH_CHECK': return 'Bitchat 需要藍牙來發現附近的節點。';
      case 'LOCATION_CHECK': return 'Android 系統要求開啟定位才能掃描藍牙裝置。';
      case 'BATTERY_OPTIMIZATION_CHECK': return '為了維持後台連線穩定，建議關閉電池最佳化。';
      default: return '請稍候。';
    }
  }

  Widget _buildActionArea(String state) {
    bool isLoading = false;
    VoidCallback? onPressed;
    String label = '繼續';

    switch (state) {
      case 'PERMISSION_EXPLANATION':
        label = '授權並開始';
        onPressed = () => BitchatBridge.requestPermissions();
        break;
      case 'BLUETOOTH_CHECK':
        label = '開啟藍牙';
        isLoading = _state['isBluetoothLoading'] ?? false;
        onPressed = () => BitchatBridge.enableBluetooth();
        break;
      case 'LOCATION_CHECK':
        label = '開啟定位';
        isLoading = _state['isLocationLoading'] ?? false;
        onPressed = () => BitchatBridge.enableLocation();
        break;
      case 'BATTERY_OPTIMIZATION_CHECK':
        return Column(
          children: [
            ElevatedButton(
              onPressed: () => BitchatBridge.requestAction('disableBatteryOptimization'),
              child: const Text('去設定關閉'),
            ),
            TextButton(
              onPressed: () => BitchatBridge.requestAction('skipBackgroundLocation'),
              child: const Text('暫時跳過'),
            ),
          ],
        );
      case 'ERROR':
        label = '重試';
        onPressed = () => BitchatBridge.retryOnboarding();
        break;
      case 'CHECKING':
      case 'INITIALIZING':
        return const CircularProgressIndicator();
    }

    return isLoading 
      ? const CircularProgressIndicator()
      : ElevatedButton(onPressed: onPressed, child: Text(label));
  }
}
