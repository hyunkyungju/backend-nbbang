package nbbang.com.nbbang.domain.chat.controller;

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
import nbbang.com.nbbang.domain.party.domain.Party;
import nbbang.com.nbbang.domain.party.repository.PartyRepository;
import nbbang.com.nbbang.domain.party.service.PartyService;
import nbbang.com.nbbang.global.dto.PageableDto;
import nbbang.com.nbbang.global.response.DefaultResponse;
import nbbang.com.nbbang.global.response.GlobalResponseMessage;
import nbbang.com.nbbang.global.response.StatusCode;
import nbbang.com.nbbang.global.support.FileUpload.FileUploadService;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Chat", description = "채팅방 api (로그인 구현시 올바른 토큰을 보내지 않을 경우 401 Unauthorized 메시지를 받습니다.).")
@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(mediaType = "application/json"))
@Slf4j
@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final FileUploadService fileUploadService;
    private final ChatService chatService;
    private final PartyService partyService;
    private final PartyRepository partyRepository;
    private final ChatService messageService;
    private Long memberId; // 로그인 기능 구현시 삭제 예정

    @Operation(summary = "채팅방 조회", description = "채팅방을 파티 id 로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @GetMapping("/{party-id}")
    public ResponseEntity select(@PathVariable("party-id") Long partyId, @ParameterObject PageableDto pageableDto) {
        Party party = partyRepository.findById(partyId).get(); // Party Service 구현 시 바꿔야 할 것 같습니다.
        Long lastMessageId = chatService.findLastMessageId(partyId);
        Page<Message> messages = chatService.findMessages(partyId, pageableDto.createPageRequest(), lastMessageId);
        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.READ_CHAT, ChatResponseDto.createByPartyEntity(party, lastMessageId, messages.getContent())), HttpStatus.OK);
    }

    @Operation(summary = "채팅 메시지 조회", description = "페이징이 적용된 채팅 메시지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @GetMapping("/{party-id}/messages")
    public ResponseEntity selectChatMessages(@PathVariable("party-id") Long partyId, @ParameterObject PageableDto pageableDto, Long startId) {

        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.READ_CHAT, ChatResponseDto.createMock()), HttpStatus.OK);
    }

    @Operation(summary = "채팅 메시지 전송", description = "채팅 메시지를 전송합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @PostMapping("/{party-id}/messages")
    public ResponseEntity sendMessage(@PathVariable("party-id") Long partyId, @RequestBody ChatMessageSendRequestDto chatMessageSendRequestDto) {
        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.UPLOADED_MESSAGE), HttpStatus.OK);
    }

    @Operation(summary = "메시지 사진 업로드", description = "메시지 사진을 업로드합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatMessageImageUploadResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json"))
    @PostMapping(path = "/{party-id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity sendImage(@PathVariable("party-id") Long partyId,
                                                       @Schema(description = "이미지 파일을 업로드합니다.")
                                                       @RequestPart MultipartFile imgFile) {
        String filePath = fileUploadService.fileUpload(imgFile);
        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.UPLOADED_MESSAGE, new ChatMessageImageUploadResponseDto(filePath)), HttpStatus.OK);
    }

    @Operation(summary = "채팅방에서 나가기", description = "채팅방에서 나갑니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Party Member", content = @Content(mediaType = "application/json"))
    @PostMapping("/{party-id}/out")
    public ResponseEntity exitChat(@PathVariable("party-id") Long partyId) {
        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.EXIT_CHAT), HttpStatus.OK);
    }

    @Operation(summary = "채팅방 상태 변경", description = "방장만 채팅방 속성을 변경할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. Status 를 올바르게 입력하세요.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Owner", content = @Content(mediaType = "application/json"))
    @PatchMapping("/{party-id}/status")
    public ResponseEntity changeStatus(@PathVariable("party-id") Long partyId,
                                       @Validated @RequestBody ChatStatusChangeRequestDto chatStatusChangeRequestDto,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(DefaultResponse.res(StatusCode.BAD_REQUEST, ChatResponseMessage.ILLEGAL_CHAT_STATUS), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.UPDATE_CHAT), HttpStatus.OK);
    }

    @Operation(summary = "채팅방 최대 참여자 수 변경", description = "방장만 채팅방 속성을 변경할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. 참여자 수를 올바르게 입력하세요.", content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Not Owner", content = @Content(mediaType = "application/json"))
    @PatchMapping("/{party-id}/number")
    public ResponseEntity changeGoalNumber(@PathVariable("party-id") Long partyId,
                                           @Schema(description = "채팅방 최대 참여자 수")
                                           @Validated @RequestBody ChatChangeGoalNumberRequestDto chatChangeGoalNumberRequestDto,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity(DefaultResponse.res(StatusCode.BAD_REQUEST, ChatResponseMessage.ILLEGAL_CHAT_GOAL_NUMBER), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(DefaultResponse.res(StatusCode.OK, ChatResponseMessage.UPDATE_CHAT), HttpStatus.OK);
    }

}
