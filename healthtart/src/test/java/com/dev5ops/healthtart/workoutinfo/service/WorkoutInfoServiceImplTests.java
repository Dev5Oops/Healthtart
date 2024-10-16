package com.dev5ops.healthtart.workoutinfo.service;

import com.dev5ops.healthtart.common.exception.CommonException;
import com.dev5ops.healthtart.common.exception.StatusEnum;
import com.dev5ops.healthtart.routine.domain.entity.Routine;
import com.dev5ops.healthtart.routine.domain.vo.response.ResponseModifyRoutineVO;
import com.dev5ops.healthtart.workoutinfo.domain.dto.WorkoutInfoDTO;
import com.dev5ops.healthtart.workoutinfo.domain.entity.WorkoutInfo;
import com.dev5ops.healthtart.workoutinfo.domain.vo.EditWorkoutInfoVO;
import com.dev5ops.healthtart.workoutinfo.domain.vo.response.ResponseDeleteWorkoutInfoVO;
import com.dev5ops.healthtart.workoutinfo.domain.vo.response.ResponseFindWorkoutInfoVO;
import com.dev5ops.healthtart.workoutinfo.domain.vo.response.ResponseInsertWorkoutInfoVO;
import com.dev5ops.healthtart.workoutinfo.domain.vo.response.ResponseModifyWorkoutInfoVO;
import com.dev5ops.healthtart.workoutinfo.repository.WorkoutInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkoutInfoServiceImplTests {

    @InjectMocks
    private WorkoutInfoServiceImpl workoutInfoService;

    @Mock
    private WorkoutInfoRepository workoutInfoRepository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("운동 정보가 존재할 때 루틴 목록 조회 테스트")
    void getWorkoutInfosSuccess() {
        WorkoutInfo workoutInfo = new WorkoutInfo();
        when(workoutInfoRepository.findAll()).thenReturn(List.of(workoutInfo));
        when(modelMapper.map(any(WorkoutInfo.class), eq(ResponseFindWorkoutInfoVO.class)))
                .thenReturn(new ResponseFindWorkoutInfoVO());

        List<ResponseFindWorkoutInfoVO> result = workoutInfoService.getWorkoutInfos();

        assertFalse(result.isEmpty());
        verify(workoutInfoRepository).findAll();
    }

    @Test
    @DisplayName("운동 정보가 없을 때 예외 발생 테스트")
    void getWorkoutInfosFail() {
        when(workoutInfoRepository.findAll()).thenReturn(List.of());

        CommonException exception = assertThrows(CommonException.class,
                () -> workoutInfoService.getWorkoutInfos());
        assertEquals(StatusEnum.ROUTINE_NOT_FOUND, exception.getStatusEnum());
    }

    @Test
    @DisplayName("단일 코드로 운동 정보 조회 테스트")
    void findWorkoutInfoSuccess() {
        WorkoutInfo workoutInfo = new WorkoutInfo();
        when(workoutInfoRepository.findById(anyLong())).thenReturn(Optional.of(workoutInfo));
        when(modelMapper.map(any(WorkoutInfo.class), eq(ResponseFindWorkoutInfoVO.class)))
                .thenReturn(new ResponseFindWorkoutInfoVO());

        ResponseFindWorkoutInfoVO result = workoutInfoService.findWorkoutInfoByCode(1L);

        assertNotNull(result);
        verify(workoutInfoRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 운동 정보를 조회하면 예외 발생 테스트")
    void findWorkoutInfoFail() {
        when(workoutInfoRepository.findById(anyLong())).thenReturn(Optional.empty());

        CommonException exception = assertThrows(CommonException.class,
                () -> workoutInfoService.findWorkoutInfoByCode(1L));
        assertEquals(StatusEnum.ROUTINE_NOT_FOUND, exception.getStatusEnum());
    }

    @Test
    @Transactional
    @DisplayName("운동 정보 등록 테스트")
    void registerWorkoutInfoSuccess() {
        WorkoutInfoDTO workoutInfoDTO = new WorkoutInfoDTO(1L,"김정은도 10kg 감량한 모닝 루틴 !!!",
                60,"http://healthtart.com","삐딱하게 - G-DRAGON",
                LocalDateTime.now(),LocalDateTime.now(),1L);

        WorkoutInfo workoutInfo = WorkoutInfo.builder()
                .workoutInfoCode(workoutInfoDTO.getWorkoutInfoCode())
                .title(workoutInfoDTO.getTitle())
                .time(workoutInfoDTO.getTime())
                .link(workoutInfoDTO.getLink())
                .recommendMusic(workoutInfoDTO.getRecommendMusic())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .routineCode(workoutInfoDTO.getRoutineCode())
                .build();

        when(workoutInfoRepository.save(any(WorkoutInfo.class))).thenReturn(workoutInfo);

        ResponseInsertWorkoutInfoVO responseVO = new ResponseInsertWorkoutInfoVO();
        when(modelMapper.map(any(WorkoutInfo.class), eq(ResponseInsertWorkoutInfoVO.class)))
                .thenReturn(responseVO);

        ResponseInsertWorkoutInfoVO result = workoutInfoService.registerWorkoutInfo(workoutInfoDTO);

        assertNotNull(result);
        verify(workoutInfoRepository).save(any(WorkoutInfo.class));
        verify(modelMapper).map(any(WorkoutInfo.class), eq(ResponseInsertWorkoutInfoVO.class));
    }

    @Test
    @Transactional
    @DisplayName("운동 정보 수정 테스트")
    void modifyWorkoutInfoSuccess() {
        Long workoutInfoCode = 1L;

        EditWorkoutInfoVO modifyWorkoutInfo = new EditWorkoutInfoVO("김정은도 10kg 감량한 저녁 루틴 !!!", 90,LocalDateTime.now());

        WorkoutInfo workoutInfo = new WorkoutInfo();
        when(workoutInfoRepository.findById(workoutInfoCode)).thenReturn(Optional.of(workoutInfo));

        ResponseModifyWorkoutInfoVO responseVO = new ResponseModifyWorkoutInfoVO();
        when(modelMapper.map(any(WorkoutInfo.class), eq(ResponseModifyWorkoutInfoVO.class)))
                .thenReturn(responseVO);

        ResponseModifyWorkoutInfoVO result = workoutInfoService.modifyWorkoutInfo(workoutInfoCode, modifyWorkoutInfo);

        assertNotNull(result);
        verify(workoutInfoRepository).findById(workoutInfoCode);
        verify(workoutInfoRepository).save(any(WorkoutInfo.class));
        verify(modelMapper).map(any(WorkoutInfo.class), eq(ResponseModifyWorkoutInfoVO.class));
    }


    @Test
    @Transactional
    @DisplayName("운동 정보 삭제 테스트")
    void deleteWorkoutInfoSuccess() {
        Long workoutInfoCode = 1L;
        WorkoutInfo workoutInfo = new WorkoutInfo();
        when(workoutInfoRepository.findById(workoutInfoCode)).thenReturn(Optional.of(workoutInfo));

        ResponseDeleteWorkoutInfoVO result = workoutInfoService.deleteWorkoutInfo(workoutInfoCode);

        assertNotNull(result);
        verify(workoutInfoRepository).findById(workoutInfoCode);
        verify(workoutInfoRepository).delete(workoutInfo);
    }


}