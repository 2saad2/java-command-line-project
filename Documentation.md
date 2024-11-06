# Gestionnaire de Fichiers en Ligne de Commande
## 1. Manuel Utilisateur


Bonjour et Bienvenue dans notre gestionnaire de fichiers en ligne de commande développé en Java, Grâce a ce manuel vous allez comprendre l'utilisation de l'application afin de pouvoir intéragir correctement avec ce gestionnaire.

----
### Utilisation
----

Pour bénéficier des fonctionnalités offertes par ce gestionnaire, suivez attentivement ces étapes détaillées :

* Installation de Java : Assurez-vous d'avoir Java installé sur votre système en téléchargeant la dernière version depuis le site officiel. (https://www.oracle.com/java/technologies/javase-downloads.html).

* Téléchargement du gestionnaire de fichiers depuis le dépôt Git : Récupérez le gestionnaire de fichiers en téléchargeant le dépôt Git via une commande appropriée ou en le clonant sur votre machine.

* Compiler le code source : 

    Dans le répertoire du projet, exécutez la commande :

    * **Cas Windows** :

        `.\mvnw.cmd package` 

    * **Cas Linux** :

        `$ ./mvnw package`

    pour compiler le code source et créer un artefact packagé dans le dossier "target". 

    Ensuite, lancez l'application en exécutant les commandes suivantes dans l'ordre suivant :

    - `mvn clean compile assembly:single`
    
    - `java -jar .\target\explorer-1.0-SNAPSHOT-jar-with-dependencies.jar.`

----
### Interface
----

Quand vous démarrez l'application, une interface apparaîtra avec le schéma suivant :

* Le chemin actuel (en `bleu`) : Indique où vous vous trouvez dans l'arborescence du système de fichiers.

* Éléments du répertoire : Chaque élément est répertorié avec un numéro (NER) et est codé par couleur :
    * Les fichiers texte apparaissent en `vert`.
    * Les dossiers apparaissent en `jaune`.
    * Le reste, c'est-à-dire tous les autres fichiers, sont en `rouge`.

* Affichage de l'élément courant et de sa note (si elle existe bien sûr).
* Et bien sûr, un message vous indiquera : "Entrez votre commande :", où vous pourrez saisir la commande nécessaire.


----
### Commandes
----

On arrive maintenant a l'étape où l'utilisateur peut saisir les commandes, voici donc les commandes expliqué qu'il peut saisir : 

* `[<NER>] copy` : Copie l'élément désigné par `<NER>`. Si l'élément existe, le nom du nouvel élément sera concaténé avec "-copy".
* `[<NER>] cut` : Coupe l'élément désigné par `<NER>`.
* `past`: Colle l'élément précédemment copié depuis le presse-papiers à l'emplacement spécifié par `<NER>`.
* `..` : Remonte d'un cran dans le système de fichiers.
* `[<NER>] .` : Entre dans un répertoire, à condition que `<NER>` désigne un répertoire.
* `mkdir <nom>` : Crée un répertoire avec le nom spécifié.
* `touch <nom>` : Cré un fichier avec le nim spécifié.
* `[<NER>] visu` : Permet de voir le contenu d'un fichier texte. Si le fichier n'est pas de type texte, affiche sa taille.
* `find <nom fichier>` : Recherche dans toutes les sous-répertoires du répertoire courant le(s) fichier(s) et les affiche.
* `delete <nom fichier>`: Supprime le fichier spécifié.
* `exit` : Termine l'interaction et clôture le programme en affichant un message de départ.
* `<NER> + "<texte>"` :  Ajoute ou concatène le texte spécifié à l'élément désigné par `<NER>`.
* `<NER> -` : Retire tout le texte associé à l'élément désigné par `<NER>`.


>**Exemple d'utilisation** : 

* `3 copy` : Copie le troisième élément du répertoire.
* `3 visu` : Affiche le contenu du troisème fichier (si bien sur c'est un fichier texte).
* `visu` : Affiche le contenu de l'élément courant (si bien sur c'est un fichier texte) .

----
### Annotations
----

La gestion des annotations s'effectue à travers un fichier JSON qui est enregistré sous le nom `annotations.json`. La structure de chaque annotation est la suivante :

`[{"Path": "chemin", "Annotation": "texte"}]`

Le champ **"Path"** représente le chemin du fichier ou du dossier annoté, tandis que le champ **"Annotation"** contient le texte ajouté à cette annotation.

 **Par exemple, l'utilisation de la commande** : `3 + Je suis le fichier 3` entraînera l'ajout de l'annotation suivante dans le fichier `annotations.json` :

 `[{ "Path" : "Chemin du fichier 3", "annotation" : "Je suis le fichier 3"},]`

 Si nous exécutons la commande `3 -`, cela aura pour effet de vider le fichier `annotations.json` qui se présentera comme suit : `[]`.

Étant donné que nous utilisons le chemin (**Path**) comme objet dans ce fichier JSON, certaines commandes telles que `copy` ou `cut` influent sur ce fichier. En effet, lorsqu'un fichier déjà annoté est copié et collé dans un autre dossier, il est nécessaire d'ajouter une nouvelle annotation spécifique à cette copie, qui sera exactement la même. En cas de découpage (**cut**), l'ancienne annotation est supprimée, et une nouvelle est ajouté qui aura comme objet (**Path**)  le nouveau chemin, qui sera exactement la même. Par exemple, si nous avons :

 `[{ "Path" : "Directory\File3", "annotation" : "Je suis le fichier 3"},]`

Et que nous découpons (**cut**) ce fichier pour le placer dans un autre répertoire que nous appellerons Directory_2, alors le fichier `annotations.json` sera mis à jour comme suit :

 `[{ "Path" : "Directory_2\File3", "annotation" : "Je suis le fichier 3"},]`

 Cela fonctionne de manière similaire pour les dossiers. Si nous coupons un dossier qui contient trois fichiers annotés, les trois fichiers seront mis à jour dans le fichier `annotations.json`.

----
### Visualisation d'une Image
----

La visualisation d'une image qu'elle soit PNG ou non se fait par la bibliothèque `Swing` qui fait partie du package `javax.swing`. Dans notre code on utilise la classe `ImageIcon` de `Swing` pour charger et afficher l'image.

Dans notre gestionnaire de fichiers nous avons implementé la visualisation d'un fichier de plusieurs formats d'image, dont PNG,JPEG,GIF et d'autres.

Documentation : (https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/javax/swing/package-summary.html).


----
### Commandes future
----

Voici quelques commandes qu'on pourrait ajouter a notre gestionnaire de fichiers :

* `info <NER>` : Affiche des informations détaillées sur l'élément désigné par `<NER>`, telles que la taille, le type, la date de création, etc.
* `cd <chemin>` : Change le répertoire de travail actuel vers le chemin spécifié.
* Les commandes `grep`, comme `grep <motif> <fichier>` qui recherche un motif spécifié dans le contenu du fichier texte.
* `unzip <archive> <destination>` : Extrait le contenu de l'archive spécifiée vers le dossier de destination.
* `append "texte" <fichier>` : Ajoute le texte spécifié à la fin du contenu du fichier.
* `rename <NER> <nouveau_nom>` : Renomme l'élément désigné par `<NER>` avec le nouveau nom spécifié.

Toutes ces commandes permettront de rendre ce gestionnaire de fichiers plus puissant et polyvalent.

----
### Amélioration d'interface future
----

Pour rendre l'interface de notre gestionnaire de fichiers par terminal plus souple et efficace pour les utilisateurs, certaines de ces améliorations sont envisageable : 

* **Mode interactif** : Ajoutez un mode interactif qui permet à l'utilisateur de sélectionner des fichiers ou des dossiers à l'aide des touches fléchées et d'appliquer des actions directement depuis l'interface.

* **Retour en arrière** : Intégrez des fonctionnalités `Annuler` et  `Rétablir` pour permettre à l'utilisateur de revenir en arrière ou de répéter des actions.

Ces suggestions offrent ainsi une expérience utilisateur plus complète et personnalisable. 

----
### Evolution envisageable
----

Dans notre gestionnaire de fichiers, nous nous sommes principalement concentrés sur un code basique qui n'est pas très étendu. Cependant, on peut vous proposer quelques idées supplémentaires pour rendre notre gestionnaire encore plus complet :

* **Prise en charge de formats de fichiers étendus** : Élargir la prise en charge des formats de fichiers permettrait aux utilisateurs de gérer différents types de fichiers, tels que des fichiers multimédias, des fichiers compressés, des fichiers PDF, etc. (Actuellement juste 3 cas *[txt,dossier,reste]*).

* **Recherche avancée** : Ajout d'une recherche avancée avec la commande `find` comme par exemple avec la date du fichier, date de modification, le type du fichier etc.

* **Intégration de plugins** : Offrir la possibilité d'ajouter des plugins pour étendre les fonctionnalités du gestionnaire de fichiers. Cela permettrait aux utilisateurs de personnaliser leur expérience en ajoutant des fonctionnalités spécifiques à leurs besoins.

Il existe bien d'autres fonctionnalités possibles, mais parmi celles-ci, ces trois-là sont celles que nous pourrions intégrer sans risque majeur et de manière rapide.

----

## 2. Manuel Technique

Bienvenue dans le manuel technique de notre gestionnaire de fichiers en ligne de commande. Ce document fournit des informations détaillées sur la conception et la mise en œuvre du projet.

----
### Structure du Projet
----

Le projet est organisé de la manière suivante :

* `src/` : Contient le code source Java.
* `target/` : Contient les fichiers générés lors de la compilation.
* `docs/` : Contient la documentation au format Markdown.
* `annotations.json` : Fichier de stockage des annotations associées aux éléments du répertoire.

Le projet a été géré par Git avec l'utilisation des commits réguliers (`git add`,`git commit -m "Message"`,`git push`) entre 2 personnes. La récupération se fait par `git pull` afin de sychroniser nos travaux.

Le projet utilise Maven avec Maven wrapper intégré et les dépendances sont gérées automatiquement par Maven dans le `pom.xml`.

Notre application est exécutée à partir d'un JAR incluant toutes les dépendances.Le Maven wrapper prend en charge le processus de build, garantissant une exécution fluide.


----
### Bibliothèques utilisé
----

Voici les bibliothèques utilisés dans notre programme : 

**Bibliothèques pour les opérations sur les fichiers** :

* `java.io.File` et `java.io.IOException` : Utilisées pour travailler avec des fichiers et gérer les exceptions liées à l'entrée/sortie (I/O).

* `java.nio.file.Paths` : Utilisée pour obtenir un objet Path à partir d'une séquence de chaînes, fournissant un moyen pratique de travailler avec des chemins de fichiers.

* `java.nio.file.Path`, `java.nio.file.DirectoryStream`, et `java.nio.file.Files` : Utilisées pour travailler avec les systèmes de fichiers. `Path` représente un chemin de fichier, DirectoryStream est utilisé pour itérer sur le contenu d'un répertoire, et Files fournit des méthodes pour effectuer des opérations sur les fichiers.

* `org.apache.commons.io.FileUtils` : Fait partie de la bibliothèque Apache Commons IO, offrant des utilitaires pour les opérations d'entrée/sortie. `FileUtils` propose des méthodes pratiques pour copier, déplacer, supprimer et manipuler des fichiers et des répertoires.

**Bibliothèques pour la gestion des flux et des collections** :

* `java.util.stream.Collectors` et `java.util.stream.Stream` : Utilisées pour effectuer des opérations de traitement de flux (stream) sur des collections.

* `java.util.ArrayList` et `java.util.Collections` : Utilisées pour travailler avec des listes (comme `ArrayList`) et effectuer des opérations de manipulation de collections (comme `Collections`).

* `java.util.List` : Interface utilisée pour représenter des listes d'éléments.

**Bibliothèques pour la manipulation des chaînes de caractères et des encodages** :

* `java.nio.charset.StandardCharsets`: Utilisée pour travailler avec des encodages de caractères.

* `java.nio.charset.Charset` : Utilisée pour représenter un ensemble de caractères, généralement associé à un encodage de caractères.

**Bibliothèques pour la manipulation des objets JSON** :

* `com.fasterxml.jackson.databind.JsonNode`, `com.fasterxml.jackson.databind.ObjectMapper`, `com.fasterxml.jackson.databind.node.ArrayNode`, `com.fasterxml.jackson.databind.node.ObjectNode` : Font partie de la bibliothèque Jackson, utilisée pour travailler avec le format JSON en Java. Permet de manipuler les objets JSON, de sérialiser et désérialiser des données JSON.

**Bibliothèques pour la gestion des couleurs dans la console** :

* `org.fusesource.jansi.AnsiConsole` et `org.fusesource.jansi.Ansi` : Appartiennent à la bibliothèque Jansi, qui fournit des fonctionnalités pour manipuler les séquences ANSI et gérer les couleurs dans la console.

**Bibliothèques pour la journalisation (logging)** :

* `org.slf4j.Logger` et `org.slf4j.LoggerFactory` : Font partie de SLF4J, une API de journalisation (logging) pour Java. Utilisées pour générer des messages de journalisation.

**Bibliothèques pour les tests unitaires** :

* `static org.junit.jupiter.api.Assertions.assertTrue` : Importe statiquement la méthode `assertTrue` de la classe `org.junit.jupiter.api.Assertions`. Partie du framework de tests unitaires JUnit. La méthode `assertTrue` est utilisée pour vérifier qu'une condition est vraie pendant l'exécution des tests.

* `org.junit.jupiter.api.Test` : Annotation du framework de tests unitaires JUnit. Utilisée pour marquer une méthode comme une méthode de test, indiquant au framework qu'il doit exécuter cette méthode lors de l'exécution des tests.

**Bibliothèques pour la lecture d'entrées utilisateur** :

* `java.util.Scanner` : Classe utilisée pour analyser les entrées. Permet de lire différentes primitives à partir du flux d'entrée, comme le clavier (`System.in`), un fichier, ou une chaîne de caractères.

**Bibliothèques pour la visualisation d'image** :

* `javax.swing.ImageIcon` : Chargé avec le chemin de l'image, permet d'afficher des images dans l'interface graphique.
  
* `javax.swing.JFrame` : Fenêtre conteneur de haut niveau qui héberge les composants Swing, offre des fonctionnalités de gestion de fenêtre.

* `javax.swing.JLabel` : Affiche du texte ou une image dans l'interface graphique.

* `javax.swing.JOptionPane` : Fournit des boîtes de dialogue standard, utilisée ici pour afficher des messages d'erreur.

* `java.awt.FlowLayout` : Gère la disposition des composants dans la fenêtre JFrame.


----
Classes
----
Voici les différentes classes utilisé dans notre code :



* Classe `FileManager` qui est responsable de la gestion des opérations sur les fichiers et répertoires dans un système de fichiers. Elle effectue diverses opérations sur les fichiers et répertoires, notamment l'affichage (`displayPathContentsNER`), la suppression(`deleteFileOrDirectory`), la mise à jour du répertoire courant(`updateCurrentDir`), la gestion des NER(`getPathByNER`), et la préparation de fichiers à copier avec leurs annotations associées(`annotationsToCopy`, `pathToCopy`).

* Classe `AnnotationManager` qui est responsable de la gestion des annotations associées à des chemins de fichiers. Elle offre des fonctionnalités pour la gestion des annotations liées à des chemins de fichiers, avec des méthodes pour l'ajout (`AnnotationAdd`), la recherche(`findAnnotation`), et la suppression d'annotations(`deleteAnnotationNER`), ainsi que la récupération de chemins de fichiers à copier avec leurs annotations associées.

* Classe `Commande` qui gére différentes opérations liées aux commandes utilisateur, en interagissant avec la classe `FileManager` et la classe `AnnotationManager`. Elle fournit des méthodes pour effectuer des opérations telles que la copie(`copyFile`), le collage(`pasteFile`), la création de répertoires(`mkdir`), l'affichage du contenu de fichiers(`visu`), et la recherche de fichiers dans le répertoire courant(`find`).

* Classe `CommandHandler` est le gestionnaire principal des commandes utilisateur dans un environnement de ligne de commande. Elle interagit avec les classes `FileManager`, `Commande`, et `AnnotationManager` pour traiter les commandes entrées par l'utilisateur. Elle assure la gestion des commandes, la navigation dans les répertoires, et l'exécution des opérations correspondantes.

* Classe `App`, la classe principale de l'application. Elle  initialise les composants principaux de l'application et en lançant le traitement des commandes.




----
### Conception
----
Notre code repose sur le paradigme de la Programmation Orientée Objet, mettant en œuvre l'agrégation pour définir les relations entre classes. L'agrégation décrit une situation où une classe contient un objet d'une autre classe.

Cette approche orientée objet s'avère particulièrement pertinente dans notre contexte, car nos classes sont distinctes. Elle nous permet de modéliser efficacement les relations entre différents objets.

Un exemple concret de l'application de ce concept se trouve dans notre classe Commande :

    public class Commande {
        private FileManager fileManager;
        private static final Logger LOGGER = LoggerFactory.getLogger(
                Commande.class);

        public Commande(FileManager fileManager) {
            this.fileManager = fileManager;
        }


En ce qui concerne la gestion des exceptions, nous utilisons les exceptions suivantes pour garantir une gestion adéquate des erreurs :

* `IllegalArgumentException` : Indique qu'un argument passé à une méthode est invalide.

* `NumberFormatException` : Se produit lorsqu'une conversion de chaîne en nombre échoue en raison d'un format incorrect.

* `Exception` : Classe de base pour toutes les exceptions, souvent utilisée pour regrouper plusieurs exceptions.

* `IOException` : Indique une erreur lors des opérations d'entrée/sortie, comme la lecture ou l'écriture de fichiers.


----
### Traitements d'une commande
----

Pour traiter une commande saisie par l'utilisateur, plusieurs étapes sont généralement suivies :

1. **Analyse de la commande** : La chaîne de caractères saisie par l'utilisateur est analysée pour identifier la commande spécifique et les éventuels paramètres associés.

2. **Validation des paramètres** : Si la commande nécessite des paramètres, ils sont validés pour s'assurer qu'ils sont conformes aux attentes. Par exemple, vérifier que les valeurs numériques sont des entiers, que les chemins de fichiers existent, etc.

3. **Exécution de la commande** : Une fois la commande analysée et les paramètres validés, l'action associée est exécutée. Cela peut impliquer l'appel de fonctions, la manipulation de données, l'interaction avec le système de fichiers, etc.

4. **Gestion des erreurs** : En cas d'erreurs pendant l'analyse, la validation ou l'exécution, des messages d'erreur appropriés sont générés pour informer l'utilisateur de la nature du problème.

----
 Conclusion
----

Félicitations ! Vous avez maintenant toutes les informations nécessaires pour utiliser notre gestionnaire de fichiers en ligne de commande développé en Java. Grâce à ce manuel d'utilisateur, vous avez appris comment interagir avec l'application et tirer parti de ses fonctionnalités.

Vous avez également pris connaissance de la manière dont ce gestionnaire pourrait évoluer à l'avenir pour offrir une expérience utilisateur plus confortable.

Nous espérons que ce manuel d'utilisateur vous a été utile et que vous pourrez profiter pleinement de notre gestionnaire de fichiers en ligne de commande.


