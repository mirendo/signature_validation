# Signature validation for Årsredovisning Online

**This readme is in Swedish. Sorry to everyone else.**

Med Årsredovisning Online kan man skriva under originalet av en årsredovisning med elektroniska signaturer via BankID.
Resultatet blir en PDF-version av årsredovisningen. I denna PDF finns ett antal bifogade filer som kan användas för
att som utomstående verifiera att rätt personer faktiskt har signerat årsredovisningen. Detta projekt är ett exempel
på hur sådan validering kan gå till.

## Användning

* Klona detta repo.
* Kör `mvn package`
* Kör `
java -cp target/uber-signature_validator-0.1-SNAPSHOT.jar <pdf-fil>
`
