package com.example.modernjavainaction.day9;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.nextOrSame;

public class TimeTest {
    // Date : 구린 것 (타임존 설정이 없다.) - 무조건 CET : 유럽 중앙 시간대를 사용, 1900년도부터 시작한다. 달 인덱스가 0부터임
    // Calendar : 덜 구리지만 구린 것 : 위랑 다를바가 없음
    // DateFormat : 스레드 세이프 하지 않음
    // 그리고 Date/Calenar는 불변이 아니다. (가변임)

    // 결론 : util.time 패키지에 있는것을 사용하자
    // LocalDate, LocalTime, LocalDateTime,
    // Instant, Duration, Period

    // Instant는 유닉스 에포크 시간을 사용한다
    // UNIX time 은 UNIX 운영체제를 개발한 벨 연구소에서 정의한 개념이다.
            //
            //Date/Timestamp 데이터형을 Numeric 데이터형으로 표현 시,
            //기존에 Date/Timestamp가 갖는 한계점을 해결할 수 있어 UNIX time 의 개념이 도입되었다.
            //
            //Date/Timestamp 의 한계점
            //1. 로컬 시간대(ex. KST) 명시 필요
            //2. 비연속적, 비선형적인 값이므로 계산 시 변환 필요
            //
            //하필 기준 시간이 1970년 1월 1일 00시 00분 00초 UTC 인 이유는,
            //UNIX 운영체제의 최초 출시년도가 1971년이어서 그렇다.
            //근방에 그럴싸한 시간을 임의로 잡은 것이다.
            //
            //UNIX time 을 epoch time 또는 POSIX time 이라고도 부르는데,
            //이는 epoch 라는 단어가 특정 시대를 구분짓는 기준점(reference point from which time is measured = reference epoch) 이라는 의미를 가졌기 때문이고,
            //POSIX 는 UNIX 운영체제를 기반으로 둔 운영체제 인터페이스여서 같은 의미로 사용된다

    // LocalDateTime은 사람이 사용하도록, Instant는 기계가 사용하도록
    //



    @Test
    void name() {
        LocalDate localDate = LocalDate.of(2013, 1, 1);

        // 다음주 일요일 구하기
        LocalDate nextSunday = localDate.with(nextOrSame(DayOfWeek.SUNDAY));

        // 이번 달 마지막 날 구하기
        LocalDate lastDayOfMonth = localDate.with(lastDayOfMonth());


    }


    // 2014-05-14T 15:33:05.941  n+01:00[Europe/London]
    // [LocalDate] [LocalTime]  [ZoneId]
    // [    LocalDateTime   ]
    // [       ZonedDateTime        ]
}
