package com.hireflow.event;

import com.hireflow.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final User user;
    private final String otp;

    public UserRegisteredEvent(Object source, User user, String otp) {
        super(source);
        this.user = user;
        this.otp = otp;
    }
}
