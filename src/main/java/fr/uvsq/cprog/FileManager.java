package fr.uvsq.cprog;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe qui gére différentes opérations liées aux commandes
 * utilisateur, en
 * interagissant avec la classe `FileManager`
 * et la classe `AnnotationManager`.
 */
public class FileManager {
    /**
     * Répertoire courant utilisé par le gestionnaire de fichiers.
     */
    private Path currentDirectory;
    /**
     * Référence au fichier ou répertoire à copier.
     */
    private Path toCopy;
    /**
     * Liste des annotations associées au fichier ou répertoire
     * en cours de copie.
     */
    private List<String> annotationsToCopy = new ArrayList<>();
    /**
     * Liste des chemins associés au fichier ou répertoire en cours
     * de copie.
     */
    private List<String> pathToCopy = new ArrayList<>();
    /**
     * Dernier NER (Numéro d'Élément de Référence) utilisé dans
     * ce gestionnaire de
     * fichiers.
     */
    private int lastNER = -1;
    /**
     * Liste des extensions de fichier d'image prises en charge.
     * Les extensions incluses sont : ".jpg", ".jpeg", ".png", ".gif" et ".bmp".
     * Utilisé pour vérifier si un fichier est une image en fonction de son
     * extension.
     */
    private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp");

    /**
     * Largeur par défaut pour l'affichage d'une image.
     */
    private static final int WIDTH = 400;
    /**
     * Hauteur par défaut pour l'affichage d'une image.
     */
    private static final int HEIGHT = 400;

    /**
     * Instance statique du logger (journal d'événements) associée
     * à la classe
     * FileManager.
     * Utilisée pour enregistrer des messages de journalisation
     * facilitant le suivi
     * et le débogage
     * des opérations effectuées par le gestionnaire de
     * fichiers.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            FileManager.class);

    /**
     * Constructeur de la classe FileManager.
     * Initialise une nouvelle instance de FileManager
     * avec le répertoire courant
     * défini sur le répertoire racine absolu ("Racine").
     */
    public FileManager() {
        this.currentDirectory = Paths.get("Racine").toAbsolutePath();
    }

    /**
     * Vérifie si le fichier spécifié par le chemin filePath est un
     * fichier texte.
     *
     * @param filePath Le chemin du fichier à vérifier.
     * @return true si le fichier est un fichier texte, false sinon.
     */
    public boolean isTextFile(final Path filePath) {
        try {
            String content = Files
                    .readString(filePath, StandardCharsets.UTF_8);

            for (int i = 0; i < content.length();) {
                int codePoint = content.codePointAt(i);

                if (Character.isISOControl(codePoint)
                        && !Character.isWhitespace(codePoint)) {
                    return false;
                }

                i += Character.charCount(codePoint);
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Le fichier n'est pas un fichier texte");
            return false;
        }
    }

    /**
     * Vérifie si le fichier spécifié est une image en fonction de son
     * extension.
     *
     * @param filePath Le chemin du fichier à vérifier.
     * @return {@code true} si le fichier est une image, sinon {@code false}.
     */
    public boolean isImageFile(final Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    /**
     * Affiche le contenu du répertoire courant avec des
     * annotations de couleur
     * pour indiquer le type de chaque élément
     * (fichier texte, dossier, etc.).
     */
    public void displayPathContentsNER() {
        AnsiConsole.systemInstall();
        System.out.println(
                Ansi.ansi().fg(Ansi.Color.CYAN).a(
                        "Chemin courant: " + currentDirectory
                                .toString() + "\n")
                        .reset());
        List<String> contents = getDirectoryContents(
                this.currentDirectory);
        for (int i = 0; i < contents.size(); i++) {
            Path filePath = Paths.get(currentDirectory
                    .toString(), contents.get(i));
            if (Files.isRegularFile(filePath)
                    && isTextFile(filePath)) {
                System.out
                        .println(Ansi.ansi().fg(Ansi.Color.GREEN)
                                .a((i + 1) + " || " + contents
                                        .get(i))
                                .reset());
            } else if (Files.isDirectory(filePath)) {
                System.out.println(
                        Ansi.ansi().fg(Ansi.Color.YELLOW).a((i + 1)
                                + " || " + contents.get(i))
                                .reset());
            } else {
                System.out
                        .println(Ansi.ansi().fg(Ansi.Color.RED)
                                .a((i + 1) + " || " + contents.get(i))
                                .reset());
            }
        }
        AnsiConsole.systemUninstall();
        System.out.println("\n");

    }

    /**
     * Met à jour le répertoire courant en fonction du choix
     * spécifié.
     * Si le choix est différent de "retour",
     * le répertoire courant est résolu
     * en ajoutant le choix comme sous-répertoire.
     * Sinon, si le répertoire parent
     * existe, le répertoire courant est mis à jour avec son parent.
     * Sinon,
     * une exception est lancée car le répertoire racine
     * ne peut pas avoir de
     * parent.
     *
     * @param choice Le choix spécifié pour mettre à jour
     *               le répertoire courant.
     * @throws IllegalArgumentException Si le répertoire courant est
     *                                  déjà le
     *                                  répertoire racine
     *                                  et aucun répertoire parent
     *                                  n'est disponible.
     */
    public void updateCurrentDir(final String choice) {
        if (!choice.equals("retour")) {
            this.currentDirectory = this.currentDirectory
                    .resolve(choice);
        } else {
            if (this.currentDirectory.getParent() != null) {
                this.currentDirectory = this.currentDirectory
                        .getParent();
            } else {
                throw new IllegalArgumentException(
                        "Vous êtes déjà au répertoire racine, "
                                + "il n'y a pas de répertoire parent.");
            }
        }
    }

    /**
     * Récupère la liste des noms des fichiers et sous-répertoires dans le
     * répertoire spécifié.
     *
     * @param directory Le chemin du répertoire dont on souhaite obtenir les
     *                  contenus.
     * @return Une liste des noms des fichiers et sous-répertoires dans le
     *         répertoire spécifié.
     *         Si une erreur d'entrée/sortie se produit, une liste vide est
     *         retournée.
     */
    public List<String> getDirectoryContents(final Path directory) {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la récupération "
                    + "des contenus du répertoire.", e);
            return Collections.emptyList();
        }
    }

    /**
     * Supprime le fichier ou le répertoire spécifié. S'il
     * s'agit d'un répertoire,
     * cette opération
     * est récursive et supprime également tous les fichiers et
     * sous-répertoires.
     *
     * @param path Le chemin du fichier ou du répertoire à supprimer.
     */
    public void deleteFileOrDirectory(final Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (DirectoryStream<Path> directoryStream = Files
                        .newDirectoryStream(path)) {
                    for (Path entry : directoryStream) {
                        deleteFileOrDirectory(entry);
                    }
                }
                Files.delete(path);
                AnnotationManager.deleteAnnotationNER(
                        path.toString());
            } else {
                Files.delete(path);
                AnnotationManager.deleteAnnotationNER(
                        path.toString());
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la suppression du "
                    + "fichier/dossier : " + e.getMessage());
        }
    }

    /**
     * Récupère le chemin du fichier ou du répertoire correspondant
     * au NER (Numéro
     * d'Élément de Référence)
     * spécifié dans le répertoire courant du FileManager.
     *
     * @param ner Le NER du fichier ou du répertoire.
     * @return Le chemin du fichier ou du répertoire correspondant
     *         au NER, ou null
     *         si le NER est invalide.
     */
    public Path getPathByNER(final int ner) {
        List<String> contents = getDirectoryContents(
                this.currentDirectory);
        if (ner >= 1 && ner <= contents.size()) {
            String fileName = contents.get(ner - 1);
            return currentDirectory.resolve(fileName);
        }
        return null;
    }

    /**
     * Obtient le chemin relatif d'un chemin complet par
     * rapport à un chemin de
     * base.
     *
     * @param fullPath Le chemin complet.
     * @param basePath Le chemin de base.
     * @return Le chemin relatif par rapport au chemin de base.
     */
    public static String getRelativePath(final String fullPath,
            final String basePath) {
        Path fullPathObj = Paths.get(fullPath);
        Path basePathObj = Paths.get(basePath);

        Path relativePathObj = basePathObj
                .relativize(fullPathObj);

        // Convertir le chemin relatif en une chaîne
        String relativePath = relativePathObj
                .toString();

        return relativePath;
    }

    /**
     * Affiche une image à partir du chemin spécifié en utilisant Swing.
     *
     * @param imagePath Le chemin de l'image à afficher.
     */
    public void displayImage(final Path imagePath) {
        try {
            ImageIcon imageIcon = new ImageIcon(imagePath.toString());
            JLabel jLabel = new JLabel(imageIcon);

            JFrame frame = new JFrame();
            frame.setLayout(new FlowLayout());
            frame.setSize(WIDTH, HEIGHT);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.add(jLabel);

            frame.setVisible(true);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'affichage de l'image.", e);
            JOptionPane.showMessageDialog(null,
                    "Erreur lors de l'affichage de l'image.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Définit le dernier NER (Numéro d'Élément de Référence)
     * utilisé dans ce
     * FileManager.
     *
     * @param ner Le dernier NER à définir.
     */
    public final void setLastNER(final int ner) {
        this.lastNER = ner;
    }

    /**
     * Obtient le dernier NER (Numéro d'Élément de Référence)
     * utilisé dans ce
     * FileManager.
     *
     * @return Le dernier NER utilisé.
     */
    public final int getLastNER() {
        return lastNER;
    }

    /**
     * Obtient le chemin du répertoire courant dans ce
     * FileManager.
     *
     * @return Le chemin du répertoire courant.
     */
    public final Path getCurrentDirectory() {
        return currentDirectory;
    }

    /**
     * Définit le fichier ou répertoire à copier dans
     * ce FileManager.
     *
     * @param file Le fichier ou répertoire à copier.
     */
    public final void setfileToCopy(final Path file) {
        this.toCopy = file;
    }

    /**
     * Obtient le fichier ou répertoire à copier
     * dans ce FileManager.
     *
     * @return Le fichier ou répertoire à copier.
     */
    public final Path getfileToCopy() {
        return toCopy;
    }

    /**
     * Définit la liste des annotations à copier dans
     * ce FileManager.
     *
     * @param annotation La liste des annotations à copier.
     */
    public final void setannotationToCopy(final List<String> annotation) {
        this.annotationsToCopy = annotation;
    }

    /**
     * Obtient la liste des annotations à copier
     * dans ce FileManager.
     *
     * @return La liste des annotations à copier.
     */
    public final List<String> getannotationToCopy() {
        return annotationsToCopy;
    }

    /**
     * Définit la liste des chemins à copier dans ce FileManager.
     *
     * @param pathtoCopy La liste des chemins à copier.
     */
    public final void setPathToCopy(final List<String> pathtoCopy) {
        this.pathToCopy = pathtoCopy;
    }

    /**
     * Obtient la liste des chemins à copier dans ce
     * FileManager.
     *
     * @return La liste des chemins à copier.
     */
    public final List<String> getPathToCopy() {
        return pathToCopy;
    }

}
