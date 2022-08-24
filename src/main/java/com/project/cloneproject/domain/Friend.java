package com.project.cloneproject.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member fromMember;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Member toMember;

    public Friend(Member member, Member friend) {
        this.fromMember = member;
        this.toMember = friend;
    }

}
