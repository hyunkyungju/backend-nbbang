package nbbang.com.nbbang.domain.party.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbbang.com.nbbang.domain.bbangpan.domain.MemberParty;
import nbbang.com.nbbang.domain.member.domain.Member;
import nbbang.com.nbbang.domain.member.dto.Place;
import nbbang.com.nbbang.global.support.validation.ValueOfEnum;

import javax.persistence.*;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.*;

@Entity @Getter @Builder
@AllArgsConstructor
public class Party {
    @Id @GeneratedValue
    @Column(name = "party_id")
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createTime;

    private Integer goalNumber;

    //@ValueOfEnum(enumClass = PartyStatus.class)
    @Enumerated(STRING)
    private PartyStatus status;

    //@ValueOfEnum(enumClass = Place.class)
    @Enumerated(STRING)
    private Place place;

    private LocalDateTime cancelTime;

    private Integer deliveryFee;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member owner;

    private Boolean isBlocked;

    @Builder.Default // https://www.inflearn.com/questions/151658
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private List<PartyHashtag> partyHashtags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "party")
    private List<MemberParty> memberParties = new ArrayList<>();

    protected Party() {}

    public void addPartyHashtag(PartyHashtag partyHashtag){
        System.out.println("partyHashtag = " + partyHashtag);
        System.out.println("partyHashtags = " + partyHashtags);
        partyHashtags.add(partyHashtag);
    }

    public void addOwner(Member member) {
        this.owner = member;
    }

    public void addMember(Member member) {
        MemberParty memberParty = MemberParty.createMemberParty(member, this);
        this.getMemberParties().add(memberParty);
    }

    public void changeGoalNumber(Integer goalNumber) {
        this.goalNumber = goalNumber;
    }

    public void changeStatus(PartyStatus status) {
        this.status = status;
    }

}
