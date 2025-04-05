package com.project.email.scheduler.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalendarLogRequest {
    @NotEmpty
    private String eventTitle;

    @NotEmpty
    private String cronExpression;
}
