const functions = require("firebase-functions");
const nodemailer = require("nodemailer");

// Email — nastav přes: firebase functions:config:set email.pass="GMAIL_APP_HESLO"
const gmailEmail = "stepintech.cz@gmail.com";
const gmailPassword = process.env.EMAIL_PASS || functions.config().email?.pass || "";

const mailTransport = nodemailer.createTransport({
  service: "gmail",
  auth: { user: gmailEmail, pass: gmailPassword },
});

// Slané suroviny zakázané v sladkých receptech
const SAVORY_ITEMS = [
  "cibule", "česnek", "maso", "kuřecí", "vepřové", "hovězí", "ryba", "losos",
  "tuňák", "pepř", "paprika", "salám", "šunka", "slanina", "klobása",
  "cibula", "cesnak", // Slovak
  "cebula", "czosnek", "pieprz", "kiełbasa", "szynka", // Polish
];

function validateIngredients(ingredients, recipeType) {
  if (recipeType !== "SWEET") return ingredients;
  return ingredients.filter(ing =>
    !SAVORY_ITEMS.some(s => ing.toLowerCase().includes(s))
  );
}

// Povolené akce (ochrana proti injection z klienta)
const ALLOWED_PRESET_ACTIONS = [
  "GENERATE_RECIPE",
  "PRESET_ACTION_OVEN_INFO",
  "PRESET_ACTION_SUBSTITUTIONS",
];

// ---------------------------------------------------------------------------
// submitNewFood — příjem neznámých produktů od komunity
// ---------------------------------------------------------------------------
exports.submitNewFood = functions.https.onRequest((req, res) => {
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).send("");
  }
  if (req.method !== "POST") return res.status(405).send("Method Not Allowed");

  const { ean, country, userEmail, macros, images, detectedName } = req.body;
  if (!ean || !userEmail) return res.status(400).send("Chybí EAN nebo E-mail.");

  const devSubject = `[NutriKalk] Nová potravina [EAN: ${ean}] [${country || "CZ"}]`;
  const devBody = [
    `EAN: ${ean}`,
    `Název (OCR): ${detectedName || "Nezjištěn"}`,
    `Země: ${country || "CZ"}`,
    `Uživatel: ${userEmail}`,
    `Kalorie: ${macros?.calories || 0} kcal`,
    `Bílkoviny: ${macros?.proteins || 0} g`,
    `Sacharidy: ${macros?.carbs || 0} g`,
    `Tuky: ${macros?.fats || 0} g`,
  ].join("\n");

  const attachments = (images || []).map((b64, i) => ({
    filename: `foto_${i + 1}.jpg`,
    content: b64,
    encoding: "base64",
  }));

  const isPolish = country === "PL";

  const userHtml = isPolish
    ? `<p>Cześć! Dziękujemy za przesłanie produktu. Sprawdzimy go i dodamy do bazy wkrótce.<br>StepIn Tech | stepintech.cz@gmail.com</p>`
    : `<p>Ahoj! Děkujeme za přispění do databáze NutriKalku. Produkt zkontrolujeme a brzy ho přidáme.<br>StepIn Tech | stepintech.cz@gmail.com</p>`;

  mailTransport.sendMail({
    from: `"NutriKalk" <${gmailEmail}>`,
    to: gmailEmail,
    subject: devSubject,
    text: devBody,
    attachments,
  })
    .then(() => mailTransport.sendMail({
      from: `"StepIn Tech" <${gmailEmail}>`,
      to: userEmail,
      subject: isPolish ? "[NutriKalk] Produkt otrzymany ✓" : "[NutriKalk] Produkt přijat ✓",
      html: userHtml,
    }))
    .then(() => res.status(200).json({ success: true }))
    .catch(err => {
      console.error("[submitNewFood]", err);
      res.status(500).json({ success: false, error: err.toString() });
    });
});

// ---------------------------------------------------------------------------
// smartRecipeBot — offline generátor receptů (bez AI, StepInTech lokální engine)
// ---------------------------------------------------------------------------
exports.smartRecipeBot = functions.https.onRequest((req, res) => {
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).send("");
  }
  if (req.method !== "POST") return res.status(405).json({ error: "POST required" });

  const { userId, action, ingredients, userAllergies, language } = req.body;

  if (!ALLOWED_PRESET_ACTIONS.includes(action)) {
    console.warn(`[smartRecipeBot] Forbidden action from ${userId}: ${action}`);
    return res.status(403).json({ success: false, error: "FORBIDDEN_ACTION" });
  }

  // Určení typu receptu
  const sweetKeywords = ["sladk", "buchta", "kolac", "dezert", "koláč", "bábovk", "bucht",
    "sweet", "dessert", "cake", "muffin", "truskawki", "cukier"];
  const ingredientStr = (ingredients || []).join(" ").toLowerCase();
  const requestLower = (req.body.customRequest || "").toLowerCase();
  const isSweet = sweetKeywords.some(k => ingredientStr.includes(k) || requestLower.includes(k));
  const recipeType = isSweet ? "SWEET" : "SAVORY";

  const safeIngredients = validateIngredients(ingredients || [], recipeType);
  const sk = language === "sk";
  const pl = language === "pl";

  let response = "";

  if (action === "GENERATE_RECIPE") {
    if (!safeIngredients.length) {
      return res.status(400).json({ error: "Chybějící suroviny." });
    }

    if (isSweet) {
      response = sk
        ? `# Nadýchaná bublanina (StepInTech AI)\n\n**Čas:** 35 min | **Kalórie:** ~320 kcal\n\n### Suroviny:\n- Hladká múka 250 g\n- Cukor 150 g\n- Vajcia 3 ks\n- Mlieko 100 ml\n- Olej alebo maslo 80 ml\n- Ovocie z lednice: ${safeIngredients.slice(0, 3).join(", ")}\n- Kypriaci prášok 1 bal.\n\n### Postup:\n1. Vajcia vyšľahajte s cukrom do peny.\n2. Pridajte olej a mlieko, premiešajte.\n3. Vsypte preosiatu múku s kypriacim práškom.\n4. Nalejte do formy, navrch dajte ovocie.\n5. Pečte 180 °C / 25–30 min do zlatista.\n\n*recipeType: SWEET*`
        : pl
        ? `# Puszyste ciasto drożdżowe (StepInTech AI)\n\n**Czas:** 35 min | **Kalorie:** ~320 kcal\n\n### Składniki:\n- Mąka 250 g\n- Cukier 150 g\n- Jajka 3 szt\n- Mleko 100 ml\n- Olej lub masło 80 ml\n- Owoce z lodówki: ${safeIngredients.slice(0, 3).join(", ")}\n- Proszek do pieczenia 1 op.\n\n### Sposób przygotowania:\n1. Ubij jajka z cukrem na puszystą pianę.\n2. Dodaj olej i mleko, wymieszaj.\n3. Wsyp przesianą mąkę z proszkiem do pieczenia.\n4. Wlej do formy, na wierzch połóż owoce.\n5. Piecz 180 °C przez 25–30 min.\n\n*recipeType: SWEET*`
        : `# Nadýchaná bublanina (StepInTech AI)\n\n**Čas:** 35 min | **Kalorie:** ~320 kcal\n\n### Suroviny:\n- Hladká mouka 250 g\n- Cukr 150 g\n- Vejce 3 ks\n- Mléko 100 ml\n- Olej nebo máslo 80 ml\n- Ovoce z lednice: ${safeIngredients.slice(0, 3).join(", ")}\n- Kypřicí prášek 1 bal.\n\n### Postup:\n1. Vyšleháme vejce s cukrem do pěny.\n2. Přidáme olej a mléko, promícháme.\n3. Vmícháme proseté mouku s kypřicím práškem.\n4. Nalijeme do formy, navrch dáme ovoce.\n5. Pečeme 180 °C / 25–30 min dozlatova.\n\n*recipeType: SWEET*`;
    } else {
      const items = safeIngredients.join(", ");
      response = sk
        ? `# Rýchly obed z lednice (StepInTech AI)\n\n**Čas:** 20 min | **Kalórie:** ~420 kcal\n\n### Dostupné suroviny:\n${safeIngredients.map(i => `- ${i}`).join("\n")}\n\n### Postup:\n1. Suroviny nakrájame na kúsky.\n2. Na panvici opražíme základ (cibuľka, mäso alebo zelenina).\n3. Dochutime soľou, korením a bylinkami.\n4. Navrch pridáme syr, prikryjeme 5 min.\n5. Podávame teplé.\n\n*recipeType: SAVORY*`
        : pl
        ? `# Szybki obiad z lodówki (StepInTech AI)\n\n**Czas:** 20 min | **Kalorie:** ~420 kcal\n\n### Dostępne składniki:\n${safeIngredients.map(i => `- ${i}`).join("\n")}\n\n### Sposób przygotowania:\n1. Pokrój składniki na kawałki.\n2. Podsmaż bazę na patelni (cebula, mięso lub warzywa).\n3. Dopraw solą, pieprzem i ziołami.\n4. Na wierzch dodaj ser, przykryj na 5 min.\n5. Podawaj na ciepło.\n\n*recipeType: SAVORY*`
        : `# Rychlý oběd z lednice (StepInTech AI)\n\n**Čas:** 20 min | **Kalorie:** ~420 kcal\n\n### Dostupné suroviny:\n${safeIngredients.map(i => `- ${i}`).join("\n")}\n\n### Postup:\n1. Suroviny nakrájíme na kousky.\n2. Na pánvi orestujeme základ (cibulka, maso nebo zelenina).\n3. Dochutíme solí, pepřem a bylinkami.\n4. Navrch přidáme sýr, přikryjeme 5 min.\n5. Podáváme teplé.\n\n*recipeType: SAVORY*`;
    }
  } else if (action === "PRESET_ACTION_OVEN_INFO") {
    response = sk
      ? "Rúru predhrejte na 180 °C (horkovzduch 160 °C) aspoň 10 minút pred vložením jedla. Na mäso počítajte cca 20–25 min / 100 g, na zeleninu 15–20 min."
      : pl
      ? "Rozgrzej piekarnik do 180 °C (termoobieg 160 °C) przez co najmniej 10 minut przed włożeniem potrawy. Na mięso ok. 20–25 min / 100 g, na warzywa 15–20 min."
      : "Troubu předehřejte na 180 °C (horkovzduch 160 °C) alespoň 10 minut před vložením jídla. Na maso počítejte cca 20–25 min / 100 g, na zeleninu 15–20 min.";
  } else if (action === "PRESET_ACTION_SUBSTITUTIONS") {
    response = sk
      ? "**Náhrady surovin:**\n- Mlieko → rostlinné mlieko (ovesné, sójové)\n- Maslo → kokosový olej alebo margarín\n- Vajcia → 1 vajce = 1 lžica chia semienok + 3 lžice vody (10 min stáť)"
      : pl
      ? "**Zamienniki składników:**\n- Mleko → mleko roślinne (owsiane, sojowe)\n- Masło → olej kokosowy lub margaryna\n- Jajka → 1 jajko = 1 łyżka nasion chia + 3 łyżki wody (odczekaj 10 min)"
      : "**Náhrady surovin:**\n- Mléko → rostlinné mléko (ovesné, sójové)\n- Máslo → kokosový olej nebo margarín\n- Vejce → 1 vejce = 1 lžíce chia semínek + 3 lžíce vody (10 min odstát)";
  }

  return res.status(200).json({
    success: true,
    action_triggered: action,
    recipeType,
    response,
  });
});

// ---------------------------------------------------------------------------
// cleanOcrText — offline čištění OCR textu a detekce značky
// ---------------------------------------------------------------------------
exports.cleanOcrText = functions.https.onRequest((req, res) => {
  res.set("Access-Control-Allow-Origin", "*");
  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    return res.status(204).send("");
  }
  if (req.method !== "POST") return res.status(405).json({ error: "POST required" });

  const { rawOcrText, detectedCountry } = req.body;
  if (!rawOcrText) return res.status(400).json({ error: "Chybí rawOcrText" });

  const KNOWN_BRANDS = [
    "Karlova Koruna", "Řezníkův Talíř", "Boni", "Machland", "Madeta", "Hamé",
    "Vitana", "Orion", "Opavia", "Milko", "Kunín", "Hollandia", "Pribina",
    "Penam", "Sedita", "Agricol", "Giana", "Pikok", "Lagris", "Kofola",
    "Łaciate", "Piątnica", "Mlekovita", "Danone", "Hochland", "Bakoma",
    "Łowicz", "Winiary", "Pudliszki", "Krakus", "Tarczyński", "Sokołów",
    "Hortex", "Tymbark", "Wedel", "Graal", "Kupiec", "Barilla", "Kamis",
    "Nutella", "Ferrero", "Milka", "Kinder", "Hellmann's", "Dr. Oetker",
  ];

  const TYPO_MAP = {
    "CAMAMBERT": "Camembert", "CAMMEMBERT": "Camembert",
    "EDAMM": "Eidam", "GAUDA": "Gouda",
    "JOGHURT": "Jogurt", "TVAROG": "Tvaroh",
    "MLEKO": "Mléko", "MASLO": "Máslo",
  };

  let corrected = rawOcrText;
  for (const [typo, fix] of Object.entries(TYPO_MAP)) {
    corrected = corrected.replace(new RegExp(typo, "gi"), fix);
  }

  const upper = rawOcrText.toUpperCase();
  const brand = KNOWN_BRANDS.find(b => upper.includes(b.toUpperCase())) || "";
  const firstLine = corrected.split("\n").find(l => l.trim()) || corrected.slice(0, 40);
  const productName = brand ? firstLine.replace(new RegExp(brand, "i"), "").trim() : firstLine.trim();

  return res.status(200).json({
    success: true,
    brand,
    cleanedName: brand ? `${brand} ${productName}`.trim() : productName,
    cleanedIngredients: corrected,
  });
});
