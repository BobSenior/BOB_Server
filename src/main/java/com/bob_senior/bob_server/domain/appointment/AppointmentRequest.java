package com.bob_senior.bob_server.domain.appointment;

import com.bob_senior.bob_server.domain.Post.PostUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

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

}
