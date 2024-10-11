package com.dev5ops.healthtart.exercise_equipment.service;

import com.dev5ops.healthtart.common.exception.CommonException;
import com.dev5ops.healthtart.common.exception.StatusEnum;
import com.dev5ops.healthtart.exercise_equipment.domain.entity.ExerciseEquipment;
import com.dev5ops.healthtart.exercise_equipment.domain.vo.request.RequestEditEquipmentVO;
import com.dev5ops.healthtart.exercise_equipment.domain.dto.ExerciseEquipmentDTO;
import com.dev5ops.healthtart.exercise_equipment.repository.ExerciseEquipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service("exerciseEquipmentService")
public class ExerciseEquipmentService {

    private final ExerciseEquipmentRepository exerciseEquipmentRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ExerciseEquipmentDTO registerEquipment(ExerciseEquipmentDTO equipmentDTO) {
        ExerciseEquipment exerciseEquipment = modelMapper.map(equipmentDTO, ExerciseEquipment.class);

        if(exerciseEquipmentRepository.findByExerciseEquipmentName(exerciseEquipment.getExerciseEquipmentName()).isPresent()) throw new CommonException(StatusEnum.EQUIPMENT_DUPLICATE);

        exerciseEquipment = exerciseEquipmentRepository.save(exerciseEquipment);
        return modelMapper.map(exerciseEquipment, ExerciseEquipmentDTO.class);
    }

    @Transactional
    public ExerciseEquipmentDTO editEquipment(Long exerciseEquipmentCode, RequestEditEquipmentVO request) {
        ExerciseEquipment exerciseEquipment = exerciseEquipmentRepository.findById(exerciseEquipmentCode).orElseThrow(() -> new CommonException(StatusEnum.EQUIPMENT_NOT_FOUND));

        exerciseEquipment.setExerciseEquipmentName(request.getExerciseEquipmentName());
        exerciseEquipment.setBodyPart(request.getBodyPart());
        exerciseEquipment.setExerciseDescription(request.getExerciseDescription());
        exerciseEquipment.setExerciseImage(request.getExerciseImage());
        exerciseEquipment.setRecommendedVideo(request.getRecommendedVideo());
        exerciseEquipment.setUpdatedAt(LocalDateTime.now());

        exerciseEquipment = exerciseEquipmentRepository.save(exerciseEquipment);

        return modelMapper.map(exerciseEquipment, ExerciseEquipmentDTO.class);
    }

    @Transactional
    public void deleteEquipment(Long exerciseEquipmentCode) {
        ExerciseEquipment exerciseEquipment = exerciseEquipmentRepository.findById(exerciseEquipmentCode).orElseThrow(() -> new CommonException(StatusEnum.EQUIPMENT_NOT_FOUND));

        exerciseEquipmentRepository.delete(exerciseEquipment);
    }

    public ExerciseEquipmentDTO findEquipmentByEquipmentCode(Long exerciseEquipmentCode) {
        ExerciseEquipment exerciseEquipment = exerciseEquipmentRepository.findById(exerciseEquipmentCode).orElseThrow(() -> new CommonException(StatusEnum.EQUIPMENT_NOT_FOUND));

        return modelMapper.map(exerciseEquipment, ExerciseEquipmentDTO.class);
    }

    public List<ExerciseEquipmentDTO> findAllEquipment() {
        List<ExerciseEquipment> exerciseEquipments = exerciseEquipmentRepository.findAll();

        return exerciseEquipments.stream()
                .map(exerciseEquipment -> modelMapper.map(exerciseEquipment, ExerciseEquipmentDTO.class))
                .collect(Collectors.toList());
    }
}
