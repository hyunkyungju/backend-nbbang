package nbbang.com.nbbang.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import nbbang.com.nbbang.domain.member.dto.Place;
import nbbang.com.nbbang.global.security.Role;

import javax.persistence.*;

import java.time.LocalDateTime;

import static javax.persistence.EnumType.STRING;

@Entity @Getter @Builder
@AllArgsConstructor
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    //@Column(nullable = false)
    private String nickname;

    //@Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    private Role role;


    public Member(String nickname, String email, String picture, Role role) {
        this.nickname = nickname;
        this.email = email;
        this.avatar = picture;
        this.role = role;
    }

    public Member update(String name, String picture) {
        this.nickname = name;
        this.avatar = picture;
        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }

    @Column
    private String avatar;

    private Boolean isLeaved;

    @Enumerated(STRING)
    private Place place;

    @Transient
    private Long activePartyId;

    protected Member() {}

    public void updateMember(String nickname, Place place) {
        this.nickname = nickname;
        this.place = place;
    }

    public void updateMember(String avatar) {
        this.avatar = avatar;
    }

    public String uploadAvatar(String avatar) {
        this.avatar = avatar;
        return avatar;
    }

    public void leaveMember() {
        this.isLeaved = true;
    }

    public static Member createMember(String nickname, Place place) {
        return Member.builder()
                .nickname(nickname)
                .place(place)
                .isLeaved(false)
                .build();
    }

    public void updateActiveParty(Long activePartyId) {
        this.activePartyId = activePartyId;
    }

}
