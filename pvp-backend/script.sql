#@(#) script.ddl

DROP TABLE IF EXISTS Failai;
DROP TABLE IF EXISTS Saskaita;
DROP TABLE IF EXISTS Gedimas;
DROP TABLE IF EXISTS uzregistruotas_prasymas;
DROP TABLE IF EXISTS Uzregistruoti_gedimai;
DROP TABLE IF EXISTS Atsiliepimas;
DROP TABLE IF EXISTS Adresas;
DROP TABLE IF EXISTS laukas;
DROP TABLE IF EXISTS Gyventojas;
DROP TABLE IF EXISTS Darbuotojas;
DROP TABLE IF EXISTS prasymas;
DROP TABLE IF EXISTS Prasymo_busena;
DROP TABLE IF EXISTS Naudotojas;

CREATE TABLE Naudotojas
(
	vardas varchar (255),
	pavarde varchar (255),
	el_pastas varchar (255),
	tel_numeris varchar(255),
	slaptazodis varchar (255),
	id_Naudotojas integer NOT NULL AUTO_INCREMENT,
	PRIMARY KEY(id_Naudotojas)
);

CREATE TABLE Prasymo_busena
(
	id_Prasymo_busena integer,
	name char (9) NOT NULL,
	PRIMARY KEY(id_Prasymo_busena)
);
INSERT INTO Prasymo_busena(id_Prasymo_busena, name) VALUES(1, 'Priimtas');
INSERT INTO Prasymo_busena(id_Prasymo_busena, name) VALUES(2, 'Atmestas');
INSERT INTO Prasymo_busena(id_Prasymo_busena, name) VALUES(3, 'Vykdomas');
INSERT INTO Prasymo_busena(id_Prasymo_busena, name) VALUES(4, 'Ivykdytas');

CREATE TABLE prasymas
(
	pavadinimas varchar (255),
	aprasymas varchar (999),
	data date,
	aktyvus boolean DEFAULT false,
	failas text,
	role varchar (45),
	id_prasymas integer NOT NULL AUTO_INCREMENT,
	PRIMARY KEY(id_prasymas)
);

CREATE TABLE Darbuotojas
(
	id_Naudotojas integer,
	role varchar (45),
	PRIMARY KEY(id_Naudotojas),
	FOREIGN KEY(id_Naudotojas) REFERENCES Naudotojas (id_Naudotojas)
);

CREATE TABLE Gyventojas
(
	id_Naudotojas integer,
	el_pasto_priminimai boolean DEFAULT false,
	tel_numerio_priminimai boolean DEFAULT false,
	atnaujinti_duomenys date,
	PRIMARY KEY(id_Naudotojas),
	FOREIGN KEY(id_Naudotojas) REFERENCES Naudotojas (id_Naudotojas)
);

CREATE TABLE laukas
(
	etikete varchar (255),
	tipas varchar (255),
	privalomas boolean DEFAULT true,
	pasirinkimai varchar (500),
	eil_nr int,
	id_laukas integer NOT NULL AUTO_INCREMENT,
	fk_prasymasid_prasymas integer NOT NULL,
	PRIMARY KEY(id_laukas),
	CONSTRAINT itraukia FOREIGN KEY(fk_prasymasid_prasymas) REFERENCES prasymas (id_prasymas)
);

CREATE TABLE Adresas
(
	miestas varchar (255),
	gatve varchar (255),
	namo_numeris integer,
	pasto_kodas integer,
	id_Adresas integer NOT NULL AUTO_INCREMENT,
	fk_Gyventojasid_Naudotojas integer NOT NULL,
	PRIMARY KEY(id_Adresas),
	CONSTRAINT turi FOREIGN KEY(fk_Gyventojasid_Naudotojas) REFERENCES Gyventojas (id_Naudotojas)
);

CREATE TABLE Atsiliepimas
(
	ivertinimas integer,
	aprasymas varchar (255),
	data date,
	id_Atsiliepimas integer NOT NULL AUTO_INCREMENT,
	fk_Gyventojasid_Naudotojas integer NOT NULL,
	PRIMARY KEY(id_Atsiliepimas),
	CONSTRAINT palieka FOREIGN KEY(fk_Gyventojasid_Naudotojas) REFERENCES Gyventojas (id_Naudotojas)
);

CREATE TABLE Uzregistruoti_gedimai
(
	id_Uzregistruoti_gedimai integer NOT NULL AUTO_INCREMENT,
	fk_Darbuotojasid_Naudotojas integer,
	fk_Gyventojasid_Naudotojas integer NOT NULL,
	PRIMARY KEY(id_Uzregistruoti_gedimai),
	CONSTRAINT valdo FOREIGN KEY(fk_Darbuotojasid_Naudotojas) REFERENCES Darbuotojas (id_Naudotojas),
	CONSTRAINT registruoja FOREIGN KEY(fk_Gyventojasid_Naudotojas) REFERENCES Gyventojas (id_Naudotojas)
);

CREATE TABLE uzregistruotas_prasymas
(
	duomenys text,
	pavadinimas varchar (255),
	aprasymas text,
	data DATETIME,
	busena integer,
	id_uzregistruotas_prasymas integer NOT NULL AUTO_INCREMENT,
	fk_Darbuotojasid_Naudotojas integer,
	fk_Gyventojasid_Naudotojas integer NOT NULL,
	fk_prasymasid_prasymas integer,
	PRIMARY KEY(id_uzregistruotas_prasymas),
	FOREIGN KEY(busena) REFERENCES Prasymo_busena (id_Prasymo_busena),
	CONSTRAINT tvarko FOREIGN KEY(fk_Darbuotojasid_Naudotojas) REFERENCES Darbuotojas (id_Naudotojas),
	CONSTRAINT pateikia FOREIGN KEY(fk_Gyventojasid_Naudotojas) REFERENCES Gyventojas (id_Naudotojas),
	CONSTRAINT priklauso FOREIGN KEY(fk_prasymasid_prasymas) REFERENCES prasymas (id_prasymas)
);

CREATE TABLE Gedimas
(
	uzregistravimo_data DATETIME,
	aprasymas varchar (500),
	tipas varchar (255),
	adresas varchar (255),
	id_Gedimas integer NOT NULL AUTO_INCREMENT,
	fk_Uzregistruoti_gedimaiid_Uzregistruoti_gedimai integer NOT NULL,
	PRIMARY KEY(id_Gedimas),
	UNIQUE(fk_Uzregistruoti_gedimaiid_Uzregistruoti_gedimai),
	CONSTRAINT ieina FOREIGN KEY(fk_Uzregistruoti_gedimaiid_Uzregistruoti_gedimai) REFERENCES Uzregistruoti_gedimai (id_Uzregistruoti_gedimai)
);

CREATE TABLE Saskaita
(
	imones_pavadinimas varchar (255),
	mokejimo_data DATETIME,
	sumoketi_iki DATETIME,
	gavimo_data DATETIME,
	iban varchar (255),
	suma decimal,
	imokos_kodas varchar (255),
	aprasymas varchar (255),
	sumoketa_suma decimal,
	sumoketa boolean DEFAULT false,
	json varchar (9999),
	id_Saskaita integer NOT NULL AUTO_INCREMENT,
	fk_Gyventojasid_Naudotojas integer NOT NULL,
	fk_Adresasid_Adresas integer NOT NULL,
	PRIMARY KEY(id_Saskaita),
	CONSTRAINT moka FOREIGN KEY(fk_Gyventojasid_Naudotojas) REFERENCES Gyventojas (id_Naudotojas),
	CONSTRAINT priklauso2 FOREIGN KEY(fk_Adresasid_Adresas) REFERENCES Adresas (id_Adresas)
);

CREATE TABLE Failai
(
	fizinis_kelias varchar (255),
	pavadinimas varchar (255),
	id_Failai integer NOT NULL AUTO_INCREMENT,
	fk_Gedimasid_Gedimas integer NOT NULL,
	PRIMARY KEY(id_Failai),
	CONSTRAINT susijes FOREIGN KEY(fk_Gedimasid_Gedimas) REFERENCES Gedimas (id_Gedimas)
);
