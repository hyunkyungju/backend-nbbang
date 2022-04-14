package nbbang.com.nbbang.domain.party.service;

import lombok.RequiredArgsConstructor;
import nbbang.com.nbbang.domain.chat.domain.Message;
import nbbang.com.nbbang.domain.chat.repository.MessageRepository;
import nbbang.com.nbbang.domain.hashtag.domain.Hashtag;
import nbbang.com.nbbang.domain.hashtag.domain.PartyHashtag;
import nbbang.com.nbbang.domain.hashtag.repository.HashtagRepository;
import nbbang.com.nbbang.domain.hashtag.service.HashtagService;
import nbbang.com.nbbang.domain.member.domain.Member;
import nbbang.com.nbbang.domain.member.dto.Place;
import nbbang.com.nbbang.domain.member.service.MemberService;
import nbbang.com.nbbang.domain.party.dto.single.request.PartyRequestDto;
import nbbang.com.nbbang.domain.partymember.domain.PartyMember;
import nbbang.com.nbbang.domain.partymember.service.PartyMemberService;
import nbbang.com.nbbang.domain.party.domain.*;
import nbbang.com.nbbang.domain.party.dto.single.PartyUpdateServiceDto;
import nbbang.com.nbbang.domain.hashtag.repository.PartyHashtagRepository;
import nbbang.com.nbbang.domain.party.repository.PartyRepository;
import nbbang.com.nbbang.global.error.exception.NotOwnerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static nbbang.com.nbbang.domain.party.controller.PartyResponseMessage.PARTY_NOT_FOUND;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;
    private final PartyHashtagRepository partyHashtagRepository;
    private final HashtagService hashtagService;
    private final MemberService memberService;
    private final PartyMemberService partyMemberService;
    private final MessageRepository messageRepository;


    @Transactional
    public Party create(PartyRequestDto partyRequestDto, Long memberId) {
        Member owner = memberService.findById(memberId);

        Party party =  Party.builder()
                .title(partyRequestDto.getTitle())
                .content(partyRequestDto.getContent())
                .place(Place.valueOf(partyRequestDto.getPlace().toUpperCase()))
                .goalNumber(partyRequestDto.getGoalNumber())
                .createTime(LocalDateTime.now())
                .status(PartyStatus.OPEN)
                .owner(owner)
                .build();

        List<Hashtag> hashtags = hashtagService.findOrCreateByContent(partyRequestDto.getHashtags());
        List<PartyHashtag> partyHashtags = new ArrayList<>();
        hashtags.stream().forEach(hashtag->partyHashtags.add(PartyHashtag.createPartyHashtag(party, hashtag)));

        Party savedParty = partyRepository.save(party);
        partyMemberService.joinParty(savedParty, owner);

        return savedParty;
    }

    @Transactional
    public Party create(Party party, Long memberId, List<String> hashtagContents) {
        Party savedParty = partyRepository.save(party);
        savedParty.changeStatus(PartyStatus.OPEN);

        List<Hashtag> hashtags = hashtagService.findOrCreateByContent(hashtagContents);
        List<PartyHashtag> partyHashtags = new ArrayList<>();
        hashtags.stream().forEach(hashtag->partyHashtags.add(PartyHashtag.createPartyHashtag(party, hashtag)));


        Member owner = memberService.findById(memberId);
        partyMemberService.joinParty(savedParty, owner);
        party.setOwner(owner);

        return savedParty;
    }


    public Party findById(Long partyId) {
        Party party = partyRepository.findById(partyId).orElseThrow(() -> new NotFoundException(PARTY_NOT_FOUND));
        return party;
    }

    public Party findByIdWithPartyMember(Long partyId) {
        Party party = partyRepository.findWithPartyMember(partyId);
        if (party == null) throw new NotFoundException(PARTY_NOT_FOUND);
        return party;
    }

    public Long findIdByParty(Party party) {
        return Optional.ofNullable(party.getId()).orElseThrow(() -> new NotFoundException("파티의 아이디가 존재하지 않습니다."));
    }

    // 현재 Near, ON, 스스로 아님만 구현. Hashtag로 찾는 기능 추가하기.
    public List<Party> findNearAndSimilar(Long partyId) {
        Party party = findById(partyId);
        Place place = party.getPlace();
        List<Party> parties = partyRepository.findByPlaceAndNotSelf(partyId);
        return parties;
    }

    public List<Party> findNearAndSimilar(Long partyId, Place place) {
        List<Party> parties = partyRepository.findByPlaceAndNotSelf(partyId, place);
        return parties;
    }
    
    @Transactional
    //public Long update(Long partyId, PartyUpdateServiceDto partyUpdateServiceDto) {
    public Long update(Long partyId, PartyUpdateServiceDto partyUpdateServiceDto, Long memberId) {
        Member member = memberService.findById(memberId);
        Party party = findById(partyId);
        if (!party.getOwner().equals(member)) {
            throw new NotOwnerException();
        }
        party.update(partyUpdateServiceDto);
        if (partyUpdateServiceDto.getHashtagContents().isPresent()) {
            List<String> oldHashtagContents = party.getHashtagContents();
            List<String> newHashtagContents = partyUpdateServiceDto.getHashtagContents().get();
            oldHashtagContents.removeAll(newHashtagContents);
            newHashtagContents.removeAll(party.getHashtagContents());

            Optional.ofNullable(oldHashtagContents).orElseGet(Collections::emptyList)
                    .stream().forEach(content -> removeHashtag(partyId, content));
            Optional.ofNullable(newHashtagContents).orElseGet(Collections::emptyList)
                    .stream().forEach(content -> addHashtag(party, content));
        }
        return partyId;
    }

    @Transactional
    public void changeStatus(Party party, Member member, PartyStatus status) {
        if (!party.getOwner().equals(member)) {
            throw new NotOwnerException();
        }
        party.changeStatus(status);
    }

    @Transactional
    public void changeGoalNumber(Party party, Member member, Integer goalNumber) {
        if (!party.getOwner().equals(member)) {
            throw new NotOwnerException();
        }
        party.changeGoalNumber(goalNumber);
    }

    @Transactional
    public void addHashtag(Party party, String content){
        Hashtag hashtag = hashtagService.findOrCreateByContent(content);
        PartyHashtag partyHashtag = PartyHashtag.createPartyHashtag(party, hashtag);
        partyHashtagRepository.save(partyHashtag);
    }


    @Transactional
    public void removeHashtag(Long partyId, String content) {
        Party party = findById(partyId);
        PartyHashtag partyHashtag = party.deletePartyHashtag(content);
        partyHashtagRepository.delete(partyHashtag);
        hashtagService.deleteIfNotReferred(partyHashtag.getHashtag());
    }

    public Message findLastMessage(Long partyId) {
        Message lastMessage = messageRepository.findLastMessage(partyId);
        return Optional.ofNullable(lastMessage).orElse(Message.builder().id(0L).build());
    }


    @Transactional
    public void changeField(Long partyId, Long memberId, Field field, Object value) throws NoSuchFieldException {
        Party party = findById(partyId);
        if(field.equals(Party.getField("deliveryFee"))){
            party.changeDeliveryFee((Integer) value);
        }
        else if(field.equals(Party.getField("accountNumber"))){
            party.changeAccount((Account) value);
        }
    }

    public List<Member> findMembers(Long partyId)  {
        List<PartyMember> partyMembers = findById(partyId).getPartyMembers();

        return partyMembers.stream()
                .map(partyMember -> partyMember.getMember()).collect(Collectors.toList());

    }

}
