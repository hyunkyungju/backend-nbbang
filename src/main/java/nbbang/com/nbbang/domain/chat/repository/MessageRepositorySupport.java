package nbbang.com.nbbang.domain.chat.repository;

import nbbang.com.nbbang.domain.chat.domain.Message;
import nbbang.com.nbbang.domain.chat.domain.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageRepositorySupport {
    Message findLastMessage(Long partyId);
    Page<Message> findAllByCursorId(Long partyId, Long enterMessageId, Pageable pageable, Long cursorId);
    void bulkNotReadSubtract(Long lastReadId, Long partyId);
    Message findFirstByTypeAndPartyIdAndSenderId(MessageType messageType, Long partyId, Long memberId);
}
