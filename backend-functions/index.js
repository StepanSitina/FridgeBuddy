const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

// Doporučuji uložit tyto údaje do Firebase Configuration/Secrets:
// Pro Gmail si musíte vygenerovat "App Password" (Heslo aplikace) v nastavení zabezpečení Google účtu
const gmailEmail = "stepintech.cz@gmail.com";
const gmailPassword = process.env.EMAIL_PASS || "ZADEJTE_HESLO_APLIKACE_ZDE"; 

const mailTransport = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

exports.submitNewFood = functions.https.onRequest((req, res) => {
  // Povolit CORS, aby šlo API volat z Android aplikace asynchronně
  res.set('Access-Control-Allow-Origin', '*');
  if (req.method === 'OPTIONS') {
    res.set('Access-Control-Allow-Methods', 'POST');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    res.status(204).send('');
    return;
  }

  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  const { ean, country, userEmail, userAllergies, macros, images } = req.body;

  if (!ean || !userEmail) {
    return res.status(400).send("Chybí EAN nebo E-mail uživatele.");
  }

  // Detekce alergenů (Mock, v produkci zde LLM projde seznam ingrediencí z OCR)
  let containsAllergens = false;
  if (userAllergies && Array.isArray(userAllergies) && userAllergies.length > 0) {
      // Mock: 10% šance, že AI najde alergen na obalu pro demonstraci
      containsAllergens = Math.random() < 0.1;
  }

  // 1. E-mail pro VÝVOJÁŘE (do StepIn Tech)
  const devSubject = `[NutriKalk-NováPotravina] [EAN: ${ean}] [Země: ${country || 'CZ'}]`;
  const devBody = `
    Nová potravina k posouzení od komunity:
    --------------------------------------
    EAN kód: ${ean}
    Země: ${country || 'CZ'}
    Uživatel: ${userEmail}
    
    Získané Víceméně (z AI / Uživatele):
    - Kalorie: ${macros?.calories || 0} kcal
    - Bílkoviny: ${macros?.proteins || 0} g
    - Sacharidy: ${macros?.carbs || 0} g (z toho cukry: ${macros?.sugars || 0} g)
    - Tuky: ${macros?.fats || 0} g
  `;

  // Příprava fotek pro odeslání, pokud přijdou z Androidu jako Base64 (max 2 ks, komprimované)
  const attachments = [];
  if (images && Array.isArray(images)) {
      images.forEach((imgBase64, index) => {
          attachments.push({
              filename: `obrazek_${index + 1}.jpg`,
              content: imgBase64,
              encoding: 'base64'
          });
      });
  }

  const devMailOptions = {
    from: `"NutriKalk Skener" <${gmailEmail}>`,
    to: gmailEmail, // Posílá se Vám na stepintech
    subject: devSubject,
    text: devBody,
    attachments: attachments
  };

  // 2. AUTO-REPLY E-mail pro UŽIVATELE
  let userSubject = "Re: [NutriKalk] Úspěšně jsme přijali nový produkt ke schválení! 🍏";
  let userHtml = `
      <div style="font-family: sans-serif; color: #333; line-height: 1.6;">
          <p>Ahoj,</p>
          <p>děkujeme, že pomáháš budovat databázi NutriKalku! Tvoje data i fotografie nového produktu úspěšně dorazily do vývojářského studia StepIn Tech.</p>
          <h3>Co se bude dít teď?</h3>
          <ol>
              <li>Náš systém a vývojář vizuálně zkontrolují tebou zaslané fotky a nutriční hodnoty.</li>
              <li>Jakmile produkt schválíme, během několika hodin se objeví v hlavní databázi pro všechny uživatele.</li>
          </ol>
          <p>Díky tobě bude příští nákup pro komunitu zase o něco snazší.</p>
          <hr />
          <p style="font-size: 11px; color: #888;">Tento e-mail byl vygenerován automaticky. | StepIn Tech (stepintech.cz@gmail.com)</p>
      </div>
  `;

  // Podpora pro Polsko
  if (country === 'PL') {
      userSubject = "Odpowiedź: [NutriKalk] Twój produkt dotarł do nas pomyślnie! 🍏";
      userHtml = `
      <div style="font-family: sans-serif; color: #333; line-height: 1.6;">
          <p>Cześć,</p>
          <p>dziękujemy za pomoc w budowaniu bazy danych NutriKalk! Twoje dane i zdjęcia nowego produktu pomyślnie dotarły do studia deweloperskiego StepIn Tech.</p>
          <h3>Co dzieje się teraz?</h3>
          <ol>
              <li>Nasz system i programista wizualnie sprawdzą przesłane zdjęcia i wartości odżywcze.</li>
              <li>Gdy tylko zatwierdzimy produkt, w ciągu kilku godzin pojawi się on w głównej bazie dla wszystkich użytkowników.</li>
          </ol>
          <p>Dzięki Tobie kolejne zakupy dla społeczności będą o krok łatwiejsze.</p>
          <hr />
          <p style="font-size: 11px; color: #888;">Ten e-mail został wygenerowany automatycznie. | StepIn Tech</p>
      </div>`;
  }

  const userMailOptions = {
    from: `"StepIn Tech - NutriKalk" <${gmailEmail}>`,
    to: userEmail,
    subject: userSubject,
    html: userHtml,
  };

  // Sekvenční odeslání obou e-mailů (Nejprve do studia, pak uživateli)
  mailTransport.sendMail(devMailOptions)
    .then(() => mailTransport.sendMail(userMailOptions))
    .then(() => {
        return res.status(200).send({ success: true, message: "E-maily úspěšně odeslány vývojáři i uživateli.", containsAllergens: containsAllergens });
    })
    .catch((error) => {
        console.error("Chyba při odesílání e-mailu: ", error);
        return res.status(500).send({ success: false, error: error.toString() });
    });
});

// Povolene akce, ktere aplikace smi predat AI (Ochrana proti Jailbreakingu a open-text zadavani)
const ALLOWED_PRESET_ACTIONS = [
    "GENERATE_RECIPE",
    "PRESET_ACTION_OVEN_INFO",
    "PRESET_ACTION_SUBSTITUTIONS"
];

// Ochranny obal nad Gemini API (Simulace prijeti requestu, validace a preposlani do AI)
exports.smartRecipeBot = functions.https.onRequest(async (req, res) => {
  // Povoleni CORS pro asynchronni hovory z mobilni applikace
  res.set('Access-Control-Allow-Origin', '*');
  if (req.method === 'OPTIONS') {
    res.set('Access-Control-Allow-Methods', 'POST');
    res.set('Access-Control-Allow-Headers', 'Content-Type');
    res.status(204).send('');
    return;
  }

  if (req.method !== "POST") {
    return res.status(405).send({ error: "Vyžadován POST request." });
  }

  const { userId, householdId, action, ingredients, userAllergies, chatHistory, language } = req.body;

  // 1. BEZPEČNOSTNÍ VALIDACE - Pěvná brána, zamezení uživatelského inputu do chatu
  if (!ALLOWED_PRESET_ACTIONS.includes(action)) {
      console.warn(`Neoprávněny manipulacní pokus za zaznamenan uzivatelem ${userId} s akci ${action}`);
      return res.status(403).json({ 
          success: false, 
          error: "FORBIDDEN_ACTION", 
          message: "Tato komunikační akce s AI není v beztextovém rozhraní NutriKalku povolena." 
      });
  }

  // 2. STAVBA PROMPTU (Zasíláme AI natvrdo pre-setované příkazy z backendu místo z telefonu)
  let executionPrompt = "SYSTÉMOVÁ INSTRUKCE: Jsi StepInTech AI, exkluzivní inteligentní kuchařský a nutriční asistent vyvinutý studiem StepIn Tech. Nikdy nesmíš zmínit, že jsi Gemini, Google AI nebo jiný jazykový model. Pokud se tě uživatel zeptá, kdo jsi, odpovíš: Jsem StepInTech AI, tvůj osobní asistent od studia StepIn Tech.\n\n";
  if (action === "GENERATE_RECIPE") {
      if (!ingredients || !Array.isArray(ingredients)) {
          return res.status(400).json({ error: "Chybějící suroviny."});
      }
      executionPrompt += `AKCE: GENERATE_RECIPE\nNavrhni recept přesně podle předepsané Markdown šablony. Tady má uživatel aktuální Suroviny v lednici: ${ingredients.join(", ")}\n`;
      executionPrompt += `
Nyní platí tyto STRIKTNÍ LOGICKÉ A KULINÁŘSKÉ ZÁSADY GENEROVÁNÍ RECEPTŮ, které nesmíš nikdy porušit:
1. ZÁKAZ KŘÍŽENÍ: Pokud je recept klasifikován jako SLADKÝ (dezerty, buchty, kaše), je přísně zakázáno používat slané suroviny (cibule, česnek, maso, ryby, pepř, paprika).
2. ZÁKLADNÍ SUROVINY: Pokud uživatel nemá v lednici hotový vývar (nebo hotové jídlo), napiš postup od základu (maso, voda, kořenová zelenina). Nesmíš vyžadovat surovinu, která je už hotovým jídlem.
3. KULINÁŘSKÁ LOGIKA: Recept musí být reálný, jedlý a odpovídající běžným kulinářským standardům.
4. JSON KONTRAKT: Do JSON výstupu nebo Markdownu přidej nové pole "recipeType": "SWEET" nebo "SAVORY". Pokud je typ "SWEET", proveď vnitřní validaci: "if (ingredients.contains(SAVORY_ITEMS)) { remove(SAVORY_ITEMS) }".
`;
      if (userAllergies && Array.isArray(userAllergies) && userAllergies.length > 0) {
          executionPrompt += `\nSTRIKTNÍ ZÁKAZ: Recept NESMÍ obsahovat tyto alergeny: ${userAllergies.join(", ")}. Pokud zadané suroviny obsahují tyto alergeny, nahraď je nebo odstraň.`;
      }
      executionPrompt += `\nNezapomeň vypsat VŠECHNO koření a soli použité v receptu do seznamu surovin!`;
  } else if (action === "PRESET_ACTION_OVEN_INFO") {
      executionPrompt += `AKCE: PRESET_ACTION_OVEN_INFO\nStručně (max 2-3 věty) mi napiš, jak mám rozehřát troubu/pánvičku a na jak dlouho.`;
  } else if (action === "PRESET_ACTION_SUBSTITUTIONS") {
      executionPrompt += `AKCE: PRESET_ACTION_SUBSTITUTIONS\nNapiš v odrážkách max 2 návrhy náhrad surovin z tohoto receptu.`;
  }

  try {
      // Zde v produkci pripojite Google AI inicializaci (vystupujici jako StepInTech AI):
      // const model = ai.getGenerativeModel({ model: "gemini-1.5-flash" });
      // const chat = model.startChat({ history: chatHistory || [] });
      // const result = await chat.sendMessage(executionPrompt);
      // let responseText = result.response.text();
      
      // LINGVISTICKÁ VRSTVA: Asynchronní oprava výstupu před odesláním klientovi
      // responseText = await performLinguisticCorrection(responseText, language || "cz");

      // Demo Response
      return res.status(200).json({
          success: true,
          action_triggered: action,
          developer_prompt_used: executionPrompt,
          message: "Validace prošla, zde by byl vygenerovaný Markdown z LLM. (S aplikovanou jazykovou korekcí)"
      });

  } catch (error) {
      console.error(error);
      return res.status(500).json({ success: false, error: "Chyba při komunikaci s AI." });
  }
});

/**
 * LINGVISTICKÝ ENGINE PRO APLIKACI NUTRIKALK
 * Automatická korekce gramatiky, překlepů a chybějící diakritiky (háčky/čárky) 
 * ze surového textu. Zachovává Markdown formátování a respektuje zvolený jazyk.
 */
async function performLinguisticCorrection(rawText, targetLanguageCode) {
    let languageName = targetLanguageCode.toUpperCase() === 'PL' ? 'Polštině' : 'Češtině';
    
    const correctionPrompt = `
Jsi expertní lingvista a korektor (Proofreader). Tvým úkolem je opravit veškeré překlepy, 
gramatické chyby a chybnou/chybějící diakritiku v následujícím textu, a to striktně v ${languageName}.

PRAVIDLA KOREKCE:
1. Rozpoznat chybějící diakritiku (např. 'koreni' -> 'koření', 'ryze' -> 'rýže') a doplnit ji.
2. V Polštině přísně dbát na speciální znaky (ą, ć, ę, ł, ń, ó, ś, ź, ż).
3. Bezpodmínečně zachovat veškeré původní formátování (Markdown tabulky, nadpisy, odrážky, tučné písmo, mezery). Nesmíš změnit strukturu kódu, jen opravit slova.
4. Vrátit POUZE opravený text bez jakýchkoliv úvodních či vysvětlujících frází!

Text k opravě:
${rawText}
`;

    try {
        // Zde zavoláte LLM s promptem pro korekci. Pro maximální rychlost použijeme 
        // menší model jako gemini-1.5-flash-8b nebo dedikovaný NLP model.
        // const correctionModel = ai.getGenerativeModel({ model: "gemini-1.5-flash-8b" });
        // const result = await correctionModel.generateContent(correctionPrompt);
        // return result.response.text();
        
        // Mock pro ukázku
        return rawText.replace("koreni", "koření").replace("ryze", "rýže"); // Ukázková rychlá replace oprava
    } catch (e) {
        console.error("Chyba při lingvistické korekci:", e);
        return rawText; // Fallback: Při chybě korpusu se alespoň vrátí původní surová data
    }
}

/**
 * Samostatný API endpoint pro aplikaci - slouží výhradně k čištění OCR dat
 * Volá se např. ve chvíli, kdy klient vyfotí v Itálii obal a OCR přečte zmuchlaný text.
 */
exports.cleanOcrText = functions.https.onRequest(async (req, res) => {
    res.set('Access-Control-Allow-Origin', '*');
    if (req.method === 'OPTIONS') {
        res.set('Access-Control-Allow-Methods', 'POST');
        res.set('Access-Control-Allow-Headers', 'Content-Type');
        return res.status(204).send('');
    }

    if (req.method !== "POST") {
        return res.status(405).send({ error: "Vyžadován POST request" });
    }

    const { rawOcrText, detectedCountry } = req.body;
    
    if (!rawOcrText) {
        return res.status(400).send({ error: "Chybí rawOcrText" });
    }

    // Prompt přizpůsobený pro extrakci smysluplných slov z rozbitého OCR bloku
    const ocrCleanupPrompt = `
    Jsi AI OCR korektor. Text pochází ze skenu obalu výrobku ze země: ${detectedCountry || 'Neznámo'}.
    Oprav "rozbitá" oříznutá slova a doplň správnou lokální diakritiku, abys vytvořil čistý název a seznam složení.
    Nevysvětluj. Pouze vrať čistý JSON objekt tvaru: { "cleanedName": "...", "cleanedIngredients": "..." }.
    
    Surový text z OCR: ${rawOcrText}
    `;

    try {
        // const result = await ai.getGenerativeModel({ model: "gemini-1.5-flash" }).generateContent(ocrCleanupPrompt);
        // const cleanedData = JSON.parse(result.response.text().replace(/```json/g, '').replace(/```/g, ''));
        
        return res.status(200).json({
            success: true,
            cleanedName: "Ukázka opraveného OCR názvu",
            cleanedIngredients: "Ukázka složení s doplněnou diakritikou"
        });
    } catch (e) {
        return res.status(500).json({ success: false, error: e.toString() });
    }
});
