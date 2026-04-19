package com.tomaflow.api.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Setting {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "work_mins", nullable = false)
    @Builder.Default
    private Integer workMins = 25;

    @Column(name = "short_break_mins", nullable = false)
    @Builder.Default
    private Integer shortBreakMins = 5;
    @Column(name = "long_break_mins", nullable = false)
    @Builder.Default
    private Integer longBreakMins = 15;

    @Column(name = "cycles_before_long", nullable = false)
    @Builder.Default
    private Integer cyclesBeforeLong = 4;
}
