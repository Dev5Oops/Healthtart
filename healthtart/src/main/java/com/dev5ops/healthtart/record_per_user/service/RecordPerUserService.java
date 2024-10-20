package com.dev5ops.healthtart.record_per_user.service;

import com.dev5ops.healthtart.common.exception.CommonException;
import com.dev5ops.healthtart.common.exception.StatusEnum;
import com.dev5ops.healthtart.recommended_workout_history.domain.dto.RecommendedWorkoutHistoryDTO;
import com.dev5ops.healthtart.record_per_user.domain.dto.RecordPerUserDTO;
import com.dev5ops.healthtart.record_per_user.domain.entity.RecordPerUser;
import com.dev5ops.healthtart.record_per_user.domain.vo.vo.request.RequestRegisterRecordPerUserVO;
import com.dev5ops.healthtart.record_per_user.repository.RecordPerUserRepository;
import com.dev5ops.healthtart.workout_per_routine.service.WorkoutPerRoutineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service("recordPerUserService")
public class RecordPerUserService {
    private final RecordPerUserRepository recordPerUserRepository;
    private final ModelMapper modelMapper;

    public List<RecordPerUserDTO> findRecordByUserCode(String userCode) {
        List<RecordPerUser> recordPerUser = recordPerUserRepository.findUserByUserCode(userCode);

        if (recordPerUser.isEmpty()) {
            throw new CommonException(StatusEnum.USER_NOT_FOUND);
        }

        return recordPerUser.stream()
                .filter(RecordPerUser::isRecordFlag)
                .map(record -> modelMapper.map(record, RecordPerUserDTO.class))
                .collect(Collectors.toList());
    }

    public List<RecordPerUserDTO> findRecordPerDate(String UserCode, LocalDate dayOfExercise) {
        List<RecordPerUser> recordPerUser = recordPerUserRepository
                .findByUser_UserCodeAndDayOfExercise(UserCode, dayOfExercise);

        if (recordPerUser.isEmpty()) {
            boolean userExists = recordPerUserRepository.existsByUser_UserCode(UserCode);

            if (!userExists) {
                throw new CommonException(StatusEnum.USER_NOT_FOUND);
            }
            throw new CommonException(StatusEnum.DAY_NOT_FOUND);
        }

        return recordPerUser.stream()
                .filter(RecordPerUser::isRecordFlag)
                .map(record -> modelMapper.map(record, RecordPerUserDTO.class))
                .collect(Collectors.toList());
    }

    public RecordPerUserDTO registerRecordPerUser(RequestRegisterRecordPerUserVO requestRegisterRecordPerUserVO){
        RecordPerUser recordPerUser = modelMapper.map(requestRegisterRecordPerUserVO, RecordPerUser.class);
        recordPerUser = recordPerUserRepository.save(recordPerUser);
        return modelMapper.map(recordPerUser, RecordPerUserDTO.class);
    }


    public void deleteRecordPerUser(Long userRecordCode) {
        RecordPerUser recordPerUser = recordPerUserRepository
                .findById(userRecordCode).orElseThrow(() -> new CommonException(StatusEnum.RECORD_NOT_FOUND));

        recordPerUserRepository.delete(recordPerUser);
    }

}
