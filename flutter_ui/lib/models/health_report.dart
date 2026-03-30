class HealthReport {
  final String reporterId;   // 回報者 ID
  final String name;         // 回報者姓名
  final String phone;        // 聯絡電話
  final String? bloodType;   // 血型（可選）
  final String status;       // 健康狀態：'安全' / '輕傷' / '重傷'
  final String? description; // 補充說明（可選）
  final double? lat;         // 緯度（可選）
  final double? lng;         // 經度（可選）
  final DateTime reportTime; // 回報時間

  HealthReport({
    required this.reporterId,
    required this.name,
    required this.phone,
    this.bloodType,
    required this.status,
    this.description,
    this.lat,
    this.lng,
    required this.reportTime,
  });

  Map<String, dynamic> toJson() => {
        'reporterId': reporterId,
        'name': name,
        'phone': phone,
        'bloodType': bloodType,
        'status': status,
        'description': description,
        'lat': lat,
        'lng': lng,
        'reportTime': reportTime.toIso8601String(),
      };

  factory HealthReport.fromJson(Map<String, dynamic> json) => HealthReport(
        reporterId: json['reporterId'] as String,
        name: json['name'] as String,
        phone: json['phone'] as String,
        bloodType: json['bloodType'] as String?,
        status: json['status'] as String,
        description: json['description'] as String?,
        lat: (json['lat'] as num?)?.toDouble(),
        lng: (json['lng'] as num?)?.toDouble(),
        reportTime: DateTime.parse(json['reportTime'] as String),
      );
}
