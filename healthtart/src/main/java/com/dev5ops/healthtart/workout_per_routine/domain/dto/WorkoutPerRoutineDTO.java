package com.dev5ops.healthtart.workout_per_routine.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class WorkoutPerRoutineDTO {
    @JsonProperty("workout_per_routine_code")
    private Long workoutPerRoutineCode;

    @JsonProperty("workout_order")
    private int workoutOrder;

    @JsonProperty("weight_set")
    private int weightSet;

    @JsonProperty("number_per_set")
    private int numberPerSet;

    @JsonProperty("weight_per_set")
    private int weightPerSet;

    @JsonProperty("workout_time")
    private int workoutTime;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("record_code")
    private Long recordCode;

    @JsonProperty("exercise_equipment_code")
    private Long exerciseEquipmentCode;
}
