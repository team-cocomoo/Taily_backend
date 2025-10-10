package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.MessageData;
import com.cocomoo.taily.entity.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MessageDataRepository extends JpaRepository<MessageData, Long> {
    List<MessageData> findByMessageRoomOrderByCreatedAtAsc(MessageRoom messageRoom);

    Optional<MessageData> findTopByMessageRoomOrderByCreatedAtDesc(MessageRoom room);
}
