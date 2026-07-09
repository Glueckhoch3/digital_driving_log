package de.digidrivelog.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId", nullable = false)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long userId;

    @Column(name = "firstname", nullable = false, length = 63)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 63)
    private String lastname;

    @Column(name = "driverLicense", nullable = false)
    private Boolean driverLicense;

    @Column(name = "birthday", nullable = true)
    private LocalDate birthday;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Car> ownedCars;

    @OneToMany(mappedBy = "driver", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Drive> drives;

    @OneToMany(mappedBy = "buyerId", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Cost> transactions;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
