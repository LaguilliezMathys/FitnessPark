# FitnessPark - AI Coding Agent Instructions

## Contexte du Projet
Application web Spring Boot 4.0.2 de gestion de parc de fitness, utilisant Java 21, Maven, MariaDB et Thymeleaf. Port : 8081.

## Architecture MVC Spring Boot

### Structure des Packages
```
fr.iut.fitnesspark/
├── controller/     # Contrôleurs MVC (@Controller)
├── model/          # Entités JPA (@Entity)
├── repository/     # Repositories Spring Data (JpaRepository)
└── FitnessparkApplication.java
```

### Entités JPA (model/)
**Pattern standard avec Lombok** :
```java
@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "routines")  // Toujours pluriel
public class Routines {     // Nom classe singulier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
}
```

**Relations JPA** :
- **OneToOne** : `@OneToOne @JoinColumn(name = "id_chef")` (côté propriétaire) + `@OneToOne(mappedBy = "chef")` (côté inverse)
- **ManyToOne** : `@ManyToOne @JoinColumn(name = "id_societe")`
- **OneToMany** : `@OneToMany(mappedBy = "societe")` (toujours côté inverse)
- **ManyToMany** : `@ManyToMany @JoinTable(name = "Article_Categorie", joinColumns = ..., inverseJoinColumns = ...)` + `@ManyToMany(mappedBy = "articles")`

⚠️ **Attention** : `toString()` peut provoquer boucle infinie avec relations bidirectionnelles.

### Repositories (repository/)
```java
public interface RoutinesRepository extends JpaRepository<Routines, Long> {
    // Méthodes CRUD auto-générées : save, findAll, findById, deleteById...
    
    // Recherche paginée personnalisée
    Page<Routines> findByNameContaining(String name, Pageable pageable);
    
    // Ou avec JPQL
    @Query("SELECT r FROM Routines r WHERE r.name LIKE :x")
    Page<Routines> rechercher(@Param("x") String mc, Pageable pageable);
}
```

### Contrôleurs (controller/)

**Pattern standard - Liste avec pagination + recherche** :
```java
@Controller
public class RoutinesController {
    private final RoutinesRepository repo;
    
    public RoutinesController(RoutinesRepository repo) {
        this.repo = repo;
    }
    
    @GetMapping("/routines")
    public String routines(
        @RequestParam(name="mc", defaultValue="") String motCle,
        @RequestParam(value="p", defaultValue="0") int page,
        @RequestParam(value="s", defaultValue="5") int size,
        @RequestParam(name="act", defaultValue="") String action,
        @RequestParam(name="id", defaultValue="0") Long id,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Routines> pageRoutines = motCle.isEmpty() 
            ? repo.findAll(pageable)
            : repo.findByNameContaining(motCle, pageable);
        
        model.addAttribute("routines", pageRoutines.getContent());
        model.addAttribute("page", pageRoutines);
        model.addAttribute("motCle", motCle);
        model.addAttribute("action", action);
        
        // Feedback après action (new/mod/del)
        if(!action.isEmpty() && id > 0) {
            repo.findById(id).ifPresent(r -> model.addAttribute("routine", r));
        }
        
        return "routines";
    }
}
```

**Pattern édition/ajout** :
```java
@GetMapping("/routineEdit")
public String edit(
    @RequestParam(name="id", defaultValue="0") Long id,
    @RequestParam(name="mc", defaultValue="") String motCle,
    @RequestParam(value="p", defaultValue="0") int page,
    @RequestParam(value="s", defaultValue="5") int size,
    Model model
) {
    if(id > 0) {
        Optional<Routines> routine = repo.findById(id);
        if(routine.isEmpty()) return "redirect:/routines";
        model.addAttribute("routine", routine.get());
    } else {
        model.addAttribute("routine", new Routines());
    }
    model.addAttribute("p", page);
    model.addAttribute("s", size);
    model.addAttribute("mc", motCle);
    return "routineEdit";
}

@PostMapping("/routineSave")
public String save(
    @Valid Routines routine,
    BindingResult bindingResult,
    @RequestParam(name="mc", defaultValue="") String motCle,
    @RequestParam(value="p", defaultValue="0") int page,
    @RequestParam(value="s", defaultValue="5") int size,
    Model model,
    RedirectAttributes redirectAttributes
) {
    if(bindingResult.hasErrors()) {
        model.addAttribute("p", page);
        model.addAttribute("s", size);
        model.addAttribute("mc", motCle);
        return "routineEdit";
    }
    
    repo.save(routine);
    String act = (routine.getId() == null) ? "new" : "mod";
    
    redirectAttributes.addAttribute("act", act);
    redirectAttributes.addAttribute("id", routine.getId());
    redirectAttributes.addAttribute("p", page);
    redirectAttributes.addAttribute("s", size);
    redirectAttributes.addAttribute("mc", motCle);
    return "redirect:/routines";
}
```

**Pattern suppression** :
```java
@GetMapping("/routineDelete")
public String delete(
    @RequestParam("id") Long id,
    @RequestParam(name="mc", defaultValue="") String motCle,
    @RequestParam(value="p", defaultValue="0") int page,
    @RequestParam(value="s", defaultValue="5") int size,
    RedirectAttributes redirectAttributes
) {
    repo.deleteById(id);
    redirectAttributes.addAttribute("act", "del");
    redirectAttributes.addAttribute("id", id);
    redirectAttributes.addAttribute("p", page);
    redirectAttributes.addAttribute("s", size);
    redirectAttributes.addAttribute("mc", motCle);
    return "redirect:/routines";
}
```

## Thymeleaf - Templates

### Layout Principal (_layout.html)
**Fragments** :
```html
<!-- Header avec paramètres -->
<head th:fragment="header(title,links,scripts)">
    <!-- CDN Bootstrap 5.3.8 / Bootswatch Cyborg / Font Awesome 7.0.1 -->
    <title th:replace="${title}">Basic Fitness Park</title>
    <th:block th:replace="${links}" />
    <th:block th:replace="${scripts}" />
</head>

<!-- Menu navigation -->
<nav th:fragment="menu" class="navbar navbar-expand-lg bg-dark">
    <!-- Navigation -->
</nav>
```

**Utilisation dans les pages** :
```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head th:replace="~{_layout::header(~{::title}, _, ~{::script})}">
    <title>Routines</title>
    <script>
        function confDelRoutine(event) {
            if(!confirm("Confirmer la suppression ?")) {
                event.preventDefault();
            }
        }
    </script>
</head>
<body>
    <nav th:replace="~{_layout::menu}"/>
    <div class="container mt-5">
        <!-- Contenu -->
    </div>
</body>
</html>
```

### Liste avec Pagination + Recherche
```html
<!-- Formulaire recherche -->
<form th:action="@{/routines}" method="get">
    <input type="text" name="mc" th:value="${motCle}" />
    <button type="submit">Rechercher</button>
</form>

<!-- Bouton Ajouter (conserve contexte) -->
<a th:href="@{/routineEdit(id=0, mc=${motCle}, p=${page.number}, s=${page.size})}" 
   class="btn btn-success">Ajouter</a>

<!-- Alertes feedback (après action) -->
<div th:if="${action=='new' || action=='mod'}" class="alert alert-success alert-dismissible">
    <span th:if="${action=='new'}">Création réussie : </span>
    <span th:if="${action=='mod'}">Modification réussie : </span>
    <strong th:text="${routine.name}"></strong>
</div>
<div th:if="${action=='del'}" class="alert alert-warning alert-dismissible">
    Suppression effectuée
</div>

<!-- Table avec boucle -->
<table class="table">
    <tr th:each="routine : ${routines}">
        <td th:text="${routine.name}"></td>
        <td>
            <!-- Éditer -->
            <a th:href="@{/routineEdit(id=${routine.id}, mc=${motCle}, p=${page.number}, s=${page.size})}">
                <i class="fa fa-edit"></i>
            </a>
            <!-- Supprimer avec confirmation -->
            <a th:href="@{/routineDelete(id=${routine.id}, mc=${motCle}, p=${page.number}, s=${page.size})}"
               onclick="confDelRoutine(event)">
                <i class="fa fa-trash"></i>
            </a>
        </td>
    </tr>
</table>

<!-- Pagination (si > 1 page) -->
<ul th:if="${page.totalPages > 1}" class="pagination">
    <li th:class="${page.first} ? 'page-item disabled' : 'page-item'">
        <a th:href="@{/routines(s=${page.size}, p=${page.number-1}, mc=${motCle})}" 
           class="page-link">Précédent</a>
    </li>
    <li th:each="i : ${#numbers.sequence(0, page.totalPages-1)}"
        th:class="${i == page.number} ? 'page-item active' : 'page-item'">
        <a th:href="@{/routines(s=${page.size}, p=${i}, mc=${motCle})}" 
           class="page-link" th:text="${i+1}"></a>
    </li>
    <li th:class="${page.last} ? 'page-item disabled' : 'page-item'">
        <a th:href="@{/routines(s=${page.size}, p=${page.number+1}, mc=${motCle})}" 
           class="page-link">Suivant</a>
    </li>
</ul>

<!-- Select taille page -->
<form method="get">
    <input type="hidden" name="p" th:value="${page.number}" />
    <input type="hidden" name="mc" th:value="${motCle}" />
    <select name="s" onchange="this.form.submit()">
        <option value="5" th:selected="${page.size==5}">5</option>
        <option value="10" th:selected="${page.size==10}">10</option>
        <option value="20" th:selected="${page.size==20}">20</option>
    </select>
</form>
```

### Formulaire Édition/Ajout
```html
<form th:action="@{/routineSave}" th:object="${routine}" method="post">
    <!-- Champs cachés (contexte) -->
    <input type="hidden" th:field="*{id}" />
    <input type="hidden" name="p" th:value="${p}" />
    <input type="hidden" name="s" th:value="${s}" />
    <input type="hidden" name="mc" th:value="${mc}" />
    
    <!-- Champs avec validation -->
    <div>
        <label>Nom</label>
        <input type="text" th:field="*{name}"
               th:class="${#fields.hasErrors('name')} ? 'form-control is-invalid' : 'form-control'" />
        <div th:errors="*{name}" class="invalid-feedback"></div>
    </div>
    
    <div>
        <label>Description</label>
        <textarea th:field="*{description}"
                  th:class="${#fields.hasErrors('description')} ? 'form-control is-invalid' : 'form-control'">
        </textarea>
        <div th:errors="*{description}" class="invalid-feedback"></div>
    </div>
    
    <button type="submit">Valider</button>
    <a th:href="@{/routines(p=${p}, s=${s}, mc=${mc})}" class="btn btn-secondary">Annuler</a>
</form>
```

## Validation Bean

**Annotations dans l'entité** :
```java
@Entity
@Table(name = "routines")
public class Routines {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Size(min=3, max=50)
    private String name;
    
    @NotNull
    @Size(min=10, max=500)
    private String description;
}
```

**Dépendance requise** : `spring-boot-starter-validation`

**Contrôleur** : `@Valid` + `BindingResult` (toujours juste après le paramètre validé)

## Configuration Base de Données

### application.properties
```properties
spring.application.name=Fitness Park
server.port=8081

# MariaDB
spring.datasource.url=jdbc:mariadb://localhost:3306/fitness_iut?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect

# JPA - Décommenter pour générer/modifier tables
# spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

⚠️ **Important** : `ddl-auto=update` uniquement en développement pour créer les tables automatiquement.

### Initialisation Données (CommandLineRunner)
```java
@SpringBootApplication
public class FitnessparkApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FitnessparkApplication.class, args);
    }
    
    @Bean
    CommandLineRunner init(RoutinesRepository repo) {
        return args -> {
            repo.save(new Routines(null, "Cardio", "Programme cardio intensif"));
            repo.save(new Routines(null, "Musculation", "Force et masse"));
            repo.findAll().forEach(System.out::println);
        };
    }
}
```

## Développement

### Lancer l'Application
```bash
.\mvnw.cmd spring-boot:run    # Windows
./mvnw spring-boot:run        # Unix
```
Accès : http://localhost:8081

### Hot Reload avec DevTools
Inclus dans le projet. Modifications Java → restart auto. Modifications templates → reload immédiat.

### Build
```bash
.\mvnw.cmd clean package
```
Génère : `target/fitnesspark-0.0.1-SNAPSHOT.jar`

## Conventions Projet

1. **Langue** : Français (variables, messages, commentaires)
2. **Nommage** : Tables pluriel (`routines`), classes singulier (`Routines`)
3. **Packages** : `fr.iut.fitnesspark.{controller|model|repository}`
4. **Paramètres pagination/recherche** : Toujours conserver `p`, `s`, `mc` dans tous les liens
5. **Feedback utilisateur** : Paramètres `act` + `id` pour afficher alertes Bootstrap après actions
6. **Confirmation suppression** : JavaScript `confirm()` + `event.preventDefault()`
7. **Spring Data REST** : Activé mais non utilisé actuellement (endpoints auto `/routines` disponibles)

## Patterns à Suivre

### Ajouter une Entité Complète
1. **Entité** : `model/NouvelleEntite.java` avec `@Entity`, `@Table`, Lombok
2. **Repository** : `repository/NouvelleEntiteRepository.java extends JpaRepository`
3. **Contrôleur** : Routes CRUD avec pagination/recherche (voir pattern ci-dessus)
4. **Templates** :
   - Liste : `templates/nouvelleEntites.html` (pagination + recherche + actions)
   - Formulaire : `templates/nouvelleEntiteEdit.html` (binding + validation)
5. **Navigation** : Ajouter lien dans `_layout.html` fragment menu

### Testing
Dépendances test incluses. `FitnessparkApplicationTests` actuellement vide - à implémenter selon besoins.
