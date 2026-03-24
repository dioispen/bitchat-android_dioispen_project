import 'dart:async';
import 'package:flutter/material.dart';
import 'bridge/bitchat_bridge.dart';
import 'ui/onboarding_screen.dart';
import 'ui/chat_demo_screen.dart';

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

  @override
  void initState() {
    super.initState();
    // 監聽來自 Native 的所有事件
    _subscription = BitchatBridge.events().listen((event) {
      if (event['type'] == 'onboardingStateChanged') {
        setState(() {
          _onboardingState = event;
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
    final isComplete = _onboardingState['state'] == 'COMPLETE';

    return MaterialApp(
      title: 'Bitchat',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF4A90E2),
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF4A90E2),
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      // 根據狀態切換根頁面
      home: isComplete 
          ? const ChatDemoScreen() 
          : OnboardingScreen(initialState: _onboardingState),
    );
  }
}
