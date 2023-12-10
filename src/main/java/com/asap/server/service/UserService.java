package com.asap.server.service;

import com.asap.server.config.jwt.JwtService;
import com.asap.server.controller.dto.request.AvailableTimeRequestDto;
import com.asap.server.controller.dto.request.HostLoginRequestDto;
import com.asap.server.controller.dto.request.UserMeetingTimeSaveRequestDto;
import com.asap.server.controller.dto.request.UserRequestDto;
import com.asap.server.controller.dto.response.HostLoginResponseDto;
import com.asap.server.controller.dto.response.UserMeetingTimeResponseDto;
import com.asap.server.controller.dto.response.UserTimeResponseDto;
import com.asap.server.domain.AvailableDate;
import com.asap.server.domain.Meeting;
import com.asap.server.domain.User;
import com.asap.server.domain.enums.Role;
import com.asap.server.domain.enums.TimeSlot;
import com.asap.server.exception.Error;
import com.asap.server.exception.model.BadRequestException;
import com.asap.server.exception.model.ConflictException;
import com.asap.server.exception.model.HostTimeForbiddenException;
import com.asap.server.exception.model.NotFoundException;
import com.asap.server.exception.model.UnauthorizedException;
import com.asap.server.repository.MeetingRepository;
import com.asap.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.asap.server.exception.Error.INVALID_MEETING_HOST_EXCEPTION;
import static com.asap.server.exception.Error.MEETING_VALIDATION_FAILED_EXCEPTION;
import static com.asap.server.exception.Error.USER_NOT_FOUND_EXCEPTION;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final TimeBlockService timeBlockService;
    private final TimeBlockUserService timeBlockUserService;
    private final AvailableDateService availableDateService;
    private final MeetingRepository meetingRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public User createUser(final Meeting meeting,
                           final String hostName,
                           final Role role) {
        User user = User.builder()
                .meeting(meeting)
                .name(hostName)
                .role(role)
                .isFixed(false)
                .build();
        userRepository.save(user);
        return user;
    }

    @Transactional
    public UserMeetingTimeResponseDto createHostTime(final Long meetingId,
                                                     final Long userId,
                                                     final List<UserMeetingTimeSaveRequestDto> requestDtos) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException(Error.MEETING_NOT_FOUND_EXCEPTION));
        if (!meeting.authenticateHost(userId))
            throw new UnauthorizedException(INVALID_MEETING_HOST_EXCEPTION);
        if (!timeBlockUserService.isEmptyHostTimeBlock(meeting.getHost()))
            throw new ConflictException(Error.HOST_TIME_EXIST_EXCEPTION);

        isDuplicatedDate(requestDtos);
        requestDtos.forEach(requestDto -> createUserTimeBlock(meeting, meeting.getHost(), requestDto));

        String accessToken = jwtService.issuedToken(meeting.getHost().getId().toString());

        return UserMeetingTimeResponseDto.builder()
                .url(meeting.getUrl())
                .accessToken(accessToken)
                .build();
    }

    @Transactional
    public UserTimeResponseDto createUserTime(final Long meetingId,
                                              final AvailableTimeRequestDto requestDto) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new NotFoundException(Error.MEETING_NOT_FOUND_EXCEPTION));

        User user = createUser(meeting, requestDto.getName(), Role.MEMBER);
        isDuplicatedDate(requestDto.getAvailableTimes());
        requestDto.getAvailableTimes().forEach(availableTime -> createUserTimeBlock(meeting, user, availableTime));

        return UserTimeResponseDto.builder()
                .role(Role.MEMBER.getRole())
                .build();
    }

    public List<String> findUserNameByMeeting(final Meeting meeting) {
        List<User> users = userRepository.findByMeeting(meeting);
        if (users.isEmpty()) {
            throw new NotFoundException(USER_NOT_FOUND_EXCEPTION);
        }
        return users.stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    private void createUserTimeBlock(final Meeting meeting,
                                     final User user,
                                     final UserMeetingTimeSaveRequestDto requestDto) {
        AvailableDate availableDate = availableDateService.findByMeetingAndDate(meeting, requestDto.getMonth(), requestDto.getDay());
        TimeSlot.getTimeSlots(requestDto.getStartTime().ordinal(), requestDto.getEndTime().ordinal() - 1)
                .stream()
                .map(timeSlot -> timeBlockService.searchTimeBlock(timeSlot, availableDate, requestDto.getPriority())).collect(Collectors.toList())
                .forEach(timeBlock -> timeBlock.addTimeBlockUsers(timeBlockUserService.create(timeBlock, user)));
    }

    private void isDuplicatedDate(final List<UserMeetingTimeSaveRequestDto> requestDtoList) {
        Map<String, List<TimeSlot>> meetingTimeAvailable = new HashMap<>();
        for (UserMeetingTimeSaveRequestDto requestDto : requestDtoList) {
            String col = String.format("%s %s %s", requestDto.getMonth(), requestDto.getDay(), requestDto.getDayOfWeek());
            List<TimeSlot> timeSlots = TimeSlot.getTimeSlots(requestDto.getStartTime().ordinal(), requestDto.getEndTime().ordinal() - 1);
            if (meetingTimeAvailable.containsKey(col)) {
                if (meetingTimeAvailable.get(col).stream().anyMatch(timeSlots::contains)) {
                    throw new BadRequestException(Error.DUPLICATED_TIME_EXCEPTION);
                }
            } else {
                meetingTimeAvailable.put(col, timeSlots);
            }
        }
    }

    public List<String> getFixedUsers(final Meeting meeting) {
        return userRepository
                .findByMeetingAndIsFixed(meeting, true)
                .stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }

    public void setFixedUsers(final Meeting meeting, final List<UserRequestDto> users) {
        List<Long> userIds = users.stream()
                .mapToLong(UserRequestDto::getId)
                .boxed()
                .collect(Collectors.toList());
        userRepository.updateUserIsFixedByMeeting(meeting, userIds);
    }

    public int getMeetingUserCount(final Meeting meeting) {
        return userRepository.countByMeeting(meeting);
    }

    @Transactional
    public HostLoginResponseDto loginByHost(
            final Long meetingId,
            final HostLoginRequestDto requestDto
    ) {
        Meeting meeting = meetingRepository.findByIdWithHost(meetingId)
                .orElseThrow(() -> new NotFoundException(Error.MEETING_NOT_FOUND_EXCEPTION));

        if (!meeting.checkHostName(requestDto.getName()))
            throw new UnauthorizedException(Error.INVALID_HOST_ID_PASSWORD_EXCEPTION);

        if (!passwordEncoder.matches(requestDto.getPassword(), meeting.getPassword()))
            throw new UnauthorizedException(Error.INVALID_HOST_ID_PASSWORD_EXCEPTION);

        if (meeting.isConfirmedMeeting())
            throw new ConflictException(MEETING_VALIDATION_FAILED_EXCEPTION);

        HostLoginResponseDto responseDto = HostLoginResponseDto
                .builder()
                .accessToken(jwtService.issuedToken(meeting.getHost().getId().toString()))
                .build();
        if (timeBlockUserService.isEmptyHostTimeBlock(meeting.getHost())) {
            throw new HostTimeForbiddenException(Error.HOST_MEETING_TIME_NOT_PROVIDED, responseDto);
        }

        return responseDto;
    }
}
