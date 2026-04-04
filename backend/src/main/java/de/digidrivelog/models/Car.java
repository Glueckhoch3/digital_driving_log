package de.digidrivelog.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Car")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carId")
    private Long carId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "platenumber", nullable = false, length = 15)
    private String plateNumber;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = true)
    private User owner;

    @Column(name = "data", columnDefinition = "jsonb", nullable = true)
    private JsonNode data;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Drive> drives;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Cost> transactions;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;
}