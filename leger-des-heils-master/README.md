# leger-des-heils
GebruikersHandleiding
ALS ER EEN ERROR KOMT OVER DRIVERS, DAN WERKT DE JAR NIET
U MOET DAN ZORGEN DAT HET DLL BESTAND BESCHIKBAAR IS VOOR HET PROGRAMMA
ANDERS GEBRUIKT U GEWOON NETBEANS
---------------------------------------------
Properties voor database connectie

De applicatie gebruikt 2 ‘properties’ bestanden: 
Database.properties: dit is data voor de geaggregeerde database
signalDatabase.properties: dit is de data voor de signaal database


In beide bestanden staan 2 properties, hieronder staat waar deze voor dienen

Property: Waar dient dit voor?
url: Dit is de locatie en het poortnummer van de server waar de database op staat. Lokaal is dit localhost:1433

database_name: Dit is de naam van de database, bij ons is was dit “signalsDatabase” voor de signaal database en “AuditBlackBox” voor de geaggregeerde database


ALS DE PROPERTIES GEGEVENS ONTBREKEN, WERKT HET PROGRAMMA NIET.
Als de geaggregeerde database niet bestaat, werkt het programma ook niet
Het programma maakt automatisch een signalsDatabase aan op de locatie gespecificeerd in het signalsDatabase.properties bestand

------------------------------------
Authenticatie van de gebruiker
De gebruiker wordt geauthenticeerd met de kolom Username in de tabel AD-Export in de geaggregeerde database. Hierin moet de e-mail van het microsoft account staan van de gebruiker, anders heeft deze geen toegang tot de applicatie. Dit bepaald of de gebruiker admin is of niet.

Hoe los je dit op en wordt je admin?
- Maak een nieuwe user in de tabel AD-Export
- Pas de code aan in het Netbeansproject in de klasse “Authenticator.java” bij de method “getLoggedInUser”. Voeg hier dit stukje code toe:
- return (new User("dummy", new ArrayList<String>(), true));







---------------------------------------------------------
Werking van de applicatie:

Werking van knoppen:

Knop                                    Beschrijving
Zoek signalen                           Zoekt signalen in geaggregeerde database
                                        Leest signalen uit signaal database
                                        Vergelijkt signalen
                                        Toont de signalen
Reset                                   Zet alle tellers en tabellen leeg
Schrijf en Update signalen              Schrijft nieuwe signalen naar signaal database
                                        Update signalen die nu opgelost zijn naar signaal database
                                        Reset de tabellen na afloop om integriteit te behouden
Scrhijf naar Csv                        Schrijft de signalen naar een excel bestand in Csv format
Klikken op een signaal                  Zoekt de impact van het signaal en toont deze
Klikken op de tekst met een bericht     Toont de berichten in een popup scherm, handig als het een lang bericht is dat niet past in de label
Sleeplijn                               Om delen van het scherm groter of kleiner te maken

-------------------------
Informatie op het scherm

Onderdeel                     Beschrijving
Data paneel                   Bevat naam van het gebruikers account en entiteit van de gebruiker
Business Rules paneel         Bevat een tabel met business rules en het aantal onopgeloste signalen passend bij de businessrule
Signalen paneel               Bevat een tabel met signalen
                              Datum opgelost is ingevuld als het signaal opgelost is en niet geschreven is in de signaal database
                              Connectie data bevat informatie over de persoon die dit signaal gevonden heeft
                              De rest spreekt voor zich
Knoppen paneel                Bevat de verschillende knoppen die gebruikt worden voor interactie met het systeem


Door op het kruisje rechtsboven het hoofdscherm te klikken, wordt het programma afgesloten. Eventuele openstaande verbindingen worden ivm veiligheid afgesloten

------------------------------
Wat is er speciaal aan onze applicatie?
We maken gebruik van multi threading voor extra snelheid.
We hebben er voor gezorgd dat de properties uit een file gelezen kunnen worden waardoor de server gespecificeerd kan worden.
Bij foutmeldingen wordt een label gevuld met tekst, waar op geklikt kan worden voor een heldere pop-up melding.
De signaal database wordt automatisch aangemaakt als deze niet bestaat.
