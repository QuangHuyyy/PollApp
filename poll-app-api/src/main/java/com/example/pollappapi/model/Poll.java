package com.example.pollappapi.model;

import com.example.pollappapi.model.audit.UserDateAudit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "polls")
public class Poll extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(length = 150)
    private String question;

    @OneToMany(
            mappedBy = "poll",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true // khi 1 item trong list bi xoa thi cung se xoa trong database
    )
    @Size(min = 2, max = 6)
    /*
    @Fetch annotation dùng để mô tả cách mà Hibernate lấy choices khi nó truy vấn một Poll
    FetchMode.SELECT chỉ ra rằng Hibernate nên tải danh sách các choices theo hướng lazy
    */
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30) // Think you have 100 records in your result and if you are defined batch size as 10 then it will fetch 10 records per db call. Logically it will increase performance.
    private List<Choice> choices = new ArrayList<>();

    @NotNull
    private Instant expirationDateTime;


    public void addChoice(Choice choice){
        choices.add(choice);
        choice.setPoll(this);
    }

    public void removeChoice(Choice choice){
        choices.remove(choice);
        choice.setPoll(null);
    }
}
