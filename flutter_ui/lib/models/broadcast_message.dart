import 'dart:convert';
import 'dart:typed_data';

/// 對應到原生 BitchatMessage.kt 的訊息格式
class BroadcastMessage {
  final String id;
  final String sender;
  final String content;
  final DateTime timestamp;
  final String? senderPeerID;
  final String? channel;
  final List<String>? mentions;
  final bool isPrivate;
  final bool isEncrypted;

  BroadcastMessage({
    required this.id,
    required this.sender,
    required this.content,
    required this.timestamp,
    this.senderPeerID,
    this.channel,
    this.mentions,
    this.isPrivate = false,
    this.isEncrypted = false,
  });

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'sender': sender,
      'content': content,
      'timestamp': timestamp.millisecondsSinceEpoch,
      'senderPeerID': senderPeerID,
      'channel': channel,
      'mentions': mentions,
      'isPrivate': isPrivate,
      'isEncrypted': isEncrypted,
    };
  }

  factory BroadcastMessage.fromMap(Map<String, dynamic> map) {
    return BroadcastMessage(
      id: map['id'] ?? '',
      sender: map['sender'] ?? '',
      content: map['content'] ?? '',
      timestamp: DateTime.fromMillisecondsSinceEpoch(map['timestamp'] ?? DateTime.now().millisecondsSinceEpoch),
      senderPeerID: map['senderPeerID'],
      channel: map['channel'],
      mentions: map['mentions'] != null ? List<String>.from(map['mentions']) : null,
      isPrivate: map['isPrivate'] ?? false,
      isEncrypted: map['isEncrypted'] ?? false,
    );
  }
}
