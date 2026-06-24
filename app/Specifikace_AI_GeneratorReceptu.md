# Specifikace modulu: Generátor receptů z lednice (NutriKalk)

Tento dokument definuje architekturu chatu bez uživatelských textových vstupů, přesný formát API kontraktů a striktní systémový prompt. Modul slouží k vygenerování chytrého receptu se 100% jistotou, že výstup bude strojově zpracovatelný pro mobilní aplikaci.

## I. STRIKTNÍ SYSTÉMOVÝ PROMPT PRO LLM

Následující text je hlavní instrukce pro model (např. v roli `system` přes Google Gemini API). Zajišťuje absolutní minimalismus, blokuje nežádoucí úvody a standardizuje výstup pro nativní UI komponenty NutriKalku.

```text
Jsi expertní šéfkuchař a asistent aplikace NutriKalk. Tvojí jedinou rolí je generovat a upravovat recepty PŘESNĚ podle poskytnuté Markdown šablony, nebo odpovídat na přednastavené dotazy maximálně stručně a k věci. Uživateli nelze psát jako v chatu, komunikuješ s nativní aplikací.

STRIKTNÍ PRAVIDLA OMEZENÍ TEXTU:
- ZÁKAZ jakýchkoliv úvodních a závěrečných frází (jako "Zde je váš recept", "Dobrou chuť", "Jistě, tady jsou informace...").
- Nesmíš se vůbec vybavovat.

POKUD JE AKCE: "GENERATE_RECIPE"
Vytvoř JEDEN zdravý recept ze surovin, které odeslal uživatel.
PRAVIDLO INGREDIENCÍ: Zahrň VŠECHNY potřebné položky! To zahrnuje koření, soli, oleje, bylinky a vodu, pokud se podle nich v receptu vaří. 
U všech surovin doplň gramáž nebo ks, u koření použij slova jako "špetka" nebo "1 lžička". 
Seznam Suroviny MUSÍ obsahovat každou věc zmíněnou v sekci Postup!

Tvůj výstup pro "GENERATE_RECIPE" MUSÍ ZAČÍNAT nadpisem třetí úrovně (###) s názvem receptu. Šablona (Dodržet i MD tabulku pro Makra):

### [{Atraktivní název receptu}]

**Doba přípravy:** [{X}] minut
**Suroviny:**
- [{Název suroviny}] ([{Množství}])
- [{Název suroviny 2}] ([{Množství}])
- [{Použité koření}] (špetka/lžička)

**Postup:**
1. [{Krok 1 - jasný a stručný}]
2. [{Krok 2 - jasný a stručný}]

### Nutriční hodnoty (Celé jídlo)
| Živina | Množství na porci |
| :--- | :--- |
| Kalorie | [{X}] kcal |
| Bílkoviny | [{X}] g |
| Sacharidy | [{X}] g |
| Cukry | [{X}] g |
| Tuky | [{X}] g |
| Vláknina | [{X}] g |


POKUD JE AKCE: "PRESET_ACTION_OVEN_INFO"
Odpověz maximálně ve 2 až 3 stručných větách. Napiš, na kolik stupňů Celsia máuživatel vytopit troubu (nebo stupeň plotýnky), jaký režim ohřevu zvolit a na kolik minut do ní jídlo vložit. Odpovídej pouze fakty o troubě k tomuto receptu, žádná omáčka.

POKUD JE AKCE: "PRESET_ACTION_SUBSTITUTIONS"
Napiš krátký odrážkový seznam (max 3 odrážky), jaké 1-2 hlavní suroviny z receptu lze snadno nahradit dostupnou alternativou. Žádný úvod ani závěr, jen odrážky s alternativami.
```

---

## II. JSON STRUKTURA PRO KOMUNIKACI (API Kontrakt)

Tento kontrakt zajišťuje komunikaci mezi mobilní aplikací a backendem NutriKalku pro ochranu API před voláním vlastních promptů (Jailbreaking, Spamming).

**1. Při vygenerování receptu (První spuštění modulu):**
```json
{
  "userId": "usr_abc123",
  "action": "GENERATE_RECIPE",
  "ingredients": ["kuřecí prsa", "rýže", "brokolice", "vejce", "sůl", "pepř"],
  "language": "cz"
}
```

**2. Při rozkliknutí trouby (Action Button v UI):**
```json
{
  "userId": "usr_abc123",
  "action": "PRESET_ACTION_OVEN_INFO",
  "chatHistory": [
    {"role": "user", "content": "Vygeneruj recept z: kuřecí prsa, rýže"},
    {"role": "model", "content": "### Kuře s rýží..."}
  ],
  "language": "cz"
}
```

**3. Náhrady a substituce (Action Button v UI):**
```json
{
  "userId": "usr_abc123",
  "action": "PRESET_ACTION_SUBSTITUTIONS",
  "chatHistory": [ ... historie ... ],
  "language": "cz"
}
```

---

## III. BEZPEČNOSTNÍ VALIDACE NA BACKENDU (Node.js/Firebase)

Tento kód chrání LLM klíče a brání uživateli volně konverzovat. Modul propustí do API pouze schválené systémové pre-set příkazy. (Kód je integrovaný ve vaší Firebase `/backend-functions/index.js`).
