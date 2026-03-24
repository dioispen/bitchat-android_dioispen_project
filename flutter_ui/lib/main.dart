import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'bridge/bitchat_bridge.dart';
import 'ui/onboarding_screen.dart';
import 'screens/home_screen.dart';
import 'screens/register_screen.dart';

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
  Map<String, dynamic> _onboardingState = {'state': 'CHECKING'};
  StreamSubscription? _subscription;
  bool _isUserRegistered = false;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _initApp();
    
    _subscription = BitchatBridge.events().listen((event) {
      if (event['type'] == 'onboardingStateChanged') {
        setState(() {
          _onboardingState = event;
        });
        // 當 Native 完成時，重新檢查註冊狀態以確保導向正確頁面
        if (event['state'] == 'COMPLETE') {
          _checkUserRegistration();
        }
      }
    });
  }

  Future<void> _initApp() async {
    await _checkUserRegistration();
    setState(() {
      _isLoading = false;
    });
  }

  Future<void> _checkUserRegistration() async {
    final prefs = await SharedPreferences.getInstance();
    final userRaw = prefs.getString('app_user');
    setState(() {
      _isUserRegistered = userRaw != null;
    });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const MaterialApp(
        home: Scaffold(body: Center(child: CircularProgressIndicator())),
      );
    }

    final isNativeComplete = _onboardingState['state'] == 'COMPLETE';

    return MaterialApp(
      title: 'Bitchat',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF5C3D2E), // 使用與 RegisterScreen 相同的棕色調
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      // 邏輯流：
      // 1. Native Onboarding 未完成 -> 顯示 OnboardingScreen
      // 2. Native 完成但未註冊 -> 顯示 RegisterScreen
      // 3. 全部完成 -> 顯示 HomeScreen
      home: isNativeComplete 
          ? (_isUserRegistered ? const HomeScreen() : const RegisterScreen())
          : OnboardingScreen(initialState: _onboardingState),
    );
  }
}
