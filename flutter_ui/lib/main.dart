import 'package:flutter/material.dart';
import 'screens/setup_screen.dart';

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
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF5C3D2E)),
        useMaterial3: true,
      ),
      // 將進入點改為 SetupScreen，負責初始化與導向
      home: const SetupScreen(),
    );
  }
}
