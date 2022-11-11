package com.bob_senior.bob_server.domain.appointment.entity;

import com.bob_senior.bob_server.domain.Post.entity.Post;
import com.bob_senior.bob_server.domain.Post.entity.PostUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRequest {

    @EmbeddedId
    private PostUser postUser;

    @Column(name = "status")
    private String status;

    @ManyToOne
    @JoinColumn(name = "postIdx")
    private Post post;

}
