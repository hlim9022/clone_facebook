package com.project.cloneproject.repository;

import com.project.cloneproject.domain.Friend;
import com.project.cloneproject.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend,Long> {

    Friend findByFromMemberAndToMember(Member member, Member toMember);
    void deleteByFromMemberAndToMember(Member toMember, Member fromMember);

    List<Friend> findAllByFromMember(Member member);
}
