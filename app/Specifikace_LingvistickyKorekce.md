# Lingvistický a Korekční Modul (NutriKalk Grammar & Diacritics Engine)

Tato specifikace popisuje modul zodpovědný za plně automatické čištění diakritiky, překlepů a gramatiky v celém ekosystému NutriKalk. Modul operuje primárně na backendu jako Node.js / Firebase Microservice, čímž nezatěžuje výkon mobilního zařízení.

## 1. Architektura a Cíle
Cílem modulu je zajistit perfektní datovou integritu pro budoucí vyhledávání a čtení. Uživatel nesmí v aplikaci (natož v její hlavní databázi) narazit na chybně zadaná slova ("kureci prsa") bez diakritiky způsobená horším rozpoznáním AI nebo spěchem lokálního jazykového modelu.

### Primární Funkce:
*   **A) Korekce AI generovaných textů (Recipes):** Rychlá post-processingová (Proofreading) vrstva, kterou proběhne každý nagenerovaný recept před jeho deserializací v Android aplikaci.
*   **B) Korekce chybného skenování z obalů (OCR Cleaning):** Samostatná Cloud funkce. Uživatel nafotí obal v zahraničí nebo vyfotí zmuchlaný pytlík mrkve. Surový `tessaract` / `ml-kit` output se ihned pošle do backendu, kde ho AI model "vyléčí", odhadne chybějící slabiky a doplní háčky/čárky, které na obalu chyběly nebo byly špatně čitelné.

---

## 2. Jazyková Uniformita a Markdown
Zásadním pravidlem proofreading engine je **ochrana struktury Markdownu** (především tabulky makroživin a nadpisy úrovní `##` a `###`). 

### Řídící Prompt pro Korekční Model:
LLM model vykonávající proofreading používá striktní instruktáž:
> "Bezpodmínečně zachovej veškeré původní formátování (Markdown tabulky, nadpisy, odrážky, tučné písmo, mezery). Nesmíš změnit strukturu kódu ani odřádkování, oprav pouze slova zasažená gramatickou/diakritickou chybou."

Díky tomu Android aplikace pro svůj `MarkdownText()` renderer vždy dostane validní a bezpečně strukturovaný string. Kód je plně kompatibilní s nativním parsováním tabulek z Markdownu.

### Lokální Specifika (Čeština vs. Polština)
Engine si ze signatury požadavku vytáhne kód jazyka (`language`).
*   Pro `PL` vynucuje pozornost na sadu: **ą, ć, ę, ł, ń, ó, ś, ź, ż**.
*   Pro `CZ` vynucuje klasickou českou diakritiku, opravuje "mě/mně", "i/y" a ypsilon tam, kde má být, zejména v názvech surovin (např. oprava "ryze" -> "rýže").
*   Pokud je v hlavičce mezinárodní EAN (např. z Německa `DE`), přiladí korekční model svůj kontext nad slovníkem dané země (doplní *Umlauty* atd.)

---

## 3. Ukázka toku dat a Workflow

1.  **Android Client:** Volá endpoint `smartRecipeBot` a čeká na vygenerovaný recept.
2.  **Backend AI (Generace):** Základní LLM vygeneruje recept. Může se stát, že model vygeneruje: `- 100g kureci prsa`.
3.  **Backend AI (Linguistic Layer):** Základní výstup putuje asynchronně (nebo streamově přes WebSockets/SSE) do `performLinguisticCorrection`. 
4.  **Proofread Output:** Korektor vylepší text na `- 100g kuřecí prsa`.
5.  **Android Client:** Dostává `200 OK` odpověď s perfektním Markdownem. Tabulky fungují, čeština/polština je bez chyb.

### 4. Implementace v Node.js
Modul byl implemetován v souboru `backend-functions/index.js` prostřednictvím:
- Metody `performLinguisticCorrection(rawText, targetLanguageCode)`, která plní Proofreading pro bot API.
- Zcela nového Endpointu `cleanOcrText`, který čistí pouze surové poškozené bloky znaků a vrací validní JSON objekt.
