package fr.uvsq.cprog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cette classe est est responsable de la gestion des annotations associées à
 * des chemins de fichiers. Elle offre des fonctionnalités pour la gestion des
 * annotations liées à des chemins de fichiers
 */
public class AnnotationManager {
    /**
     * Gestionnaire des annotations associées aux fichiers ou répertoires.
     * Il gère le chargement, la sauvegarde et la gestion des annotations
     * dans un fichier JSON défini par {@code ANNOTATION_FILE_PATH}.
     */
    private static final String ANNOTATION_FILE_PATH = "./annotations.json";
    /**
     * Fichier utilisé pour stocker les annotations. Il est associé
     * au chemin spécifié par {@code ANNOTATION_FILE_PATH}.
     */
    private static final File ANNOTATION_FILE = new File(ANNOTATION_FILE_PATH);
    /**
     * Dernière annotation utilisée dans le FileManager.
     * Cette variable conserve la dernière annotation utilisée.
     */
    private String lastAnnotation = "";
    /**
     * Instance statique du logger (journal d'événements) associée à la classe
     * Commande.
     * Utilisée pour enregistrer des messages de journalisation facilitant
     * le suivi
     * et le débogage
     * des opérations effectuées par la classe Commande.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            AnnotationManager.class);

    /**
     * Constructeur par défaut de la classe AnnotationManager.
     * Ce constructeur est vide et n'effectue aucune initialisation
     * spécifique lors de la création d'une instance de la classe.
     */
    public AnnotationManager() {

    }

    /**
     * Ajoute une annotation associée au chemin spécifié dans le fichier JSON.
     * Si le fichier JSON n'existe pas, il est créé. L'annotation est ajoutée
     * au tableau JSON correspondant au chemin spécifié. Si une annotation avec
     * le même chemin existe déjà, la nouvelle annotation est concaténée à
     * l'annotation existante.
     *
     * @param eRPath     Le chemin associé à l'annotation.
     * @param annotation La nouvelle annotation à ajouter.
     */
    public static void annotationAdd(final String eRPath,
            final String annotation) {
        // Créer le fichier s'il n'existe pas
        if (!ANNOTATION_FILE.exists()) {
            try {
                ANNOTATION_FILE.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Impossible de créer "
                        + "le fichier 'annotations.json'");
                return;
            }
        }
        if (!annotation.equals("")) {
            try {
                // Charger le contenu existant du fichier JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode existingJson = objectMapper
                        .readTree(ANNOTATION_FILE);

                // Créer un tableau JSON s'il n'existe pas
                ArrayNode annotationsArray;
                if (existingJson != null && existingJson.isArray()) {
                    annotationsArray = (ArrayNode) existingJson;
                } else {
                    annotationsArray = objectMapper.createArrayNode();
                }

                // Vérifier si une annotation avec le même Path existe déjà
                boolean existingAnnotation = false;
                for (int i = 0; i < annotationsArray.size(); i++) {
                    JsonNode annotationNode = annotationsArray.get(i);
                    if (annotationNode.has("Path")
                            && annotationNode.get("Path")
                                    .asText()
                                    .equals(eRPath)) {
                        // Concater la nouvelle anno à l'anno existante
                        ((ObjectNode) annotationNode).put(
                                "annotation",
                                annotationNode.get("annotation")
                                        .asText() + " "
                                        + annotation);
                        existingAnnotation = true;

                    }
                }

                // Si une annotation avec le même Path n'existe pas
                if (!existingAnnotation) {
                    ObjectNode annotationObject = objectMapper
                            .createObjectNode();
                    annotationObject.put("Path", eRPath);
                    annotationObject.put("annotation",
                            annotation);
                    annotationsArray.add(annotationObject);
                }

                // Enregistrer le tableau mis à jour dans le fichier JSON
                objectMapper.writeValue(ANNOTATION_FILE, annotationsArray);
                System.out.println("Annotation ajoutée et JSON enregistré "
                        + "avec succès dans le fichier.");
            } catch (final Exception e) {
                LOGGER.error("Erreur lors de l'enregistrement du "
                        + "JSON dans le fichier.");
            }
        }
        return;
    }

    /**
     * Ajoute des annotations aux chemins spécifiés dans le fichier JSON.
     * Pour chaque chemin et annotation fournis, cette méthode utilise la
     * méthode annotationAdd pour ajouter l'annotation au fichier
     * JSON.
     *
     * @param targetPath        Le chemin de destination principal où les
     *                          annotations doivent être ajoutées.
     * @param pathToCopy        La liste des sous-chemins relatifs à
     *                          {@code targetPath}.
     * @param annotationsToCopy La liste des annotations correspondant aux
     *                          sous-chemins.
     */
    public static void addAnnotationToDirectory(final Path targetPath,
            final List<String> pathToCopy,
            final List<String> annotationsToCopy) {
        for (int i = 0; i < pathToCopy.size(); i++) {
            String path = targetPath.toString() + "\\"
                    + pathToCopy.get(i);
            String anno = annotationsToCopy.get(i);
            annotationAdd(path, anno);
        }
    }

    /**
     * Recherche et renvoie l'annotation correspondant à l'index NER spécifié
     * et au chemin ER donné.
     * Si l'annotation est trouvée, elle est renvoyée sous forme de chaîne.
     * Sinon, une chaîne vide est renvoyée.
     *
     * @param indexNER L'index NER spécifié pour la recherche d'annotation.
     * @param eRPath   Le chemin ER (Élément de Référence) pour lequel
     *                 l'annotation doit être trouvée.
     * @param type     Le type de recherche (non utilisé dans cette méthode).
     * @return L'annotation correspondant à l'index NER et au chemin ER
     *         spécifiés, ou une chaîne vide si elle n'est pas trouvée.
     */
    public static String findAnnotation(final int indexNER,
            final String eRPath, final String type) {

        // Créer le fichier s'il n'existe pas
        if (!ANNOTATION_FILE.exists()) {
            try {
                ANNOTATION_FILE.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Impossible de créer le fichier "
                        + "'annotations.json'");
                return ("");
            }
        }

        String annotation = "";
        try {
            // Charger le contenu du fichier JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper
                    .readTree(ANNOTATION_FILE);

            // Récupérer l'annotation par index NER
            JsonNode annotationNode = findAnnotationPerNER(
                    jsonNode, eRPath);

            if (annotationNode != null) {
                annotation = annotationNode
                        .get("annotation").asText();
            }

            // Afficher l'annotation
            if (type == "Stream") {
                if ((annotationNode != null)
                        && (annotationNode.get("annotation")
                                .asText() != "")) {
                    annotation = annotationNode
                            .get("annotation").asText();
                    System.out.println("Annotation à l'index NER "
                            + indexNER + ": " + annotation);
                } else {
                    System.out.println("Aucune annotation trouvée à "
                            + "l'index NER " + indexNER);
                }
            }

        } catch (IOException e) {
            LOGGER.error("Erreur lors de la lecture du JSON "
                    + "depuis le fichier : " + e.getMessage());
        }
        return annotation;
    }

    /**
     * Supprime l'annotation associée au chemin spécifié du fichier JSON
     * d'annotations.
     * Si l'annotation est trouvée, elle est supprimée du fichier, sinon,
     * un message est affiché.
     *
     * @param path Le chemin pour lequel l'annotation doit être supprimée.
     */
    public static void deleteAnnotationNER(final String path) {
        // Créer le fichier s'il n'existe pas
        if (!ANNOTATION_FILE.exists()) {
            try {
                ANNOTATION_FILE.createNewFile();
            } catch (IOException e) {
                LOGGER.error("Impossible de créer le fichier "
                        + "'annotations.json'");
                return;
            }
        }

        try {
            // Charger le contenu du fichier JSON
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(ANNOTATION_FILE);

            if (!(jsonNode instanceof ArrayNode)) {
                System.err.println("Le contenu du fichier JSON n'est pas "
                        + "un tableau.");
                return;
            }
            ArrayNode annotationsArray = (ArrayNode) jsonNode;

            // Trouver l'index de l'annotation à supprimer
            JsonNode annotationNode = findAnnotationPerNER(annotationsArray,
                    path);
            int indexToRemove = -1;
            for (int i = 0; i < annotationsArray.size(); i++) {
                if (annotationsArray.get(i).equals(annotationNode)) {
                    indexToRemove = i;
                    break;
                }
            }

            // Supprimer l'annotation si trouvée
            if (indexToRemove != -1) {
                annotationsArray.remove(indexToRemove);
                objectMapper.writeValue(ANNOTATION_FILE, annotationsArray);
                System.out.println("Annotation supprimée avec succès!");
            } else {
                System.out.println("Aucune annotation trouvée!");
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la lecture/modification"
                    + "/enregistrement du JSON : " + e.getMessage());
        }
    }

    /**
     * Recherche et retourne le nœud JSON d'annotation associé au chemin
     * spécifié dans le fichier JSON d'annotations.
     * Si l'annotation est trouvée, le nœud JSON correspondant est renvoyé,
     * sinon, null est renvoyé.
     *
     * @param jsonNode Le nœud JSON racine du fichier d'annotations.
     * @param path     Le chemin pour lequel l'annotation doit être trouvée.
     * @return Le nœud JSON d'annotation associé au chemin spécifié, ou
     *         null si non trouvé.
     */
    public static JsonNode findAnnotationPerNER(
            final JsonNode jsonNode, final String path) {
        // Normaliser le chemin fourni
        Path normalizedPath = Paths.get(path).normalize();
        for (JsonNode annotationNode : jsonNode) {
            if (annotationNode.has("Path")) {
                String jsonPathString = annotationNode
                        .get("Path").asText();
                // Normaliser le chemin du JSON
                Path jsonPath = Paths.get(jsonPathString)
                        .normalize();
                if (jsonPath.equals(normalizedPath)) {
                    return annotationNode;
                }
            }
        }
        return null;
    }

    /**
     * Recherche et ajoute les chemins et annotations des fichiers à copier
     * dans le répertoire spécifié et ses sous-répertoires.
     *
     * @param directory         Le répertoire à rechercher.
     * @param original          Le chemin d'origine du répertoire à copier.
     * @param pathToCopy        La liste des chemins relatifs des fichiers
     *                          à copier.
     * @param annotationsToCopy La liste des annotations des fichiers à
     *                          copier.
     * @param fileManager       Le gestionnaire de fichiers utilisé pour
     *                          effectuer des opérations sur les fichiers.
     */
    public static void findAddToCopy(final Path directory,
            final Path original, final List<String> pathToCopy,
            final List<String> annotationsToCopy,
            final FileManager fileManager) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                directory)) {
            for (Path entry : stream) {
                System.out.println(original + "----" + entry);
                if (Files.isDirectory(entry)) {
                    findAddToCopy(entry, original, pathToCopy,
                            annotationsToCopy, fileManager);
                } else {

                    pathToCopy.add(FileManager.getRelativePath(entry.toString(),
                            original.toString()));
                    annotationsToCopy.add(findAnnotation(0,
                            entry.toString(), ""));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la recuperation des annotations.", e);
        }

    }

    /**
     * Obtient la dernière annotation utilisée dans ce gestionnaire
     * d'annotations.
     *
     * @return La dernière annotation utilisée.
     */
    public String getLastAnnotation() {
        return lastAnnotation;
    }

    /**
     * Définit la dernière annotation utilisée dans ce gestionnaire
     * d'annotations.
     *
     * @param lastannotation La dernière annotation à définir.
     */
    public void setLastAnnotation(final String lastannotation) {
        this.lastAnnotation = lastannotation;
    }
}
