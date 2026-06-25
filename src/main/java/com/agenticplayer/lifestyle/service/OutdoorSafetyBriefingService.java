package com.agenticplayer.lifestyle.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.agenticplayer.lifestyle.tool.LifestyleResponses.AirQualitySnapshot;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.OutdoorSafetyBriefing;
import com.agenticplayer.lifestyle.tool.LifestyleResponses.WeatherSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OutdoorSafetyBriefingService {

    private static final String DEFAULT_ACTIVITY = "일반 외출";
    private static final String DEFAULT_LOCATION = "서울";
    private static final List<LocationAlias> KOREA_LOCATION_FALLBACKS = List.of(
            new LocationAlias("마포", new ResolvedLocation("서울 마포구", 37.5663, 126.9019)),
            new LocationAlias("강남", new ResolvedLocation("서울 강남구", 37.5172, 127.0473)),
            new LocationAlias("송파", new ResolvedLocation("서울 송파구", 37.5145, 127.1059)),
            new LocationAlias("종로", new ResolvedLocation("서울 종로구", 37.5735, 126.9788)),
            new LocationAlias("해운대", new ResolvedLocation("부산 해운대구", 35.1631, 129.1636)),
            new LocationAlias("서울", new ResolvedLocation("서울", 37.5665, 126.9780)),
            new LocationAlias("부산", new ResolvedLocation("부산", 35.1796, 129.0756)),
            new LocationAlias("제주", new ResolvedLocation("제주", 33.4996, 126.5312)),
            new LocationAlias("인천", new ResolvedLocation("인천", 37.4563, 126.7052)),
            new LocationAlias("대구", new ResolvedLocation("대구", 35.8714, 128.6014)),
            new LocationAlias("대전", new ResolvedLocation("대전", 36.3504, 127.3845)),
            new LocationAlias("광주", new ResolvedLocation("광주", 35.1595, 126.8526)),
            new LocationAlias("울산", new ResolvedLocation("울산", 35.5384, 129.3114)),
            new LocationAlias("세종", new ResolvedLocation("세종", 36.4800, 127.2890)));

    private final SafetyDataClient dataClient;

    public OutdoorSafetyBriefingService() {
        this(new OpenMeteoSafetyDataClient(RestClient.create(), new ObjectMapper()));
    }

    OutdoorSafetyBriefingService(SafetyDataClient dataClient) {
        this.dataClient = dataClient;
    }

    public OutdoorSafetyBriefing brief(
            String location,
            Double latitude,
            Double longitude,
            String activity) {
        String safeActivity = blankToDefault(activity, DEFAULT_ACTIVITY);
        ResolvedLocation resolvedLocation = resolveLocation(location, latitude, longitude);
        SafetyMeasurements measurements = dataClient.fetch(resolvedLocation.latitude(), resolvedLocation.longitude());

        List<Finding> findings = evaluate(measurements, safeActivity);
        Finding worst = findings.stream()
                .max(Comparator.comparingInt(Finding::severity))
                .orElse(new Finding(0, "좋음", "특별한 위험 신호가 크지 않습니다."));

        List<String> reasons = findings.stream()
                .filter(finding -> finding.severity() > 0)
                .map(Finding::message)
                .toList();
        if (reasons.isEmpty()) {
            reasons = List.of("비, 대기질, 자외선, 체감온도, 바람 수치에서 큰 주의 요인이 확인되지 않았습니다.");
        }

        return new OutdoorSafetyBriefing(
                resolvedLocation.name(),
                resolvedLocation.latitude(),
                resolvedLocation.longitude(),
                safeActivity,
                worst.label(),
                buildRecommendation(worst.severity(), safeActivity),
                new WeatherSnapshot(
                        measurements.temperatureCelsius(),
                        measurements.apparentTemperatureCelsius(),
                        measurements.humidityPercent(),
                        measurements.precipitationMm(),
                        measurements.precipitationProbabilityPercent(),
                        measurements.uvIndex(),
                        measurements.windSpeedKmh(),
                        weatherCodeDescription(measurements.weatherCode())),
                new AirQualitySnapshot(
                        measurements.pm10(),
                        measurements.pm25(),
                        measurements.usAqi(),
                        airQualityGrade(measurements.usAqi(), measurements.pm25())),
                reasons,
                buildChecklist(measurements, safeActivity),
                List.of(
                        "실시간 관측 지점과 실제 위치의 날씨·대기질은 다를 수 있습니다.",
                        "기상특보, 재난문자, 건강 이상 증상은 공식 안내와 전문가 판단을 우선하세요.",
                        "민감군(영유아, 고령자, 임산부, 호흡기·심혈관 질환자)은 더 보수적으로 판단하세요."),
                List.of(
                        "Open-Meteo Geocoding API",
                        "Open-Meteo Weather Forecast API",
                        "Open-Meteo Air Quality API"));
    }

    private ResolvedLocation resolveLocation(String location, Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            return new ResolvedLocation(blankToDefault(location, "입력 좌표"), latitude, longitude);
        }

        String query = blankToDefault(location, DEFAULT_LOCATION);
        ResolvedLocation fallback = findKoreaFallback(query);
        if (fallback != null) {
            return fallback;
        }
        try {
            return dataClient.geocode(query);
        } catch (IllegalArgumentException exception) {
            ResolvedLocation defaultFallback = findKoreaFallback(DEFAULT_LOCATION);
            if (defaultFallback != null) {
                return new ResolvedLocation(
                        "위치 해석 실패로 기본값 사용: " + defaultFallback.name(),
                        defaultFallback.latitude(),
                        defaultFallback.longitude());
            }
            throw exception;
        }
    }

    private ResolvedLocation findKoreaFallback(String query) {
        String normalized = query.replace(" ", "");
        for (LocationAlias alias : KOREA_LOCATION_FALLBACKS) {
            if (normalized.contains(alias.keyword())) {
                return alias.location();
            }
        }
        return null;
    }

    private List<Finding> evaluate(SafetyMeasurements measurements, String activity) {
        List<Finding> findings = new ArrayList<>();
        String normalizedActivity = activity.toLowerCase(Locale.ROOT);
        boolean exercise = containsAny(normalizedActivity, "운동", "러닝", "달리기", "산책", "등산", "자전거");
        boolean child = containsAny(normalizedActivity, "아이", "아기", "어린이", "등원", "등교");

        int aqiSeverity = measurements.usAqi() >= 151 || measurements.pm25() >= 55 ? 3
                : measurements.usAqi() >= 101 || measurements.pm25() >= 35 ? 2
                : measurements.usAqi() >= 51 || measurements.pm25() >= 15 ? 1
                : 0;
        if ((exercise || child) && aqiSeverity > 0) {
            aqiSeverity = Math.min(3, aqiSeverity + 1);
        }
        if (aqiSeverity > 0) {
            findings.add(new Finding(aqiSeverity, label(aqiSeverity),
                    "대기질 주의: US AQI " + measurements.usAqi() + ", PM2.5 "
                            + round(measurements.pm25()) + "㎍/㎥입니다."));
        }

        int rainSeverity = measurements.precipitationProbabilityPercent() >= 70 || measurements.precipitationMm() >= 3 ? 2
                : measurements.precipitationProbabilityPercent() >= 40 || measurements.precipitationMm() > 0 ? 1
                : 0;
        if (rainSeverity > 0) {
            findings.add(new Finding(rainSeverity, label(rainSeverity),
                    "강수 주의: 강수확률 " + measurements.precipitationProbabilityPercent()
                            + "%, 현재 강수량 " + round(measurements.precipitationMm()) + "mm입니다."));
        }

        int uvSeverity = measurements.uvIndex() >= 8 ? 2 : measurements.uvIndex() >= 6 ? 1 : 0;
        if (uvSeverity > 0) {
            findings.add(new Finding(uvSeverity, label(uvSeverity),
                    "자외선 주의: UV 지수 " + round(measurements.uvIndex()) + "입니다."));
        }

        int heatColdSeverity = measurements.apparentTemperatureCelsius() >= 35 ? 3
                : measurements.apparentTemperatureCelsius() >= 30 ? 2
                : measurements.apparentTemperatureCelsius() <= -10 ? 3
                : measurements.apparentTemperatureCelsius() <= 0 ? 2
                : 0;
        if (heatColdSeverity > 0) {
            findings.add(new Finding(heatColdSeverity, label(heatColdSeverity),
                    "체감온도 주의: 현재 체감온도 " + round(measurements.apparentTemperatureCelsius()) + "℃입니다."));
        }

        int windSeverity = measurements.windSpeedKmh() >= 45 ? 2 : measurements.windSpeedKmh() >= 30 ? 1 : 0;
        if (windSeverity > 0) {
            findings.add(new Finding(windSeverity, label(windSeverity),
                    "바람 주의: 풍속 " + round(measurements.windSpeedKmh()) + "km/h입니다."));
        }

        return findings;
    }

    private String buildRecommendation(int severity, String activity) {
        return switch (severity) {
            case 3 -> activity + "은(는) 가능하면 미루거나 실내 대안을 권합니다. 꼭 나가야 한다면 짧게 다녀오고 상태를 자주 확인하세요.";
            case 2 -> activity + "은(는) 주의가 필요합니다. 시간대를 조정하거나 보호용품을 챙기는 편이 좋습니다.";
            case 1 -> activity + "은(는) 대체로 가능하지만 우산, 마스크, 자외선 차단 등 기본 대비를 챙기세요.";
            default -> activity + "을(를) 하기 무난한 편입니다. 그래도 출발 전 현지 상황을 한 번 더 확인하세요.";
        };
    }

    private List<String> buildChecklist(SafetyMeasurements measurements, String activity) {
        List<String> checklist = new ArrayList<>();
        checklist.add("출발 직전 하늘 상태와 현지 안내를 한 번 더 확인하기");
        if (measurements.precipitationProbabilityPercent() >= 40 || measurements.precipitationMm() > 0) {
            checklist.add("우산 또는 방수 외투 챙기기");
        }
        if (measurements.usAqi() >= 51 || measurements.pm25() >= 15) {
            checklist.add("마스크 착용과 장시간 야외활동 줄이기");
        }
        if (measurements.uvIndex() >= 6) {
            checklist.add("자외선 차단제, 모자, 선글라스 챙기기");
        }
        if (measurements.apparentTemperatureCelsius() >= 30) {
            checklist.add("물 챙기고 그늘에서 휴식하기");
        }
        if (measurements.apparentTemperatureCelsius() <= 0) {
            checklist.add("장갑, 목도리 등 방한용품 챙기기");
        }
        if (containsAny(activity.toLowerCase(Locale.ROOT), "러닝", "운동", "등산", "자전거")) {
            checklist.add("운동 강도를 낮추고 몸 상태가 나쁘면 중단하기");
        }
        return checklist;
    }

    private String airQualityGrade(int usAqi, double pm25) {
        if (usAqi >= 151 || pm25 >= 55) {
            return "나쁨";
        }
        if (usAqi >= 101 || pm25 >= 35) {
            return "민감군 나쁨";
        }
        if (usAqi >= 51 || pm25 >= 15) {
            return "보통";
        }
        return "좋음";
    }

    private String label(int severity) {
        return switch (severity) {
            case 3 -> "위험";
            case 2 -> "나쁨";
            case 1 -> "주의";
            default -> "좋음";
        };
    }

    private String weatherCodeDescription(int code) {
        return switch (code) {
            case 0 -> "맑음";
            case 1, 2, 3 -> "대체로 맑음 또는 구름";
            case 45, 48 -> "안개";
            case 51, 53, 55, 56, 57 -> "이슬비";
            case 61, 63, 65, 66, 67 -> "비";
            case 71, 73, 75, 77 -> "눈";
            case 80, 81, 82 -> "소나기";
            case 85, 86 -> "눈 소나기";
            case 95, 96, 99 -> "뇌우";
            default -> "기상 코드 " + code;
        };
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    interface SafetyDataClient {
        ResolvedLocation geocode(String query);

        SafetyMeasurements fetch(double latitude, double longitude);
    }

    record ResolvedLocation(String name, double latitude, double longitude) {
    }

    private record LocationAlias(String keyword, ResolvedLocation location) {
    }

    record SafetyMeasurements(
            double temperatureCelsius,
            double apparentTemperatureCelsius,
            int humidityPercent,
            double precipitationMm,
            int precipitationProbabilityPercent,
            double uvIndex,
            double windSpeedKmh,
            int weatherCode,
            double pm10,
            double pm25,
            int usAqi) {
    }

    private record Finding(int severity, String label, String message) {
    }

    private static final class OpenMeteoSafetyDataClient implements SafetyDataClient {

        private final RestClient restClient;
        private final ObjectMapper objectMapper;

        private OpenMeteoSafetyDataClient(RestClient restClient, ObjectMapper objectMapper) {
            this.restClient = restClient;
            this.objectMapper = objectMapper;
        }

        @Override
        public ResolvedLocation geocode(String query) {
            URI uri = UriComponentsBuilder.fromUriString("https://geocoding-api.open-meteo.com/v1/search")
                    .queryParam("name", query)
                    .queryParam("count", 1)
                    .queryParam("language", "ko")
                    .queryParam("format", "json")
                    .build()
                    .encode()
                    .toUri();
            JsonNode root = getJson(uri);
            JsonNode first = root.path("results").path(0);
            if (first.isMissingNode()) {
                throw new IllegalArgumentException("위치를 찾을 수 없습니다: " + query);
            }
            String name = first.path("name").asText(query);
            String admin = first.path("admin1").asText("");
            String country = first.path("country").asText("");
            String displayName = List.of(name, admin, country).stream()
                    .filter(value -> !value.isBlank())
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(query);
            return new ResolvedLocation(displayName, first.path("latitude").asDouble(), first.path("longitude").asDouble());
        }

        @Override
        public SafetyMeasurements fetch(double latitude, double longitude) {
            JsonNode weather = getJson(UriComponentsBuilder.fromUriString("https://api.open-meteo.com/v1/forecast")
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m")
                    .queryParam("hourly", "precipitation_probability,uv_index")
                    .queryParam("forecast_days", 1)
                    .queryParam("timezone", "Asia/Seoul")
                    .build()
                    .encode()
                    .toUri());

            JsonNode air = getJson(UriComponentsBuilder.fromUriString("https://air-quality-api.open-meteo.com/v1/air-quality")
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "pm10,pm2_5,us_aqi")
                    .queryParam("timezone", "Asia/Seoul")
                    .build()
                    .encode()
                    .toUri());

            JsonNode current = weather.path("current");
            JsonNode currentAir = air.path("current");
            return new SafetyMeasurements(
                    current.path("temperature_2m").asDouble(),
                    current.path("apparent_temperature").asDouble(),
                    current.path("relative_humidity_2m").asInt(),
                    current.path("precipitation").asDouble(),
                    firstHourlyInt(weather, "precipitation_probability"),
                    firstHourlyDouble(weather, "uv_index"),
                    current.path("wind_speed_10m").asDouble(),
                    current.path("weather_code").asInt(),
                    currentAir.path("pm10").asDouble(),
                    currentAir.path("pm2_5").asDouble(),
                    currentAir.path("us_aqi").asInt());
        }

        private int firstHourlyInt(JsonNode root, String field) {
            return root.path("hourly").path(field).path(0).asInt();
        }

        private double firstHourlyDouble(JsonNode root, String field) {
            return root.path("hourly").path(field).path(0).asDouble();
        }

        private JsonNode getJson(URI uri) {
            String body = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
            try {
                return objectMapper.readTree(body);
            } catch (Exception exception) {
                throw new IllegalStateException("외부 데이터 응답을 해석할 수 없습니다.", exception);
            }
        }
    }
}
