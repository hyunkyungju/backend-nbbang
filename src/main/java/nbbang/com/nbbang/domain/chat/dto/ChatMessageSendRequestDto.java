package nbbang.com.nbbang.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ChatMessageSendRequestDto {
    private String content;
}