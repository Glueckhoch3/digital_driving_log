package de.digidrivelog.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

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

    @Column(name = "platenumber", nullable = false, length = 15)
    private String plateNumber;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User owner;

    @Column(name = "data", columnDefinition = "jsonb")
    private JsonNode data;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Drive> drives;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}