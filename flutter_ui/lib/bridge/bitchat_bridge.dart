import 'dart:async';

import 'package:flutter/services.dart';

/// Flutter <-> Native 橋接（只共用 UI 的最小骨架）
///
/// - MethodChannel: Flutter 發指令給 Native（send/start/stop...）
/// - EventChannel: Native 推事件給 Flutter（訊息/狀態/peer list...）
class BitchatBridge {
  static const MethodChannel _method =
      MethodChannel('com.bitchat/bridge/methods');
  static const EventChannel _events =
      EventChannel('com.bitchat/bridge/events');

  static Stream<Map<String, dynamic>> events() {
    return _events.receiveBroadcastStream().map((dynamic e) {
      if (e is Map) {
        return e.map((k, v) => MapEntry(k.toString(), v));
      }
      return <String, dynamic>{'type': 'unknown', 'raw': e};
    });
  }

  static Future<void> start() => _method.invokeMethod<void>('start');

  static Future<void> stop() => _method.invokeMethod<void>('stop');

  static Future<void> sendMessage({
    required String peerId,
    required String text,
  }) {
    return _method.invokeMethod<void>('sendMessage', <String, dynamic>{
      'peerId': peerId,
      'text': text,
    });
  }
}

