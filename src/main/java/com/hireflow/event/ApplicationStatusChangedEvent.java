package com.hireflow.event;

import com.hireflow.entity.Application;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationStatusChangedEvent extends ApplicationEvent {
    private final Application application;
    private final String previousStatus;

    public ApplicationStatusChangedEvent(Object source, Application application, String previousStatus) {
        super(source);
        this.application = application;
        this.previousStatus = previousStatus;
    }
}
