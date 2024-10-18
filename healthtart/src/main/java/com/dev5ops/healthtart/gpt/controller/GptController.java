package com.dev5ops.healthtart.gpt.controller;

import com.dev5ops.healthtart.common.exception.StatusEnum;
import com.dev5ops.healthtart.gpt.service.GptService;
import com.dev5ops.healthtart.user.domain.dto.UserDTO;
import com.dev5ops.healthtart.user.service.UserService;
import com.dev5ops.healthtart.exercise_equipment.service.ExerciseEquipmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gpt")
@RequiredArgsConstructor
public class GptController {

    private final GptService gptService;
    private final UserService userService;
    private final ExerciseEquipmentService exerciseEquipmentService;

    @PostMapping("/generate-routine")
    @Operation(summary = "GPT 운동 루틴 생성")
    public ResponseEntity<String> generateRoutine(@RequestParam String userCode, @RequestParam String bodyPart, @RequestParam int time) {
        try {
            UserDTO userDTO = userService.findById(userCode);
            if (userDTO == null) {
                return ResponseEntity.badRequest().body(StatusEnum.USER_NOT_FOUND.getMessage());
            }

            var equipmentList = exerciseEquipmentService.findByBodyPart(bodyPart);
            if (equipmentList.isEmpty()) {
                return ResponseEntity.badRequest().body(StatusEnum.EQUIPMENT_NOT_FOUND.getMessage());
            }

            String routine = gptService.generateRoutine(userCode, bodyPart, time);
            Map<String, Object> workoutData = gptService.routineParser(routine);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(workoutData);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(StatusEnum.ROUTINES_CREATED_ERROR.getMessage());
        }
    }

    @PostMapping("/process-routine")
    @Operation(summary = "GPT 운동 루틴 저장")
    public ResponseEntity<String> processRoutine(@RequestBody String response) {
        try {
            gptService.processRoutine(response);
            return ResponseEntity.ok("운동 루틴이 성공적으로 저장되었습니다.");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("운동 루틴 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("루틴 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
