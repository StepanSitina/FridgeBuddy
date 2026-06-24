# Technická specifikace: Cestovní režim & AI Automatický sběrač neznámých potravin (NutriKalk)

Tento dokument definuje technickou architekturu, logický tok a implementační detaily pro modul chytrého dohledávání potravin v zahraničí a crowd-sourced rozšiřování databáze aplikací NutriKalk.

---

## 1. AUTOMATICKÁ GEOLOKACE (Cestovní režim)

Aplikace inteligentně přizpůsobuje vyhledávací zdroje (API) podle aktuální lokality uživatele, aby maximalizovala šanci na nalezení lokálních potravin v zahraničí.

### Technické provedení:
*   **Detekce lokality (Offline First):** Aplikace primárně využívá `TelephonyManager` (Android) / `CTTelephonyNetworkInfo` (iOS) k získání `networkCountryIso` ze SIM karty a sítě.
*   **Fallback (GPS / IP):** Pokud uživatel nemá signál nebo simuluje polohu, použije se hrubá geolokace přes IP adresu server-side, případně `FusedLocationProviderClient` s přesností na úroveň města.
*   **Přepínač priorit:** Modul skeneru má v sobě `RegionManager`. Pokud `currentCountryCode !in listOf("CZ", "SK", "PL")`, aktivuje se profil `TRAVEL_MODE`.
*   **Zdroje v Cestovním režimu:** Dotaz na čárový kód se po neúspěchu v lokální offline DB odešle asynchronně do globálních open-source DB, jako je OpenFoodFacts API, případně lokálních partnerských zdrojů (např. D-A-CH databáze).

---

## 2. AI OCR SKENER ETIKET (Záchranný scénář)

Když selžou všechny databáze pro naskenovaný EAN, aplikace uživateli předá kontrolu a využije AI pro okamžitou digitalizaci nutriční tabulky.

### Uživatelský tok (UI -> AI):
1.  **"EAN Nenalezen" Screen:** Místo prázdné obrazovky se objeví výzva: *"Neznáme. Pomůžeš to změnit? Vyfoť nutriční tabulku."*
2.  **Zachycení obrazu (CameraX):** Uživatel vyfotí zadní tabulku. Doplní se rámeček, do kterého by se měla tabulka vejít pro usnadnění ořezu.
3.  **Extrakce textu:**
    *   *Možnost A (On-Device OCR):* Rychlý pre-processing pomocí Google ML Kit Text Recognition pro získání surového textu lokálně bez dat.
    *   *Možnost B (Cloud Vision API + Gemini):* Pokud je online, obrázek i text se odešlou na zabezpečený server. Gemini/Vision API strukturalizuje surový text (řeší překlady např. "Kohlenhydrate" -> Sacharidy, "Białko" -> Bílkoviny) a vrátí JSON s přesnými gramážemi makroživin na 100g.
4.  **Verifikace:** Uživatel vidí přehledný formulář předvyplněný z AI a může čísla případně upravit. Následně potvrdí přidání potraviny.

---

## 3. AUTOMATICKÝ E-MAIL PRO VÝVOJÁŘE (Sběr dat s fotografiemi)

Pro naplnění komunitní databáze a vizuální kontrolu se strukturovaná data s komprimovanými fotkami okamžitě posílají do studia StepIn Tech.

### Zpracování fotografií:
*   Získá se foto č. 1: "Ověření názvu a obalu (předek)".
*   Získá se foto č. 2: "Nutriční tabulka (zadek)".
*   **Lokální komprese:** Abychom šetřili uživatelova data a vyhnuli se `OutOfMemory` chybám, použijeme kompresi přes `Bitmap.compress` a změnu rozlišení se ztrátovou kompresí tak, aby jedna fotografie měla maximálně **500 KB**. Dostačující kvalita pro vizuální čtení je přibližně 1024x1024 s 80% kvalitou JPEGu.

### Odeslání e-mailu:
Odeslání nikdy neprovádíme přímo ze zařízení klienta přes SMTP protokoly (kvůli exponování hesel). Mobil zašle data na náš zabezpečený backendový webhook, který se o e-mail postará, nebo – pokud chybí server – použije např. Firebase Cloud Functions s integrací SendGrid/Mailgun.

**Emailový standard:**
*   **Adresát:** `stepintech.cz@gmail.com`
*   **Předmět:** `[NutriKalk-NováPotravina] [EAN: {EAN_KOD}] [Země: {ZEME}]`
*   **Tělo e-mailu (JSON/Struktura):**
    ```json
    {
      "ean": "8594001030045",
      "country": "CZ",
      "user_email": "jan.novak@gmail.com",
      "macros_100g": {
        "calories": 320,
        "proteins": 19.0,
        "carbs": 1.0,
        "sugars": 0.5,
        "fats": 26.5
      }
    }
    ```
*   **Přílohy:** `front_pic.jpg`, `nutrition_pic.jpg` (každá max 500 KB).

---

## 4. LOGIKA BEZPEČNOSTI A SÍTĚ (Offline fronta)

Uživatel na dovolené často bojuje s připojením. Systém ho nesmí blokovat kolečkem „Nahrávám…“.

### Architektura fronty (WorkManager / Room):
1.  **Potvrzení Uložit:** Odeslání proběhne z pohledu uživatele "instantně" (ukončí se flow, jídlo se ihned objeví u něj v aplikaci v deníku).
2.  **Zařazení do offline databáze:** Data a komprimované fotky se uloží lokálně (Room DB: `pending_food_submissions`).
3.  **WorkManager (NetworkType.UNMETERED):** K odeslání e-mailu do studia (odeslání backendového requestu) dojde asynchronně na pozadí, jakmile OS detekuje, že se zařízení připojilo dostatečně rychlou a nejlépe Wi-Fi / neomezenou sítí (`Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()`). Nastavíme strategii Backoff pro exponenciální pokusy o znovuspuštění v případě nestability sítě.

---

## 5. MIKROSLUŽBA PRO AUTOMATICKOU ODPOVĚĎ (Google Cloud Function s Gmail API)

Backend Node.js skript navěšený nad schránkou studia StepIn Tech.

*   Využije se **Google Cloud Pub/Sub** trigger navázaný na historii doručené pošty účtu `stepintech.cz@gmail.com` (Gmail Push Notifications). Tím pádem nemusíme schránku agresivně poll-ovat.
*   Cloud Funkce (Node.js) se spustí hned při obdržení nového mailu.

### Ukázková logika filtru a auto-reply (Node.js):

```javascript
// index.js v Google Cloud Functions
const { google } = require('googleapis');
const nodemailer = require('nodemailer');

// Inicializace OAuth pro Gmail
const oAuth2Client = new google.auth.OAuth2(CLIENT_ID, CLIENT_SECRET, REDIRECT_URI);
oAuth2Client.setCredentials({ refresh_token: REFRESH_TOKEN });
const gmail = google.gmail({ version: 'v1', auth: oAuth2Client });

exports.handleNewEmail = async (message, context) => {
    // 1. Dekódovat notifikaci a získat historyId
    const data = JSON.parse(Buffer.from(message.data, 'base64').toString());
    const emailData = await fetchNewMessageFromGmail(data.historyId);
    
    // 2. Filtrovat striktní formát předmětu
    const subjectLine = emailData.subject; // np. "[NutriKalk-NováPotravina] [EAN: 8594001030045] [Země: CZ]"
    if (!subjectLine.startsWith("[NutriKalk-NováPotravina]")) {
        console.log("Ignoruji zprávu, nesouhlasí předmět.");
        return;
    }
    
    // Získat e-mail uživatele z hlavičky 'Reply-To' nebo 'From'
    const userEmail = emailData.replyTo || emailData.fromAddress;
    
    // Zjistit zemi (CZ, PL, zbytek EN) z předmětu pomocí Regexu
    const countryMatch = subjectLine.match(/\[Země:\s*(.*?)\]/);
    const country = countryMatch ? countryMatch[1] : 'CZ';

    // 3. Odeslat automatickou odpověď uživateli
    await sendAutoReply(userEmail, country);
};

async function sendAutoReply(toEmail, countryCode) {
    const accessToken = await oAuth2Client.getAccessToken();
    const transport = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            type: 'OAuth2',
            user: 'stepintech.cz@gmail.com',
            clientId: CLIENT_ID,
            clientSecret: CLIENT_SECRET,
            refreshToken: REFRESH_TOKEN,
            accessToken: accessToken.token,
        },
    });

    // Rozhodnutí o Jazyce HTML emailu
    let subject = "Re: [NutriKalk] Úspěšně jsme přijali nový produkt ke schválení! 🍏";
    let bodyHtml = `
        <div style="font-family: sans-serif; color: #333;">
            <p>Ahoj,</p>
            <p>děkujeme, že pomáháš budovat databázi NutriKalku! Tvoje data i fotografie úspěšně dorazily do vývojářského studia StepIn Tech.</p>
            <h3>Co se bude dít teď?</h3>
            <ol>
                <li>Náš systém a vývojář vizuálně zkontrolují tebou zaslané fotky a nutriční hodnoty.</li>
                <li>Jakmile produkt schválíme, během několika hodin se objeví v hlavní databázi pro všechny uživatele.</li>
            </ol>
            <p>Díky tobě bude příští nákup pro komunitu zase o něco snazší.</p>
            <hr />
            <p style="font-size: 12px; color: #888;">StepIn Tech | stepintech.cz@gmail.com</p>
        </div>
    `;

    if (countryCode === 'PL') {
        subject = "Odpowiedź: [NutriKalk] Twój produkt dotarł do nas pomyślnie! 🍏";
        bodyHtml = `... Polská verze textu ...`;
    }

    const mailOptions = {
        from: 'StepIn Tech - NutriKalk <stepintech.cz@gmail.com>',
        to: toEmail,
        subject: subject,
        html: bodyHtml,
    };

    await transport.sendMail(mailOptions);
    console.log(`Auto-reply successfully sent to: ${toEmail}`);
}
```
Úplné flow zaručuje plynulý rozvoj katalogu produktu ve chvíli, kdy klient zkusí naskenovat cokoliv nového, neblokuje mu frontend a díky micro-service mu automaticky posílá kvalitní děkovný feedback na email.
