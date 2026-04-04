package de.digidrivelog.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(name = "firstname", nullable = false, length = 63)
    private String firstname;

    @Column(name = "lastname", nullable = false, length = 63)
    private String lastname;

    @Column(name = "driverLicense", nullable = false)
    private Boolean driverLicense;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Car> ownedCars;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<Drive> drives;

    @OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL)
    private List<Cost> transactions;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}