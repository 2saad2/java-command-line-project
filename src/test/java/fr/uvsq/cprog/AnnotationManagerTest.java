package fr.uvsq.cprog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AnnotationManagerTest {
    @TempDir
    Path tempDir;
    private FileManager fileManager;
    private Commande commande;

    private static final String TEST_ANNOTATION_FILE = "./annotations.json";
    private String backupContent = "";

    @BeforeEach
    public void setUp() throws IOException {
        // Sauvegarder le contenu original du fichier annotations.json
        File annotationFile = new File(TEST_ANNOTATION_FILE);
        if (annotationFile.exists()) {
            backupContent = Files.readString(annotationFile.toPath());
        }

        fileManager = new FileManager();
        commande = new Commande(fileManager);
        // Mettre à jour le répertoire courant du FileManager
        fileManager.updateCurrentDir(tempDir.toString());
    }

    @Test
    public void testannotationAdd()  throws IOException {
        // Chemin de test et annotation
        String testPath = "test/path.txt";
        String testAnnotation = "Test annotation";

        // Ajouter l'annotation
        AnnotationManager.annotationAdd(testPath, testAnnotation);

        // Vérifier que l'annotation est ajoutée
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(TEST_ANNOTATION_FILE));
        boolean annotationFound = false;

        for (JsonNode node : rootNode) {
            if (node.has("Path") && node.get("Path").asText().equals(testPath) &&
                    node.has("annotation") && node.get("annotation").asText().contains(testAnnotation)) {
                annotationFound = true;
                break;
            }
        }
        assertTrue(annotationFound, "L'annotation n'a pas été ajoutée comme prévu.");
    }

    @Test
    public void testAddAnnotationToDirectory() throws IOException {
        // Préparation : Créer des fichiers dans un répertoire temporaire
        Path testDir = tempDir.resolve("testDir");
        Files.createDirectory(testDir);

        List<String> filesToCreate = List.of("file1.txt", "file2.txt", "file3.txt");
        List<String> pathsToCopy = new ArrayList<>();
        List<String> annotationsToCopy = new ArrayList<>();

        for (String fileName : filesToCreate) {
            Path file = testDir.resolve(fileName);
            Files.createFile(file);
            pathsToCopy.add(file.toString());
            annotationsToCopy.add("Annotation pour " + fileName);
        }

        // Action : Ajouter des annotations à ces fichiers
        AnnotationManager.addAnnotationToDirectory(testDir, pathsToCopy, annotationsToCopy);

        // Vérification : Lire le fichier JSON et vérifier les annotations
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode rootNode = (ArrayNode) objectMapper.readTree(new File(TEST_ANNOTATION_FILE));

        for (String path : pathsToCopy) {
            boolean annotationFound = false;
            for (JsonNode node : rootNode) {
                if (node.has("Path") && node.get("Path").asText().contains(path)) {
                    annotationFound = true;
                    break;
                }
            }
            assertTrue(annotationFound, "Annotation non trouvée pour le chemin : " + path);
        }
    }

    @Test
    public void testFindAnnotation() throws IOException {
        String testFilePath = tempDir.resolve("testFile.txt").toString();
        String expectedAnnotation = "Test annotation";

        // Ajouter une annotation pour le test
        AnnotationManager.annotationAdd(testFilePath, expectedAnnotation);

        // Appeler findAnnotation pour récupérer l'annotation
        String foundAnnotation = AnnotationManager.findAnnotation(0, testFilePath, "Stream");

        // Vérifier que l'annotation trouvée correspond à l'annotation attendue
        assertEquals(expectedAnnotation, foundAnnotation);
    }

    @Test
    public void testDeleteAnnotationNER() throws IOException {
        String testFilePath = tempDir.resolve("testFile.txt").toString();
        String testAnnotation = "Test annotation";

        // Ajouter une annotation pour le test
        AnnotationManager.annotationAdd(testFilePath, testAnnotation);

        String testAnnotationFilePath = TEST_ANNOTATION_FILE;
        // Vérifier que l'annotation a été ajoutée
        assertTrue(annotationExists(testFilePath, testAnnotationFilePath));

        // Supprimer l'annotation
        AnnotationManager.deleteAnnotationNER(testFilePath);

        // Vérifier que l'annotation a été supprimée
        assertFalse(annotationExists(testFilePath, testAnnotationFilePath));
    }

    private boolean annotationExists(String filePath, String annotationFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(annotationFilePath));
        for (JsonNode node : rootNode) {
            if (node.has("Path") && node.get("Path").asText().equals(filePath)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testCopyDirectoryWithAnnotations() throws IOException {
        // Création de l'environnement de test
        Path testDir = Files.createDirectory(tempDir.resolve("testDir"));
        Path testFile = Files.createFile(testDir.resolve("testFile.txt"));
        Path subDir = Files.createDirectory(testDir.resolve("subDir"));
        Path subFile = Files.createFile(subDir.resolve("subTestFile.txt"));

        // Ajout d'annotations
        String annotationTestFile = "Annotation pour testFile";
        String annotationSubTestFile = "Annotation pour subTestFile";
        AnnotationManager.annotationAdd(testFile.toString(), annotationTestFile);
        AnnotationManager.annotationAdd(subFile.toString(), annotationSubTestFile);

        // Mise à jour du répertoire courant de FileManager et copie du répertoire
        fileManager.updateCurrentDir(tempDir.toString());
        commande.copyFile(1, false); // Supposons que 1 est le NER pour testDir

        // Récupération des listes
        List<String> copiedAnnotations = fileManager.getannotationToCopy();

        // Obtention des chemins relatifs
        String relativeTestFilePath = testDir.relativize(testFile).toString();
        String relativeSubTestFilePath = testDir.relativize(subFile).toString();

        System.out.println(relativeSubTestFilePath+"    "+relativeTestFilePath);
        // Vérification
        assertTrue(fileManager.getPathToCopy().contains(relativeTestFilePath));
        assertTrue(fileManager.getPathToCopy().contains(relativeSubTestFilePath));
        assertTrue(copiedAnnotations.contains(annotationTestFile), "L'annotation pour testFile n'est pas présente.");
        assertTrue(copiedAnnotations.contains(annotationSubTestFile), "L'annotation pour subTestFile n'est pas présente.");
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Restaurer le contenu original du fichier annotations.json
        Files.writeString(Path.of(TEST_ANNOTATION_FILE), backupContent);
    }
}