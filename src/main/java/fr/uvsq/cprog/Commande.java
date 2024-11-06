package fr.uvsq.cprog;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.FlowLayout;
import java.nio.file.Path;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import java.nio.file.Files;
import org.apache.commons.io.FileUtils;

import org.fusesource.jansi.Ansi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Commande {
        /**
     * Constante pour la taille du cadre.
     */
        private static final int FRAME_SIZE = 400;
        /**
     * Liste pour les types d'images.
     */
        private static final List<String> IMAGE_EXTENSIONS = Arrays
        .asList(".jpg",
        ".jpeg", ".png", ".gif", ".bmp");
    /**
     * Gestionnaire de fichiers utilisé pour la manipulation des opérations
     * liées
     * aux fichiers
     * dans le contexte de la classe où cette variable est déclarée.
     */
    private FileManager fileManager;
    /**
     * Instance statique du logger (journal d'événements) associée à la classe
     * Commande.
     * Utilisée pour enregistrer des messages de journalisation facilitant
     * le suivi
     * et le débogage
     * des opérations effectuées par la classe Commande.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            Commande.class);

    /**
     * Constructeur de la classe Commande.
     * Initialise une nouvelle instance de Commande en associant le FileManager
     * spécifié.
     *
     * @param filemanager Le gestionnaire de fichiers utilisé par
     *                    cette instance de
     *                    Commande.
     */
    public Commande(final FileManager filemanager) {
        this.fileManager = filemanager;
    }

    /**
     * Copie le fichier ou répertoire spécifié par le NER (Numéro
     * d'Élément de
     * Référence) dans le répertoire
     * courant du FileManager. La copie peut être une simple copie
     * ou une opération
     * de découpe (cut)
     * en fonction de la valeur de la variable isToCut.
     *
     * @param fileNER Le NER du fichier ou répertoire à copier.
     * @param isToCut Indique si l'opération de copie doit être
     *                traitée comme une
     *                découpe (cut).
     */
    public void copyFile(final int fileNER, final boolean isToCut) {

        if (isToCut) {
            fileManager.deleteFileOrDirectory(fileManager
                    .getfileToCopy());
        }

        List<String> contents = fileManager
                .getDirectoryContents(fileManager
                        .getCurrentDirectory());
        if (fileNER < 1 || fileNER > contents.size()) {
            System.out.println("NER invalide.");
            return;
        }

        String fileName = contents.get(fileNER - 1);
        Path theCopy = fileManager.getCurrentDirectory()
                .resolve(fileName);

        fileManager.setfileToCopy(theCopy);
        List<String> pathToCopy = new ArrayList<>();
        List<String> annotationsToCopy = new ArrayList<>();
        if (Files.isDirectory(theCopy)) {
            Path original = theCopy;
            AnnotationManager.findAddToCopy(theCopy, original,
                    pathToCopy, annotationsToCopy,
                    fileManager);
            fileManager.setPathToCopy(pathToCopy);
            fileManager.setannotationToCopy(
                    annotationsToCopy);
            System.out.println(pathToCopy
                    + "   " + annotationsToCopy);

        } else {
            fileManager.setPathToCopy(
                    Collections.singletonList(
                            theCopy.toString()));
            fileManager.setannotationToCopy(
                    Collections.singletonList(
                            AnnotationManager.findAnnotation(fileNER,
                                    fileManager.getfileToCopy()
                                            .toString(),
                                    "")));
        }

    }

    /**
     * Colle le fichier ou répertoire précédemment copié ou
     * découpé dans le
     * répertoire courant du FileManager.
     *
     * @param type Le type de l'opération précédente
     *             ("cut" pour découpe, "copy"
     *             pour copie).
     */
    public void pasteFile(final String type) {
        if (fileManager.getfileToCopy() != null) {
            String fileName = fileManager.getfileToCopy()
                    .getFileName().toString();

            // Séparer le nom du fichier de son extension
            int dotIndex = fileName.lastIndexOf('.');
            String baseName = (dotIndex == -1)
                    ? fileName
                    : fileName.substring(0, dotIndex);
            String extension = (dotIndex == -1)
                    ? ""
                    : fileName.substring(dotIndex);

            // Construire le nouveau nom de fichier avec "-copy"
            System.out.println(extension);
            String newFileName = type.equals("cut")
                    ? baseName + extension
                    : baseName + "-copy" + extension;
            Path targetPath = fileManager
                    .getCurrentDirectory().resolve(newFileName);

            try {
                if (Files.isDirectory(fileManager.getfileToCopy())) {
                    FileUtils.copyDirectory(
                            fileManager.getfileToCopy()
                                    .toFile(),
                            targetPath.toFile());
                    System.out.println("Répertoire "
                            + fileName
                            + " et son contenu collés avec succès.");

                    AnnotationManager.addAnnotationToDirectory(
                            targetPath, fileManager.getPathToCopy(),
                            fileManager.getannotationToCopy());
                } else {
                    Files.copy(fileManager.getfileToCopy(), targetPath);
                    System.out.println(fileManager.getfileToCopy()
                            + "\\" + targetPath);
                    AnnotationManager.annotationAdd(targetPath
                            .toString(),
                            fileManager
                                    .getannotationToCopy().get(0));
                    System.out.println("Fichier copié en : "
                            + newFileName);

                }
                if (type.equals("cut")
                        && !targetPath.equals(fileManager
                                .getfileToCopy())) {
                    fileManager.deleteFileOrDirectory(fileManager
                            .getfileToCopy());
                    System.out.println(fileName
                            + " a été supprimé.");
                }

            } catch (IOException e) {
                LOGGER.error("Erreur lors de la copie du fichier.");
            }
        } else {
            System.out.println("Aucun fichier sélectionné pour "
                    + "la copie ou le fichier n'existe pas.");
        }
    }

    /**
     * Crée un nouveau répertoire avec le nom spécifié dans
     * le répertoire courant du
     * FileManager.
     *
     * @param dirName Le nom du nouveau répertoire à créer.
     */
    public void mkdir(final String dirName) {
        Path newDirPath = fileManager
                .getCurrentDirectory().resolve(dirName);
        try {
            if (!Files.exists(newDirPath)) {
                Files.createDirectory(newDirPath);
                System.out.println("Répertoire créé : " + dirName);
            } else {
                System.out.println("Le répertoire existe déjà.");

            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la création "
                    + "du répertoire");
        }
    }

    /**
     * Affiche le contenu ou les informations d'un fichier
     * ou dossier spécifié par le NER
     * (Numéro d'Élément de Référence) dans le
     * répertoire courant du FileManager.
     *
     * @param ner Le NER du fichier ou dossier à visualiser.
     */
    public void visu(final int ner) {
        try {
            List<String> contents = fileManager
                    .getDirectoryContents(fileManager
                            .getCurrentDirectory());
            if (ner >= 1 && ner <= contents.size()) {
                String fileName = contents.get(ner - 1);
                Path filePath = fileManager.getCurrentDirectory()
                        .resolve(fileName);

                if (fileManager.isTextFile(filePath)) {
                    Files.lines(filePath, Charset.defaultCharset())
                            .forEach(System.out::println);
                } else {
                    long size = Files.size(filePath);
                    if (Files.isDirectory(filePath)) {
                        System.out.println("Taille du dossier: "
                                + size + " bytes");
                    } else {
                        if (isImageFile(filePath)) {
                                displayImage(filePath);
                        } else {
                                System.out.println("Taille du fichier: "
                                + size + " bytes");
                        }
                    }
                }
            } else {
                System.out.println("NER invalide.");
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la lecture du fichier.");
        }
    }

         /**
         * Vérifie si le fichier spécifié est un fichier image.
         *
         * @param filePath Le chemin du fichier à vérifier.
         * @return true si le fichier est un fichier image, false sinon.
         */
    public static boolean isImageFile(final Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
    /**
     * Recherche et affiche les chemins des fichiers avec
     * le nom spécifié dans le répertoire
     * courant du FileManager, y compris les sous-répertoires.
     *
     * @param fileName Le nom du fichier à rechercher.
     */
    public void find(final String fileName) {
        try (Stream<Path> paths = Files.walk(fileManager
                .getCurrentDirectory())) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString().equals(fileName))
                    .forEach(System.out::println);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la "
                    + "recherche de fichiers.");
        }
    }

    /**
     * Affiche une aide pour l'utilisateur.
     */
    public void help() {
        String[] helpLines = {
        "[<NER>] copy : " + Ansi.ansi().fg(Ansi.Color.BLACK).a(
                        "Copie l'élément désigné par <NER>."
                        + " Si l'élément existe, le nom du nouvel "
                        + "élément sera concaténé avec \"-copy\".")
                        .reset(),
        "[<NER>] cut : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Coupe l'élément désigné par <NER>.")
                        .reset(),
        "past : " + Ansi.ansi().fg(Ansi.Color.BLACK).a(
                        "Colle l'élément précédemment copié "
                        + "depuis le presse-papiers à l'emplacement "
                        + "spécifié par `<NER>`.")
                        .reset(),
        ".. : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Remonte d'un cran dans le système "
                        + "de fichiers.").reset(),
        "[<NER>] . : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Entre dans un répertoire, à condition "
                        + "que <NER> désigne un répertoire.")
                        .reset(),
        "mkdir <nom> : "
                        + Ansi.ansi().fg(Ansi.Color.BLACK)
                                        .a("Crée un répertoire avec"
                                        + " le nom spécifié.").reset(),
        "touch <nom> : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Crée un fichier avec le nom spécifié.")
                        .reset(),
        "[<NER>] visu : " + Ansi.ansi().fg(Ansi.Color.BLACK).a(
                        "Permet de voir le contenu d'un fichier texte."
                        + " Si le fichier n'est pas de type texte, affiche "
                        + "sa taille.")
                        .reset(),
        "find <nom fichier> : " + Ansi.ansi().fg(Ansi.Color.BLACK).a(
                        "Recherche dans toutes les sous-répertoires "
                        + "du répertoire courant le(s) fichier(s) et les "
                        + "affiche.")
                        .reset(),
        "rename <NER> <nouveau_nom> : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Renomme l'élément désigné par `<NER>` "
                        + "avec le nouveau nom spécifié.")
                        .reset(),
        "delete <nom fichier>: " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Supprime le fichier spécifié.").reset(),
        "exit : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Termine l'interaction et clôture le "
                        + "programme en affichant un message de départ.")
                        .reset(),
        "<NER> + <texte> : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Ajoute ou concatène le texte spécifié "
                        + "à l'élément désigné par `<NER>`.")
                        .reset(),
        "<NER> - : " + Ansi.ansi().fg(Ansi.Color.BLACK)
                        .a("Retire tout le texte associé à l'élément "
                        + "désigné par `<NER>`.")
                        .reset()
        };
        System.out.println("\n");
        for (String line : helpLines) {
                System.out.println(line);
        }
}

        /**
         * Fonction permettant de créer un fichier
         * texte avec un nom spécifié.
         * @param fileName Le nom du fichier à rechercher.
         */
        public void createTextFile(final String fileName) {
                Path newFilePath = fileManager.getCurrentDirectory()
                .resolve(fileName + ".txt");
                try {
                        if (!Files.exists(newFilePath)) {
                                Files.createFile(newFilePath);
                                System.out.println("Fichier créé : "
                                 + fileName + ".txt");
                        } else {
                                System.out.println("Le fichier existe "
                                + "déjà.");
                        }
                } catch (IOException e) {
                        LOGGER.error("Erreur lors de la création du "
                        + "fichier");
                }
        }

        /**
         * Affiche une image à partir du chemin spécifié.
         *
         * @param imagePath Le chemin de l'image à afficher.
         */
        public static void displayImage(final Path imagePath) {
        try {
            ImageIcon imageIcon = new ImageIcon(imagePath.toString());
            JLabel jLabel = new JLabel(imageIcon);

            JFrame frame = new JFrame();
            frame.setLayout(new FlowLayout());
            // Ajustez la taille du cadre selon vos besoins
            frame.setSize(FRAME_SIZE, FRAME_SIZE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(jLabel);

            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Erreur"
            + " lors de l'affichage de l'image.", "Erreur",
            JOptionPane.ERROR_MESSAGE);
        }
    }

}
