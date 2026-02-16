package fr.iut.fitnesspark.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exercises")
public class Exercises {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La routine est obligatoire")
    @ManyToOne
    @JoinColumn(name = "routine_id", nullable = false)
    @JsonBackReference
    private Routines routine;
    
    @NotNull(message = "Le nom est obligatoire")
    @Size(min = 2, max = 120, message = "Le nom doit contenir entre 2 et 120 caractères")
    @Column(nullable = false, length = 120)
    private String name;
    
    @NotNull(message = "Le nombre de répétitions est obligatoire")
    @Min(value = 1, message = "Le nombre de répétitions doit être au moins 1")
    @Column(nullable = false)
    private Integer repetitions;
    
    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.0", message = "Le poids doit être positif ou nul")
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal weight = BigDecimal.ZERO;
}
