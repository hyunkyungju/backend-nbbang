package nbbang.com.nbbang.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import nbbang.com.nbbang.domain.member.domain.Member;

@Data @Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String avatar;
    private String nickname;
    private Integer breadNumber;
    private String place;

    public static MemberResponseDto createByEntity(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .avatar(member.getAvatar())
                .nickname(member.getNickname())
                .place(member.getPlace().toString())
                .build();
    }

}