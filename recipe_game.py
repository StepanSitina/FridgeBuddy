#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
=============================================================================
             INTELIGENTNÍ KULINÁŘSKÝ GENERÁTOR - RECEPTY (v2.0)
=============================================================================
Tento program simuluje moderní mobilní aplikaci v textovém rozhraní konzole.
Všechny informace jsou zarovnány do čistých vizuálních karet.
=============================================================================
"""

import os
import sys

# Databáze sušených polévek z kulinářského generátoru
POLEVKY = [
    {
        "name": "Kuřecí polévka s kapáním",
        "time": "25 min",
        "kcal": 180,
        "protein": "14g",
        "carbs": "18g",
        "fat": "6g",
        "contains": "3/4",
        "gluten_free": False,
        "vegan": False,
        "vegetarian": False
    },
    {
        "name": "Koprová polévka s bramborem",
        "time": "20 min",
        "kcal": 210,
        "protein": "6g",
        "carbs": "28g",
        "fat": "8g",
        "contains": "4/5",
        "gluten_free": True,
        "vegan": False,
        "vegetarian": True
    },
    {
        "name": "Zelná polévka s uzeninou",
        "time": "35 min",
        "kcal": 320,
        "protein": "16g",
        "carbs": "14g",
        "fat": "22g",
        "contains": "5/6",
        "gluten_free": True,
        "vegan": False,
        "vegetarian": False
    },
    {
        "name": "Hráškový krém se smetanou",
        "time": "15 min",
        "kcal": 150,
        "protein": "8g",
        "carbs": "20g",
        "fat": "5g",
        "contains": "4/4",
        "gluten_free": True,
        "vegan": False,
        "vegetarian": True
    }
]

# Aktivní stavy filtrů (simulované)
filters = {
    "gluten_free": False,
    "vegan": False,
    "vegetarian": False
}

def clear_screen():
    os.system('cls' if os.name == 'nt' else 'clear')

def draw_header():
    print("=" * 60)
    print("             INTELIGENTNÍ KULINÁŘSKÝ GENERÁTOR")
    print("=" * 60)

def draw_filters():
    # Zobrazení přepínatelných tlačítek filtrů
    gf_status = "[X] Bezlepkový 🌾" if filters["gluten_free"] else "[ ] Bezlepkový 🌾"
    vegan_status = "[X] Veganský 🌱" if filters["vegan"] else "[ ] Veganský 🌱"
    veg_status = "[X] Vegetarián 🧀" if filters["vegetarian"] else "[ ] Vegetarián 🧀"
    
    print(f" {gf_status}   {vegan_status}   {veg_status} ")
    print("-" * 60)

def len_utf8(s):
    """Pomocná funkce pro odhad šířky řetězce v terminálu s ohledem na diakritiku a emoji."""
    # Emojis jako 🌾, 🌱, 🧀, ⏱️, ⚡ mohou mít v různých terminálech různou šířku.
    # Pro standardní mono-space bereme standardní délku stringu.
    return len(s)

def draw_recipe_card(recipe):
    width = 56
    
    # 1. Horní okraj
    print("┌" + "─" * width + "┐")
    
    # 2. První řádek: Název receptu a info o obsahu
    name = recipe["name"]
    badge = f"[ Obsahuje {recipe['contains']} ]"
    
    # Vypočítáme mezery mezi názvem a štítkem (název + mezera + štítek = width)
    # Emojis v badge mohou posunout zarovnání, upravíme šířku
    spacing = width - len(name) - len(badge) - 2
    if spacing < 1:
        spacing = 1
    
    print(f"│  {name}" + " " * spacing + f"{badge}  │")
    
    # 3. Druhý řádek: Čas a kalorie
    time_kcal = f"⏱️  {recipe['time']}    ⚡  {recipe['kcal']} kcal"
    # Odstraníme diakritické posuny pro přesné zarovnání spaceru
    spacing_time = width - 26 # Přibližná vizuální délka fixního textu
    print(f"│  ⏱️  {recipe['time'].ljust(8)}  ⚡  {str(recipe['kcal']) + ' kcal' : <28}│")
    
    # 4. Prázdný řádek pro vzdušnost
    print("│" + " " * width + "│")
    
    # 5. Spodní řádek: Živiny
    nutrients = f"Proteiny: {recipe['protein'].ljust(10)}Carbs: {recipe['carbs'].ljust(12)}Fats: {recipe['fat']}"
    print(f"│  {nutrients.ljust(width - 4)}  │")
    
    # 6. Spodní okraj
    print("└" + "─" * width + "┘")

def draw_navbar():
    print("=" * 60)
    print(" 🏠 Domů  |  🗄️ Spížírna  |  [📖 Recepty]  |  🛒 Nákupy  |  📐 Blueprint")
    print("=" * 60)

def main():
    while True:
        clear_screen()
        draw_header()
        draw_filters()
        
        # Filtrování receptů
        filtered = []
        for r in POLEVKY:
            if filters["gluten_free"] and not r["gluten_free"]:
                continue
            if filters["vegan"] and not r["vegan"]:
                continue
            if filters["vegetarian"] and not r["vegetarian"]:
                continue
            filtered.append(r)
            
        print(" FILTROVANÉ POLÉVKY:")
        print()
        if not filtered:
            print("  [!] Žádné polévky neodpovídají zvoleným filtrům.")
        else:
            for r in filtered:
                draw_recipe_card(r)
                print()
                
        draw_navbar()
        
        print("\n MOŽNOSTI OVLÁDÁNÍ:")
        print(" 1 - Přepnout filtr: Bezlepkový 🌾")
        print(" 2 - Přepnout filtr: Veganský 🌱")
        print(" 3 - Přepnout filtr: Vegetarián 🧀")
        print(" 0 - Ukončit program")
        print("-" * 60)
        
        volba = input("Zadejte volbu: ").strip()
        if volba == "1":
            filters["gluten_free"] = not filters["gluten_free"]
        elif volba == "2":
            filters["vegan"] = not filters["vegan"]
        elif volba == "3":
            filters["vegetarian"] = not filters["vegetarian"]
        elif volba == "0":
            print("\nDěkujeme za použití Kulinářského Generátoru!")
            break

if __name__ == "__main__":
    main()
