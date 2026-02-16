# API REST - Fitness IUT

## Base URL
```
http://localhost:8081/api
```

## Endpoints

### 1. Liste des routines
**GET** `/api/routines`

**Paramètres optionnels (query string):**
- `page` (Integer) : Numéro de page (commence à 0)
- `size` (Integer) : Taille de la page
- `mc` (String) : Mot-clé de recherche (nom de routine)

**Exemples:**
```bash
# Toutes les routines
GET http://localhost:8081/api/routines

# Avec pagination
GET http://localhost:8081/api/routines?page=0&size=5

# Avec recherche
GET http://localhost:8081/api/routines?mc=cardio

# Recherche + pagination
GET http://localhost:8081/api/routines?page=0&size=5&mc=routine
```

**Réponse (sans pagination):**
```json
[
  {
    "id": 1,
    "name": "Routine Matinale",
    "description": "Routine pour réveiller le corps et l'esprit.",
    "creationDate": "2025-01-15",
    "status": "active",
    "exercises": [...]
  }
]
```

**Réponse (avec pagination):**
```json
{
  "content": [...],
  "totalElements": 9,
  "totalPages": 2,
  "size": 5,
  "number": 0
}
```

---

### 2. Détail d'une routine
**GET** `/api/routines/{id}`

**Exemple:**
```bash
GET http://localhost:8081/api/routines/1
```

**Réponse 200 OK:**
```json
{
  "id": 1,
  "name": "Routine Matinale",
  "description": "Routine pour réveiller le corps et l'esprit.",
  "creationDate": "2025-01-15",
  "status": "active",
  "exercises": [
    {
      "id": 1,
      "name": "Pompes",
      "repetitions": 20,
      "weight": 0.0
    },
    {
      "id": 2,
      "name": "Squats",
      "repetitions": 15,
      "weight": 0.0
    }
  ]
}
```

**Réponse 404 Not Found:** (si l'ID n'existe pas)

---

### 3. Créer une routine
**POST** `/api/routines`

**Headers:**
```
Content-Type: application/json
```

**Body (exemple):**
```json
{
  "name": "Nouvelle Routine",
  "description": "Ma routine personnalisée",
  "creationDate": "2026-02-12",
  "status": "active"
}
```

**Réponse 201 Created:**
```json
{
  "id": 10,
  "name": "Nouvelle Routine",
  "description": "Ma routine personnalisée",
  "creationDate": "2026-02-12",
  "status": "active",
  "exercises": []
}
```

**Réponse 400 Bad Request:** (si validation échoue)

---

### 4. Supprimer une routine
**DELETE** `/api/routines/{id}`

**Exemple:**
```bash
DELETE http://localhost:8081/api/routines/10
```

**Réponse 204 No Content:** (succès)

**Réponse 404 Not Found:** (si l'ID n'existe pas)

---

## Tests avec curl

```bash
# Liste
curl http://localhost:8081/api/routines

# Détail
curl http://localhost:8081/api/routines/1

# Créer
curl -X POST http://localhost:8081/api/routines \
  -H "Content-Type: application/json" \
  -d '{"name":"Test API","description":"Via curl","creationDate":"2026-02-12","status":"active"}'

# Supprimer
curl -X DELETE http://localhost:8081/api/routines/10
```

## Tests avec Postman

Importer cette collection ou créer les requêtes manuellement avec les exemples ci-dessus.

---

## Notes importantes

1. **Cascade DELETE** : La suppression d'une routine supprime automatiquement tous ses exercices
2. **Relations** : Les exercices sont inclus dans la réponse (attention aux boucles de sérialisation)
3. **Pagination** : Optionnelle - si non spécifiée, retourne toutes les routines
4. **Validation** : Les contraintes Bean Validation sont appliquées (voir modèle Routines)
