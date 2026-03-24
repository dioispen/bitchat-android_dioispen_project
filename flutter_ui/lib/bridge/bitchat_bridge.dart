import 'dart:async';
import 'package:flutter/services.dart';

/// Bitchat 橋接類別 - 負責與 Android 原生邏輯通訊
class BitchatBridge {
  static const MethodChannel _method = MethodChannel('com.bitchat/bridge/methods');
  static const EventChannel _events = EventChannel('com.bitchat/bridge/events');

  /// 監聽來自 Native 的事件（如：狀態更新、新訊息）
  static Stream<Map<String, dynamic>> events() {
    return _events.receiveBroadcastStream().map((dynamic e) {
      if (e is Map) {
        return e.map((k, v) => MapEntry(k.toString(), v));
      }
      return <String, dynamic>{};
    });
  }

  /// 請求 Native 執行操作
  static Future<void> requestAction(String action, [Map<String, dynamic>? params]) async {
    await _method.invokeMethod('requestAction', {
      'action': action,
      'params': params,
    });
  }

  /// 取得身份資訊
  static Future<Map<String, dynamic>?> getIdentityInfo() async {
    final result = await _method.invokeMethod('getIdentityInfo');
    return result != null ? Map<String, dynamic>.from(result) : null;
  }

  /// 啟動 Mesh 服務
  static Future<void> startMesh() async {
    await _method.invokeMethod('startMesh');
  }

  /// 發送私訊
  static Future<void> sendMessage({
    required String peerId,
    required String text,
  }) async {
    await _method.invokeMethod('sendMessage', {
      'peerId': peerId,
      'text': text,
    });
  }

  // --- 捷徑方法 ---
  
  static Future<void> enableBluetooth() => requestAction('enableBluetooth');
  static Future<void> enableLocation() => requestAction('enableLocation');
  static Future<void> requestPermissions() => requestAction('requestPermissions');
  static Future<void> retryOnboarding() => requestAction('retryOnboarding');
  static Future<void> openSettings() => requestAction('openSettings');
}
