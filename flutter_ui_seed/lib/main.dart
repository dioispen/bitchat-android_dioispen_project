import 'package:flutter/material.dart';

import 'ui/chat_demo_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const BitchatFlutterUiApp());
}

class BitchatFlutterUiApp extends StatelessWidget {
  const BitchatFlutterUiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Bitchat UI',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF4A90E2)),
        useMaterial3: true,
      ),
      home: const ChatDemoScreen(),
    );
  }
}

