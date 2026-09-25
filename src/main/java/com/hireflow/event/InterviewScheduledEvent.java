package com.hireflow.event;

import com.hireflow.entity.Interview;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InterviewScheduledEvent extends ApplicationEvent {
    private final Interview interview;

    public InterviewScheduledEvent(Object source, Interview interview) {
        super(source);
        this.interview = interview;
    }
}
