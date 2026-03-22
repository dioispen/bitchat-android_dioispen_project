import 'dart:async';

import 'package:flutter/material.dart';

import '../bridge/bitchat_bridge.dart';

class ChatDemoScreen extends StatefulWidget {
  const ChatDemoScreen({super.key});

  @override
  State<ChatDemoScreen> createState() => _ChatDemoScreenState();
}

class _ChatDemoScreenState extends State<ChatDemoScreen> {
  final _controller = TextEditingController();
  final _scrollController = ScrollController();
  final List<_ChatItem> _items = <_ChatItem>[];

  StreamSubscription<Map<String, dynamic>>? _sub;

  @override
  void initState() {
    super.initState();

    _sub = BitchatBridge.events().listen((e) {
      final type = (e['type'] ?? '').toString();
      if (type == 'message') {
        setState(() {
          _items.add(_ChatItem(
            from: (e['from'] ?? 'peer').toString(),
            text: (e['text'] ?? '').toString(),
            isMine: false,
            ts: DateTime.now(),
          ));
        });
        _scrollToBottomSoon();
      }
    });

    // Best-effort: start native layer (Android/iOS 可自行決定要不要實作)
    unawaited(BitchatBridge.start());
  }

  @override
  void dispose() {
    _sub?.cancel();
    _controller.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _send() async {
    final text = _controller.text.trim();
    if (text.isEmpty) return;
    _controller.clear();

    setState(() {
      _items.add(_ChatItem(
        from: 'me',
        text: text,
        isMine: true,
        ts: DateTime.now(),
      ));
    });
    _scrollToBottomSoon();

    // Demo: peerId 先固定，之後可接原生 peer list
    await BitchatBridge.sendMessage(peerId: 'demo-peer', text: text);
  }

  void _scrollToBottomSoon() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scrollController.hasClients) return;
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 200),
        curve: Curves.easeOut,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;

    return Scaffold(
      appBar: AppBar(
        title: const Text('Chat (Flutter UI Demo)'),
        backgroundColor: cs.surface,
      ),
      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: ListView.builder(
                controller: _scrollController,
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
                itemCount: _items.length,
                itemBuilder: (context, i) {
                  final item = _items[i];
                  return _ChatBubble(item: item);
                },
              ),
            ),
            const Divider(height: 1),
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 8, 12, 12),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _controller,
                      textInputAction: TextInputAction.send,
                      onSubmitted: (_) => _send(),
                      decoration: const InputDecoration(
                        hintText: '輸入訊息…',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  FilledButton(
                    onPressed: _send,
                    child: const Text('送出'),
                  )
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ChatItem {
  final String from;
  final String text;
  final bool isMine;
  final DateTime ts;

  _ChatItem({
    required this.from,
    required this.text,
    required this.isMine,
    required this.ts,
  });
}

class _ChatBubble extends StatelessWidget {
  final _ChatItem item;

  const _ChatBubble({required this.item});

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final bg = item.isMine ? cs.primaryContainer : cs.secondaryContainer;
    final fg = item.isMine ? cs.onPrimaryContainer : cs.onSecondaryContainer;

    return Align(
      alignment: item.isMine ? Alignment.centerRight : Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 320),
        child: Card(
          color: bg,
          elevation: 0,
          margin: const EdgeInsets.symmetric(vertical: 6),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item.from,
                  style: TextStyle(
                    color: fg.withValues(alpha: 0.8),
                    fontSize: 12,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  item.text,
                  style: TextStyle(color: fg, fontSize: 16),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

