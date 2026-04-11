2026-04-11 18:16:15.472 10789-11045 BluetoothG...entManager com.bitchat.droid                    I  Client: Received packet from 40:13:68:D2:FC:B7, size: 256 bytes
2026-04-11 18:16:15.472 10789-11045 BluetoothG...entManager com.bitchat.droid                    D  Client: Parsed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.472 10789-11045 BluetoothC...ionManager com.bitchat.droid                    D  onPacketReceived: Packet received from 40:13:68:D2:FC:B7 (753151ff11d7af0e)
2026-04-11 18:16:15.474 10789-10906 PacketUplinkManager     com.bitchat.droid                    D   正在上傳封包 (Type: 48, Format: Binary) 至伺服器...
2026-04-11 18:16:15.474 10789-11045 PacketProcessor         com.bitchat.droid                    D  processPacket 48
2026-04-11 18:16:15.475 10789-10915 PacketProcessor         com.bitchat.droid                    D  📦 Processing packet type 48 from 753151ff11d7af0e (serialized)
2026-04-11 18:16:15.475 10789-10915 SecurityManager         com.bitchat.droid                    D  Packet validation passed for 753151ff11d7af0e, messageID: 1775902576815-753151ff11d7af0e--460352167
2026-04-11 18:16:15.476 10789-10915 PacketProcessor         com.bitchat.droid                    D  Processing packet type HEALTH_REPORT from 753151ff11d7af0e
2026-04-11 18:16:15.476 10789-10915 PacketProcessor         com.bitchat.droid                    D  Processing HEALTH_REPORT from 753151ff11d7af0e
2026-04-11 18:16:15.476 10789-10899 MessageHandler          com.bitchat.droid                    D  🔍 收到健康報告封包，大小: 110 字節，來自: 753151ff11d7af0e
2026-04-11 18:16:15.476 10789-10899 HealthReportPayload     com.bitchat.droid                    D  🔍 開始解碼，資料長度: 110 字節
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  資料預覽 (前32字節): 1C 6A 48 4D 4D 79 31 50 66 66 68 59 64 56 49 64 30 53 51 54 43 49 57 69 7A 44 72 52 32 04 61 62
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  reporterId 長度: 28 (預期 < 256)
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ reporterId: 'jHMMy1PffhYdVId0SQTCIWizDrR2'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  name 長度: 4
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ name: 'abcd'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  phone 長度: 10
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ phone: '8689263564'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  bloodType 長度: 6
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ bloodType: 'AB 型'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  status 長度: 6
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ status: '輕傷'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  description 長度: 6 (2字節大端序)
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ description: '擦傷'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ lat: 23.9514268, lng: 120.926105
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  reportTime 長度: 26
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ reportTime: '2026-04-11T18:16:16.812295'
2026-04-11 18:16:15.477 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✅ 解碼成功！
2026-04-11 18:16:15.477 10789-10899 MessageHandler          com.bitchat.droid                    D  📢 成功解碼健康報告 - 回報者: abcd, 狀態: 輕傷
2026-04-11 18:16:15.478 10789-10899 BitchatBridge           com.bitchat.droid                    D  📨 收到封包，類型: 48 (HEALTH_REPORT=48), 大小: 110
2026-04-11 18:16:15.478 10789-10899 BitchatBridge           com.bitchat.droid                    D  🎯 這是 HEALTH_REPORT，開始解碼...
2026-04-11 18:16:15.478 10789-11045 BluetoothG...entManager com.bitchat.droid                    I  Client: Received packet from 40:13:68:D2:FC:B7, size: 256 bytes
2026-04-11 18:16:15.478 10789-10915 PacketRelayManager      com.bitchat.droid                    D  Evaluating relay for packet type 48 from 753151ff11d7af0e (TTL: 3)
2026-04-11 18:16:15.478 10789-10915 PacketRelayManager      com.bitchat.droid                    D  Decremented TTL from 3 to 2
2026-04-11 18:16:15.478 10789-10915 PacketRelayManager      com.bitchat.droid                    D  Small network (1 peers), relaying
2026-04-11 18:16:15.478 10789-10915 PacketRelayManager      com.bitchat.droid                    D  🔄 Relaying packet type 48 with TTL 2
2026-04-11 18:16:15.479 10789-10915 FragmentManager         com.bitchat.droid                    D  🔀 Creating fragments for packet type 48, payload: 110 bytes
2026-04-11 18:16:15.479 10789-10905 MessageHandler          com.bitchat.droid                    D  🌐 Internet available, uploading health report jHMMy1PffhYdVId0SQTCIWizDrR2 to management center
2026-04-11 18:16:15.479 10789-11045 BluetoothG...entManager com.bitchat.droid                    D  Client: Parsed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  🔍 開始解碼，資料長度: 110 字節
2026-04-11 18:16:15.479 10789-11045 BluetoothC...ionManager com.bitchat.droid                    D  onPacketReceived: Packet received from 40:13:68:D2:FC:B7 (753151ff11d7af0e)
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  資料預覽 (前32字節): 1C 6A 48 4D 4D 79 31 50 66 66 68 59 64 56 49 64 30 53 51 54 43 49 57 69 7A 44 72 52 32 04 61 62
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  reporterId 長度: 28 (預期 < 256)
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ reporterId: 'jHMMy1PffhYdVId0SQTCIWizDrR2'
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  name 長度: 4
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ name: 'abcd'
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  phone 長度: 10
2026-04-11 18:16:15.479 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ phone: '8689263564'
2026-04-11 18:16:15.479 10789-11045 PacketProcessor         com.bitchat.droid                    D  processPacket 48
2026-04-11 18:16:15.480 10789-10915 FragmentManager         com.bitchat.droid                    D  📦 Encoded to 256 bytes
2026-04-11 18:16:15.480 10789-10915 FragmentManager         com.bitchat.droid                    D  📏 Unpadded to 196 bytes
2026-04-11 18:16:15.480 10789-10915 PacketProcessor         com.bitchat.droid                    D  Completed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.480 10789-10903 PacketUplinkManager     com.bitchat.droid                    D   正在上傳封包 (Type: 48, Format: Binary) 至伺服器...
2026-04-11 18:16:15.481 10789-10905 PacketProcessor         com.bitchat.droid                    D  📦 Processing packet type 48 from 753151ff11d7af0e (serialized)
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  bloodType 長度: 6
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ bloodType: 'AB 型'
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  status 長度: 6
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ status: '輕傷'
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  description 長度: 6 (2字節大端序)
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ description: '擦傷'
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ lat: 23.9514268, lng: 120.926105
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  reportTime 長度: 26
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✓ reportTime: '2026-04-11T18:16:16.812295'
2026-04-11 18:16:15.481 10789-10899 HealthReportPayload     com.bitchat.droid                    D  ✅ 解碼成功！
2026-04-11 18:16:15.481 10789-10899 BitchatBridge           com.bitchat.droid                    D  ✅ HEALTH_REPORT 解碼成功: abcd (輕傷), lat=23.9514268, lng=120.926105
2026-04-11 18:16:15.481 10789-10899 BitchatBridge           com.bitchat.droid                    D  📤 正在發送事件給 Flutter...
2026-04-11 18:16:15.481 10789-10900 BluetoothP...roadcaster com.bitchat.droid                    I  Broadcasting packet v1 type 48 to 1 server + 3 client connections
2026-04-11 18:16:15.482 10789-10899 BitchatBridge           com.bitchat.droid                    D  ✨ 事件已發送給 Flutter
2026-04-11 18:16:15.482 10789-10905 SecurityManager         com.bitchat.droid                    D  Dropping duplicate packet: 1775902576815-753151ff11d7af0e--460352167
2026-04-11 18:16:15.482 10789-10905 PacketProcessor         com.bitchat.droid                    D  Packet failed security validation from 753151ff11d7af0e
2026-04-11 18:16:15.482 10789-10905 PacketProcessor         com.bitchat.droid                    D  Completed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.483 10789-10900 BluetoothP...roadcaster com.bitchat.droid                    D  Skipping broadcast to server back to relayer: 40:13:68:D2:FC:B7
2026-04-11 18:16:15.538 10789-10812 BluetoothG...verManager com.bitchat.droid                    I  Server: Received packet from 5D:A2:B5:D0:C7:42, size: 256 bytes
2026-04-11 18:16:15.538 10789-10812 BluetoothG...verManager com.bitchat.droid                    D  Server: Parsed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.538 10789-10812 BluetoothC...ionManager com.bitchat.droid                    D  onPacketReceived: Packet received from 5D:A2:B5:D0:C7:42 (753151ff11d7af0e)
2026-04-11 18:16:15.539 10789-10812 PacketProcessor         com.bitchat.droid                    D  processPacket 48
2026-04-11 18:16:15.539 10789-10900 PacketUplinkManager     com.bitchat.droid                    D   正在上傳封包 (Type: 48, Format: Binary) 至伺服器...
2026-04-11 18:16:15.539 10789-10905 PacketProcessor         com.bitchat.droid                    D  📦 Processing packet type 48 from 753151ff11d7af0e (serialized)
2026-04-11 18:16:15.539 10789-10905 SecurityManager         com.bitchat.droid                    D  Dropping duplicate packet: 1775902576815-753151ff11d7af0e--460352167
2026-04-11 18:16:15.539 10789-10905 PacketProcessor         com.bitchat.droid                    D  Packet failed security validation from 753151ff11d7af0e
2026-04-11 18:16:15.539 10789-10905 PacketProcessor         com.bitchat.droid                    D  Completed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.607 10789-10789 BluetoothG...entManager com.bitchat.droid                    D  Deduplication: Peer d5d6404fea11f30d is already connected (ignoring 40:13:68:D2:FC:B7)
2026-04-11 18:16:15.617 10789-10812 BluetoothG...entManager com.bitchat.droid                    I  Client: Received packet from 40:13:68:D2:FC:B7, size: 256 bytes
2026-04-11 18:16:15.617 10789-10812 BluetoothG...entManager com.bitchat.droid                    D  Client: Parsed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.617 10789-10812 BluetoothC...ionManager com.bitchat.droid                    D  onPacketReceived: Packet received from 40:13:68:D2:FC:B7 (753151ff11d7af0e)
2026-04-11 18:16:15.618 10789-10905 PacketUplinkManager     com.bitchat.droid                    D   正在上傳封包 (Type: 48, Format: Binary) 至伺服器...
2026-04-11 18:16:15.619 10789-10812 PacketProcessor         com.bitchat.droid                    D  processPacket 48
2026-04-11 18:16:15.619 10789-10899 PacketProcessor         com.bitchat.droid                    D  📦 Processing packet type 48 from 753151ff11d7af0e (serialized)
2026-04-11 18:16:15.620 10789-10899 SecurityManager         com.bitchat.droid                    D  Dropping duplicate packet: 1775902576815-753151ff11d7af0e--460352167
2026-04-11 18:16:15.620 10789-10899 PacketProcessor         com.bitchat.droid                    D  Packet failed security validation from 753151ff11d7af0e
2026-04-11 18:16:15.620 10789-10899 PacketProcessor         com.bitchat.droid                    D  Completed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.623 10789-10812 BluetoothG...entManager com.bitchat.droid                    I  Client: Received packet from 40:13:68:D2:FC:B7, size: 256 bytes
2026-04-11 18:16:15.623 10789-10812 BluetoothG...entManager com.bitchat.droid                    D  Client: Parsed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.623 10789-10812 BluetoothC...ionManager com.bitchat.droid                    D  onPacketReceived: Packet received from 40:13:68:D2:FC:B7 (753151ff11d7af0e)
2026-04-11 18:16:15.624 10789-10899 PacketUplinkManager     com.bitchat.droid                    D   正在上傳封包 (Type: 48, Format: Binary) 至伺服器...
2026-04-11 18:16:15.624 10789-10812 PacketProcessor         com.bitchat.droid                    D  processPacket 48
2026-04-11 18:16:15.624 10789-10915 PacketProcessor         com.bitchat.droid                    D  📦 Processing packet type 48 from 753151ff11d7af0e (serialized)
2026-04-11 18:16:15.625 10789-10915 SecurityManager         com.bitchat.droid                    D  Dropping duplicate packet: 1775902576815-753151ff11d7af0e--460352167
2026-04-11 18:16:15.625 10789-10915 PacketProcessor         com.bitchat.droid                    D  Packet failed security validation from 753151ff11d7af0e
2026-04-11 18:16:15.625 10789-10915 PacketProcessor         com.bitchat.droid                    D  Completed packet type 48 from 753151ff11d7af0e
2026-04-11 18:16:15.881 10789-10789 BluetoothG...entManager com.bitchat.droid                    D  Deduplication: Peer d5d6404fea11f30d is already connected (ignoring 40:13:68:D2:FC:B7)
2026-04-11 18:16:15.940 10789-10903 PacketUplinkManager     com.bitchat.droid                    W   封包上傳失敗: 404 - 
2026-04-11 18:16:15.941 10789-10906 PacketUplinkManager     com.bitchat.droid                    W   封包上傳失敗: 404 - 
2026-04-11 18:16:15.941 10789-10899 PacketUplinkManager     com.bitchat.droid                    W   封包上傳失敗: 404 - 
2026-04-11 18:16:15.941 10789-10900 PacketUplinkManager     com.bitchat.droid                    W   封包上傳失敗: 404 - 
2026-04-11 18:16:15.942 10789-10905 PacketUplinkManager     com.bitchat.droid                    W   封包上傳失敗: 404 - 
