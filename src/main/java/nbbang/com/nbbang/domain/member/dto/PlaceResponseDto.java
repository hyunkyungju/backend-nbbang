package nbbang.com.nbbang.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponseDto{ // 얘를 어디에 둘지 고민해보기 + 이름.. place가 과연 엔티티인가
    private String place;
}