package com.asap.server.service.vo;

import com.asap.server.domain.TimeBlock;
import com.asap.server.domain.TimeBlockUser;
import com.asap.server.domain.enums.TimeSlot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@ToString
public class TimeBlockVo implements Comparable<TimeBlockVo> {
    private Long id;
    private int weight;
    private TimeSlot timeSlot;
    private List<UserVo> users;

    public static TimeBlockVo of(TimeBlock timeBlock) {
        return new TimeBlockVo(
                timeBlock.getId(),
                timeBlock.getWeight(),
                timeBlock.getTimeSlot(),
                timeBlock.getTimeBlockUsers()
                        .stream()
                        .map(TimeBlockUser::getUser)
                        .map(UserVo::of)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public int compareTo(@NotNull TimeBlockVo o) {
        return Integer.compare(this.timeSlot.ordinal(), o.timeSlot.ordinal());
    }
}
