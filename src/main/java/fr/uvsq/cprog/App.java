package fr.uvsq.cprog;

/**
 * Cette classe est la classe principale de l'application. Elle initialise les
 * composants principaux de l'application et en lançant le traitement des
 * commandes.
 *
 */
public final class App {
    /**
     * Constructeur privé de la classe App pour empêcher l'instanciation.
     * Cette classe ne doit pas être instanciée, car elle contient uniquement
     * une méthode statique.
     */
    private App() {

    }

    /**
     * Méthode principale de l'application.
     * Elle initialise les composants nécessaires (FileManager, Commande,
     * AnnotationManager,CommandHandler) et lance la gestion des commandes
     * utilisateur.
     *
     * @param args Les arguments de la ligne de commande (non utilisés dans
     *             cette
     *             application).
     */
    public static void main(final String[] args) {

        FileManager fileManager = new FileManager();
        Commande commande = new Commande(fileManager);
        AnnotationManager annotationManager = new AnnotationManager();
        CommandHandler commandHandler = new CommandHandler(fileManager,
                commande, annotationManager);

        commandHandler.handleCommands();
    }
}
