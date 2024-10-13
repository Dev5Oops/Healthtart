package com.dev5ops.healthtart.workout_per_routine.service;


import com.dev5ops.healthtart.workout_per_routine.domain.dto.WorkoutPerRoutineDTO;
import com.dev5ops.healthtart.workout_per_routine.domain.entity.WorkoutPerRoutine;
import com.dev5ops.healthtart.workout_per_routine.domain.vo.response.ResponseFindWorkoutPerRoutineVO;
import com.dev5ops.healthtart.workout_per_routine.domain.vo.response.ResponseInsertWorkoutPerRoutineVO;
import com.dev5ops.healthtart.workout_per_routine.repository.WorkoutPerRoutineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkoutPerRoutineServiceImplTests {

    @InjectMocks
    private WorkoutPerRoutineServiceImpl workoutPerRoutineService;

    @Mock
    private WorkoutPerRoutineRepository workoutPerRoutineRepository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("운동 루틴 전체 조회 테스트")
    void getWorkoutPerRoutinesSuccess() {
        WorkoutPerRoutine routine = new WorkoutPerRoutine();
        when(workoutPerRoutineRepository.findAll()).thenReturn(List.of(routine));
        when(modelMapper.map(any(WorkoutPerRoutine.class), eq(ResponseFindWorkoutPerRoutineVO.class)))
                .thenReturn(new ResponseFindWorkoutPerRoutineVO());

        List<ResponseFindWorkoutPerRoutineVO> result = workoutPerRoutineService.getWorkoutPerRoutines();

        assertFalse(result.isEmpty());
        verify(workoutPerRoutineRepository).findAll();
    }

    @Test
    @DisplayName("운동 루틴 단일 조회 테스트")
    void findWorkoutPerRoutineSuccess() {
        WorkoutPerRoutine routine = new WorkoutPerRoutine();
        when(workoutPerRoutineRepository.findById(anyLong())).thenReturn(Optional.of(routine));
        when(modelMapper.map(any(WorkoutPerRoutine.class), eq(ResponseFindWorkoutPerRoutineVO.class)))
                .thenReturn(new ResponseFindWorkoutPerRoutineVO());

        ResponseFindWorkoutPerRoutineVO result = workoutPerRoutineService.findWorkoutPerRoutineByCode(1L);

        assertNotNull(result);
        verify(workoutPerRoutineRepository).findById(1L);
    }


}
