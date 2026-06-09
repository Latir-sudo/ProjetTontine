/*==============================================================*/
/* SUPPRESSION DES TABLES                                       */
/*==============================================================*/

DROP TABLE IF EXISTS NOTIFICATION CASCADE;
DROP TABLE IF EXISTS PENALITE CASCADE;
DROP TABLE IF EXISTS PAIEMENT CASCADE;
DROP TABLE IF EXISTS ORDRE_PASSAGE CASCADE;
DROP TABLE IF EXISTS TOUR CASCADE;
DROP TABLE IF EXISTS CYCLE CASCADE;
DROP TABLE IF EXISTS INVITATION CASCADE;
DROP TABLE IF EXISTS MEMBRE CASCADE;
DROP TABLE IF EXISTS ROLE CASCADE;
DROP TABLE IF EXISTS TONTINE CASCADE;
DROP TABLE IF EXISTS COTISATION CASCADE;
DROP TABLE IF EXISTS UTILISATEURS CASCADE;

/*==============================================================*/
/* UTILISATEURS                                                 */
/*==============================================================*/
CREATE TABLE UTILISATEURS (
   USERID SERIAL PRIMARY KEY,
   PRENOM_UTILISATEUR VARCHAR(80),
   EMAIL VARCHAR(100) UNIQUE,
   TELEPHONE VARCHAR(20),
   USERPASSWORD VARCHAR(255),
   DATE_INSCRIPTION DATE,
   STATUT_COMPTE VARCHAR(15),
   VILLE VARCHAR(30)
);

/*==============================================================*/
/* ROLE                                                         */
/*==============================================================*/
CREATE TABLE ROLE (
   ID_ROLE SERIAL PRIMARY KEY,
   NOM_ROLE VARCHAR(30) UNIQUE
);

INSERT INTO ROLE (NOM_ROLE) VALUES
('ADMIN'),
('MEMBRE'),
('TRESORIER');

/*==============================================================*/
/* TONTINE                                                      */
/*==============================================================*/
CREATE TABLE TONTINE (
   ID_TONTINE SERIAL PRIMARY KEY,
   NOM_TONTINE VARCHAR(50),
   DESCRIPTION_TONTINE TEXT,
   MONTANT_COTISATION BIGINT,
   FREQUENCE VARCHAR(20),
   DATE_DE_CREATION DATE,
   STATUT_TONTINE VARCHAR(15),
   POLITIQUE_TONTINE TEXT,
   CODE_ACCES VARCHAR(20)
);

/*==============================================================*/
/* MEMBRE                                                       */
/*==============================================================*/
CREATE TABLE MEMBRE (
   ID_MEMBRE SERIAL PRIMARY KEY,
   USERID INT NOT NULL,
   ID_TONTINE INT NOT NULL,
   ID_ROLE INT NOT NULL,
   DATEADHESION DATE,
   PREFERENCE_NOTIFICATION VARCHAR(20),

   CONSTRAINT FK_MEMBRE_USER FOREIGN KEY (USERID)
      REFERENCES UTILISATEURS(USERID),

   CONSTRAINT FK_MEMBRE_TONTINE FOREIGN KEY (ID_TONTINE)
      REFERENCES TONTINE(ID_TONTINE),

   CONSTRAINT FK_MEMBRE_ROLE FOREIGN KEY (ID_ROLE)
      REFERENCES ROLE(ID_ROLE),

   CONSTRAINT UNIQUE_MEMBRE UNIQUE (USERID, ID_TONTINE)
);

/*==============================================================*/
/* COTISATION                                                   */
/*==============================================================*/
CREATE TABLE COTISATION (
   ID_COTISATION SERIAL PRIMARY KEY,
   ID_MEMBRE INT NOT NULL,
   MONTANT_TOTAL BIGINT NOT NULL,
   STATUT_COTISATION VARCHAR(15) DEFAULT 'EN_ATTENTE',
   DATE_COTISATION DATE DEFAULT CURRENT_DATE,

   CONSTRAINT FK_COTISATION_MEMBRE FOREIGN KEY (ID_MEMBRE)
      REFERENCES MEMBRE(ID_MEMBRE)
);

/*==============================================================*/
/* CYCLE                                                        */
/*==============================================================*/
CREATE TABLE CYCLE (
   ID_CYCLE SERIAL PRIMARY KEY,
   ID_TONTINE INT NOT NULL,
   DATE_DEBUT DATE,
   DATE_FIN DATE,
   STATUT_CYCLE VARCHAR(20),

   CONSTRAINT FK_CYCLE_TONTINE FOREIGN KEY (ID_TONTINE)
      REFERENCES TONTINE(ID_TONTINE)
);

/*==============================================================*/
/* TOUR                                                         */
/*==============================================================*/
CREATE TABLE TOUR (
   ID_TOUR SERIAL PRIMARY KEY,
   ID_CYCLE INT NOT NULL,
   ORDRE_TOUR INT,
   DATE_TOUR DATE,
   ID_BENEFICIAIRE INT,

   CONSTRAINT FK_TOUR_CYCLE FOREIGN KEY (ID_CYCLE)
      REFERENCES CYCLE(ID_CYCLE),

   CONSTRAINT FK_TOUR_BENEFICIAIRE FOREIGN KEY (ID_BENEFICIAIRE)
      REFERENCES MEMBRE(ID_MEMBRE)
);

/*==============================================================*/
/* ORDRE DE PASSAGE                                             */
/*==============================================================*/
CREATE TABLE ORDRE_PASSAGE (
   ID_ORDRE SERIAL PRIMARY KEY,
   ID_CYCLE INT NOT NULL,
   ID_MEMBRE INT NOT NULL,
   POSITION INT NOT NULL,

   CONSTRAINT FK_ORDRE_CYCLE FOREIGN KEY (ID_CYCLE)
      REFERENCES CYCLE(ID_CYCLE),

   CONSTRAINT FK_ORDRE_MEMBRE FOREIGN KEY (ID_MEMBRE)
      REFERENCES MEMBRE(ID_MEMBRE),

   CONSTRAINT UNIQUE_POSITION UNIQUE (ID_CYCLE, POSITION),
   CONSTRAINT UNIQUE_MEMBRE_CYCLE UNIQUE (ID_CYCLE, ID_MEMBRE)
);

/*==============================================================*/
/* PAIEMENT                                                     */
/*==============================================================*/
CREATE TABLE PAIEMENT (
   ID_PAIEMENT SERIAL PRIMARY KEY,
   ID_COTISATION INT NOT NULL,
   MONTANT BIGINT NOT NULL,
   DATE_PAIEMENT DATE DEFAULT CURRENT_DATE,
   MODE_PAIEMENT VARCHAR(20),
   REFERENCE VARCHAR(100),
   VALIDE BOOLEAN DEFAULT FALSE,

   CONSTRAINT FK_PAIEMENT_COTISATION FOREIGN KEY (ID_COTISATION)
      REFERENCES COTISATION(ID_COTISATION)
);

/*==============================================================*/
/* PENALITE                                                     */
/*==============================================================*/
CREATE TABLE PENALITE (
   ID_PENALITE SERIAL PRIMARY KEY,
   ID_MEMBRE INT NOT NULL,
   ID_TOUR INT,
   MONTANT BIGINT NOT NULL,
   MOTIF VARCHAR(100),
   DATE_PENALITE DATE,
   STATUT VARCHAR(20),

   CONSTRAINT FK_PENALITE_MEMBRE FOREIGN KEY (ID_MEMBRE)
      REFERENCES MEMBRE(ID_MEMBRE),

   CONSTRAINT FK_PENALITE_TOUR FOREIGN KEY (ID_TOUR)
      REFERENCES TOUR(ID_TOUR)
);

/*==============================================================*/
/* INVITATION                                                   */
/*==============================================================*/
CREATE TABLE INVITATION (
   ID_INVITATION SERIAL PRIMARY KEY,
   ID_MEMBRE INT NOT NULL,
   CODE_INVITATION VARCHAR(20),
   MESSAGE_INVITATION TEXT,
   TYPE_INVITATION VARCHAR(20),
   DATE_INVITATION DATE,
   STATUT_INVITATION VARCHAR(15),

   CONSTRAINT FK_INVITATION_MEMBRE FOREIGN KEY (ID_MEMBRE)
      REFERENCES MEMBRE(ID_MEMBRE)
);

/*==============================================================*/
/* NOTIFICATION                                                 */
/*==============================================================*/
CREATE TABLE NOTIFICATION (
   ID_NOTIFICATION SERIAL PRIMARY KEY,
   USERID INT NOT NULL,
   TITRE VARCHAR(100),
   MESSAGE TEXT,
   TYPE_NOTIFICATION VARCHAR(30),
   DATE_ENVOI TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   EST_LUE BOOLEAN DEFAULT FALSE,
   CANAL VARCHAR(20),

   ID_TOUR INT,
   ID_PAIEMENT INT,
   ID_INVITATION INT,

   CONSTRAINT FK_NOTIFICATION_USER FOREIGN KEY (USERID)
      REFERENCES UTILISATEURS(USERID),

   CONSTRAINT FK_NOTIF_TOUR FOREIGN KEY (ID_TOUR)
      REFERENCES TOUR(ID_TOUR),

   CONSTRAINT FK_NOTIF_PAIEMENT FOREIGN KEY (ID_PAIEMENT)
      REFERENCES PAIEMENT(ID_PAIEMENT),

   CONSTRAINT FK_NOTIF_INVITATION FOREIGN KEY (ID_INVITATION)
      REFERENCES INVITATION(ID_INVITATION)
);