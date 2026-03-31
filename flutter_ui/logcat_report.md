026-04-01 00:00:41.899 27776-27776 flutter                 com.bitchat.droid                    E  [ERROR:flutter/runtime/dart_vm_initializer.cc(40)] Unhandled Exception: [cloud_firestore/permission-denied] The caller does not have permission to execute the specified operation.
#0      EventChannelExtension.receiveGuardedBroadcastStream (package:_flutterfire_internals/src/exception.dart:67:43)
#1      MethodChannelQuery.snapshots.<anonymous closure> (package:cloud_firestore_platform_interface/src/method_channel/method_channel_query.dart:183:18)
<asynchronous suspension>
2026-04-01 00:00:42.085 27776-7260  BluetoothAdapter        com.bitchat.droid                    D  46479864: getState(). Returning ON
2026-04-01 00:00:42.085 27776-7260  BluetoothAdapter        com.bitchat.droid                    D  46479864: getState(). Returning ON
2026-04-01 00:00:42.086 27776-7260  MeshServiceHolder       com.bitchat.droid                    D  Reusing existing BluetoothMeshService instance
2026-04-01 00:00:47.101 27776-7260  BluetoothAdapter        com.bitchat.droid                    D  46479864: getState(). Returning ON
2026-04-01 00:00:47.101 27776-7260  BluetoothAdapter        com.bitchat.droid                    D  46479864: getState(). Returning ON
2026-04-01 00:00:47.102 27776-7260  MeshServiceHolder       com.bitchat.droid                    D  Reusing existing BluetoothMeshService instance
2026-04-01 00:00:47.407 27776-7956  GoogleApiManager        com.bitchat.droid                    E  Failed to get service from broker.  (Fix with AI)
java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'.
at android.os.Parcel.createExceptionOrNull(Parcel.java:3255)
at android.os.Parcel.createException(Parcel.java:3239)
at android.os.Parcel.readException(Parcel.java:3222)
at android.os.Parcel.readException(Parcel.java:3164)
at bhqm.a(:com.google.android.gms@261133035@26.11.33 (260400-887465546):36)
at bhon.z(:com.google.android.gms@261133035@26.11.33 (260400-887465546):143)
at bgup.run(:com.google.android.gms@261133035@26.11.33 (260400-887465546):42)
at android.os.Handler.handleCallback(Handler.java:1014)
at android.os.Handler.dispatchMessage(Handler.java:102)
at cwvx.mr(:com.google.android.gms@261133035@26.11.33 (260400-887465546):1)
at cwvx.dispatchMessage(:com.google.android.gms@261133035@26.11.33 (260400-887465546):5)
at android.os.Looper.loopOnce(Looper.java:250)
at android.os.Looper.loop(Looper.java:340)
at android.os.HandlerThread.run(HandlerThread.java:107)
2026-04-01 00:00:47.408 27776-7956  GoogleApiManager        com.bitchat.droid                    W  Not showing notification since connectionResult is not user-facing: ConnectionResult{statusCode=DEVELOPER_ERROR, resolution=null, message=null, clientMethodKey=null}
2026-04-01 00:00:50.950 27776-27776 SecureIden...ateManager com.bitchat.droid                    D  Loaded static identity key from secure storage
2026-04-01 00:00:50.957 27776-7257  PacketUplinkManager     com.bitchat.droid                    D   正在上傳封包 (Type: 48, Size: 256 bytes) 至伺服器...
2026-04-01 00:00:50.965 27776-7260  EncryptionService       com.bitchat.droid                    D  ✅ Generated Ed25519 signature (64 bytes)
2026-04-01 00:00:50.965 27776-7260  BluetoothMeshService    com.bitchat.droid                    D  ✅ Signed packet type 2 (signature 64 bytes)
2026-04-01 00:00:50.965 27776-7260  FragmentManager         com.bitchat.droid                    D  🔀 Creating fragments for packet type 2, payload: 222 bytes
2026-04-01 00:00:50.966 27776-7260  FragmentManager         com.bitchat.droid                    D  📦 Encoded to 512 bytes
2026-04-01 00:00:50.966 27776-7260  FragmentManager         com.bitchat.droid                    D  📏 Unpadded to 292 bytes
2026-04-01 00:00:50.971 27776-7253  BluetoothP...roadcaster com.bitchat.droid                    I  Broadcasting packet v1 type 2 to 0 server + 0 client connections
2026-04-01 00:00:51.284 27776-7951  Firestore               com.bitchat.droid                    W  (26.1.2) [WriteStream]: (7f8b950) Stream closed with status: Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}.
2026-04-01 00:00:51.306 27776-7951  Firestore               com.bitchat.droid                    W  (26.1.2) [Firestore]: Write failed at health_reports/ACK6QRxjQUeNXKq4kCsduK1wqFK2: Status{code=PERMISSION_DENIED, description=Missing or insufficient permissions., cause=null}
2026-04-01 00:00:51.854 27776-7257  PacketUplinkManager     com.bitchat.droid                    W   封包上傳失敗: 503 - 