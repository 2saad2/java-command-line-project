package fr.uvsq.cprog;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cette classe est le gestionnaire principal des commandes utilisateur
 * dans un environnement de ligne de commande. Elle interagit avec
 * les classes FileManager, Commande, et AnnotationManager pour
 * traiter les commandes entrées par l'utilisateur.
 */
public class CommandHandler {
    /**
     * Gestionnaire des commandes utilisateur.
     * Ce gestionnaire prend en charge la manipulation des commandes entrées par
     * l'utilisateur en utilisant un FileManager et une instance de Commande.
     */
    private final FileManager fileManager;
    /**
     * Instance de la classe Commande utilisée pour exécuter les commandes
     * utilisateur.
     */
    private final Commande commande;
    /**
     * Gestionnaire d'annotations utilisé pour la manipulation des annotations
     * associées aux éléments.
     */
    private final AnnotationManager annotationManager;
    /**
     * Scanner utilisé pour la saisie des commandes utilisateur.
     */
    private final Scanner scanner;
    /**
     * Type de l'opération en cours (cut, copy) pour gérer les actions de
     * découpe/copie de fichiers.
     */
    private String type = "";
    /**
     * Indique si l'action en cours est une découpe de fichier
     * (true) ou une copie (false).
     */
    private boolean isToCut = false;
    /**
     * Instance statique du logger (journal d'événements) associée à la classe
     * Commande.
     * Utilisée pour enregistrer des messages de journalisation facilitant
     * le suivi
     * et le débogage
     * des opérations effectuées par la classe Commande.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            CommandHandler.class);

    /**
     * Constructeur de la classe CommandHandler.
     *
     * @param filemanager       Le gestionnaire de fichiers associé.
     * @param command           L'instance de la classe Commande.
     * @param annotationmanager Le gestionnaire d'annotations.
     */
    public CommandHandler(final FileManager filemanager, final Commande command,
            final AnnotationManager annotationmanager) {
        this.fileManager = filemanager;
        this.commande = command;
        this.annotationManager = annotationmanager;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Gère l'exécution des commandes utilisateur de manière interactive.
     */
    public void handleCommands() {
        try {
            while (true) {
                fileManager.displayPathContentsNER();
                if (fileManager.getLastNER() == -1) {
                    System.out.println("Aucune note pour "
                            + "l'élément courant.");
                } else {
                    System.out.println("Elément courant: "
                            + fileManager.getLastNER());
                }

                if (annotationManager.getLastAnnotation() == "") {
                    System.out.println("Aucune annotation pour "
                            + "l'élément courant.\n");
                } else {
                    System.out.println("Annotation courante: "
                            + annotationManager.getLastAnnotation()
                            + "\n");
                }

                System.out.print("Entrez votre commande : ");
                String inputLine = scanner.nextLine();
                System.out.print("\n");

                if (!processCommand(inputLine)) {
                    break;
                }
                System.out.println("\n----------------------------"
                        + "--------------------------------------------- \n");
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage());
            // Nettoyer ou réinitialiser le scanner si nécessaire
            // Ignore le reste pour éviter les problèmes.
            scanner.nextLine();
        } catch (Exception e) {
            LOGGER.error("Error : " + e.getMessage());
            // Décider de continuer ou non
        }
    }

    /**
     * Traite la commande utilisateur entrée en paramètre.
     *
     * @param inputLine La ligne de commande entrée par l'utilisateur.
     * @return {@code true} si le programme doit continuer, {@code false} si
     *         l'utilisateur a saisi une commande de sortie.
     */
    private boolean processCommand(final String inputLine) {
        final String[] commandParts = inputLine.split("\\s+", 3);
        int ner = -1;
        String command = "default"; // Commande par défaut
        String name = "";
        if (commandParts.length >= 1) {
            try {
                ner = Integer.parseInt(commandParts[0]);
                fileManager.setLastNER(ner);
                String eRPath = fileManager.getPathByNER(ner).toString();
                annotationManager.setLastAnnotation(AnnotationManager
                        .findAnnotation(ner, eRPath, ""));
                command = commandParts.length > 1
                        ? commandParts[1]
                        : command;
                name = commandParts.length > 2
                        && commandParts[2] instanceof String
                                ? (String) commandParts[2]
                                : name;
            } catch (NumberFormatException e) {
                command = commandParts[0];
                ner = fileManager.getLastNER();
                name = commandParts.length > 1
                        && commandParts[1] instanceof String
                                ? (String) commandParts[1]
                                : name;
            }
        } else {
            ner = fileManager.getLastNER();
        }
        switch (command) {
            case "..":
                fileManager.updateCurrentDir("retour");
                break;
            case "cut":
            case "copy":
            case "visu":
            case "default":
            case ".":
                String actualName = fileManager
                        .getDirectoryContents(fileManager
                                .getCurrentDirectory())
                        .get(ner - 1);
                if (name.isEmpty() || name.equals(actualName)) {
                    switch (command) {
                        case "cut":
                            commande.copyFile(ner, isToCut);
                            this.type = "cut";
                            isToCut = true;
                            break;
                        case "copy":
                            commande.copyFile(ner, isToCut);
                            isToCut = false;
                            break;
                        case "visu":
                        case "default":
                            commande.visu(ner);
                            break;
                        case ".":
                            List<String> contents = fileManager
                                    .getDirectoryContents(fileManager
                                            .getCurrentDirectory());
                            if (ner < 1 || ner > contents.size()) {
                                System.out.println("NER invalide.");
                            } else {
                                String fileName = contents.get(ner - 1);
                                Path potentialDir = fileManager
                                        .getCurrentDirectory()
                                        .resolve(fileName);
                                if (Files.isDirectory(potentialDir)) {
                                    fileManager
                                            .updateCurrentDir(fileName);
                                } else {
                                    System.out
                                            .println("Elément non répertoire");
                                }
                            }
                            break;
                        default:
                            System.out.println("Commande non reconnue.");
                    }
                } else {
                    System.out.println("Nom ne correspond pas au NER");
                }
                break;
            case "help":
                commande.help();
                break;
            case "delete":
                Path pathRef = fileManager.getPathByNER(ner);
                fileManager.deleteFileOrDirectory(pathRef);
                annotationManager.setLastAnnotation("");
                fileManager.setLastNER(-1);
                break;
            case "past":
                commande.pasteFile(this.type);
                isToCut = false;
                this.type = "";
                break;
            case "mkdir":
                if (!name.isEmpty()) {
                    commande.mkdir(name);
                } else {
                    System.out.println("Nom repertoire manquant");
                }
                break;
            case "touch":
                if (!name.isEmpty()) {
                    commande.createTextFile(name);
                } else {
                    System.out.println("Nom fichier manquant");
                }
                break;
            case "find":
                if (!name.isEmpty()) {
                    commande.find(name);
                } else {
                    System.out.println("Nom fichier manquant");
                }
                break;
            case "+":
                executeCommandeOnAnnotation(ner, "+", commandParts);
                break;
            case "-":
                executeCommandeOnAnnotation(ner, "-", commandParts);
                break;
            case "note":
                if (ner > 0) {
                    String eRPath = fileManager.getPathByNER(ner).toString();
                    AnnotationManager.findAnnotation(ner, eRPath, "Stream");
                } else {
                    System.out.println("NER invalide.");
                }
                break;
            case "exit":
                if (isToCut) {
                    fileManager.deleteFileOrDirectory(fileManager
                            .getfileToCopy());
                }
                System.out.println("Au revoir!");
                return false;
            default:
                System.out.println("Commande non reconnue.");
        }
        return true;
    }

    private void executeCommandeOnAnnotation(final int ner, final
                        String command, final String[] commandParts) {
        if (command.equals("+")) {
            if (ner > 0 && commandParts.length > 2) {
                String annotationText = commandParts[2];
                String eRPath = fileManager.getPathByNER(ner).toString();
                AnnotationManager.annotationAdd(eRPath, annotationText);
                annotationManager.setLastAnnotation(AnnotationManager
                    .findAnnotation(ner, eRPath, ""));
            } else {
                System.out.println("Mauvais NER ou anno manquante");
            }
        } else {
            if (ner > 0) {
                String eRPath = fileManager.getPathByNER(ner)
                        .toString();
                AnnotationManager.deleteAnnotationNER(eRPath);
                annotationManager.setLastAnnotation("");
            } else {
                System.out.println("NER invalide.");
            }
        }
    }
}
