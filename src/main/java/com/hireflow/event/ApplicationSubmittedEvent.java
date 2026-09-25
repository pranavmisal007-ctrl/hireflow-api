package com.hireflow.event;

import com.hireflow.entity.Application;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ApplicationSubmittedEvent extends ApplicationEvent {
    private final Application application;

    public ApplicationSubmittedEvent(Object source, Application application) {
        super(source);
        this.application = application;
    }
}
