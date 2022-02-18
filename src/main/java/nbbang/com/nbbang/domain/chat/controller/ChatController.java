package nbbang.com.nbbang.domain.chat.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbbang.com.nbbang.domain.chat.domain.Message;
import nbbang.com.nbbang.domain.chat.dto.*;
import nbbang.com.nbbang.domain.chat.service.ChatService;
import nbbang.com.nbbang.domain.member.domain.Member;
import nbbang.com.nbbang.domain.member.service.MemberService;
import nbbang.com.nbbang.domain.party.domain.Party;
import nbbang.com.nbbang.domain.party.repository.PartyRepository;
import nbbang.com.nbbang.domain.party.service.PartyService;
import nbbang.com.nbbang.global.dto.CursorPageableDto;
import nbbang.com.nbbang.global.dto.PageableDto;
import nbbang.com.nbbang.global.exception.CustomIllegalArgumentException;
import nbbang.com.nbbang.global.exception.ErrorResponse;
import nbbang.com.nbbang.global.exception.PartyId;
import nbbang.com.nbbang.global.response.DefaultResponse;
import nbbang.com.nbbang.global.response.StatusCode;
import nbbang.com.nbbang.global.support.FileUpload.FileUploadService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Tag(name = "Chat", description = "채팅방 api, 채팅 기능은 미구현입니다. (로그인 구현시 올바른 토큰을 보내지 않을 경우 401 Unauthorized 메시지를 받습니다.).")
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
@Slf4j
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final FileUploadService fileUploadService;
    private final ChatService chatService;
    private final MemberService memberService;
    private final PartyService partyService;
    private final PartyRepository partyRepository;
    private Long memberId = 1L; // 로그인 기능 구현시 삭제 예정



    @Operation(summary = "채팅방 조회", description = "채팅방을 파티 id 로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @GetMapping("/{party-id}")
    public DefaultResponse select(@PathVariable("party-id") Long partyId, @RequestParam(required = false) Integer pageSize) {
        if (pageSize == null) {
            pageSize = 10;
        }
        Party party = partyRepository.findById(partyId).get(); // Party Service 구현 시 바꿔야 할 것 같습니다.
        Page<Message> messages = chatService.findMessages(party, PageRequest.of(0, pageSize));
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.READ_CHAT, ChatResponseDto.createByPartyAndMessagesEntity(party, messages.getContent()));
    }

    @Operation(summary = "채팅 메시지 조회", description = "페이징이 적용된 채팅 메시지를 조회합니다. 쿼리 파라미터로 커서 id 를 전송하면 쿼리에 커서 페이징이 적용됩니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @GetMapping("/{party-id}/messages")
    public DefaultResponse selectChatMessages(@PathVariable("party-id") Long partyId, @ParameterObject PageableDto pageableDto, @RequestParam(required = false) Long cursorId) {
        Party party = partyRepository.findById(partyId).get(); // Party Service 구현 시 바꿔야 할 것 같습니다.
        if (cursorId == null) {
            cursorId = chatService.findLastMessage(party).getId();
        }
        Page<Message> messages = chatService.findMessagesByCursorId(party, pageableDto.createPageRequest(), cursorId);
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.READ_CHAT, ChatMessageListResponseDto.createByEntity(messages.getContent()));
    }


    @Operation(summary = "채팅 메시지 전송", description = "채팅 메시지를 전송합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @PostMapping("/{party-id}/messages")
    public DefaultResponse sendMessage(@PathVariable("party-id") Long partyId, @RequestBody ChatMessageSendRequestDto chatMessageSendRequestDto) {
        chatService.sendMessage(memberId, partyId, chatMessageSendRequestDto.getContent(), LocalDateTime.now());
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.UPLOADED_MESSAGE);
    }

    @Operation(summary = "메시지 사진 업로드", description = "메시지 사진을 업로드합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageImageUploadResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json"))
    @PostMapping(path = "/{party-id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DefaultResponse sendImage(@PathVariable("party-id") Long partyId,
                                     @Schema(description = "이미지 파일을 업로드합니다.")
                                     @RequestPart MultipartFile imgFile) {
        String filePath = fileUploadService.fileUpload(imgFile);
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.UPLOADED_MESSAGE, new ChatMessageImageUploadResponseDto(filePath));
    }

    @Operation(summary = "채팅방에서 나가기", description = "채팅방에서 나갑니다. 소켓 종료 용도로 쓰일 것 같습니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @PostMapping("/{party-id}/out")
    public DefaultResponse exitChat(@PathVariable("party-id") Long partyId) {
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.EXIT_CHAT);
    }


    /**
     * 무한 스크롤을 쿠키로 구현한 메소드입니다.
     */
    @Operation(summary = "채팅방 조회-쿠키", description = "채팅방을 파티 id 로 조회합니다. 가장 마지막에 조회한 메시지 id 를 쿠키로 받습니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @GetMapping("/{party-id}/with-cookie")
    public DefaultResponse selectWithCookie(@PathVariable("party-id") Long partyId, @RequestParam(required = false) Integer pageSize, HttpServletResponse response) {
        if (pageSize == null) {
            pageSize = 10;
        }
        Party party = partyRepository.findById(partyId).get(); // Party Service 구현 시 바꿔야 할 것 같습니다.
        Message lastMessage = chatService.findLastMessage(party);
        Page<Message> messages = chatService.findMessagesByCursorId(party, PageRequest.of(0, pageSize), lastMessage.getId());
        Cookie cursorIdCookie = new Cookie("cursorId", partyId.toString()+"="+lastMessage.getId().toString());
        cursorIdCookie.setPath("/");
        cursorIdCookie.setMaxAge(1000000);
        response.addCookie(cursorIdCookie);
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.READ_CHAT, ChatResponseDto.createByPartyAndMessagesEntity(party, messages.getContent()));
    }

    @Operation(summary = "채팅 메시지 조회-쿠키", description = "커서 페이징이 적용된 채팅 메시지를 조회합니다. 클라이언트는 쿠키로 커서 id 를 전송합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @GetMapping("/{party-id}/messages/with-cookie")
    public DefaultResponse selectChatMessagesWithCookie(@PathVariable("party-id") Long partyId, @ParameterObject PageableDto pageableDto, HttpServletRequest request, @CookieValue(value = "cursorId", required = false) Cookie cookie) {
        Party party = partyRepository.findById(partyId).get(); // Party Service 구현 시 바꿔야 할 것 같습니다.
        String [] cookieVals = cookie.getValue().split("=");
        Long cookiePartyId = Long.parseLong(cookieVals[0]);
        Long cursorId = Long.parseLong(cookieVals[1]);
        if (cookiePartyId != partyId) throw new RuntimeException();
        Page<Message> messages = chatService.findMessagesByCursorId(party, pageableDto.createPageRequest(), cursorId);
        return DefaultResponse.res(StatusCode.OK, ChatResponseMessage.READ_CHAT, ChatMessageListResponseDto.createByEntity(messages.getContent()));
    }

}
