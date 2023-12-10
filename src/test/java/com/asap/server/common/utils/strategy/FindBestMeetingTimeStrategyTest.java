package com.asap.server.common.utils.strategy;

import com.asap.server.common.utils.strategy.impl.FindBestMeetingTimeStrategyImpl;
import com.asap.server.domain.enums.Duration;
import com.asap.server.service.vo.BestMeetingTimeVo;
import com.asap.server.service.vo.TimeBlockVo;
import com.asap.server.service.vo.TimeBlocksByDateVo;
import com.asap.server.service.vo.UserVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.asap.server.domain.enums.TimeSlot.SLOT_11_00;
import static com.asap.server.domain.enums.TimeSlot.SLOT_11_30;
import static com.asap.server.domain.enums.TimeSlot.SLOT_12_00;
import static com.asap.server.domain.enums.TimeSlot.SLOT_12_30;
import static com.asap.server.domain.enums.TimeSlot.SLOT_13_00;
import static com.asap.server.domain.enums.TimeSlot.SLOT_13_30;
import static com.asap.server.domain.enums.TimeSlot.SLOT_14_00;
import static com.asap.server.domain.enums.TimeSlot.SLOT_14_30;
import static com.asap.server.domain.enums.TimeSlot.SLOT_20_00;
import static com.asap.server.domain.enums.TimeSlot.SLOT_20_30;
import static com.asap.server.domain.enums.TimeSlot.SLOT_21_00;
import static com.asap.server.domain.enums.TimeSlot.SLOT_21_30;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FindBestMeetingTimeStrategyTest {
    private FindBestMeetingTimeStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FindBestMeetingTimeStrategyImpl();
    }

    @Test
    @DisplayName("모든 유저들이 특정 날짜에 회의 시간이 가능할 경우")
    public void getBestMeetingTime() {
        // given
        UserVo kwy = new UserVo(1L, "KWY");
        UserVo dsy = new UserVo(1L, "DSY");
        List<UserVo> users = List.of(kwy, dsy);

        TimeBlockVo timeBlock = new TimeBlockVo(1L, 0, SLOT_11_00, users);
        TimeBlockVo timeBlock2 = new TimeBlockVo(1L, 0, SLOT_11_30, users);
        TimeBlockVo timeBlock3 = new TimeBlockVo(1L, 0, SLOT_12_00, users);
        TimeBlockVo timeBlock4 = new TimeBlockVo(1L, 0, SLOT_12_30, users);
        TimeBlockVo timeBlock5 = new TimeBlockVo(1L, 0, SLOT_13_00, users);
        TimeBlockVo timeBlock6 = new TimeBlockVo(1L, 0, SLOT_13_30, users);
        TimeBlockVo timeBlock7 = new TimeBlockVo(1L, 0, SLOT_14_00, users);
        List<TimeBlockVo> timeBlocks = new ArrayList<>(Arrays.asList(timeBlock, timeBlock2, timeBlock3, timeBlock4, timeBlock5, timeBlock6, timeBlock7));

        LocalDate meetingDate = LocalDate.of(2023, 9, 8);
        TimeBlocksByDateVo availableDate = new TimeBlocksByDateVo(1L, meetingDate, timeBlocks);

        BestMeetingTimeVo bestMeetingTime = new BestMeetingTimeVo(meetingDate, SLOT_11_00, SLOT_13_00, users, 0);
        BestMeetingTimeVo bestMeetingTime2 = new BestMeetingTimeVo(meetingDate, SLOT_11_30, SLOT_13_30, users, 0);
        BestMeetingTimeVo bestMeetingTime3 = new BestMeetingTimeVo(meetingDate, SLOT_12_00, SLOT_14_00, users, 0);
        BestMeetingTimeVo bestMeetingTime4 = new BestMeetingTimeVo(meetingDate, SLOT_12_30, SLOT_14_30, users, 0);
        List<BestMeetingTimeVo> bestMeetingTimes = new ArrayList<>(List.of(bestMeetingTime, bestMeetingTime2, bestMeetingTime3, bestMeetingTime4));

        // when
        List<BestMeetingTimeVo> result = strategy.find(availableDate, Duration.TWO_HOUR.getNeedBlock(), 2);

        // then
        assertThat(result).isEqualTo(bestMeetingTimes);
    }

    @Test
    @DisplayName("특정 날짜에 최적의 회의 시간대가 아침에 하나 저녁에 하나 있을 경우")
    public void getBestMeetingTime2() {
        // given
        UserVo kwy = new UserVo(1L, "KWY");
        UserVo dsy = new UserVo(1L, "DSY");
        List<UserVo> users = List.of(kwy, dsy);

        TimeBlockVo timeBlock = new TimeBlockVo(1L, 0, SLOT_11_00, users);
        TimeBlockVo timeBlock2 = new TimeBlockVo(1L, 0, SLOT_11_30, users);
        TimeBlockVo timeBlock3 = new TimeBlockVo(1L, 0, SLOT_12_00, users);

        TimeBlockVo timeBlock4 = new TimeBlockVo(1L, 0, SLOT_12_30, List.of(kwy));
        TimeBlockVo timeBlock5 = new TimeBlockVo(1L, 0, SLOT_13_00, List.of(kwy));
        TimeBlockVo timeBlock6 = new TimeBlockVo(1L, 0, SLOT_13_30, List.of(kwy));

        TimeBlockVo timeBlock7 = new TimeBlockVo(1L, 0, SLOT_20_00, users);
        TimeBlockVo timeBlock8 = new TimeBlockVo(1L, 0, SLOT_20_30, users);
        TimeBlockVo timeBlock9 = new TimeBlockVo(1L, 0, SLOT_21_00, users);
        List<TimeBlockVo> timeBlocks = new ArrayList<>(Arrays.asList(timeBlock, timeBlock2, timeBlock3, timeBlock4, timeBlock5, timeBlock6, timeBlock7, timeBlock8, timeBlock9));

        LocalDate meetingDate = LocalDate.of(2023, 9, 8);
        TimeBlocksByDateVo availableDate = new TimeBlocksByDateVo(1L, meetingDate, timeBlocks);

        BestMeetingTimeVo bestMeetingTime = new BestMeetingTimeVo(meetingDate, SLOT_11_00, SLOT_12_00, users, 0);
        BestMeetingTimeVo bestMeetingTime2 = new BestMeetingTimeVo(meetingDate, SLOT_11_30, SLOT_12_30, users, 0);
        BestMeetingTimeVo bestMeetingTime3 = new BestMeetingTimeVo(meetingDate, SLOT_20_00, SLOT_21_00, users, 0);
        BestMeetingTimeVo bestMeetingTime4 = new BestMeetingTimeVo(meetingDate, SLOT_20_30, SLOT_21_30, users, 0);
        List<BestMeetingTimeVo> bestMeetingTimes = new ArrayList<>(List.of(bestMeetingTime, bestMeetingTime2, bestMeetingTime3, bestMeetingTime4));

        // when
        List<BestMeetingTimeVo> result = strategy.find(availableDate, Duration.HOUR.getNeedBlock(), 2);

        // then
        assertThat(result).isEqualTo(bestMeetingTimes);
    }
}