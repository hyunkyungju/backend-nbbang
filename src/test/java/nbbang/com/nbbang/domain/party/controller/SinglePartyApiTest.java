package nbbang.com.nbbang.domain.party.controller;

import nbbang.com.nbbang.domain.member.domain.Member;
import nbbang.com.nbbang.domain.member.service.MemberService;
import nbbang.com.nbbang.domain.party.domain.Party;
import nbbang.com.nbbang.domain.party.dto.single.request.PartyRequestDto;
import nbbang.com.nbbang.domain.party.service.PartyService;
import nbbang.com.nbbang.global.error.ErrorResponse;
import nbbang.com.nbbang.global.interceptor.CurrentMember;
import nbbang.com.nbbang.global.response.DefaultResponse;
import nbbang.com.nbbang.global.security.CustomOAuth2MemberService;
import nbbang.com.nbbang.global.validator.PartyMemberValidator;
import nbbang.com.nbbang.global.validator.PartyMemberValidatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static nbbang.com.nbbang.global.response.StatusCode.BAD_REQUEST;
import static nbbang.com.nbbang.global.response.StatusCode.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// @SpringBootTest
// @AutoConfigureMockMvc
@WebMvcTest(PartyController.class)
@MockBean(JpaMetamodelMappingContext.class)
@Import(ControllerTestUtil.class)
class SinglePartyApiTest {

    @Autowired private ControllerTestUtil controllerTestUtil;
    @MockBean private PartyService partyService;
    @MockBean private MemberService memberService;
    @MockBean private CustomOAuth2MemberService customOAuth2MemberService;
    @MockBean private CurrentMember currentMember;
    // @MockBean private PartyMemberValidator partyMemberValidator;
    // @MockBean private PartyMemberValidatorService partyMemberValidatorService;

    @Test
    void createPartySuccess() throws Exception {
        PartyRequestDto successDto = PartyRequestDto.builder().title("hello").place("sinchon").goalNumber(4).hashtags(Arrays.asList("hello", "wow")).build();
        DefaultResponse res = controllerTestUtil.expectDefaultResponseWithDto(post("/parties"), successDto);
        assertThat(res.getStatusCode() == OK);
    }

    @Test
    void createPartyGoalNumberError() throws Exception {
        PartyRequestDto dto = PartyRequestDto.builder().title("hello").place("sinchon").hashtags(Arrays.asList("hello", "wow")).build();
        ErrorResponse res = controllerTestUtil.expectErrorResponseWithDto(post("/parties"), dto);
        assertThat(res.getStatusCode() == BAD_REQUEST);
    }

    @Test
    void updatePartyGoalNumberSuccess() throws Exception {
        PartyRequestDto dto = PartyRequestDto.builder().title("hello").place("sinchon").hashtags(Arrays.asList("hello", "wow")).build();

        DefaultResponse res = controllerTestUtil.expectDefaultResponseWithDto(patch("/parties/1"), dto);
        assertThat(res.getStatusCode() == OK);
    }


    @Test
    void hashtagError() throws Exception {
        Member memberA = Member.builder().nickname("memberA").build();
        Party partyA = Party.builder().id(1L).title("partyA").build();
        PartyRequestDto dto = PartyRequestDto.builder().title("hello").place("sinchon").goalNumber(4).hashtags(Arrays.asList(" ", "wow!")).build();

        ErrorResponse createRes = controllerTestUtil.expectErrorResponseWithDto(post("/parties"), dto);
        assertThat(createRes.getStatusCode() == BAD_REQUEST);

        ErrorResponse updateRes = controllerTestUtil.expectErrorResponseWithDto(patch("/parties/1"), dto);
        assertThat(updateRes.getStatusCode() == BAD_REQUEST);
    }


}