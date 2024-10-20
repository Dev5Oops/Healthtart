package com.dev5ops.healthtart.record_per_user.domain.dto;

import com.dev5ops.healthtart.user.domain.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecordPerUserDTO {

    @JsonProperty("user_record_code")
    private Long userRecordCode;

    @JsonProperty("day_of_exercise")
    private LocalDate dayOfExercise;

    @JsonProperty("exercise_duration")
    private LocalDateTime exerciseDuration;

    @JsonProperty("record_flag")
    private boolean recordFlag;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("user_code")
    private String userCode;

    @JsonProperty("routine_code")
    private Long routineCode;


}


