package nbbang.com.nbbang.global.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import nbbang.com.nbbang.global.response.DefaultResponse;
import nbbang.com.nbbang.global.response.StatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Login", description = "로그인 GET 요청시 로그인 페이지로 리다이렉트 되며, /logout 으로 GET 요청시 로그아웃 됩니다.")
@RestController
@RequiredArgsConstructor
public class LoginController {

    @Operation(summary = "구글 로그인", description = "로그인 페이지로 이동합니다(구글). 올바른 요청 시 자신의 정보를 리턴합니다. 직접 주소로 GET 을 보내세요.")
    @GetMapping("/oauth2/authorization/google")
    public void memberLogin() {
        System.out.println("login");
    }

    @GetMapping("/logout")
    public DefaultResponse logout() {
        return DefaultResponse.res(StatusCode.OK, "로그아웃");
    }
}
