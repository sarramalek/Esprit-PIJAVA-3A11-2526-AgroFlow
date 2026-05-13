-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : lun. 27 avr. 2026 à 12:41
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `agro`
--

-- --------------------------------------------------------

--
-- Structure de la table `abonnements`
--

CREATE TABLE `abonnements` (
  `id_abonn` int(11) NOT NULL,
  `cin` int(11) NOT NULL,
  `id_offre` int(11) NOT NULL,
  `date_inscription` date NOT NULL,
  `date_expiration` date NOT NULL,
  `situation` varchar(9) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `abonnements`
--

INSERT INTO `abonnements` (`id_abonn`, `cin`, `id_offre`, `date_inscription`, `date_expiration`, `situation`) VALUES
(1, 11111112, 1, '2026-04-14', '2026-04-24', 'actif'),
(2, 14532929, 1, '2026-04-18', '2026-06-13', 'actif'),
(4, 14532929, 3, '2026-04-19', '2026-04-25', 'actif'),
(5, 11111112, 2, '2026-04-23', '2026-04-24', 'actif');

-- --------------------------------------------------------

--
-- Structure de la table `animaux`
--

CREATE TABLE `animaux` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `espece` varchar(255) NOT NULL,
  `date_naissance` date NOT NULL,
  `sexe` varchar(255) NOT NULL,
  `poids` double DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `animaux`
--

INSERT INTO `animaux` (`id`, `nom`, `espece`, `date_naissance`, `sexe`, `poids`, `user_id`) VALUES
(1, 'Marguerite', 'Mouton', '2026-02-06', 'FEMELLE', 550, NULL),
(14, 'kiki', 'vache', '2026-02-12', 'MALE', 550, NULL),
(15, 'jacky', 'chien', '2026-02-09', 'FEMELLE', 6.5, NULL),
(18, 'bobo', 'chien', '2026-02-13', 'MALE', 10, NULL),
(19, 'luca', 'Chien', '2026-02-13', 'MALE', 4.5, NULL),
(20, 'Marguerite', 'Bovin', '2026-02-13', 'FEMELLE', 550, NULL),
(21, 'bibi', 'Chien', '2025-07-13', 'FEMELLE', 15, NULL),
(22, 'coco', 'volailles', '2026-02-02', 'MALE', 16, 14532929),
(24, 'lili', 'chat', '2023-01-17', 'FEMELLE', 4.800000190734863, NULL),
(26, 'kiki', 'chat', '2026-02-12', 'MALE', 15, NULL),
(28, 'lily', 'Chat', '2026-01-26', 'MALE', 10, NULL),
(29, 'cocoli', 'Cheval', '2026-02-11', 'MALE', 15, NULL),
(30, 'rex', 'Chèvre', '2026-02-05', 'FEMELLE', 15, NULL),
(31, 'roxi', 'Vache', '2026-02-20', 'FEMELLE', 15, NULL),
(33, 'foufou ', 'Chat', '2026-01-26', 'MALE', 520, NULL),
(34, 'leo', 'Cheval', '2026-02-20', 'MALE', 150, NULL),
(35, 'chocho', 'Chèvre', '2026-02-20', 'FEMELLE', 3, NULL),
(36, 'chocho', 'Vache', '2024-03-31', 'FEMELLE', 120, 88888888),
(37, 'nono', 'Chien', '2015-03-30', 'MALE', 13, 88888888),
(38, 'moumou', 'Mouton', '2026-03-13', 'FEMELLE', 20, 88888888),
(39, 'bobo', 'Mouton', '2024-02-04', 'MALE', 15, 88888888),
(40, 'Rex', 'Chien', '2024-01-15', 'MALE', 25, 88888888),
(41, 'Mimi', 'Chat', '2023-05-20', 'MALE', 4.5, 88888888),
(42, 'Blanchette', 'Chèvre', '2022-11-10', 'FEMELLE', 15, 88888888),
(43, 'Bibi', 'Mouton', '2023-08-05', 'MALE', 40, 88888888),
(44, 'Flicka', 'Cheval', '2021-06-12', 'FEMELLE', 450, 88888888),
(45, 'Bella', 'Vache', '2022-02-28', 'FEMELLE', 550, 88888888),
(46, 'roys', 'Cheval', '2022-07-04', 'MALE', 500, 88888888),
(47, 'lisa', 'Cheval', '2025-04-05', 'FEMELLE', 600, 88888888),
(48, 'vava', 'Vache', '2022-02-06', 'FEMELLE', 250, 88888888),
(49, 'bichou', 'Chat', '2024-07-06', 'MALE', 13, 88888888),
(52, 'AASLA', 'Chèvre', '2026-04-02', 'FEMELLE', 4.5, 14532929),
(54, 'MOUMOU', 'Chèvre', '2026-04-02', 'MALE', 25, 14532929),
(55, 'bibi', 'Chien', '2026-04-03', 'MALE', 2.5, 14532929),
(56, 'Marguerite', 'Vache', '2023-05-12', 'Femelle', 520.5, 88888388),
(57, 'Rex', 'Chien', '2022-01-10', 'Mâle', 28, 88888388),
(58, 'Blanchette', 'Chèvre', '2024-02-20', 'Femelle', 45.3, 88888388),
(59, 'Shaun', 'Mouton', '2023-11-05', 'Mâle', 70, 88888388),
(60, 'Mistigri', 'Chat', '2021-08-15', 'Femelle', 4.2, 88888388),
(61, 'Eclair', 'Cheval', '2020-04-30', 'Mâle', 600, 88888388),
(62, 'Babe', 'Vache', '2024-01-15', 'Mâle', 85, 88888388),
(63, 'Bella', 'Mouton', '2022-09-10', 'Femelle', 490, 88888388),
(64, 'Panpan', 'Chat', '2025-03-01', 'Mâle', 2.5, 88888388),
(65, 'Galopin', 'Cheval', '2021-06-20', 'Mâle', 200, 88888388),
(100, 'Hercule', 'Cheval', '2019-05-20', 'Mâle', 720, 88888388),
(101, 'Luna', 'Chat', '2023-10-12', 'Femelle', 3.8, 88888388),
(102, 'Bibi', 'Mouton', '2024-01-05', 'Femelle', 65.2, 88888388),
(103, 'Goliath', 'Vache', '2022-03-15', 'Mâle', 810, 88888388),
(104, 'Daisy', 'Vache', '2021-11-30', 'Femelle', 540.5, 88888388),
(105, 'Filou', 'Chien', '2025-02-14', 'Mâle', 12, 88888388),
(106, 'Nala', 'Chat', '2022-06-25', 'Femelle', 4.5, 88888388),
(107, 'Cabri', 'Chèvre', '2024-03-01', 'Mâle', 30, 88888388),
(108, 'Spirit', 'Cheval', '2018-08-18', 'Mâle', 680, 88888388),
(109, 'Molly', 'Mouton', '2023-09-09', 'Femelle', 58, 88888388),
(110, 'bibi', 'chien', '2020-04-16', 'male', 2.5, 14532929);

-- --------------------------------------------------------

--
-- Structure de la table `article`
--

CREATE TABLE `article` (
  `id_article` int(11) NOT NULL,
  `nom` varchar(150) NOT NULL,
  `quantite_en_stock` double NOT NULL,
  `seuil_alerte` double NOT NULL,
  `unite_mesure` varchar(20) NOT NULL,
  `id_categorie` int(11) DEFAULT NULL,
  `id_user` int(11) DEFAULT NULL,
  `prix_unitaire` double DEFAULT NULL,
  `devise` varchar(255) NOT NULL DEFAULT 'Dinar Tunisien (TND)',
  `prix_achat_devise` float DEFAULT NULL,
  `id_admin` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `article`
--

INSERT INTO `article` (`id_article`, `nom`, `quantite_en_stock`, `seuil_alerte`, `unite_mesure`, `id_categorie`, `id_user`, `prix_unitaire`, `devise`, `prix_achat_devise`, `id_admin`) VALUES
(26, 'azote', 0, 20, 'kg', 12, 14532929, 33.951, 'EUR', 10, NULL),
(30, 'engrais', 80, 10, 'kg', 15, 14532929, 10, '', NULL, NULL),
(32, 'azote engrais', 1, 9, 'kg', 1, 88888888, 43.224, 'USD', 15, 11111111);

-- --------------------------------------------------------

--
-- Structure de la table `categorie`
--

CREATE TABLE `categorie` (
  `id_categorie` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` longtext DEFAULT NULL,
  `date_creation` datetime NOT NULL,
  `id_user` int(11) NOT NULL,
  `id_admin` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categorie`
--

INSERT INTO `categorie` (`id_categorie`, `nom`, `description`, `date_creation`, `id_user`, `id_admin`) VALUES
(1, 'engrais', 'engrais', '2026-04-21 21:15:46', 88888888, NULL),
(10, 'engrais', 'eng1', '2026-04-09 15:50:45', 14532929, NULL),
(12, 'engrais2.0', 'cdvhjjc', '2026-04-09 16:03:14', 14532929, NULL),
(13, 'engrais2.0', 'cdvhjjc', '2026-04-09 16:04:08', 14532929, NULL),
(14, 'sema', 'sem', '2026-03-03 16:04:18', 14532929, NULL),
(15, 'eya', 'wiusthiugsht', '2026-04-10 15:16:42', 36939696, NULL),
(16, 'semances 2', 'semances', '2026-04-21 21:34:33', 11111111, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `categorieevenement`
--

CREATE TABLE `categorieevenement` (
  `id_categorie` int(11) NOT NULL,
  `nom_categorie` varchar(100) NOT NULL,
  `description` longtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `login_history`
--

CREATE TABLE IF NOT EXISTS `login_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_cin` int(11) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `login_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `ip_address` varchar(45) DEFAULT NULL,
  `success` tinyint(1) DEFAULT NULL,
  `two_factor_used` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `evenement`
--

CREATE TABLE `evenement` (
  `id_evenement` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` longtext NOT NULL,
  `type_evenement` varchar(100) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `lieu` varchar(255) NOT NULL,
  `statut` varchar(50) NOT NULL,
  `id_categorie` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `examens_sante`
--

CREATE TABLE `examens_sante` (
  `id` int(11) NOT NULL,
  `date_examen` date DEFAULT NULL,
  `type_examen` varchar(100) DEFAULT NULL,
  `diagnostic` longtext DEFAULT NULL,
  `traitement` varchar(255) DEFAULT NULL,
  `id_animal` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `examens_sante`
--

INSERT INTO `examens_sante` (`id`, `date_examen`, `type_examen`, `diagnostic`, `traitement`, `id_animal`) VALUES
(1, '2026-04-05', 'Vaccin', 'En bonne santé', 'Observation', 1),
(2, '2026-04-11', 'Consultation', 'Urgence', 'Repos', 1),
(7, '2026-11-04', 'radio', 'radiographie', 'Chirurgie', 14),
(10, '2026-09-03', 'Scanner', 'Infection', 'Antibiotiques', 1),
(12, '2026-02-21', 'Scanner', 'Urgence', 'Chirurgie', 22),
(13, '2026-02-25', 'Scanner', 'Fracture', 'Repos', 15),
(14, '2026-03-04', 'Vaccin', 'En bonne santé', 'Repos', 18),
(15, '2026-04-01', 'Consultation', 'Infection', 'Observation', 37),
(16, '2026-04-10', 'Vaccin', 'Rappel annuel', 'Injection terminée', 1),
(17, '2026-04-12', 'Consultation', 'Contrôle de routine', 'Rien à signaler', 37),
(18, '2026-04-15', 'Vaccin', 'Grippe équine', 'Repos 24h', 34),
(19, '2026-04-18', 'Radio', 'Vérification patte', 'Légère entorse', 33),
(20, '2026-04-20', 'Scanner', 'Bilan complet', 'Observation', 35),
(21, '2026-04-12', 'Vaccin', 'Rappel annuel', 'Observation', 47),
(22, '2026-04-11', 'Consultation', 'Contrôle sabots', 'Repos', 46),
(23, '2026-04-02', 'Radio', 'Gestation OK', 'Observation', 45),
(24, '2026-03-25', 'Scanner', 'Bilan respiratoire', 'Repos', 44),
(25, '2026-04-10', 'Vaccin', 'Fièvre aphteuse', 'Observation', 21),
(26, '2026-04-05', 'Consultation', 'Examen de routine', 'Observation', 42),
(27, '2026-03-15', 'Vaccin', 'Rage', 'Observation', 41),
(28, '2026-04-08', 'Radio', 'Vérification hanche', 'Antibiotiques', 40),
(29, '2026-04-01', 'Consultation', 'Légère infection', 'Antibiotiques', 39),
(30, '2026-04-04', 'Vaccin', 'Rappel', 'Observation', 38),
(31, '2026-04-12', 'Consultation', 'CONTRÔLE DE ROUTINE', 'Rien à signaler', 37),
(32, '2026-04-01', 'Consultation', 'INFECTION', 'Observation', 37),
(33, '2026-03-12', 'Scanner', 'Infection', 'Chirurgie', 42),
(34, '2026-04-01', 'Vaccin', 'En bonne santé', 'Repos', 38),
(35, '2026-04-02', 'Vaccin', 'En bonne santé', 'Repos', 43),
(36, '2026-03-31', 'Vaccin', 'En bonne santé', 'Repos', 48),
(38, '2026-04-07', 'Vaccin', 'En bonne santé', 'Repos', 52),
(39, '2026-04-01', 'Scanner', 'Fracture', 'Chirurgie', 54),
(40, '2026-04-10', 'Consultation', 'En bonne santé', 'Observation', 56),
(41, '2026-04-12', 'Vaccin', 'En bonne santé', 'Observation', 57),
(42, '2026-04-14', 'Consultation', 'Infection', 'Antibiotiques', 58),
(43, '2026-04-15', 'Scanner', 'Fracture', 'Chirurgie', 59),
(44, '2026-04-16', 'Radio', 'Urgence', 'Chirurgie', 60),
(45, '2026-04-16', 'Consultation', 'Infection', 'Repos', 61),
(46, '2026-04-16', 'Vaccin', 'En bonne santé', 'Repos', 62),
(83, '2026-04-10', 'Vaccin', 'En bonne santé', 'Antibiotiques', 102),
(84, '2025-02-19', 'Vaccin', 'Infection', 'Observation', 100),
(85, '2026-05-08', 'Vaccin', 'En bonne santé', 'Repos', 62),
(86, '2026-05-10', 'Vaccin', 'Infection', 'Observation', 104),
(87, '2026-04-22', 'Vaccin', 'En bonne santé', 'Antibiotiques', 43),
(88, '2026-04-22', 'vaccination', 'rien', 'rage ', 55);

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `failed_login_attempts`
-- (Voir ci-dessous la vue réelle)
--
CREATE TABLE `failed_login_attempts` (
`email` varchar(255)
,`nombre_echecs` bigint(21)
,`derniere_tentative` datetime
,`ip_address` varchar(255)
);

-- --------------------------------------------------------

--
-- Structure de la table `machine`
--

CREATE TABLE `machine` (
  `idM` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `marque` varchar(255) NOT NULL,
  `modele` varchar(255) NOT NULL,
  `numeroSerie` varchar(255) DEFAULT NULL,
  `etatM` varchar(255) NOT NULL,
  `dateAchat` date DEFAULT NULL,
  `kilometrage` int(11) NOT NULL,
  `dateLastVisite` date DEFAULT NULL,
  `kmLastVisite` int(11) NOT NULL,
  `prochaineMaintenance` date DEFAULT NULL,
  `cin` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `machine`
--

INSERT INTO `machine` (`idM`, `nom`, `marque`, `modele`, `numeroSerie`, `etatM`, `dateAchat`, `kilometrage`, `dateLastVisite`, `kmLastVisite`, `prochaineMaintenance`, `cin`) VALUES
(1, 'Tracteur Principal', 'John Deere', '5075E', 'JD-5075E-2024-001', 'Bon', '2024-03-15', 0, NULL, 0, NULL, 11111112),
(2, 'Tracteur Polyvalent', 'Kubota', 'M7-153', 'KB-M7153-2023-002', 'Bon', '2023-07-20', 0, NULL, 0, NULL, 14532929),
(3, 'Presse à Balles', 'New Holland', 'BB960', 'NH-BB960-2022-005', 'Occasion', '2022-09-01', 0, NULL, 0, NULL, 11111112);

-- --------------------------------------------------------

--
-- Structure de la table `maintenance`
--

CREATE TABLE `maintenance` (
  `idMain` int(11) NOT NULL,
  `typePanne` varchar(255) NOT NULL,
  `cout` double NOT NULL,
  `dateMain` date DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `idM` int(11) DEFAULT NULL,
  `statut` enum('en_cours','termine','planifie') NOT NULL,
  `recommandation` text NOT NULL,
  `priorite` enum('faible','moyenne','haute','urgente') NOT NULL,
  `kilometrage` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `maintenance`
--

INSERT INTO `maintenance` (`idMain`, `typePanne`, `cout`, `dateMain`, `description`, `idM`, `statut`, `recommandation`, `priorite`, `kilometrage`) VALUES
(1, 'Hydraulique', 180, '2026-03-10', 'Vidange + filtres', 1, 'en_cours', '', 'faible', 0),
(2, 'Changement pneus', 320, '2026-04-05', 'Remplacement des 4 pneus arrière', 1, 'en_cours', '', 'faible', 0),
(3, 'Réparation hydraulique', 450, '2026-04-12', 'Fuite circuit hydraulique', 2, 'en_cours', '', 'faible', 0);

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `messenger_messages`
--

INSERT INTO `messenger_messages` (`id`, `body`, `headers`, `queue_name`, `created_at`, `available_at`, `delivered_at`) VALUES
(1, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1436:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, malek emna !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>benmalek.sarra55@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>94473118</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:26:\\\"benmalek.sarra55@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 10:47:32', '2026-04-17 10:47:32', NULL),
(2, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, malek emna !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>maleksarra362@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>94473118</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 10:49:22', '2026-04-17 10:49:22', NULL),
(3, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, malek emna !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>maleksarra362@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>94473118</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 10:51:20', '2026-04-17 10:51:20', NULL),
(4, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>votre.email@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:21:\\\"votre.email@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:00:26', '2026-04-17 11:00:26', NULL),
(5, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1435:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>maleksarra362@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:01:25', '2026-04-17 11:01:25', NULL),
(6, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1435:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>maleksarra362@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:01:33', '2026-04-17 11:01:33', NULL),
(7, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1435:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>maleksarra362@gmail.com</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:01:54', '2026-04-17 11:01:54', NULL),
(8, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>sarra.malek@esprit.tn</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:21:\\\"sarra.malek@esprit.tn\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:02:34', '2026-04-17 11:02:34', NULL),
(9, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>sarra.malek@esprit.tn</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:21:\\\"sarra.malek@esprit.tn\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:02:36', '2026-04-17 11:02:36', NULL),
(10, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>sarra.malek@esprit.tn</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:21:\\\"sarra.malek@esprit.tn\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:02:46', '2026-04-17 11:02:46', NULL);
INSERT INTO `messenger_messages` (`id`, `body`, `headers`, `queue_name`, `created_at`, `available_at`, `delivered_at`) VALUES
(11, 'O:36:\\\"Symfony\\\\Component\\\\Messenger\\\\Envelope\\\":2:{s:44:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0stamps\\\";a:1:{s:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\";a:1:{i:0;O:46:\\\"Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\\":1:{s:55:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Stamp\\\\BusNameStamp\\0busName\\\";s:21:\\\"messenger.bus.default\\\";}}}s:45:\\\"\\0Symfony\\\\Component\\\\Messenger\\\\Envelope\\0message\\\";O:51:\\\"Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\\":2:{s:60:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0message\\\";O:28:\\\"Symfony\\\\Component\\\\Mime\\\\Email\\\":6:{i:0;N;i:1;N;i:2;s:1433:\\\"\r\n                <div style=\\\'font-family: Segoe UI, sans-serif; max-width: 500px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden;\\\'>\r\n                    <div style=\\\'background: linear-gradient(135deg, #2d6a4f, #52b788); padding: 1.5rem 2rem;\\\'>\r\n                        <h2 style=\\\'color: white; margin: 0;\\\'>🌿 Bienvenue, Ouvrier Test !</h2>\r\n                    </div>\r\n                    <div style=\\\'padding: 2rem;\\\'>\r\n                        <p>Votre compte ouvrier a été créé. Voici vos identifiants de connexion :</p>\r\n                        <table style=\\\'width:100%; border-collapse: collapse; margin: 1rem 0;\\\'>\r\n                            <tr style=\\\'background:#f4f4f4;\\\'>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>📧 Email</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>sarra.malek@esprit.tn</td>\r\n                            </tr>\r\n                            <tr>\r\n                                <td style=\\\'padding:.6rem 1rem; font-weight:600;\\\'>🔑 Mot de passe</td>\r\n                                <td style=\\\'padding:.6rem 1rem;\\\'>55123456</td>\r\n                            </tr>\r\n                        </table>\r\n                        <p style=\\\'color:#888; font-size:.85rem;\\\'>Pensez à changer votre mot de passe après votre première connexion.</p>\r\n                    </div>\r\n                </div>\r\n            \\\";i:3;s:5:\\\"utf-8\\\";i:4;a:0:{}i:5;a:2:{i:0;O:37:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\\":2:{s:46:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0headers\\\";a:3:{s:4:\\\"from\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:4:\\\"From\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:23:\\\"maleksarra362@gmail.com\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:2:\\\"to\\\";a:1:{i:0;O:47:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:2:\\\"To\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:58:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\MailboxListHeader\\0addresses\\\";a:1:{i:0;O:30:\\\"Symfony\\\\Component\\\\Mime\\\\Address\\\":2:{s:39:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0address\\\";s:21:\\\"sarra.malek@esprit.tn\\\";s:36:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Address\\0name\\\";s:0:\\\"\\\";}}}}s:7:\\\"subject\\\";a:1:{i:0;O:48:\\\"Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\\":5:{s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0name\\\";s:7:\\\"Subject\\\";s:56:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lineLength\\\";i:76;s:50:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0lang\\\";N;s:53:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\AbstractHeader\\0charset\\\";s:5:\\\"utf-8\\\";s:55:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\UnstructuredHeader\\0value\\\";s:53:\\\"🌿 Vos identifiants de connexion - Gestion Agricole\\\";}}}s:49:\\\"\\0Symfony\\\\Component\\\\Mime\\\\Header\\\\Headers\\0lineLength\\\";i:76;}i:1;N;}}s:61:\\\"\\0Symfony\\\\Component\\\\Mailer\\\\Messenger\\\\SendEmailMessage\\0envelope\\\";N;}}', '[]', 'default', '2026-04-17 11:23:14', '2026-04-17 11:23:14', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `mouvement_stock`
--

CREATE TABLE `mouvement_stock` (
  `id` int(11) NOT NULL,
  `article_id` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `quantite` double NOT NULL,
  `date_mouvement` datetime NOT NULL,
  `motif` varchar(255) DEFAULT NULL,
  `id_user` int(11) NOT NULL,
  `id_admin` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `mouvement_stock`
--

INSERT INTO `mouvement_stock` (`id`, `article_id`, `type`, `quantite`, `date_mouvement`, `motif`, `id_user`, `id_admin`) VALUES
(1, 18, 'SORTIE', 100, '2026-04-05 16:11:21', ',nkj', 0, NULL),
(2, 18, 'ENTREE', 10, '2026-04-05 16:13:22', '', 0, NULL),
(3, 15, 'ENTREE', 10, '2026-04-05 16:13:58', 'jhgf', 0, NULL),
(4, 18, 'ENTREE', 50, '2026-04-08 00:46:10', 'qklkxjk', 0, NULL),
(5, 21, 'SORTIE', 5, '2026-04-08 23:13:53', 'ougyujgk', 12345678, NULL),
(6, 22, 'SORTIE', 4, '2026-04-08 23:59:47', 'khkjugj', 0, NULL),
(7, 22, 'ENTREE', 100, '2026-04-09 00:05:10', 'kqjbd<jhf', 12345678, NULL),
(9, 26, 'ENTREE', 12, '2026-04-09 15:59:17', '', 14532929, NULL),
(10, 26, 'SORTIE', 20, '2026-04-09 15:59:53', '', 14532929, NULL),
(40, 28, 'SORTIE', 50, '2026-04-21 16:34:47', 'Sortie par l’ouvrier 11223344', 11223344, NULL),
(100, 28, 'SORTIE', 40, '2026-04-21 18:12:30', 'Sortie par l’ouvrier 11223344', 11223344, NULL),
(101, 28, 'ENTREE', 100, '2026-04-21 18:19:15', '', 14532929, NULL),
(102, 30, 'SORTIE', 20, '2026-04-21 23:37:31', 'Sortie par l\'ouvrier 12369984', 12369984, NULL),
(103, 32, 'SORTIE', 90, '2026-04-22 00:07:03', '', 88888888, NULL),
(104, 32, 'ENTREE', 10, '2026-04-22 00:07:30', '', 88888888, NULL),
(105, 32, 'SORTIE', 11, '2026-04-22 00:08:51', '', 88888888, NULL),
(106, 32, 'SORTIE', 9, '2026-04-22 00:10:38', '', 88888888, NULL),
(107, 32, 'ENTREE', 100, '2026-04-23 15:32:02', 'hhdhd', 88888888, NULL),
(108, 32, 'SORTIE', 9, '2026-04-23 15:32:51', '', 88888888, NULL),
(109, 32, 'SORTIE', 90, '2026-04-23 15:33:27', '', 88888888, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `offres`
--

CREATE TABLE `offres` (
  `id_offres` int(11) NOT NULL,
  `nom_offre` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `prix` double DEFAULT NULL,
  `duree_offre` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `offres`
--

INSERT INTO `offres` (`id_offres`, `nom_offre`, `description`, `prix`, `duree_offre`) VALUES
(1, 'agricole pro', 'agricole pro qfqsff', 200, 56),
(2, 'Starter Agricole', 'Accès basique : suivi des terrains, animaux et stocks simples.', 29.9, 1),
(3, 'Pro Agricole', 'Accès complet : terrains, animaux, stocks, événements, rapports PDF et alertes.', 79.9, 6),
(4, 'Premium Agricole', 'Tout inclus + support prioritaire, alertes SMS, rapports avancés et analyses prédictives.', 139.9, 12),
(5, 'Starter Employé', 'Accès limité : consultation des tâches et participation aux événements.', 19.9, 1),
(6, 'Pro Employé', 'Accès complet employé : gestion des tâches, machines, maintenances et événements.', 49.9, 6),
(7, 'Pack Annuel Agricole', 'Offre Premium Agricole pour 12 mois avec 2 mois offerts.', 1199, 14),
(8, 'Essai Gratuit 10 jours', 'Accès complet à toutes les fonctionnalités pendant 10 jours.', 10, 10);

-- --------------------------------------------------------

--
-- Structure de la table `participation`
--

CREATE TABLE `participation` (
  `id_participation` int(11) NOT NULL,
  `statut_participation` varchar(50) NOT NULL,
  `date_inscription` date NOT NULL,
  `presence` tinyint(4) NOT NULL,
  `id_evenement` int(11) NOT NULL,
  `id_user` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `plante`
--

CREATE TABLE `plante` (
  `id_plante` int(11) NOT NULL,
  `nom_p` varchar(100) NOT NULL,
  `variete` varchar(100) DEFAULT NULL,
  `besoin_eau` double DEFAULT NULL,
  `cycle_jours` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `plante`
--

INSERT INTO `plante` (`id_plante`, `nom_p`, `variete`, `besoin_eau`, `cycle_jours`) VALUES
(1, 'Blé Dur', 'Karim', 0.6, 150),
(2, 'Blé Tender', 'Razzek', 0.65, 140),
(3, 'Orge', 'Rihane', 0.5, 130),
(4, 'Avoine', 'Local', 0.55, 120),
(5, 'Tomate', 'Heinz 1370', 0.9, 85),
(6, 'Tomate Cerise', 'Sweet 100', 0.85, 75),
(7, 'Piment Doux', 'Beldi', 0.7, 90),
(8, 'Poivron', 'California Wonder', 0.75, 95),
(9, 'Carotte', 'Nantaise', 0.65, 80),
(10, 'Pomme de Terre', 'Spunta', 0.8, 90),
(11, 'Olivier', 'Chemlali', 0.3, 365),
(12, 'Vigne', 'Muscat de Kelibia', 0.5, 180),
(13, 'Pommier', 'Golden Delicious', 0.7, 150),
(14, 'Citronnier', 'Meyer', 0.6, 300),
(15, 'Maïs', 'DK 501', 1.1, 120),
(16, 'Pastèque', 'Crimson Sweet', 0.9, 80),
(17, 'Melon', 'Cantaloup', 0.8, 75),
(18, 'Lentille', 'Verte du Puy', 0.4, 100);

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `recent_login_activity`
-- (Voir ci-dessous la vue réelle)
--
CREATE TABLE `recent_login_activity` (
`id` int(11)
,`cin` int(11)
,`nom_complet` varchar(511)
,`email` varchar(255)
,`login_time` datetime
,`ip_address` varchar(255)
,`success` tinyint(4)
,`two_factor_used` tinyint(4)
,`role_name` varchar(11)
);

-- --------------------------------------------------------

--
-- Structure de la table `rotation`
--

CREATE TABLE `rotation` (
  `id_rotation` int(11) NOT NULL,
  `date_debut_t` date DEFAULT NULL,
  `date_fin_t` date DEFAULT NULL,
  `status` int(11) NOT NULL DEFAULT 1,
  `id_terrain` int(11) DEFAULT NULL,
  `id_plante` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `rotation`
--

INSERT INTO `rotation` (`id_rotation`, `date_debut_t`, `date_fin_t`, `status`, `id_terrain`, `id_plante`) VALUES
(11, '2025-11-01', '2026-10-30', 1, 21, 8),
(12, '2026-04-10', '2026-06-30', 1, 22, 3),
(13, '2026-04-05', '2026-07-15', 1, 23, 1),
(14, '2026-03-20', '2026-06-10', 1, 24, 3),
(15, '2026-06-15', '2026-09-05', 0, 24, 5),
(16, '2026-04-01', '2026-07-20', 1, 1, 7),
(17, '2025-12-01', '2026-03-15', 0, 1, 9);

-- --------------------------------------------------------

--
-- Structure de la table `taches`
--

CREATE TABLE `taches` (
  `id_tache` int(11) NOT NULL,
  `nom_tache` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `etat` varchar(255) DEFAULT NULL,
  `priorite` varchar(255) DEFAULT NULL,
  `date_echeancee` date DEFAULT NULL,
  `assignee` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `taches`
--

INSERT INTO `taches` (`id_tache`, `nom_tache`, `description`, `etat`, `priorite`, `date_echeancee`, `assignee`) VALUES
(3, 'arrosage ', 'arrosage plantes . ', 'à faire', 'normale', '2026-04-23', 45632123),
(4, 'Arrosage des oliviers', 'Arroser les jeunes plants dans la parcelle nord', 'à faire', 'haute', '2026-04-20', 25852858),
(5, 'Nettoyage de l\'étable', 'Nettoyer les boxes des vaches et changer la litière', 'en cours', 'moyenne', '2026-04-18', 78978989),
(6, 'Récolte des tomates', 'Récolter les tomates mûres dans la serre', 'à faire', 'haute', '2026-04-22', 65412336),
(7, 'Contrôle des machines', 'Vérifier l\'état du tracteur John Deere', 'terminée', 'moyenne', '2026-04-16', 25852858),
(8, 'Vaccination des chèvres', 'Administrer le vaccin contre la fièvre aphteuse', 'à faire', 'haute', '2026-04-25', 78978989),
(10, '', '', 'à faire', 'normale', NULL, 25852858),
(11, 'Irrigation du Verger Pommes', 'Effectuer une irrigation complète du Verger Pommes pour éviter la sécheresse des plants de pommes', 'à faire', 'haute', '2026-04-19', 45632123),
(12, 'Arrosage d\'urgence du Verger Pommes', 'L\'arrosage du Verger Pommes est nécessaire en raison de la sécheresse actuelle pour préserver les pommes', 'à faire', 'haute', '2026-04-19', 65412336),
(13, 'Irrigation du Verger Pommes', 'Il est nécessaire d\'irriguer le Verger Pommes pour éviter la sécheresse et assurer la croissance des pommes', 'à faire', 'haute', '2026-04-26', 96336996),
(14, 'Labour du terrain \'la senia\'', 'Préparer le sol pour les cultures printanières', 'à faire', 'haute', '2026-04-23', 25852858),
(15, 'Désherbage au Verger Pommes', 'Enlever les mauvaises herbes', 'à faire', 'haute', '2026-04-22', 96336996),
(16, 'Vermifugation de Max', 'Traitement anti-parasitaire', 'à faire', 'urgente', '2026-04-23', 65412336),
(17, 'Nettoyage du tracteur', 'Nettoyer le tracteur pour éviter la corrosion', 'à faire', 'urgente', '2026-04-22', 45632123),
(18, 'Vermifugation de Max', 'Traitement antiparasitaire pour Max', 'à faire', 'urgente', '2026-04-22', 78978989),
(19, 'désherbage', 'Enlever les mauvaises herbes', 'à faire', 'urgente', '2026-04-23', 96336996),
(20, 'Vermifugation de Bibi', 'vaccination ', 'à faire', 'haute', '2026-04-25', 12369984),
(21, 'Vérification des filtres du tracteur', 'Vérifier les filtres à air et à huile', 'à faire', 'normale', '2026-04-23', 78978989),
(22, 'Vermifugation de printemps', 'Traitement antiparasitaire pour Max', 'à faire', 'haute', '2026-04-22', 78978989),
(23, 'Vidange du Tracteur Polyvalent', 'Vidange de l\'huile et des fluides', 'à faire', 'urgente', '2026-04-24', 25852858),
(24, 'Alimentation spéciale printemps pour Max', 'Fournir une alimentation riche pour Max', 'à faire', 'normale', '2026-04-24', 45632123),
(25, 'Arrosage des orge', 'Apporter des nutriments aux orges', 'à faire', 'haute', '2026-04-25', 12369984),
(26, 'fertilisation printemps la senia', 'Fertiliser le terrain \'la senia\' pour favoriser la croissance des cultures', 'à faire', 'haute', '2026-04-22', 12369985),
(27, 'fertilisation printemps Verger Pommes', 'Fertilisation des sols pour stimuler la croissance printanière', 'à faire', 'haute', '2026-04-26', 12369984),
(28, 'graissage du tracteur', 'Graissage des roues et des pièces mobiles', 'à faire', 'normale', '2026-04-27', 12369984),
(29, 'Alimentation spéciale printemps pour le chien Max', 'Fournir une alimentation équilibrée à Max pour le printemps', 'à faire', 'haute', '2026-04-27', 14532966);

-- --------------------------------------------------------

--
-- Structure de la table `terrain`
--

CREATE TABLE `terrain` (
  `id_terrain` int(11) NOT NULL,
  `nom_terrain` varchar(50) DEFAULT NULL,
  `surface` double DEFAULT NULL,
  `type_sol` varchar(200) DEFAULT NULL,
  `localisation` varchar(2000) DEFAULT NULL,
  `p_h` double DEFAULT NULL,
  `cin` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `terrain`
--

INSERT INTO `terrain` (`id_terrain`, `nom_terrain`, `surface`, `type_sol`, `localisation`, `p_h`, `cin`) VALUES
(1, 'test', 21, 'Argileux', 'jerba', 12, 11111112),
(21, 'Parcelle Oléicole Nord', 8.5, 'Argileux', 'Ariana - Raoued', 7.8, 12345678),
(22, 'Verger Pommes', 4.2, 'Limoneux', 'Tunis - Manouba', 7.2, 14532929),
(23, 'Champ Céréales', 15, 'Calcaire', 'Bizerte - Mateur', 8.1, 36939696),
(24, 'Parcelle Maraîchère', 2.8, 'Sableux', 'Nabeul - Soliman', 6.9, 12345678),
(25, 'la senia', 2, 'Argileux', 'jerba', 12, 14532929);

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `cin` int(11) NOT NULL,
  `nom` varchar(255) DEFAULT NULL,
  `prenom` varchar(255) DEFAULT NULL,
  `tel` varchar(8) DEFAULT NULL,
  `date_naiss` date DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `mdp` varchar(255) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `ville` varchar(255) DEFAULT NULL,
  `role` int(11) DEFAULT NULL,
  `date_creationcpt` date DEFAULT NULL,
  `date_dernierchg` date DEFAULT NULL,
  `two_factor_enabled` tinyint(4) NOT NULL DEFAULT 0,
  `two_factor_secret` varchar(32) DEFAULT NULL,
  `two_factor_backup_codes` longtext DEFAULT NULL,
  `img` varchar(255) NOT NULL DEFAULT 'default.png',
  `google_authenticator_secret` varchar(255) DEFAULT NULL,
  `id_terrain` int(11) DEFAULT NULL,
  `telegram_chat_id` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`cin`, `nom`, `prenom`, `tel`, `date_naiss`, `email`, `mdp`, `adresse`, `ville`, `role`, `date_creationcpt`, `date_dernierchg`, `two_factor_enabled`, `two_factor_secret`, `two_factor_backup_codes`, `img`, `google_authenticator_secret`, `id_terrain`, `telegram_chat_id`) VALUES
(11111111, 'sarra', 'malek', '94473118', '2004-08-05', 'sarra.malek@esprit.tn', '$2y$13$xUwgFTm40r0AnG3kFguvN.q6xyCOArKMbDAMfIdVaZVWcl7zaCMIK', 'tunis', 'soukra', 3, NULL, NULL, 0, NULL, NULL, 'https://res.cloudinary.com/dp0ily6qa/image/upload/v1776100433/agroflow/avatars/yzxffbuufvpbdc4xh1ua.jpg', NULL, NULL, NULL),
(11111112, 'agricole', 'agri', '94473118', '2004-08-05', 'agricole@gmail.com', '$2y$13$2pWEvWgkvWZq2TuptxMqNuW47ByzCeD2f.AgKVuKTlUz/8VGZKFOy', 'soukra', 'ariana', 2, NULL, '2026-04-23', 0, NULL, NULL, 'https://res.cloudinary.com/dp0ily6qa/image/upload/v1776252861/agroflow/avatars/jdzcbnuzkta0q01qlymf.png', NULL, NULL, NULL),
(12369984, 'MALEK', 'emna', '94473118', NULL, 'emnamalek97@gmail.com', '$2y$13$lvWe73YKrjOYbAFkgj0.QeFIYHXe2p6/pO1svJ3NcRZ.mR9PfkCz6', 'Centre-ville', 'Tunis', 1, '2026-04-17', NULL, 0, NULL, NULL, 'https://res.cloudinary.com/dp0ily6qa/image/upload/v1776535615/agroflow/avatars/q9l4spbmfdrbtzw3ftip.png', NULL, 25, NULL),
(12369985, 'emna', 'malek', '94473118', NULL, 'maleksarra362@gmail.com', '$2y$13$1tc9REDjgN7AZQue5JLAr.0l0JoEthx3NBilUPwHzeL0Rhop9Kiga', 'Centre-ville', 'Tunis', 1, '2026-04-17', NULL, 0, NULL, NULL, 'default.png', NULL, 25, NULL),
(14532929, 'Malek', 'Mariem', '94530100', '2004-05-21', 'mariem.malek@agroflow.tn', '$2y$13$KCR8rT35ZLJ.03DfbKb8/.HaOI0.J4wsqJ256o6dnSzbUCuDzaOGS', 'Centre Ville', 'Tunis', 2, '2026-04-04', '2026-04-15', 0, NULL, NULL, 'https://res.cloudinary.com/dp0ily6qa/image/upload/v1776342958/agroflow/avatars/lp8o8j2irhf9izbd9log.jpg', NULL, NULL, NULL),
(14532966, 'nounou ', 'landoulsi', '94473118', NULL, 'nourane.landoulsi@esprit.tn', '$2y$13$Cbo/PPrB9jVOsSDKuzuDrOi1VQJa1QUsRx5zQr4.R6D/iC7EuTi2S', 'Centre-ville', 'Tunis', 1, '2026-04-19', NULL, 0, NULL, NULL, 'default.png', NULL, 25, NULL),
(25852855, 'sarra', 'ouvrier', '94473118', NULL, 'benmalek.sarra55@gmail.com', '$2y$13$1IO/2jgkbHyJ.2mIaxmLreXo9OZSqCJxgQOW951NDVzkDO0IW2jWO', 'Centre-ville', 'Tunis', 1, '2026-04-17', NULL, 0, NULL, NULL, 'default.png', NULL, 25, NULL),
(25852858, 'Trabelsi', 'Emna', '94530100', '2004-06-16', 'emna.trabelsi@agroflow.tn', '$2y$13$/wnNMou8ZGFCBVPse3Ium.q2cSsZaeiZTIs.mX4pjBo0mZ6eoDKPa', 'La Soukra', 'Ariana', 1, '2026-04-06', '2026-04-15', 0, NULL, NULL, 'default.png', NULL, 22, NULL),
(36939696, 'Landoulsi', 'Nourane', '92587410', '2004-11-15', 'nourane.landoulsi@agroflow.tn', '$2y$13$EOHJaOW6zCvo.2V.mhqKp.HoP7hb.ucsUp7xjClbAzsOxzgHzaMPS', 'La Marsa', 'Tunis', 2, '2026-04-07', '2026-04-15', 0, NULL, NULL, 'default.png', NULL, NULL, NULL),
(45632123, 'ammar', 'ouvrier ', '78963258', NULL, 'ammar.ouvrier@gmail.com', '$2y$13$49AAw94AqN4yamoMZDwBze67RHEoucZToD45HwY9WScv9Nsve6xwW', 'Centre-ville', 'Tunis', 1, '2026-04-15', NULL, 0, NULL, NULL, 'default.png', NULL, 22, NULL),
(56565656, 'landousi', 'nounou', NULL, '2004-05-08', 'sarra.mal@esprit.tn', '$2y$13$WgxVRM1BudR2l3JvP2RgdOm4FGz3xLu9JdCscEzs9i/bUiTp.E7yu', 'Ariana', 'Tunis', 2, '2026-04-23', '2026-04-23', 0, NULL, NULL, 'default.png', NULL, NULL, NULL),
(65412336, 'Jlassi', 'Omar', '96587412', '1998-12-05', 'omar.jlassi@agroflow.tn', '$2y$13$t7TtihVm8NMT.kqPfwW/QelVi.4PRQ724LojwroZFcVJtbHcIn/6a', 'Soliman', 'Nabeul', 1, '2026-04-11', '2026-04-15', 0, NULL, NULL, 'default.png', NULL, 22, NULL),
(66666666, 'Admin', 'Principal', '98123456', '1985-06-15', 'admin@agroflow.tn', '$2y$13$7XpmB.34SGKRrjqstR7A..yynf5GjpJYBjKktTKxroO7mc5Ncazza', 'Siège Social', 'Tunis', 3, '2026-04-01', '2026-04-15', 0, NULL, NULL, 'default.png', NULL, NULL, NULL),
(78787878, 'landousi', 'nounou', '94473118', NULL, 'emna.rbii@esprit.tn', '$2y$13$tq/emjWOGglO3LTDrtMVPOKUbps/hilc0YUnorhG4rQlq27KHwJXu', 'Centre-ville', 'Tunis', 1, '2026-04-23', NULL, 0, NULL, NULL, 'default.png', NULL, 1, NULL),
(78978989, 'Khalifa', 'Ahmed', '97234567', '1995-09-10', 'ahmed.khalifa@agroflow.tn', '$2y$13$HxHcZnpzVcnObtrwH4bfKuJiDvEw6W.zrFK9FeocB/pzZ0RGmDTGm', 'Mornag', 'Ben Arous', 1, '2026-04-09', '2026-04-15', 0, NULL, NULL, 'default.png', NULL, 25, NULL),
(78998998, 'agriculteur', 'sarra', NULL, '2005-06-08', 'sarra.malek@esprit.nn', '$2y$13$rpBWV9RPibOwEYUkvVbbMuHPGI0fzxKmy24JPEU7//MZswHGzp7Ky', 'Sidi Makhlouf', 'Médenine', 2, '2026-04-13', '2026-04-18', 0, NULL, NULL, 'default.png', NULL, NULL, NULL),
(88888388, 'loulou', 'lili', NULL, '2004-05-08', 'lili@esprit.tn', '$2y$13$w04A/btif8vNqnMyB/WmWubQSrjWElFT5uxq/uZkY9rlionvDr.gu', 'Centre-ville', 'Tunis', 2, '2026-04-16', '2026-04-16', 0, NULL, NULL, 'default.png', NULL, NULL, NULL),
(88888887, 'loulou', 'lili', NULL, '2004-05-08', 'lili22@esprit.tn', '$2y$13$C0TVHPmyY.t1qRxyPZHKLO7ZrQGRAqscUyYqZRMBlvITKGHGwVxx6', 'Centre-ville', 'Tunis', 2, '2026-04-16', '2026-04-16', 0, NULL, NULL, 'default.png', NULL, NULL, NULL),
(88888888, 'Yessmine', 'Rezgui', '92345678', '1988-03-20', 'mallouli.eya@esprit.tn', '$2y$13$9VcyH9sWEV2KixYQBj.p0eVe3mDgfsVpe4FRRxGKP1Li3bqmR/PNi', 'Route de Bizerte Km 12', 'Ariana', 2, '2026-04-05', '2026-04-15', 0, NULL, NULL, 'default.png', NULL, NULL, '6221067209 '),
(94473112, 'salma', 'tissaoui', '94473118', NULL, 'salmaa.tissaoui@gmail.com', '$2y$13$q2rYiWszf4kH61E5rumuDueiIf6BvW1tulr0kP2qvMTfLjh.QMyly', 'Centre-ville', 'Tunis', 1, '2026-04-19', NULL, 0, NULL, NULL, 'default.png', NULL, 25, NULL),
(96336996, 'mohsen', 'ouvrier', '94473118', NULL, 'mohsen.ouvrier@esprit.tn', '$2y$13$XhGYE8pli6M/oZBGv862neSvR1b./wIg9TjWdCsI2klQQa1BIL1ki', 'Centre-ville', 'Tunis', 1, '2026-04-15', NULL, 0, NULL, NULL, 'default.png', NULL, 22, NULL);

-- --------------------------------------------------------

--
-- Doublure de structure pour la vue `users_with_2fa`
-- (Voir ci-dessous la vue réelle)
--
CREATE TABLE `users_with_2fa` (
`cin` int(11)
,`nom_complet` varchar(511)
,`email` varchar(255)
,`role` int(11)
,`two_factor_enabled` tinyint(4)
,`date_creation` date
);

-- --------------------------------------------------------

--
-- Structure de la vue `failed_login_attempts`
--
DROP TABLE IF EXISTS `failed_login_attempts`;

CREATE VIEW `failed_login_attempts` AS 
SELECT `lh`.`email` AS `email`, count(0) AS `nombre_echecs`, max(`lh`.`login_time`) AS `derniere_tentative`, `lh`.`ip_address` AS `ip_address` 
FROM `login_history` AS `lh` 
WHERE `lh`.`success` = 0 AND `lh`.`login_time` > current_timestamp() - interval 24 hour 
GROUP BY `lh`.`email`, `lh`.`ip_address` 
HAVING `nombre_echecs` >= 3 
ORDER BY count(0) DESC, max(`lh`.`login_time`) DESC ;

-- --------------------------------------------------------

--
-- Structure de la vue `recent_login_activity`
--
DROP TABLE IF EXISTS `recent_login_activity`;

CREATE VIEW `recent_login_activity` AS 
SELECT `lh`.`id` AS `id`, `u`.`cin` AS `cin`, concat(`u`.`prenom`,' ',`u`.`nom`) AS `nom_complet`, `u`.`email` AS `email`, `lh`.`login_time` AS `login_time`, `lh`.`ip_address` AS `ip_address`, `lh`.`success` AS `success`, `lh`.`two_factor_used` AS `two_factor_used`, 
CASE `u`.`role` WHEN 1 THEN 'Utilisateur' WHEN 2 THEN 'Employé' WHEN 3 THEN 'Admin' ELSE 'Inconnu' END AS `role_name` 
FROM `login_history` `lh` 
JOIN `users` `u` ON `lh`.`user_cin` = `u`.`cin` 
ORDER BY `lh`.`login_time` DESC ;

-- --------------------------------------------------------

--
-- Structure de la vue `users_with_2fa`
--
DROP TABLE IF EXISTS `users_with_2fa`;

CREATE VIEW `users_with_2fa` AS 
SELECT `users`.`cin` AS `cin`, concat(`users`.`prenom`,' ',`users`.`nom`) AS `nom_complet`, `users`.`email` AS `email`, `users`.`role` AS `role`, `users`.`two_factor_enabled` AS `two_factor_enabled`, cast(`users`.`date_creationcpt` as date) AS `date_creation` 
FROM `users` 
WHERE `users`.`two_factor_enabled` = 1 
ORDER BY `users`.`date_creationcpt` DESC ;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `abonnements`
--
ALTER TABLE `abonnements`
  ADD PRIMARY KEY (`id_abonn`);

--
-- Index pour la table `animaux`
--
ALTER TABLE `animaux`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_animaux_user` (`user_id`);

--
-- Index pour la table `article`
--
ALTER TABLE `article`
  ADD PRIMARY KEY (`id_article`),
  ADD KEY `id_user` (`id_user`),
  ADD KEY `id_admin` (`id_admin`);

--
-- Index pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id_categorie`),
  ADD KEY `id_admin` (`id_admin`),
  ADD KEY `id_user` (`id_user`);

--
-- Index pour la table `categorieevenement`
--
ALTER TABLE `categorieevenement`
  ADD PRIMARY KEY (`id_categorie`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD PRIMARY KEY (`id_evenement`),
  ADD KEY `IDX_B26681EC9486A13` (`id_categorie`);

--
-- Index pour la table `examens_sante`
--
ALTER TABLE `examens_sante`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_animaux_examens` (`id_animal`);

--
-- Index pour la table `machine`
--
ALTER TABLE `machine`
  ADD PRIMARY KEY (`idM`),
  ADD KEY `IDX_1505DF84ABE530DA` (`cin`);

--
-- Index pour la table `maintenance`
--
ALTER TABLE `maintenance`
  ADD PRIMARY KEY (`idMain`),
  ADD KEY `fk_maintenance_machine` (`idM`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750` (`queue_name`,`available_at`,`delivered_at`,`id`);

--
-- Index pour la table `mouvement_stock`
--
ALTER TABLE `mouvement_stock`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id_user` (`id_user`),
  ADD KEY `id_admin` (`id_admin`);

--
-- Index pour la table `offres`
--
ALTER TABLE `offres`
  ADD PRIMARY KEY (`id_offres`);

--
-- Index pour la table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`id_participation`),
  ADD KEY `IDX_AB55E24F8B13D439` (`id_evenement`),
  ADD KEY `IDX_AB55E24F6B3CA4B` (`id_user`);

--
-- Index pour la table `plante`
--
ALTER TABLE `plante`
  ADD PRIMARY KEY (`id_plante`);

--
-- Index pour la table `rotation`
--
ALTER TABLE `rotation`
  ADD PRIMARY KEY (`id_rotation`),
  ADD KEY `IDX_297C98F116EBFAC1` (`id_terrain`),
  ADD KEY `IDX_297C98F1774DDCAA` (`id_plante`);

--
-- Index pour la table `taches`
--
ALTER TABLE `taches`
  ADD PRIMARY KEY (`id_tache`),
  ADD KEY `IDX_3BF2CD987C9DFC0C` (`assignee`);

--
-- Index pour la table `terrain`
--
ALTER TABLE `terrain`
  ADD PRIMARY KEY (`id_terrain`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`cin`),
  ADD UNIQUE KEY `UNIQ_1483A5E9E7927C74` (`email`),
  ADD KEY `IDX_1483A5E916EBFAC1` (`id_terrain`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `abonnements`
--
ALTER TABLE `abonnements`
  MODIFY `id_abonn` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `animaux`
--
ALTER TABLE `animaux`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=111;

--
-- AUTO_INCREMENT pour la table `article`
--
ALTER TABLE `article`
  MODIFY `id_article` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT pour la table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id_categorie` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT pour la table `categorieevenement`
--
ALTER TABLE `categorieevenement`
  MODIFY `id_categorie` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id_evenement` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT pour la table `examens_sante`
--
ALTER TABLE `examens_sante`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=89;

--
-- AUTO_INCREMENT pour la table `machine`
--
ALTER TABLE `machine`
  MODIFY `idM` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `maintenance`
--
ALTER TABLE `maintenance`
  MODIFY `idMain` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT pour la table `mouvement_stock`
--
ALTER TABLE `mouvement_stock`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=110;

--
-- AUTO_INCREMENT pour la table `offres`
--
ALTER TABLE `offres`
  MODIFY `id_offres` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `participation`
--
ALTER TABLE `participation`
  MODIFY `id_participation` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `plante`
--
ALTER TABLE `plante`
  MODIFY `id_plante` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `rotation`
--
ALTER TABLE `rotation`
  MODIFY `id_rotation` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT pour la table `taches`
--
ALTER TABLE `taches`
  MODIFY `id_tache` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT pour la table `terrain`
--
ALTER TABLE `terrain`
  MODIFY `id_terrain` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `animaux`
--
ALTER TABLE `animaux`
  ADD CONSTRAINT `fk_animaux_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`cin`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `article`
--
ALTER TABLE `article`
  ADD CONSTRAINT `fk_admin_article` FOREIGN KEY (`id_admin`) REFERENCES `users` (`cin`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_users_article` FOREIGN KEY (`id_user`) REFERENCES `users` (`cin`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD CONSTRAINT `fk_admin_categorie` FOREIGN KEY (`id_admin`) REFERENCES `users` (`cin`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_users_categorie` FOREIGN KEY (`id_user`) REFERENCES `users` (`cin`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD CONSTRAINT `FK_B26681EC9486A13` FOREIGN KEY (`id_categorie`) REFERENCES `categorieevenement` (`id_categorie`);

--
-- Contraintes pour la table `examens_sante`
--
ALTER TABLE `examens_sante`
  ADD CONSTRAINT `fk_animaux_examens` FOREIGN KEY (`id_animal`) REFERENCES `animaux` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `machine`
--
ALTER TABLE `machine`
  ADD CONSTRAINT `FK_1505DF84ABE530DA` FOREIGN KEY (`cin`) REFERENCES `users` (`cin`) ON DELETE CASCADE;

--
-- Contraintes pour la table `maintenance`
--
ALTER TABLE `maintenance`
  ADD CONSTRAINT `fk_maintenance_machine` FOREIGN KEY (`idM`) REFERENCES `machine` (`idM`);

--
-- Contraintes pour la table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `FK_AB55E24F6B3CA4B` FOREIGN KEY (`id_user`) REFERENCES `users` (`cin`),
  ADD CONSTRAINT `FK_AB55E24F8B13D439` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`);

--
-- Contraintes pour la table `rotation`
--
ALTER TABLE `rotation`
  ADD CONSTRAINT `FK_297C98F116EBFAC1` FOREIGN KEY (`id_terrain`) REFERENCES `terrain` (`id_terrain`),
  ADD CONSTRAINT `FK_297C98F1774DDCAA` FOREIGN KEY (`id_plante`) REFERENCES `plante` (`id_plante`);

--
-- Contraintes pour la table `taches`
--
ALTER TABLE `taches`
  ADD CONSTRAINT `FK_3BF2CD987C9DFC0C` FOREIGN KEY (`assignee`) REFERENCES `users` (`cin`);

--
-- Contraintes pour la table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `FK_1483A5E916EBFAC1` FOREIGN KEY (`id_terrain`) REFERENCES `terrain` (`id_terrain`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
