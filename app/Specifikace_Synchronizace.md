# Modul Synchronizace s fitness hodinkami a prsteny (NutriKalk)

Tento manuál popisuje kompletní technickou specifikaci a UX/UI návrh pro modul synchronizace spálených kalorií z nositelné elektroniky v reálném čase.

## 1. Technická Integrace (Architektura)

Systém se s externími zdroji nesynchronizuje neustále, ale prostřednictvím systémových background jobů a pull-to-refresh.

*   **iOS (Apple HealthKit):**
    *   **Implementace:** Nativní propojení pomocí `HKHealthStore`.
    *   **Práva:** Aplikace bude žádat POUZE o quyền k přečtení (Read) pro typ `HKQuantityTypeIdentifier.activeEnergyBurned`.
    *   **Proces:** V momentě spuštění (nebo background refresh) aplikace se zadotazuje na spálené aktivní kalorie pro aktuální kalendářní den uzel -> `HKStatisticsOptions.cumulativeSum`.

*   **Android (Google Health Connect):**
    *   **Implementace:** Integrace pomocí androidx.health.connect:health-connect-client doporučeného M3 rozhraní.
    *   **Práva:** Žádost o `HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)`.
    *   **Proces:** Android aplikace zavolá `readRecords` s `TimeRangeFilter` pro dnešní interval od půlnoci (00:00) do aktuálního okamžiku.
    *   **Kompatibilita:** Tímto API NutriKalk automaticky pokryje Garmin Connect, Samsung Health, Strava a Oura ring (které ukládají data do Health Connect).

*   **Specifikum pro Huawei (Huawei Health):**
    *   Bohužel, Huawei Health mnohdy přímo nekomunikuje na Androidu s Google Health Connect. 
    *   **Řešení v NutriKalk UI:** Aplikace uživateli s Huawei zařízením poradí použití doporučované 3rd-party aplikace **Health Sync** (synchronizuje data z Huawei Health do Google Fit / Health Connect), nebo v budoucnu integrujeme nativní `Huawei Health Kit SDK` (což by ale znamenalo distribuci přes AppGallery, resp. integraci s HMS Core). Pro začátek je preferované UI navést u Huawei na můstek Health Sync.

## 2. Logika Matematiky Kalorií a Zamezení Duplicitám

Základní myšlenka: **NutriKalk si BMR určuje sám, nestahujeme BMR odjinud.**

*   **Extrahované metrum:** Stahujeme striktně JEN "Active Energy Burned" (Aktivní kalorie, např. chůze, sport, cvičení), nikoliv Total Energy (kompletní denní spálená energie).
*   **Aplikovaná rovnice na domovské stránce (Domov):**
    `Aktuální zbývající kalorie k jídlu = (Základní denní cíl + Aktivně spálené kalorie z hodinek) - Přijaté kalorie v jídle.`
*   **Příklad:** Uživatel má základní cíl 2 000 kcal. Dnes šel po obědě běhat a hodinky zaznamenaly +450 kcal jako aktivní energii. Snědl 1 500 kcal. 
    *   Kapacita dne je uměle rozšířena: 2000 + 450 = 2450.
    *   Zbývá dojíst: 2450 - 1500 = 950 kcal.

## 3. Vizuální Rozhraní (UX/UI v Natural stylu)

### A. Nastavení
*   V menu **Profil & Nastavení** přibude modul `Propojené hodinky a prsteny`.
*   Otevře detailní, ale čistý list s jasným logem integrace. 
*   **UI Prvky:** Minimalistické on/off toggle (přepínače) pro Apple Health nebo Google Health Connect se stavovými texty `Odpojeno` vs `Automatická synchronizace aktivní`.
*   Taktéž zde bude malá edukační dlaždice pro "Uživatele hodinek Huawei/Honor", vedoucí na rychlý návod k zapnutí Health Sync.

### B. Hlavní Obrazovka (Dashboard)
V hlavním kolečku se propíše stálá hodnota. Jak se to ale uživateli "vysvětlí", aniž bychom přeplácali UI?

*   Došlo k úpravě centrálního domovského štítku. Velké číslo hlásí ZBÝVAJÍCÍ (nebo sežrané) kalorie – defaultně `Kcal celkem` / `/ Cíl úměrný k aktivitě`.
*   Přímo pod centrálním textem denního příjmu kalorií je jemná, šedá typografie uvádějící přesný rozpad:
    `Základ: 2 000 kcal | Aktivita: +450 kcal`
*   **Indikátor synchronizace:** Vedle (či pod) tímto rozpadem je vsazena malá ikona hodinek (např. smartwatch) doplněná o miniaturní „online“ svítivě zelenou tečku indikující aktuálnost. 
*   Pro manuální vyvolání stáhneme obrazovku dolů `Swipe to refresh / Pull down`, což problikne ikonku loga, zatočí s ní a aktualizuje stav "Aktivních kalorií".
