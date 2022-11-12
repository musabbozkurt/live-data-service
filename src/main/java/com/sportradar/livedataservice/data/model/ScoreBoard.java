package com.sportradar.livedataservice.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "score_board")
@Where(clause = "deleted=false")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE score_board SET deleted=true WHERE id=?")
public class ScoreBoard extends BaseEntity {

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private String homeTeamName;

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private String awayTeamName;

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private int homeTeamScore;

    @Column(nullable = false)
    @Audited(withModifiedFlag = true)
    private int awayTeamScore;
}