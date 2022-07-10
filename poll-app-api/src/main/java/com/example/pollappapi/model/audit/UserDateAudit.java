package com.example.pollappapi.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(
        value = {"createBy", "updateBy"},
        allowGetters = true
)
public class UserDateAudit extends DateAudit{
    @CreatedBy
    @Column(updatable = false) // không thể cập nhật
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;
}
