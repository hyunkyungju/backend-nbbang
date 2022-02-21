package nbbang.com.nbbang.global.config;

import lombok.RequiredArgsConstructor;
import nbbang.com.nbbang.global.handler.ChatHandler;
import nbbang.com.nbbang.global.handler.ReplyEchoHandler;
import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.*;


// handler를 등록하는 곳
// https://dev-gorany.tistory.com/212
@Configuration // 설정이다.
@EnableWebSocket // web socket을 가능하게 함
@RequiredArgsConstructor
//@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketConfigurer { // WebSocketMessageBrokerConfigurer

    private final ChatHandler chatHandler;
    private final ReplyEchoHandler replyEchoHandler;
    // private final static String CHAT_ENDPOINT = "/chat";

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 도메인이 다른 서버에서도 접속 가능하도록 CORS : setAllowedOrigins("*"); 를 추가해준다.
        // 이제 클라이언트가 ws://localhost:8080/chat으로 커넥션을 연결하고 메세지 통신을 할 수 있는 준비를 마쳤다.

        registry.addHandler(chatHandler, "ws/chat").setAllowedOriginPatterns("*");

        // registry.addHandler(replyEchoHandler, "/replyEcho").setAllowedOrigins("*"); //pure Web Socket 방식
        // 소켓 서버의 domain과 사이트의 ref가 달라서 어뷰징일 수 있음.
        // 다른 사이트에서 소켓 쏘는 것을 방지
        // 어디서 오는지 알고 거기만 걸어놔야함.
        // 소켓을 replyEco로 걸어줘야함. ex) var ws = new WebSocket("ws://localhost:8094/replyEcho");

        registry.addHandler(replyEchoHandler, "/replyEcho").setAllowedOriginPatterns("*").withSockJS(); //sockJS 방식


        //sockJS 방식. 클라이언트는 라이브러리 올려야함.

        // 사실 STOMP 방식을 가장 많이 사용함. 구독 방식이므로 하나의 토픽만 보고있으면 된다. 핸드폰에 푸시 보내는 것처럼 구독 방식
    }
}
