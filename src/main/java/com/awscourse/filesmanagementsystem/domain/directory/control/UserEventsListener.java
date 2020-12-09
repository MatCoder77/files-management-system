package com.awscourse.filesmanagementsystem.domain.directory.control;

import com.awscourse.filesmanagementsystem.domain.user.entity.User;
import com.awscourse.filesmanagementsystem.infrastructure.event.crud.single.CreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventsListener {

    private final DirectoryService directoryService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    private void createDirectoryForRegisteredUser(CreateEvent<User> event) {

    }

}
