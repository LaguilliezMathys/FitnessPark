package fr.iut.fitnesspark.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exercise_types")
public class ExerciseTypes {
    
    @Id
    private Long id;
    
    @NotNull(message = "Le nom est obligatoire")
    @Size(min = 2, max = 120, message = "Le nom doit contenir entre 2 et 120 caractères")
    @Column(nullable = false, unique = true, length = 120)
    private String name;
}
