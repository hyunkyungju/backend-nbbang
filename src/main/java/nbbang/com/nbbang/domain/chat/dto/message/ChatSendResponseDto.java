package nbbang.com.nbbang.domain.chat.dto.message;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbbang.com.nbbang.domain.chat.domain.Message;
import nbbang.com.nbbang.domain.chat.domain.MessageType;
import nbbang.com.nbbang.domain.member.domain.Member;
import nbbang.com.nbbang.domain.member.dto.MemberDto;
import nbbang.com.nbbang.domain.member.dto.MemberSimpleResponseDto;
import nbbang.com.nbbang.domain.party.domain.Party;
import nbbang.com.nbbang.global.interceptor.CurrentMember;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Slf4j
public class ChatSendResponseDto {
    private Long id;
    private LocalDateTime createTime;
    private Integer notReadNumber;
    private MessageType type;
    private String content;
    private Boolean isSender;
    private ChatSendResponseSenderDto sender;

    public static ChatSendResponseDto createByMessage(Message message, Long memberId) {
        Integer partyMemberNumber =message.getParty().countPartyMemberNumber();
        return ChatSendResponseDto.builder()
                .id(message.getId())
                .createTime(message.getCreateTime())
                .notReadNumber(partyMemberNumber - message.getReadNumber())
                .type(message.getType()!=null?message.getType():MessageType.CHAT)
                .content(message.getContent())
                .sender(message.getSender()!=null?
                        ChatSendResponseSenderDto.builder().id(message.getSender().getId())
                                .nickname(message.getSender().getNickname()).build() :null)
                .isSender(memberId.equals(message.getSender().getId()))
                .build();
    }

}
