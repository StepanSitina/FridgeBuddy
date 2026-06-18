package com.example.ui.screens

object LocalRecipesProvider {
    fun getRecipes(): List<FeaturedRecipe> {
        val rawData = listOf(
            // Polévky (1–30)
            "Hovězí vývar s nudlemi a játrovými knedlíčky|Hovädzí vývar s rezancami a pečeňovými haluškami|obed",
            "Kuřecí vývar se zeleninou|Kurací vývar so zeleninou|obed",
            "Bramboračka s houbami|Zemiaková polievka s hubami|obed",
            "Kulajda se zastřeným vejcem|Kulajda so strateným vajcom|obed",
            "Česnečka se sýrem a krutony|Cesnačka so syrom a krutónmi|vecere",
            "Gulášová polévka|Gulášová polievka|obed",
            "Drštková polévka|Držková polievka|obed",
            "Čočková polévka s párkem|Šošovicová polievka s párkom|obed",
            "Hrachová polévka s opečenou houskou|Hrachová polievka s opečenou žemľou|obed",
            "Fazolová polévka s uzeným masem|Fazuľová polievka s údeným mäsom|obed",
            "Rajská polévka s rýží nebo těstovinami|Rajčinová polievka s ryžou alebo cestovinami|obed",
            "Zelňačka s klobásou|Kapustnica s klobásou|obed",
            "Kyselo (krkonošské)|Krkonošské kyselo|obed",
            "Pórková polévka s vejcem|Pórová polievka s vajcom|obed",
            "Květákový krém|Karfiolový krém|obed",
            "Brokolicový krém|Brokolicový krém|obed",
            "Dýňová polévka|Tekvicová polievka|obed",
            "Špenátová polévka|Špenátová polievka|obed",
            "Rybí polévka (vánoční)|Rybia polievka|obed",
            "Boršč (česká variace)|Boršč|obed",
            "Frankfurtská polévka|Frankfurtská polievka|obed",
            "Cibulová polévka se sýrovou gratinovanou vekou|Cibuľová polievka so syrovou krutónou|vecere",
            "Krupicová polévka s vejcem|Krupicová polievka s vajcom|obed",
            "Kmínová polévka|Rascová polievka|obed",
            "Drožďová polévka|Drožďová polievka|obed",
            "Zeleninový Askorti (směs zeleniny)|Zeleninová polievka|obed",
            "Houbový krém|Hubový krém|obed",
            "Celerový krém s jablkem|Zelerový krém s jablkom|obed",
            "Polévka z pečeného česneku|Polievka z pečeného cesnaku|obed",
            "Demikát (brynzová polévka)|Demikát|obed",

            // Hlavní jídla s hovězím masem (31–55)
            "Svíčková na smetaně s houskovým knedlíkem|Sviečková na smotane s knedľou|obed",
            "Hovězí guláš s cibulí a houskovým knedlíkem|Hovädzí guláš s knedľou|obed",
            "Rajská omáčka s hovězím masem a těstovinami|Rajská omáčka s hovädzím mäsom a cestovinami|obed",
            "Koprová omáčka s hovězím masem a knedlíkem|Kôprová omáčka s hovädzím mäsom a knedľou|obed",
            "Španělský ptáček s rýží|Španielsky vtáčik s ryžou|obed",
            "Hovězí na česneku se špenátem a bramborovým knedlíkem|Hovädzie na cesnaku so špenátom a zemiakovou knedľou|obed",
            "Hovězí pečeně na hříbkách|Hovädzie pečenie na dubákoch|obed",
            "Znojemská hovězí pečeně s rýží|Znojemská hovädzia pečienka s ryžou|obed",
            "Hovězí líčka na červeném víně s kaší|Hovädzie líčka na červenom víne s kašou|obed",
            "Štěpánská hovězí pečeně|Štepanská hovädzia pečienka|obed",
            "Vařené hovězí s křenovou omáčkou|Varené hovädzie s chrenovou omáčkou|obed",
            "Hovězí tokáň|Hovädzí tokáň|obed",
            "Dušené hovězí v mrkvi s bramborem|Dusené hovädzie v mrkve s zemiakmi|obed",
            "Hovězí roštěná na tataráku (na cibulce)|Hovädzia roštenka na cibuľke|obed",
            "Vídeňská roštěná s vídeňskou cibulkou|Viedenská roštenka s cibuľkou|obed",
            "Segedínský guláš z hovězího masa|Segedínsky guláš z hovädzieho mäsa|obed",
            "Hovězí plátek na paprice|Hovädzí plátok na paprike|obed",
            "Karbanátky z hovězího masa|Fašírky z hovädzieho mäsa|obed",
            "Hovězí Stroganov|Hovädzí Stroganov|obed",
            "Sekaná pečeně (směs s hovězím)|Sekaná pečená|obed",
            "Hovězí steak s pepřovou omáčkou|Hovädzí steak s peprovou omáčkou|obed",
            "Tatarák z hovězího svíčkového masa s topinkami|Hovädzí tatarák s hriankami|vecere",
            "Hovězí maso v koprovce s bramborem|Hovädzie mäso v kôprovke s zemiakmi|obed",
            "Plov (rýže s hovězím masem a mrkví)|Plov - ryža s hovädzím mäsom|obed",
            "Hovězí kostky na rajčatech|Hovädzie kocky na rajčinách|obed",

            // Hlavní jídla s vepřovým masem (56–85)
            "Vepřo-knedlo-zelo|Bravčo-knedlo-zelo|obed",
            "Smažený vepřový řízek s bramborovým salátem|Vypražený bravčový rezeň s zemiakovým šalátom|obed",
            "Vepřový guláš|Bravčový guláš|obed",
            "Pečené vepřové koleno s křenem a hořčicí|Pečené bravčové koleno s chrenom a horčicou|obed",
            "Pečená vepřová žebírka v medové marinádě|Pečené bravčové rebierka v medovej marináde|obed",
            "Vepřová panenka s hříbkovou omáčkou|Bravčová panenka s dubákovou omáčkou|obed",
            "Výpečky se špenátem a bramborovým knedlíkem|Bravčové výpečky so špenátom a knedľou|obed",
            "Vepřové kotlety na kmíně|Bravčové kotlety na rasci|obed",
            "Segedínský guláš (vepřový) s houskovým knedlíkem|Segedínsky guláš bravčový s knedľou|obed",
            "Vepřová játra na cibulce s rýží|Bravčová pečeň na cibuľke s ryžou|obed",
            "Smažená vepřová játra s tatarkou|Vyprážaná bravčová pečeň s tatárskou|obed",
            "Vepřové kousky na žampionech|Bravčové kúsky na šampiňónoch|obed",
            "Plněná vepřová panenka|Bravčová panenka plnená|obed",
            "Katův šleh (pikantní vepřová směs)|Katov šľah|vecere",
            "Vepřová krkovice pečená na česneku|Bravčová krkovička pečená na cesnaku|obed",
            "Moravský vrabec s dušeným zelím|Moravský vrabec s dusenou kapustou|obed",
            "Vepřové plátky na hořčici|Bravčové plátky na horčici|obed",
            "Vepřový závitek se šunkou a sýrem|Bravčový závitok so šunkou a syrom|obed",
            "Prejt s kyselým zelím a bramborem|Jaternica s kapustou a zemiakmi|obed",
            "Jitrnice a jelita s lepenicou|Jaternice a klobásky s lepenicou|obed",
            "Opečená klobása s čočkou na kyselo|Opečená klobása s šošovicou na kyslo|vecere",
            "Vepřové maso v mrkvi|Bravčové mäso v mrkve|obed",
            "Vepřové rizoto se sýrem a kyselou okurkou|Bravčové rizoto so syrom|vecere",
            "Smažený bůček|Vyprážaný bôčik|obed",
            "Plněné papriky s mletým vepřovým masem a rajskou omáčkou|Plnené papriky s mletým mäsom a rajskou omáčkou|obed",
            "Holandský řízek se sýrem a bramborovou kaší|Holandský rezeň so syrom a kašou|obed",
            "Šunkofleky (zapékané těstoviny s uzeným masem)|Šunkofleky|vecere",
            "Uzené maso s křenovou omáčkou|Údené mäso s chrenovou omáčkou|obed",
            "Uzené maso s hrachovou kaší|Údené mäso s hrachovou kašou|obed",
            "Vepřové soté se zeleninou|Bravčové soté so zeleninou|vecere",

            // Hlavní jídla s drůbežím masem (86–115)
            "Pečené kuře s nádivkou a bramborem|Pečené kura s plnkou a zemiakmi|obed",
            "Kuře na paprice s houskovým knedlíkem nebo těstovinami|Kura na paprike s knedľou|obed",
            "Smažený kuřecí řízek s kaší|Vyprážaný kurací rezeň s kašou|obed",
            "Kuřecí směs se zeleninou (čínská variace po česku)|Kuracia zmes so zeleninou|vecere",
            "Kuřecí prsa na broskvi se sýrem|Kuracie prsia na broskyni so syrom|vecere",
            "Kuřecí soté s pórkem a žampiony|Kuracie soté s pórom a šampiňónmi|vecere",
            "Pečená kachna s červeným zelím a lokšemi|Pečená kačka s červenou kapustou a lokšami|obed",
            "Pečená husa s karlovarským knedlíkem|Pečená hus s karlovarskou knedľou|obed",
            "Kuřecí játra na cibulce|Kuracia pečeň na cibuľke|vecere",
            "Kuřecí nudličky na kari se smetanou|Kuracie rezance na kari so smotanou|vecere",
            "Zapékaná kuřecí prsa se špenátem a sýrem|Zapekané kuracie prsia so špenátom a syrom|vecere",
            "Kuře pečené na divoko (s jalovcem a slaninou)|Kura pečené na divoko|obed",
            "Kuřecí roláda s míchanými vajíčky a párkem|Kuracia roláda s miešanými vajíčkami|obed",
            "Kuřecí křídla na medu a pivu|Kuracie krídla na mede a pive|vecere",
            "Krůtí guláš|Morčací guláš|obed",
            "Pečená krůtí stehna na česneku|Pečené morčacie stehná na cesnaku|obed",
            "Smažený krůtí řízek v cornflakes obalu|Vyprážaný morčací rezeň v cornflakes obale|obed",
            "Krůtí plátek s bylinkovým máslem|Morčací plátok s bylinkovým maslom|vecere",
            "Kuřecí placičky s cuketou|Kuracie placky s cuketou|vecere",
            "Kuře pečené na zelí|Kura pečené na kapuste|obed",
            "Kuřecí stehna na paprice|Kuracie stehná na paprike|obed",
            "Krůtí maso s chřestem a smetanovou omáčkou|Morčacie mäso so špargľou a smotanou|obed",
            "Kuřecí rizoto s hráškem a kukuřicí|Kuracie rizoto s hráškom a kukuricou|vecere",
            "Kuřecí špíz s cibulí a paprikou|Kurací špíz s cibuľou a paprikou|vecere",
            "Slepice na paprice|Sliepka na paprike|obed",
            "Pečené kuřecí čtvrtky na másle|Pečené kuracie štvrtky na masle|obed",
            "Kuřecí kapsa plněná nivou|Kuracia vrecko plnené nivou|vecere",
            "Kuřecí stripsy s česnekovým dipem|Kuracie stripsy s cesnakovým dipom|vecere",
            "Kachní prsa s brusinkovou omáčkou|Kačacie prsia s brusnicovou omáčkou|obed",
            "Husí játra na sádle|Husia pečeň na masti|vecere",

            // Ryby a zvěřina (116–130)
            "Smažený kapr s bramborovým salátem|Vyprážaný kapor s zemiakovým šalátom|obed",
            "Kapr na černo (s perníkem, šwestkami a ořechy)|Kapor načierno|obed",
            "Kapr na kmíně a másle|Kapor na rasci a masle|obed",
            "Pečený pstruh na másle s bylinkami|Pečený pstruh na masle s bylinkami|obed",
            "Pstruh na modro|Pstruh na modro|obed",
            "Candát na kmíně s bramborovou kaší|Zubáč na rasci s zemiakovou kašou|obed",
            "Pečený sumec na česneku|Pečený sumec na cesnaku|obed",
            "Kančí guláš s brusinkami a šípkovou omáčkou|Diviačí guláš s brusnicami|obed",
            "Jelení hřbet s omáčkou z lesního ovoce|Jelení chrbát s omáčkou z lesného ovocia|obed",
            "Srnčí na smetaně|Srnčie na smotane|obed",
            "Bažant pečený na slanině|Bažant pečený na slanine|obed",
            "Divočák se zelím a šípkovou omáčkou|Diviak s kapustou a šípkovou omáčkou|obed",
            "Zajíc na smetaně|Zajac na smotane|obed",
            "Rybí filé zapečené se sýrem a smetanou|Rybie filé zapečené so syrom a smotanou|vecere",
            "Smažené rybí prsty (domácí)|Domáce rybie prsty|vecere",

            // Bezmasá a zeleninová jídla (131–160)
            "Bryndzové halušky se slaninou|Bryndzové halušky so slaninou|obed",
            "Smažený sýr s hranolkami a tatarkou|Vyprážaný syr s hranolkami|vecere",
            "Květákový mozek s bramborem|Karfiolový mozog s zemiakmi|vecere",
            "Smažený květák s vařeným bramborem|Vyprážaný karfiol s varenými zemiakmi|obed",
            "Lečo s vejcem|Lečo s vajcom|vecere",
            "Smažená žampiony s tatarkou|Vyprážané šampiňóny s tatárskou|vecere",
            "Dušená mrkev s hráškem a bramborem|Dusená mrkva s hráškom a zemiakmi|vecere",
            "Bramboráky (cmunda)|Zemiakové placky|vecere",
            "Kynuté ovocné knedlíky s tvarohem|Kysnuté ovocné knedle s tvarohom|obed",
            "Bramborové knedlíky plněné uzeným masem|Zemiakové knedle plnené údeným mäsom|obed",
            "Bramborové šišky s mákem a máslem|Zemiakové šišky s makom a maslom|dezert",
            "Nudle s mákem a cukrem|Rezance s makom a cukrom|dezert",
            "Dukátové buchtičky s vanilkovým šodó|Dukátové buchtičky s vanilkovým krémom|obed",
            "Palačinky s džemem nebo tvarohem|Palacinky s džemom alebo tvarohom|dezert",
            "Zapékané brambory se sýrem a smetanou|Zapekané zemiaky so syrom a smotanou|vecere",
            "Houbový kuba (kroupy s houbami)|Hubový kuba|vecere",
            "Smažené cuketové placky|Vyprážané cuketové placky|vecere",
            "Cuketa zapékaná s mletým masem a sýrem|Cuketa zapekaná s mletým mäsom a syrom|vecere",
            "Pečená dýně s balkánským sýrem|Pečená tekvica s balkánskym syrom|vecere",
            "Špenát s vejcem a vařeným bramborem|Špenát s vajcom a vareným zemiakom|vecere",
            "Čočka na kyselo s volským okem a okurkou|Šošovica na kyslo s volským okom|vecere",
            "Hrachová kaše s cibulkou a chlebem|Hrachová kaša s cibuľkou a chlebom|vecere",
            "Tvarohové knedlíky s meruňkami|Tvarohové knedle s marhuľami|obed",
            "Krupicová kaše s kakaem a máslem|Krupicová kaša s kakaom a maslom|dezert",
            "Rýžový nákyp s meruňkami nebo švestkami|Ryžový nákyp s marhuľami alebo slivkami|obed",
            "Žemlovka s jablky a tvarohem|Žemľovka s jablkami a tvarohom|obed",
            "Zapékaná brokolice se sýrovou omáčkou|Zapekaná brokolica so syrovou omáčkou|vecere",
            "Koprová omáčka s vejcem a bramborem|Kôprová omáčka s vajcom a zemiakmi|obed",
            "Houbová omáčka z čerstvých hub s knedlíkem|Hubová omáčka s knedľou|obed",
            "Těstovinový salát s majonézou a zeleninou|Cestovinový šalát s majonézou a zeleninou|vecere",
            "Smažený celer jako řízek|Vyprážaný zeler ako rezeň|vecere",

            // Svačinky, studená kuchyně a chuťovky (161–185)
            "Obložené chlebíčky (šunkový, sýrový, vajíčkový)|Obložené chlebíčky|vecere",
            "Utopenci s cibulí a octovým nálevem|Utopenci s cibuľou|vecere",
            "Nakládaný hermelín s česnekem|Nakladaný hermelín s cesnakom|vecere",
            "Pivní sýr s topinkou|Pivný syr s hriankou|vecere",
            "Škvarková pomazánka s chlebem|Oškvarková nátierka s chlebom|vecere",
            "Budapešťská pomazánka|Budapeštianska nátierka|vecere",
            "Vajíčková pomazánka|Vajíčková nátierka|vecere",
            "Rybičková pomazánka s cibulkou|Rybičková nátierka s cibuľkou|vecere",
            "Tvarohová pomazánka s pažitkou|Tvarohová nátierka s pažítkou|vecere",
            "Česneková pomazánka se sýrem|Cesnaková nátierka so syrom|vecere",
            "Hermelínový salát|Hermelínový šalát|vecere",
            "Pařížský salát s rohlíkem|Parížsky šalát s rožkom|vecere",
            "Vlašský salát|Vlašský šalát|vecere",
            "Bramborový salát klasický|Zemiakový šalát klasický|obed",
            "Tlačenka s cibulí a octem|Tlačenka s cibuľou a octom|vecere",
            "Domácí huspenina (sulc)|Domáca huspenina|vecere",
            "Pečenáče (nakládané ryby)|Pečenáče|vecere",
            "Opečený párek s hořčicí a křenem|Opečený párok s horčicou a chrenom|vecere",
            "Topinky s česnekem|Hrianky s cesnakom|vecere",
            "Ďábelské tousty s mletým masem|Diabolské toasty s mletým mäsom|vecere",
            "Domácí paštika z drůbežích jater|Domáca paštéta z hydinovej pečene|vecere",
            "Nakládané olomoucké tvarůžky|Nakladané olomoucké tvarôžky|vecere",
            "Smažené olomoucké tvarůžky v těstíčku|Vyprážané olomoucké tvarôžky|vecere",
            "Domácí škvarky|Domáce oškvarky|vecere",
            "Šunka od kosti s křenovou pěnou|Šunka od kosti s chrenovou penou|vecere",

            // Tradiční pečení a moučníky (186–200)
            "Český honzovy buchty|České honzove buchty|dezert",
            "Tažený jablečný závin s ořechy|Ťahaný jablkový závin s orechmi|dezert",
            "Bábovka (mramorová nebo tvarohová)|Bábovka mramorová|dezert",
            "Kynuté koláče s drobenkou|Kysnuté koláče s posýpkou|dezert",
            "Bublanina s třešněmi nebo višněmi|Bublanina s čerešňami|dezert",
            "Rybízový koláč s drobenkou|Ríbezľový koláč s posýpkou|dezert",
            "Linecký koláč s rybízovou zavařeninou|Linecký koláč|dezert",
            "Perník na plech s čokoládovou polevou|Perník na plech s čokoládovou polevou|dezert",
            "Medovník|Medovník|dezert",
            "Kremrole se sněhem|Kremrole so snehom|dezert",
            "Větrníky s karamelovým krémem|Veterníky s karamelovým krémom|dezert",
            "Laskonky s ořechovým krémem|Laskonky s orechovým krémom|dezert",
            "Punčový dort|Punčový recept|dezert",
            "Vánočka s rozinkami a mandlemi|Vianočka s hrozienkami a mandľami|dezert",
            "Velikonoční beránek|Veľkonočný baránok|dezert",

            // Drinks (Pití) to keep category fresh (20 items)
            "Osvěžující jablečný koktejl|Osviežujúci jablkový kokteil|piti",
            "Bylinkový ledový čaj s citronem|Bylinkový ľadový čaj s citrónom|piti",
            "Domácí bezinková limonáda|Domaca bazova limonada|piti",
            "Teplý jablečný mošt se skořicí|Teplý jablkový mušt so škoricou|piti",
            "Ledová káva se zmrzlinou|Ľadová káva so zmrzlinou|piti",
            "Nealkoholické Mojito|Nealkoholické Mojito|piti",
            "Jahodové smoothie s mlékem|Jahodové smoothie s mliekom|piti",
            "Banánový mléčný koktejl|Banánový mliečny kokteil|piti",
            "Horká čokoláda se šlehačkou|Horúca čokoláda so šľahačkou|piti",
            "Klasická citronáda s mátou|Klasická citrónáda s mätou|piti",
            "Zelený ledový čaj Matcha|Zelený ľadový čaj Matcha|piti",
            "Pomerančový čerstvý džus|Pomarančový svieži džús|piti",
            "Teplé mléko s medem|Teplé mlieko s medom|piti",
            "Ledový broskvový čaj|Ľadový broskyňový čaj|piti",
            "Horký šípkový čaj s medem|Horúci šípkový čaj s medom|piti",
            "Ostružinový milkshake|Ostružinový kokteil|piti",
            "Okurková limonáda s limetkou|Uhorková limonáda s limetkou|piti",
            "Zázvorový čaj s citronem|Zázvorový čaj s citrónom|piti",
            "Tradiční studené kakao|Tradičné studené kakao|piti",
            "Mango lassi s kardamomem|Mango lassi s kardamónom|piti"
        )

        return rawData.map { line ->
            val parts = line.split("|")
            val czTitle = parts[0]
            val skTitle = parts[1]
            val category = parts[2]

            // Dedicated High-Fidelity Override for Most Popular Iconic Recipes to preserve premium quality and exact kuchařsky správné steps:
            if (czTitle == "Svíčková na smetaně s houskovým knedlíkem" || czTitle == "Svíčková na smetaně") {
                return@map FeaturedRecipe(
                    "Svíčková na smetaně", "Sviečková na smotane", "45 min", 680, "35g", "48g", "32g",
                    listOf("800g hovězí zadní", "150g špeku", "250g mrkve", "150g celeru", "150g petržele", "250ml smetany (33%)", "50g másla", "1 lžíce plnotučné hořčice", "nové koření (5 kuliček)", "bobkový list (3 ks)", "citronka (1 lžíce)", "cukr (2 lžíce)"),
                    listOf("800g hovädzie zadné", "150g slaniny", "250g mrkvy", "150g zeleru", "150g petržlenu", "250ml smotany (33%)", "50g masla", "1 lyžica plnotučnej horčice", "nové korenie (5 guličiek)", "bobkový list (3 ks)", "citrón (1 lyžica)", "cukor (2 lyžice)"),
                    false, false, false,
                    "1. Hovězí maso prošpikujte proužky špeku, osolte a opepřete. Na pánvi ho ze všech stran zprudka opečte po dobu 5 minut, aby se zatáhlo.\n2. V pekáči na rozpuštěném másle orestujte kořenovou zeleninu (mrkev, celer, petržel) nakrájenou na kostičky dozlatova (cca 10 minut). Přidejte koření (nové koření, bobkový list, pepř), plnotučnou hořčici a cukr.\n3. Do pekáče vložte maso, podlijte 300ml hovězího vývaru a pečte zakryté v troubě na 180°C cca 90 minut do změknutí.\n4. Maso a divoké koření vyjměte. Zeleninu s výpekem rozmixujte tyčovým mixérem dohladka, vlijte smetanu a nechte přejít varem. Dochuťte citronem, octem nebo špetkou soli podle chuti.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Nové koření: Usušené bobule pimentovníku lékařského z Karibiku. Kombinuje chuť hřebíčku, skořice a černého pepře.",
                    "1. Hovädzie mäso prešpikujte prúžkami slaniny, osoľte a okoreňte. Na panvici ho zo všetkých strán sprudka opečte po dobu 5 minút, aby sa zatiahlo.\n2. V pekáči na rozpustenom masle orestujte koreňovú zeleninu (mrkva, zeler, petržlen) nakrájanú na kocky dozlatista (cca 10 minút). Pridajte nové korenie, bobkový list, celú horčicu a cukor.\n3. Do pekáča vložte mäso, podlejte 300ml hovädzieho vývaru a pečte zakryté v rúre na 180°C cca 90 minút do zmäknutia.\n4. Mäso a divoké korenie vyberte. Zeleninu s výpekom rozmixujte tyčovým mixérom dohladka, vlejte smotanu a nechajte prejsť varom. Dochuťte citrónom a octom.",
                    65.0, "obed"
                )
            }
            if (czTitle == "Smažený kuřecí řízek s kaší") {
                return@map FeaturedRecipe(
                    "Smažený kuřecí řízek s kaší", "Vyprážaný kurací rezeň s kašou", "30 min", 580, "38g", "42g", "24g",
                    listOf("600g kuřecích prsních řízků", "3 ks čerstvých vajec", "150g polohrubé mouky", "200g jemné strouhanky", "1 kg brambor", "100ml plnotučného mléka", "80g másla", "sůl podélně", "200ml oleje na smažení"),
                    listOf("600g kuracích prsných rezňov", "3 ks čerstvých vajec", "150g polohrubej múky", "200g jemnej strúhanky", "1 kg zemiakov", "100ml plnotučného mlieka", "80g masla", "soľ", "200ml oleja na vyprážanie"),
                    false, false, false,
                    "1. Kuřecí prsa nakrájejte na řízky, přes potravinovou fólii lehce naklepejte, osolte ze všech stran.\n2. Obalte v trojobalu: nejprve v hladké či polohrubé mouce, poté v rozšlehaných vejcích s kapkou mléka a nakonec v jemné strouhance. Strouhanku nezatlačujte příliš silně.\n3. Smažte ve vyšší vrstvě rozpáleného oleje z obou stran přesně 4-5 minut dozlatova.\n4. Brambory uvařte v osolené vodě, sceďte, přidejte 80g másla a vlažné plnotučné mléko. Vyšlehejte bramborovou kaši dohladka.",
                    "1. Kuracie prsia nakrájajte na rezne, cez fóliu zľahka naklepte, osoľte.\n2. Obaľte v trojobale: najprv v hladkej múke, potom v rozšľahaných vajciach s kapkou mlieka a nakoniec v strúhanke.\n3. Smažte vo vyššej vrstve rozpáleného oleja z oboch strán presne 4-5 minút dozlatista.\n4. Zemiaky uvarte v osolenej vode, sceďte, pridajte maslo, mlieko a vyšľahajte kašu.",
                    49.0, "obed"
                )
            }
            if (czTitle == "Bramboráky (cmunda)") {
                return@map FeaturedRecipe(
                    "Bramboráky (cmunda)", "Tradičné zemiakové placky", "20 min", 310, "5g", "34g", "18g",
                    listOf("1 kg brambor (varný typ C)", "5 stroužků českého česneku", "2 lžíce sušené majoránky", "2 ks čerstvých vajec", "4 lžíce hladké mouky", "1 lžička soli", "0.5 lžičky mletého černého pepře", "150g vepřového sádla na smažení"),
                    listOf("1 kg zemiakov (varný typ C)", "5 strúčikov cesnaku", "2 lyžice sušenej majoránky", "2 ks vajec", "4 lyžice hladkej múky", "1 lyžička soli", "150g bravčovej masti"),
                    false, false, true,
                    "1. Syrové brambory oloupejte, polovinu nastrouhejte najemno a polovinu nahrubo. Rukama vymačkejte maximum přebytečné škrobové vody.\n2. Přidejte prolisovaný česnek, majoránku promnutou v dlaních (rozvoní se!), vejce, mouku, sůl a pepř. Rychle promíchejte.\n3. Na pánvi rozpalte vepřové sádlo. Lžící tvořte tenké placky a smažte z každé strany při střední teplotě dozlatova a dokřupava (cca 3 minuty z každé strany).\n4. Nechte okapat na papírovém ubrousku a podávejte horké.",
                    "1. Surové zemiaky ošúpte, polovicu nastrúhajte najjemno a polovicu nahrubo. Rukami vytlačte prebytočnú vodu.\n2. Pridajte cesnak, majoránku, vajcia, múku, soľ. Premiešajte.\n3. Na panvici rozpáľte masť. Smažte tenké placky z každej strany dozlatista a dokruhava (cca 3 minúty z každej strany).\n4. Podávajte teplé.",
                    22.0, "vecere"
                )
            }
            if (czTitle == "Smažený sýr s hranolkami a tatarkou") {
                return@map FeaturedRecipe(
                    "Smažený sýr s hranolkami a tatarkou", "Vyprážaný syr s hranolkami", "20 min", 620, "28g", "48g", "36g",
                    listOf("400g sýru Eidam (skvěle funguje 30% nebo 45%)", "2 ks vajec", "100g hladké mouky", "150g strouhanky", "600g brambor na domácí hranolky", "150ml tatarské omáčky", "olej na smažení"),
                    listOf("400g syru Eidam", "2 ks vajec", "100g hladkej múky", "150g strúhanky", "600g zemiakov na hranolky", "150ml tatárskej omáčky", "olej na vyprážanie"),
                    false, false, true,
                    "1. Sýr nakrájejte na silné plátky (cca 1.5 - 2 cm). Obalte ho v mouce, rozšlehaných vejcích a strouhance.\n2. VELMI DŮLEŽITÉ: Celý trojobal zopakujte ještě jednou (znovu do vajec a strouhanky). Tím vytvoříte silný krunýř, který zabrání vytečení sýra při smažení!\n3. Smažte v dostatečně vysoké vrstvě velmi rozpáleného oleje rychle (cca 1.5 minuty z každé strany).\n4. Hranolky nakrájejte na stejné hranoly, propláchněte studenou vodou, vysušte a usmažte dozlatova. Podávejte s tatarkou.",
                    "1. Syr nakrájajte na hrubé plátky (cca 1.5 - 2 cm). Obaľte ho v múke, rozšľahaných vajciach a strúhanke.\n2. DÔLEŽITÉ: Celý trojobal zopakujte ešte raz (znovu vajcia a strúhanka). Tým vytvoríte ochranný obal proti vytečeniu!\n3. Smažte vo vysokej vrstve rozpáleného oleja rýchlo (cca 1.5 minúty z každej strany).\n4. Podávajte s hranolkami a tatárskou.",
                    45.0, "vecere"
                )
            }
            if (czTitle == "Teplý jablečný mošt se skořicí") {
                return@map FeaturedRecipe(
                    "Teplý jablečný mošt se skořicí", "Teplý jablkový mušt so škoricou", "10 min", 110, "0g", "26g", "0g",
                    listOf("1 litr čerstvého jablečného moštu", "2 ks celé skořice", "4 ks hřebíčku", "2 ks badyánu", "0.5 ks citronu na plátky"),
                    listOf("1 liter čerstvého jablkového muštu", "2 ks celej škorice", "4 ks klinčekov", "2 ks badyánu", "0.5 ks citróna"),
                    true, true, true,
                    "1. Do kastrůlku nalijte čistý jablečný mošt. Přidejte svitky skořice, hřebíček a hvězdičky badyánu.\n2. Zahřívejte pozvolna na mírném plameni cca 8 minut pod pokličkou. Mošt by se měl prohřát, ale nesmí vřít do klokotu, aby si zachoval svěží ovocnou chuť.\n3. Odstavte, vyjměte celé koření a podávejte ozdobené plátkem citronu s kapkou medu.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Badyán: Hvězdicovité koření z plodů čínského anýzovníku. Má výraznou vůni s lékořicovým podtónem, působí protizánětlivě.",
                    "1. Do kastrólika nalejte jablkový mušt. Pridajte celú škoricu, klinčeky a hviezdičky badyánu.\n2. Zahrievajte pozvoľna cca 8 minút. Mušt by nemal prudko vrieť.\n3. Odstráňte korenie a podávajte s plátkom citróna.",
                    14.0, "piti"
                )
            }
            if (czTitle == "Demikát (brynzová polévka)" || czTitle == "Demikát") {
                return@map FeaturedRecipe(
                    "Demikát (brynzová polévka)", "Demikát (tradičná bryndzová polievka)", "20 min", 290, "12g", "24g", "16g",
                    listOf("250g pravé slovenské bryndzy", "4 ks brambor", "1 ks střední cibule", "1 lžíce mleté sladké papriky", "1 lžíce vepřového sádla", "200ml zakysané smetany", "pažitka", "divoké koření (kmín)", "starší chléb na krutony"),
                    listOf("250g pravej slovenskej bryndze", "4 ks zemiakov", "1 ks cibule", "1 lyžica mletej sladkej papriky", "1 lyžica masti", "200ml kyslej smotany", "pažítka", "drvená rasca", "starší chlieb"),
                    false, false, true,
                    "1. Oloupané brambory nakrájejte na kostky a uvařte v 1 litru osolené vody s drceným kmínem do měkka.\n2. Na sádle orestujte cibuli dosklovita, zaprašte mletou sladkou paprikou, rychle promíchejte a stáhněte z plamene.\n3. V misce rozmačkejte bryndzu vidličkou, zalijte teplým vývarem z uvařených brambor a zakysanou smetanou. Metličkou vyšlehejte hladký základ.\n4. Tento bryndzový základ vlijte zpět k vařeným bramborám i s paprikovým sádlem. Prohřejte, ale již nevařte (bryndza by se srazila!). Podávejte s opečenými krutony z chleba a pažitkou.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Bryndza: Tradiční slovenský měkký slaný ovčí sýr. Je to probiotická bomba s bohatou specifickou chutí a dlouhou historií v karpatských salaších.",
                    "1. Ošúpané zemiaky nakrájajte na kocky a dajte variť do 1 litra osolenej vody s rascou.\n2. Na masti orestujte cibuľu, pridajte mletú papriku a stiahnite z ohňa.\n3. V miske rozmiešajte bryndzu s kyslou smotanou a troškou teplej vody zo zemiakov.\n4. Zmes vlejte k zemiakom, prehrejte (nevariť!) a podávajte s chlebovými krutónmi.",
                    32.0, "obed"
                )
            }
            if (czTitle == "Hovězí guláš s cibulí a houskovým knedlíkem" || czTitle == "Hovězí guláš") {
                return@map FeaturedRecipe(
                    "Hovězí guláš na sádle", "Hovädzí guláš na masti", "45 min", 640, "36g", "42g", "28g",
                    listOf("800g hovězího kližku", "800g cibule", "4 lžíce vepřového sádla", "3 lžíce mleté sladké papriky", "1 lžíce sušené majoránky", "4 stroužky česneku", "1 lžička drceného kmínu", "1 litr hovězího vývaru", "sůl a černý pepř"),
                    listOf("800g hovädzieho kližku", "800g cibule", "4 lyžice bravčovej masti", "3 lyžice mletej sladkej papriky", "1 lyžica majoránky", "4 strúčiky cesnaku", "1 lyžička drvenej rasce", "1 liter hovädzieho vývaru"),
                    false, false, false,
                    "1. Cibuli nakrájejte nadrobno. V silnostěnném hrnci rozpusťte sádlo a cibuli restujte za stálého míchání do tmavě hnědé barvy cca 15-20 minut (nepřipálit!).\n2. Maso nakrájejte na 3cm kostky, přidejte k cibuli a zprudka opékejte 10 minut. Zaprašte mletou paprikou, drceným kmínem, zamíchejte a hned podlijte teplým vývarem.\n3. Přidejte sůl, pepř a prolisovaný česnek. Pod pokličkou pozvolna duste na mírném ohni 90 minut, dokud hovězí kližek není měkký a omáčka hustá.\n4. Na závěr vmíchejte majoránku v dlaních a povařte 3 minuty. Podávejte s čerstvou cibulí a houskovým knedlíkem.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Hovězí kližek: Řez hovězího masa s vysokým podílem šlach a kolagenu. Právě kolagen se při dlouhém dušení postará o přirozeně sametovou hustotu gulášové omáčky bez potřeby zahušťování moukou.",
                    "1. Cibuľu nakrájajte nadrobno. V hrnci rozpustite masť a cibuľu restujte do tmavohnedej farby.\n2. Mäso nakrájajte na kocky a pridajte k cibuli. Opečte, zasypte mletou paprikou, rascou a zalejte vývarom.\n3. Pridajte cesnak, osoľte a zakryté duste 90 minút.\n4. Na záver pridajte majoránku.",
                    60.0, "obed"
                )
            }
            if (czTitle == "Plněné papriky s mletým vepřovým masem a rajskou omáčkou" || czTitle == "Plněné papriky") {
                return@map FeaturedRecipe(
                    "Plněné papriky v rajské omáčce", "Plnené papriky v rajskej omáčke", "45 min", 520, "26g", "44g", "22g",
                    listOf("8 ks dlouhých bílých paprik", "500g mletého vepřového masa (plec)", "100g rýže", "1 ks vejce", "1 litr rajského protlaku", "3 lžíce cukru", "1 ks drcené skořice", "4 svitky hřebíčku", "máslová jíška"),
                    listOf("8 ks dlhých bielych paprík", "500g mletého bravčového mäsa", "100g ryže", "1 ks vajca", "1 liter rajského pretlaku", "3 lyžice cukru", "kúsok škorice", "maslová zápražka"),
                    false, false, false,
                    "1. Rýži předvařte. Mleté maso osolte, opepřete, smíchejte s rýží, vejcem a dobře promíchejte.\n2. Paprikám odřízněte vršky, odstraňte vnitřky se semínky a naplňte připravenou masovou směsí.\n3. Do hrnce nalijte rajský protlak, přidejte skořici, hřebíček, divoké koření, cukr, sůl a 200 ml vody. Přiveďte k varu, vložte naplněné papriky a duste zakryté 40 minut.\n4. Papriky vyjměte, omáčku přeceďte, zahustěte světlou máslovou jíškou a jemně povařte ještě 10 minut. Podávejte s rýží.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Rajský protlak (kondenzovaný): Vysoce redukovaná rajčata bez semen a slupek, která dodávají omáčce plné umami tělo a přirozeně sladkokyselý tón.",
                    "1. Mleté mäso zmiešajte s predvarenou ryžou, vajcom, soľou a korením. Papriky zbavte vnútra a naplňte zmesou.\n2. Rajský pretlak dajte variť so škoricou, klinčekmi, cukrom a soľou.\n3. Vložte papriky a duste 40 minút.\n4. Omáčku zahustite zápražkou a podávajte s ryžou alebo knedľou.",
                    38.0, "obed"
                )
            }
            if (czTitle == "Medovník" || czTitle == "Český honzovy buchty") {
                return@map FeaturedRecipe(
                    "Tradiční babiččin Medovník", "Tradičný medovník babičky", "45 min", 420, "6g", "58g", "18g",
                    listOf("500g hladké mouky", "200g cukru krupice", "150g másla", "3 lžíce poctivého včelího medu", "1 lžička jedlé sody", "2 ks vajec", "1 konzerva zkaramelizovaného kondenzovaného mléka Salko", "250g másla do krému", "150g mlessných ořechů"),
                    listOf("500g hladkej múky", "200g cukru", "150g masla", "3 lyžice včelieho medu", "1 lyžička jedlej sódy", "2 ks vajec", "1 konzerva Salka", "250g masla do krému", "150g vlašských orechov"),
                    false, false, true,
                    "1. Ve vodní lázni v kastrůlku rozpusťte máslo, med, cukr. Přidejte vejce, jedlou sodu a šlehejte 5 minut do husté pěny. Vmíchejte hladkou mouku a vypracujte vláčné medové těsto.\n2. Těsto rozdělte na 5 dílů. Každý vyválejte na tenký plát a pečte na pečicím papíru v troubě na 170°C cca 6 minut dozlatova. Okraje ihned po upečení ořízněte podle kulaté formy.\n3. Krém ušlehejte z celého zkaramelizovaného salka a změklého másla pokojové teploty. Mažte pláty střídavě s krémem.\n4. Povrch celého medovníku potřete zbylým krémem a posypte jemnou drobenkou ze zbylých oříznutých plátů smíchaných s mletými ořechy. Nechte uležet 24 hodin v lednici.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Včelí med: Přírodní zlatý nektar dodávající těstu hydrofobní vlastnosti, které zajišťují, že pláty po namazání ideálně změknou a zachovají si úžasnou vlhkost.",
                    "1. Nad parou rozpusťte maslo, med, cukor. Pridajte vajcia, sódu a šľahajte. Vmiešajte múku.\n2. Rozdelte na 5 dielov, rozvaľkajte na tenko a pečte na 170°C cca 6 minút.\n3. Ušľahajte krém zo skaramelizovaného salka a masla.\n4. Navrstvite pláty, potrite krémom, posypte orechmi a dajte odležať na 24 hodín.",
                    48.0, "dezert"
                )
            }
            if (czTitle == "Mango lassi s kardamomem") {
                return@map FeaturedRecipe(
                    "Mango lassi s kardamomem", "Mango lassi s kardamónom", "5 min", 160, "5g", "28g", "3g",
                    listOf("1 ks zralého sladkého manga", "250g bílého hustého řeckého jogurtu", "100ml studeného plnotučného mléka", "2 lžíce medu", "0.5 lžičky celých semen zeleného kardamomu (drcených)"),
                    listOf("1 ks zrelého manga", "250g gréckeho jogurtu", "100ml mlieka", "2 lyžice medu", "0.5 lyžičky zeleného kardamónu"),
                    true, false, true,
                    "1. Mango oloupejte, odřízněte dužinu od pecky a nakrájejte ji na kostky.\n2. Do stolního vysokorychlostního mixéru vložte kostky manga, řecký jogurt, studené mléko a včelí med.\n3. Přidejte drcená semínka zeleného kardamomu (vyklepněte ze zelené tobolky a utlučte v hmoždíři).\n4. Mixujte na maximální výkon po dobu 40 sekund, dokud nezískáte naprosto sametový nápoj bez kousků. Podávejte ve vychlazených pohárech.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Zelený kardamom: Vysoce ceněné indické koření s citrusovým aromatem a nádechem eukalyptu. Působí skvěle na žaludek a zjemňuje sladkost manga.",
                    "1. Mango ošúpte a nakrájajte na kocky.\n2. V mixéri rozmixujte mango, jogurt, mlieko, med a čerstvo podrvený zelený kardamón dohladka.\n3. Podávajte chladené.",
                    19.0, "piti"
                )
            }
            if (czTitle == "Zelený ledový čaj Matcha") {
                return@map FeaturedRecipe(
                    "Zelený ledový čaj Matcha", "Zelený ľadový čaj Matcha", "5 min", 85, "1g", "16g", "0g",
                    listOf("4g cereálního Matcha prášku (Uji ceremonial grade)", "150ml studené filtrované vody", "100ml bio mandlového mléka", "2 lžičky javorového sirupu", "5 ks kostek drceného ledu"),
                    listOf("4g Matcha prášku", "150ml studenej vody", "100ml mandľového mlieka", "2 lyžičky javorového sirupu", "ľad"),
                    true, true, true,
                    "1. Prášek Matcha prosejte přes jemné nerezové sítko přímo do misky Chawan, abyste předešli vzniku nerozpustných hrudek.\n2. Zalijte studenou filtrovanou vodou (cca 70-80°C maximum, nikdy ne vroucí!).\n3. Tradiční bambusovou metličkou (chasen) intenzivně šlehejte rychlým pohybem zápěstí ve tvaru písmene W cca 30 sekund, dokud na povrchu nevznikne stabilní hustá zelená pěna.\n4. Vyšlehanou matchu opatrně vlijte do sklenice plné ledu s javorovým sirupem a mandlovým mlékem.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Matcha: Unikátní mletý zelený čaj z lístků Tencha pěstovaných v Japonsku pod stínicí plachtou. Obsahuje obrovské množství chlorofylu, antioxidantů a dodává dlouhotrvající energii l-theanin.",
                    "1. Matcha prášok preosejte do misky a zalejte vodou s teplotou max 80°C.\n2. Bambusovou metličkou chasen vyšľahajte do penista v smere písmena W.\n3. Podávajte v pohári s ľadom, japorovým sirupom a mandľovým mliekom.",
                    26.0, "piti"
                )
            }
            if (czTitle == "Tažený jablečný závin s ořechy") {
                return@map FeaturedRecipe(
                    "Tažený jablečný závin s ořechy", "Ťahaný jablkový závin s orechmi", "45 min", 290, "4g", "48g", "10g",
                    listOf("300g hladké pšeničné výběrové mouky", "1 lžička octa", "1 lžíce stolního oleje", "180ml vlažné vody", "100g rozpuštěného másla", "6 ks kyselých šťavnatých jablek", "80g strouhanky orestované na másle", "60g vlašských ořechů", "1 lžička mleté skořice"),
                    listOf("300g výberovej hladkej múky", "1 lyžička octu", "180ml vlažnej vody", "100g roztopeného masla", "6 ks jĺk", "80g strúhanky", "60g vlašských orechov", "škorica"),
                    false, false, true,
                    "1. Z hladké mouky, oleje, octa, špetky soli a vlažné vody vypracujte hladké a dokonale pružné nelepivé těsto. Těsto se musí aspoň 10 minut bít o pracovní vál, dokud nezíská sametový lesklý povrch.\n2. Zformujte bochánek, potřete teplým olejem, přikryjte teplou miskou a nechte 30 minut odpočívat.\n3. Na velký pomoučený bavlněný ubrus těsto položte a pomalu jej hřbety rukou vytahujte od středu k okrajům, dokud není tenké jako papír (uvidíte skrz něj vzor ubrusu). Silnější okraje odřízněte.\n4. Pokapejte rozpuštěným máslem, posypte orestovanou strouhankou, plátky jablek, nasekanými ořechy, skořicí a cukrem.\n5. Pomocí zvedání ubrusu závin zaviňte, konce stlačte dolů, položte na plech s pečicím papírem, potřete máslem a pečte při 180°C po dobu 40 minut dozlatova.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Tažené těsto: Klasické vídeňské těsto na závin, které díky přídavku octa (kyselost mění strukturu lepkové mřížky) a propracování získává mimořádnou elasticitu.",
                    "1. Z múky, vody, octu a soli vypracujte vláčne elastické cesto, ktoré nechajte odpočívať pod teplým hrncom 30 minút.\n2. Cesto položte na čistý pomúčený obrus a rukami ho opatrne vytiahnite zospodu od stredu do všetkých strán do priesvitna.\n3. Pokvapkajte maslom, posypte strúhankou, nakrájanými jablkami, orechmi, škoricou a cukrom.\n4. Závin zviňte pomocou obrusu, konce podahnite, dajte na plech, potrite maslom a pečte pri 180°C cca 40 minút.",
                    30.0, "dezert"
                )
            }
            if (czTitle == "Špagety Carbonara" || czTitle == "Šunkofleky (zapékané těstoviny s uzeným masem)") {
                return@map FeaturedRecipe(
                    "Originální italské špagety Carbonara", "Originálne talianske špagety Carbonara", "20 min", 580, "28g", "52g", "26g",
                    listOf("400g italských semolinových špaget", "150g slaniny Guanciale (nebo Pancetta)", "4 ks čerstvých žloutků", "1 ks celého vejce", "100g sýru Pecorino Romano (nebo Parmigiano Reggiano)", "čerstvě mletý černý pepř", "hrubá sůl na vodu"),
                    listOf("400g semolinových špagiet", "150g slaniny Guanciale", "4 ks čerstvých žĺtkov", "1 ks vajca", "100g syru Pecorino Romano", "mleté čierne korenie"),
                    false, false, false,
                    "1. Guanciale (sušený vepřový podbradek) nakrájejte na 1cm kostičky a na suché cold pánvi pomalu restujte dokřupava cca 10 minut. Tuk se musí roztavit. Slaninu vyjměte z pánve, tuk nechte v pánvi.\n2. V misce smíchejte žloutky, celé vejce, jemně nastrouhaný ovčí sýr Pecorino a velkou dávku čerstvě namletého pepře.\n3. Špagety uvařte ve vroucí slané vodě o 1 minutu méně, než je obvyklé al dente. Špagety kleštěmi přendejte přímo do pánve k horkému uvolněnému tuku ze slaniny. Přidejte naběračku vody z těstovin. Pánev sundejte z plamene.\n4. Ihned vlijte vaječno-sýrovou směs a rychle míchejte. Vznikne dokonale krémová emulze, vajíčka nesmí v žádném případě vytvořit míchaná vejce (proto bez plamene). Přisypte křupavé guanciale a hned podávejte.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Guanciale: Tradiční italský nasolený a usušený vepřový podbradek (líčko). Má bohatou chuť a je zásadní pro autentické Carbonara.",
                    "1. Guanciale nakrájajte na prúžky a pomaly opečte na panvici. Vyberte chrumkavé kúsky, panvicu odložte.\n2. V miske zmiešajte žĺtky, celé vajce, nastrúhaný syr Pecorino a mleté čierne korenie.\n3. Uvarené špagety dajte priamo do panvice s tukom zo slaniny, pridajte lyžicu horúcej vody zo špagiet a odstavte.\n4. Vlejte vaječnú zmes a rýchlo premiešajte na krémovú emulziu.",
                    40.0, "vecere"
                )
            }
            if (czTitle == "Bryndzové halušky se slaninou") {
                return@map FeaturedRecipe(
                    "Tradiční slovenské bryndzové halušky", "Tradičné slovenské bryndzové halušky", "25 min", 610, "18g", "54g", "32g",
                    listOf("800g syrových brambor (varný typ C)", "300g polohrubé pšeničné výběrové mouky", "250g pravé slovenské ovčí bryndzy", "150g vyuzené slaniny (špeku)", "1 ks čerstvého vejce", "1 lžička soli do těsta", "2 lžíce zakysané smetany na zjemnění"),
                    listOf("800g surových zemiakov (varný typ C)", "300g polohrubej výberovej múky", "250g pravej slovenskej ovčej bryndze", "150g údenej gazdovskej slaniny", "1 ks vajca", "soľ", "2 lyžice kyslej smotany"),
                    false, false, true,
                    "1. Syrové brambory oloupejte a nastrouhejte najemno na struhadle. Nechte chvíli odstát a vymačkejte nebo slijte přebytečnou vodu.\n2. Do nastrouhaných brambor přidejte jedno vejce, sůl a polohrubou mouku. Vypracujte polotuhé těsto, které by mělo lehce stékat z vařečky. Pokud je příliš řídké, doplňte mouku.\n3. Do velkého hrnce s vroucí osolenou vodou protlačte těsto přes síto na halušky (nebo odkrajujte malé halušky nožem z prkýnka). Vařte krátce – jakmile všechny halušky vyplavou na hladinu, nechte je max. 1 minutu dojit a hned sceďte (neproplachujte studenou vodou!).\n4. Na pánvi rozehřejte nakrájenou slaninu a vyškvařte ji dokřupava. Slaninu vyjměte.\n5. Hocky teplých halušek v míse smíchejte s bryndzou, lžící teplé vody z vaření halušek a lžící vyškvařeného tuku ze slaniny. Rozdělte na talíře, posypte křupavou slaninou a zakápněte zbytkem tuku.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Ovčí bryndza: Tradiční slovenský měkký, solený a zralý ovčí sýr. Je to mimořádná probiotická bomba vyráběná z ovčího hrudkového sýra, která dává haluškám nezaměnitelnou plnou chuť.",
                    "1. Surové zemiaky ošúpte a nastrúhajte najjemno, podľa potreby zlejte prebytočnú vodu.\n2. Pridajte vajce, soľ, múku a vypracujte cesto.\n3. Halušky hádžte cez haluškár do vriacej osolenej vody. Keď vyplávu, nechajte prejsť varom 1 minútu a sceďte.\n4. Na panvici upečte na drobno nakrájanú slaninku.\n5. Horúce halušky premiešajte s bryndzou, troškou vody z varenia a polejte slaninkou aj s výpekom.",
                    34.0, "obed"
                )
            }
            if (czTitle == "Kulajda se zastřeným vejcem" || czTitle == "Kulajda") {
                return@map FeaturedRecipe(
                    "Poctivá jihočeská kulajda s koprem", "Poctivá juhočeská kulajda s kôprom", "25 min", 340, "12g", "28g", "18g",
                    listOf("40g sušených lesních hub (nebo 150g čerstvých)", "4 ks brambor (nakrájených na kostičky)", "1 litr zeleninového nebo masového vývaru", "4 ks velmi čerstvých vajec", "250ml zakysané lahůdkové smetany (min. 15-18%)", "3 lžíce hladké mouky", "40g másla", "1 svazek čerstvého kopru", "2 lžíce třtinového cukru", "3 lžíce octa", "koření: kmín, 3 kuličky nového koření, 1 bobkový list, sůl, černý pepř"),
                    listOf("40g sušených lesných húb", "4 ks zemiakov", "1 liter vývaru", "4 ks čerstvých vajec", "250ml kyslej smotany", "3 lyžice hladkej múky", "40g masla", "čerstvý kôpor", "cukor", "ocot, nové korenie, bobkový list, soľ"),
                    false, false, true,
                    "1. Sušené houby předem zalijte horkou vodou na 15 minut. Poté vodu sceďte, ale nevylévejte ji.\n2. V hrnci na rozpuštěném maisle orestujte na kostičky nakrájené brambory s drceným kmínem. Přisypte hladkou mouku a vytvořte světlou jíšku (cca 2 minuty). Postupně za stálého míchání přilévejte studený vývar a vodu z hub, aby se nevytvořily hrudky.\n3. Přidejte houby, nové koření, bobkový list, sůl a pepř. Vařte na mírném ohni 15 minut, dokud brambory nezměknou.\n4. Zakysanou smetanu rozšlehejte s naběračkou teplé polévky a vlijte do hrnce. Prohřejte, dochuťte octem a cukrem na vyváženou sladkokyselou chuť. Na samotném konci odstavte z ohně a vmíchejte najemno nasekaný čerstvý kopr.\n5. V samostatném hrnci připravte zastřená (pošírovaná) vejce: do mírně vroucí vody s kapkou octa udělejte lžící vír, doprostřed vlijte vyklepnuté vejce a vařte přesně 3 minuty, aby žloutek zůstal tekutý. Podávejte uprostřed talíře.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Bobkový list: Sušené aromatické listy vavřínu vznešeného. Dodávají polévkám hluboké, mírně dřevité aroma a podporují trávení.",
                    "1. Sušené huby vopred namočte. V hrnci na masle orestujte zemiaky na kocky s rascou, poprášte múkou a zalejte vývarom.\n2. Pridáme huby, bobkový list, nové korenie, soľ a varíme 15 minút.\n3. Vmiešajte kyslú smotanu, dochuťte octom, cukrom a nakoniec pridajte nasekaný čerstvý kôpor.\n4. Zvlášť uvarte stratené vajcia vo vode s octom a podávajte navrchu polievky.",
                    25.0, "obed"
                )
            }
            if (czTitle == "Bramboračka s houbami") {
                return@map FeaturedRecipe(
                    "Klasická staročeská bramboračka", "Klasická staročeská zemiaková polievka", "30 min", 280, "8g", "38g", "12g",
                    listOf("5 ks brambor", "250g směsi lesních hub (čerstvých nebo namočených sušených)", "1 ks velké mrkve", "1 ks kořenové petržele", "0.5 ks celeru", "1 ks cibule", "4 stroužky česneku", "50g másla", "3 lžíce polohrubé mouky na jíšku", "1.2 litru vývaru", "majoránka, drcený kmín, sůl, mletý černý pepř, čerstvá hladkolistá petrželka"),
                    listOf("5 ks zemiakov", "250g lesných húb", "1 ks mrkvy", "0.5 ks zeleru a petržlenu", "1 ks cibule", "4 strúčiky cesnaku", "50g masla", "3 lyžice múky", "1.2 litra vývaru", "majoránka, rasca, soľ, petržlenová vňať"),
                    false, false, true,
                    "1. Všechnu kořenovou zeleninu (mrkev, celer, petržel) a brambory oloupejte a nakrájejte na malé kostičky.\n2. V hrnci rozpusťte máslo, přidejte nakrájenou cibuli a orestujte dosklovita. Poté přisypte kořenovou zeleninu a restujte cca 5 minut. Zaprašte moukou a míchejte další 2 minuty do voňavé zlatavé jíšky.\n3. Pomalu přilévejte studený vývar za stálého důkladného míchání. Přidejte nakrájené brambory, kmín, pepř, sůl a připravené houby.\n4. Vařte 20 minut na mírném plameni, dokud brambory a zelenina nejsou zcela měkké.\n5. Na závěr přidejte prolisovaný česnek, majoránku rozetřenou v dlaních a čerstvou petrželku. Nechte 2 minuty odležet pod pokličkou mimo plamen.",
                    "1. Zeleninu a zemiaky nakrájajte na kocky. V hrnci na masle opečieme cibuľku a nakrájanú zeleninu.\n2. Poprášime múkou, vytvoríme zápražku a zalejeme vývarom.\n3. Pridáme zemiaky, huby, rascu, soľ a varíme do zmäknutia zemiakov.\n4. Na koniec primiešame pretlačený cesnak, majoránku a ozdobíme petržlenovou vňaťou.",
                    22.0, "obed"
                )
            }
            if (czTitle == "Vepřo-knedlo-zelo") {
                return@map FeaturedRecipe(
                    "Tradiční vepřo-knedlo-zelo", "Tradičné bravčo-knedlo-zelo", "45 min", 720, "38g", "64g", "34g",
                    listOf("800g vepřové krkovice nebo bůčku", "1 kg kysaného bílého zelí", "500g houskového knedlíku (nebo suroviny na domácí)", "2 ks cibule", "5 stroužků českého česneku", "2 lžíce sádla", "2 lžičky drceného kmínu", "1 lžíce hladké mouky na zahuštění výpeku", "cukr krupice a sůl na zelí"),
                    listOf("800g bravčovej krkovičky alebo bôčiku", "1 kg kyslej bielej kapusty", "500g knedle", "2 ks cibule", "5 strúčikov cesnaku", "2 lyžice masti", "rasca", "múka", "cukor, soľ"),
                    false, false, false,
                    "1. Vepřové maso očistěte, ze všech stran ho osolte, posypte drceným kmínem a důkladně potřete prolisovaným česnekem. Položte ho do pekáče na nakrájenou cibuli a podlijte trochou vody.\n2. Pekáč přikryjte a pečte v předehřáté troubě na 170°C cca 90 minut. Během pečení maso pravidelně podlévejte vlastním výpekem. Posledních 15 minut pekáč odkryjte, zvyšte teplotu na 200°C a dopečte křupavou kůrčičku.\n3. Kysané zelí propláchněte podle kyselosti, zalijte trochou vody se solí a kmínem a duste 20 minut. Na sádle orestujte druhou cibuli, zaprašte lžící cukru, nechte zkaramelizovat, přidejte k zelí a zaprašte lžící hladké mouky pro zahuštění. Povařte 5 minut.\n4. Upečené maso vyjměte, výpek zaprašte trochou hladké mouky, orestujte, zalijte horkou vodou či vývarem a provařte 10 minut na silný sos.\n5. Podávejte plátky teplého masa, knedlíky a dušené zelí bohatě přelité vypečenou šťávou.",
                    "1. Bravčové mäso osoľte, okoreňte rascou a cesnakom. Pečte na cibuli zakryté na 170°C, ku koncu odkryte na chrumkavú kôrku.\n2. Kapustu poduste s rascou a soľou. Na masti osmažte cibuľu s cukrom, pridajte ku kapuste a jemne zahustite múkou.\n3. Výpek z mäsa poprášte múkou, zalejte vodou a prevarte na hustú omáčku.\n4. Podávajte s knedľou.",
                    58.0, "obed"
                )
            }
            if (czTitle == "Dukátové buchtičky s vanilkovým šodó") {
                return@map FeaturedRecipe(
                    "Dukátové buchtičky s pravým vanilkovým krémem", "Dukátové buchtičky s pravým vanilkovým krémom", "35 min", 490, "10g", "78g", "14g",
                    listOf("500g polohrubé mouky", "250ml vlažného plnotučného mléka", "30g čerstvého droždí", "60g cukru krupice", "80g másla", "2 ks vaječných žloutků", "špetka soli", "citronová kůra", "500ml mléka na krém", "1 ks pravého vanilkového lusku Bourbon", "1 ks vanilkového pudinku", "3 ks žloutků do krému", "80g cukru do krému"),
                    listOf("500g polohrubej múky", "250ml vlažného mlieka", "30g čerstvého droždia", "60g cukru", "80g masla", "2 žĺtky", "citrónová kôra", "500ml mlieka na krém", "1 ks vanilkového struku", "3 žĺtky do krému", "80g cukru"),
                    false, false, true,
                    "1. Do vlažného mléka rozdrobte droždí, lžičku cukru a nechte vzejít kvásek (cca 10 minut).\n2. V míse smíchejte mouku, zbytek cukru, sůl, citronovou kůru, žloutky, rozpuštěné máslo a vzešlý kvásek. Vypracujte hladké a lesklé kynuté těsto, které se nelepí na stěny mísy. Poprašte se moukou a nechte pod utěrkou na teplém místě kynout 45 minut.\n3. Vykynuté těsto vyklopte, rukama rozválejte na válečky a odkrajujte malé kousky (buchtičky). Pokládejte vedle sebe do máslem vymazaného pekáčku a každou ze stran potřete rozpuštěným máslem, aby se po upečení neslepily. Pečte v předehřáté troubě na 180°C dozlatova (cca 20-25 minut).\n4. Mezitím připravte pravý krém: v hrnci zahřejte mléko se semínky z vanilkového lusku. Žloutky utřete s cukrem a lžičkou pudinkového prášku. horké vanilkové mléko pomalu vlijte k žloutkové směsi, vraťte zpět na sporák a za stálého míchání mírně zahřívejte do zhoustnutí (nevařte, žloutky by se srazily!).\n5. Teplé zlatavé buchtičky rozdělte na talíře a bohatě přelijte horkým vanilkovým krémem.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Vanilkový lusk Bourbon: Vysoce kvalitní lusk z ostrova Réunion (dříve Bourbon). Obsahuje tisíce drobných aromatických semínek s intensivním a hřejivým vanilkovým profilem.",
                    "1. Pripravte kvások z mlieka, droždia a cukru. Vypracujte cesto z múky, žĺtkov, masla a kvásku a nechajte vykysnúť 45 minút.\n2. Z cesta vytvarujte malé buchtičky, dajte do maslom vymasteného pekáča a dobre potrite maslom z boku.\n3. Pečte na 180°C cca 20 minút.\n4. Z mlieka, vanilkového struku, žĺtkov a cukru pripravte nad parou hladký krém.\n5. Teplé buchtičky nalejte vanilkovým krémom.",
                    32.0, "obed"
                )
            }
            if (czTitle == "Česnečka se sýrem a krutony" || czTitle == "Česnečka") {
                return@map FeaturedRecipe(
                    "Vyprošťovací česnečka s krutony a sýrem", "Ozdravujúca cesnačka so syrom a krutónmi", "15 min", 260, "10g", "25g", "12g",
                    listOf("8 stroužků českého česneku", "4 ks brambor nakrájených na kostičky", "1.2 litru silného hovězího vývaru", "200g sýru Eidam nebo Gouda (nastrouhaného)", "2 krajíce staršího chleba na krutony", "1 lžíce sádla", "1 ks vejce (volitelně)", "1 lžička drceného kmínu", "1 lžíce sušené majoránky", "sůl a mletý černý pepř po chuti"),
                    listOf("8 strúčikov cesnaku", "4 ks zemiakov", "1.2 litra hovädzieho vývaru", "200g nastrúhaného syra", "2 krajce chleba", "1 lyžica masti", "1 ks vajca", "rasca, majoránka, soľ"),
                    false, false, true,
                    "1. Brambory oloupejte, nakrájejte na malé kostičky a uvařte je ve vývaru s drceným kmínem a trochou soli doměkka (cca 15 minut).\n2. Mezitím na pánvi na sádle orestujte kostičky chleba dozlatova a dokřupava (krutony).\n3. Česnek oloupejte a utřete se špetkou soli najemno. Přidejte jej do hrnce k uvařeným bramborám společně s majoránkou rozetřenou v dlaních (česnek se nesmí dlouho vařit, aby neztratil svou sílu a pálivost, stačí 1 minuta).\n4. Pokud chcete, do polévky na závěr za stálého míchání vlijte rozšlehané vejce a nechte je srazit.\n5. Do hlubokého talíře dejte na dno hrst nastrouhaného sýra, zalijte horkou polévkou s bramborami, posypte krutony a ihned podávejte, dokud se sýr krásně táhne.",
                    "1. Zemiaky dajte variť do vývaru s rascou a soľou do zmäknutia.\n2. Na panvici na masti opečte chlebové krutóny.\n3. Na konci pridajte do polievky pretlačený cesnak a majoránku a nechajte prejsť varom 1 minútu.\n4. Do taniera dajte nastrúhaný syr, zalejte horúcou polievkou a pridajte chrumkavé krutóny.",
                    19.0, "vecere"
                )
            }
            if (czTitle == "Segedínský guláš (vepřový) s houskovým knedlíkem" || czTitle == "Segedínský guláš") {
                return@map FeaturedRecipe(
                    "Poctivý maďarský segedínský guláš", "Poctivý maďarský segedínsky guláš", "45 min", 680, "32g", "52g", "36g",
                    listOf("600g vepřového ramínka (plece)", "500g kysaného zelí", "2 ks cibule", "2 lžíce sádla", "200ml smetany ke šlehání (31%)", "3 lžíce mleté sladké papriky", "1 lžička pálivé papriky", "drcený kmín, bobkový list, nové koření", "2 lžíce hladké mouky na zahuštění", "sůl a pepř"),
                    listOf("600g bravčového pliecka", "500g kyslej kapusty", "2 ks cibule", "2 lyžice masti", "200ml smotany na šľahanie (31%)", "3 lyžice sladkej papriky", "kmín, bobkový list, nové korenie", "2 lyžice múky", "soľ, korenie"),
                    false, false, false,
                    "1. Na sádle v hrnci orestujte oloupanou a najemno nakrájenou cibuli dozlatova. Přidejte na 3cm kostky nakrájené vepřové ramínko a nechte ho ze všech stran 5 minut zatáhnout.\n2. Hrnec stáhněte z plamene, přisypte mletou papriku (sladkou i pálivou), rychle promíchejte a ihned zalijte teplou vodou nebo vývarem (aby paprika nezhořkla). Přidejte kmín, bobkový list, nové koření, sůl, pepř a duste pod pokličkou 30 minut.\n3. Kysané zelí propláchněte vodou (upravte tak jeho kyselost), překrájejte na kratší kousky a přidejte do hrnce k poloměkkému masu. Duste společně dalších 20 minut doměkka.\n4. Smetanu pokojové teploty důkladně promíchejte s hladkou moukou (vytvořte zátrepku). Za stálého míchání vlijte do guláše. Nechte na mírném ohni provařit minimálně 10 minut, aby mouka ztratila syrovou chuť.\n5. Podávejte s houskovým knedlíkem.\n\n✦ PREMIUM SLOVNÍČEK KOŘENÍ:\n- Kysané zelí: Mléčně kvašené bílé zelí. Je to tradiční zásobárna vitamínu C a tělu prospěšných bakterií, která dává segedínu jeho charakteristický říz.",
                    "1. Na masti osmažte cibuľu, pridajte kocky mäsa a nechajte zatiahnuť. Poprášte mletou paprikou a zalejte vodou.\n2. Pridajte soľ, rascu, bobkový list a duste 30 minút.\n3. Pridajte kyslú kapustu a duste ďalších 20 minút.\n4. V smotane rozmiešajte hladkú múku a za stáleho miešania vlejte do guláša, prevarte 10 minút.\n5. Podávajte s knedľou.",
                    42.0, "obed"
                )
            }
            if (czTitle == "Palačinky s džemem nebo tvarohem" || czTitle == "Palačinky") {
                return@map FeaturedRecipe(
                    "Jemné francouzské palačinky s džemem", "Jemné francúzske palacinky s džemom", "15 min", 390, "12g", "56g", "12g",
                    listOf("500ml plnotučného mléka", "250g hladké pšeničné mouky (nejlépe světlé)", "2 ks čerstvých vajec", "1 lžíce vanilkového cukru", "špetka soli", "2 lžíce rozpuštěného másla do těsta", "máslo nebo sádlo na potření pánve", "200g domácího jahodového či meruňkového džemu"),
                    listOf("500ml plnotučného mlieka", "250g hladkej múky", "2 ks vajec", "1 lyžica vanilkového cukru", "štipka soli", "2 lyžice roztopeného masla do cesta", "džem na potretie"),
                    false, false, true,
                    "1. V míse metličkou vyslehejte vejce s vlažným mlékem a špetkou soli. Postupně prosívejte hladkou mouku za stálého šlehání tak, aby se nevytvořily hrudky.\n2. Do vzniklého hladkého tekutého těsta vmíchejte rozpuštěné máslo. Těsto nechte před smažením odstát v pokojové teplotě aspoň 10-15 minut (lepková mřížka si odpočine a palačinky se nebudou trhat).\n3. Pánev na palačinky rozehřejte na středně silném plameni a mašlovačkou ji lehce potřete trochou rozpuštěného másla nebo sádla.\n4. Nalijte sběračku těsta, okamžitě s pánví zakružte, aby se těsto rozlilo do co nejtenčí vrstvy. Smažte cca 1.5 minuty, poté palačinku podeberte, otočte a smažte z druhé strany ještě 45 sekund dozlatova.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Francouzské těsto (Crêpes): Velmi tenké palačinky, u kterých se mléko předem smíchá s rozpuštěným máslem. Odpočinutí těsta zaručí dokonalou měkkost a pružnost bez trhání.",
                    "1. V miske vyšľahajte mlieko s vajcami a štipkou soli, postupne pridávajte múku a vyšľahajte na hladké cesto.\n2. Cesto nechajte 10 minút odstáť.\n3. Na rozpálenej panvici potretej maslom smažte tenké palacinky z oboch strán.\n4. Potrite džemom alebo tvarohom a zviňte.",
                    24.0, "dezert"
                )
            }
            if (czTitle == "Kynuté ovocné knedlíky s tvarohem" || czTitle == "Kynuté borůvkové knedlíky s tvarohem" || czTitle == "Kysnuté ovocné knedle s tvarohom") {
                return@map FeaturedRecipe(
                    "Kynuté borůvkové knedlíky s tvarohem", "Kysnuté čučoriedkové knedle s tvarohom", "30 min", 450, "12g", "84g", "8g",
                    listOf("500g hrubé mouky", "250ml vlažného plnotučného mléka", "20g čerstvého droždí", "2 lžíce cukru krupice", "1 ks vejce", "špetka soli", "250g čerstvých borůvek (nebo jahod, meruněk)", "150g tvrdého tvarohu na strouhání", "100g rozpuštěného másla", "moučkový cukr k posypání"),
                    listOf("500g hrubej múky", "250ml vlažného mlieka", "20g čerstvého droždia", "1 lyžica cukru", "1 ks vajca", "soľ", "250g čučoriedok", "150g tvrdého tvarohu", "100g rozpusteného masla"),
                    false, false, true,
                     "1. V troše vlažného mléka rozmíchejte droždí, lžíci cukru a nechte vzejít kvásek (cca 10 minut).\n2. V míse smíchejte hrubou mouku, špetku soli, vejce, zbytek mléka a vzešlý kvásek. Vypracujte vláčné těsto, zakryjte utěrkou a nechte kynout na teplém místě 40 minut.\n3. Vykynuté těsto rozprostřete na pomoučený vál, lžící odebírejte kousky těsta, prsty ho zploštěte, doprostřed vložte čerstvé borůvky nebo jiné ovoce a pevně zabalte do kulatého knedlíku. Nechte na vále ještě 10 minut dokynout.\n4. Knedlíky vařte v mírně osolené vroucí vodě zakryté pokličkou přesně 6-8 minut (v polovině času je vařečkou otočte). Ihned po vytažení každý knedlík propíchněte vidličkou, aby z něj vyšla pára a nesrazil se.\n5. Podávejte bohatě posypané nastrouhaným tvrdým tvarohem, moučkovým cukrem a přelité horkým máslem.",
                    "1. Pripravte kvások z droždia, mlieka a cukru. Zamieste cesto z múky, vajca a kvásku, nechajte kysnúť 40 minút.\n2. Cesto rozdelte, naplňte čučoriedkami a vytvarujte gulaté knedle. Nechajte ešte 10 minút nakysnúť.\n3. Varte vo vriacej osolenej vode prikryté 6-8 minút, po vytiahnutí ihneď prepichnite vidličkou.\n4. Podávajte s nastrúhaným tvarohom, cukrom a teplým maslom.",
                    38.0, "obed"
                )
            }
            if (czTitle == "Špagety Carbonara" || czTitle == "Šunkofleky (zapékané těstoviny s uzeným masem)") {
                return@map FeaturedRecipe(
                    "Originální italské špagety Carbonara", "Originálne talianske špagety Carbonara", "20 min", 580, "28g", "52g", "26g",
                    listOf("400g italských semolinových špaget", "150g slaniny Guanciale (nebo Pancetta)", "4 ks čerstvých žloutků", "1 ks celého vejce", "100g sýru Pecorino Romano (nebo Parmigiano Reggiano)", "čerstvě mletý černý pepř", "hrubá sůl na vodu"),
                    listOf("400g semolinových špagiet", "150g slaniny Guanciale", "4 ks čerstvých žĺtkov", "1 ks vajca", "100g syru Pecorino Romano", "mleté čierne korenie"),
                    false, false, false,
                    "1. Guanciale (sušený vepřový podbradek) nakrájejte na 1cm kostičky a na suché cold pánvi pomalu restujte dokřupava cca 10 minut. Tuk se musí roztavit. Slaninu vyjměte z pánve, tuk nechte v pánvi.\n2. V misce smíchejte žloutky, celé vejce, jemně nastrouhaný ovčí sýr Pecorino a velkou dávku čerstvě namletého pepře.\n3. Špagety uvařte ve vroucí slané vodě o 1 minutu méně, než je obvyklé al dente. Špagety kleštěmi přendejte přímo do pánve k horkému uvolněnému tuku ze slaniny. Přidejte naběračku vody z těstovin. Pánev sundejte z plamene.\n4. Ihned vlijte vaječno-sýrovou směs a rychle míchejte. Vznikne dokonale krémová emulze, vajíčka nesmí v žádném případě vytvořit míchaná vejce (proto bez plamene). Přisypte křupavé guanciale a hned podávejte.\n\n✦ PREMIUM SLOVNÍČEK INGREDIENCÍ:\n- Guanciale: Tradiční italský nasolený a usušený vepřový podbradek (líčko). Má bohatou chuť a je zásadní pro autentické Carbonara.",
                    "1. Guanciale nakrájajte na prúžky a pomaly opečte na panvici. Vyberte chrumkavé kúsky, panvicu odložte.\n2. V miske zmiešajte žĺtky, celé vajce, nastrúhaný syr Pecorino a mleté čierne korenie.\n3. Uvarené špagety dajte priamo do panvice s tukom zo slaniny, pridajte lyžicu horúcej vody zo špagiet a odstavte.\n4. Vlejte vaječnú zmes a rýchlo premiešajte na krémovú emulziu.",
                    40.0, "vecere"
                )
            }

            // Generate realistic ingredients dynamically:
            val ingredientsCZ = generateIngredientsCZ(czTitle)
            val ingredientsSK = generateIngredientsSK(skTitle)

            // Determine time based on category or content
            val timeValue = when {
                category == "piti" -> "5 min"
                czTitle.contains("vývar") || czTitle.contains("pečen") || czTitle.contains("guláš") || czTitle.contains("bábovka") || czTitle.contains("závin") -> "45 min"
                category == "dezert" || czTitle.contains("polévka") || czTitle.contains("krém") || czTitle.contains("pomazánka") || czTitle.contains("řízek") -> "20 min"
                else -> "30 min"
            }

            // Determine calories & macros
            val calculatedCalories = when {
                category == "piti" -> 140
                czTitle.contains("vývar") || czTitle.contains("polévka") || czTitle.contains("krém") || czTitle.contains("salát") -> 220
                czTitle.contains("řízek") || czTitle.contains("sýr") || czTitle.contains("bůček") || czTitle.contains("koleno") || czTitle.contains("guláš") -> 640
                category == "dezert" -> 310
                else -> 480
            }

            val proteinGauge = when {
                czTitle.contains("hovězí") || czTitle.contains("vepřové") || czTitle.contains("kuřecí") || czTitle.contains("krůtí") || czTitle.contains("maso") || czTitle.contains("vývar") -> "28g"
                czTitle.contains("sýr") || czTitle.contains("tlačenka") || czTitle.contains("hermelín") -> "14g"
                else -> "6g"
            }

            val fatGauge = when {
                czTitle.contains("bůček") || czTitle.contains("koleno") || czTitle.contains("sýr") || czTitle.contains("slanina") -> "22g"
                czTitle.contains("polévka") || category == "piti" -> "4g"
                else -> "12g"
            }

            val carbsGauge = when {
                category == "dezert" || czTitle.contains("knedlík") || czTitle.contains("těstoviny") || czTitle.contains("nudle") -> "48g"
                category == "piti" -> "22g"
                else -> "28g"
            }

            val isVegetarian = !czTitle.contains("maso") && !czTitle.contains("vepřové") && !czTitle.contains("hovězí") && !czTitle.contains("kuřecí") && !czTitle.contains("řízek") && !czTitle.contains("slanina") && !czTitle.contains("klobás") && !czTitle.contains("párek") && !czTitle.contains("vývar") && !czTitle.contains("tlačenka") && !czTitle.contains("kachna") && !czTitle.contains("husa") && !czTitle.contains("sumec") && !czTitle.contains("pstruh") && !czTitle.contains("kapr") && !czTitle.contains("jelen") && !czTitle.contains("srnčí") && !czTitle.contains("šunka")
            val isVegan = isVegetarian && !czTitle.contains("vejce") && !czTitle.contains("vajíčková") && !czTitle.contains("sýr") && !czTitle.contains("máslo") && !czTitle.contains("mléko") && !czTitle.contains("smetana") && !czTitle.contains("tvaroh") && !czTitle.contains("bryndza") && !czTitle.contains("hermelín")
            val isGlutenFree = !czTitle.contains("mouka") && !czTitle.contains("knedlíky") && !czTitle.contains("knedlíkem") && !czTitle.contains("těstoviny") && !czTitle.contains("nudle") && !czTitle.contains("chléb") && !czTitle.contains("veka") && !czTitle.contains("rohlík") && !czTitle.contains("buchtičky") && !czTitle.contains("řízek") && !czTitle.contains("sýr") && !czTitle.contains("štrúdl") && !czTitle.contains("závin") && !czTitle.contains("bábovka")

            val instructionsCZ = getCustomInstructionsCZ(czTitle, category, ingredientsCZ)
            val instructionsSK = getCustomInstructionsSK(skTitle, category, ingredientsSK)

            val basePrice = when {
                czTitle.contains("hovězí") || czTitle.contains("vepřové") || czTitle.contains("pstruh") || czTitle.contains("sumec") || czTitle.contains("steak") -> 62.0
                category == "piti" -> 16.0
                category == "dezert" || czTitle.contains("polévka") -> 25.0
                else -> 38.0
            }

            FeaturedRecipe(
                czTitle = czTitle,
                skTitle = skTitle,
                time = timeValue,
                calories = calculatedCalories,
                protein = proteinGauge,
                carbs = carbsGauge,
                fat = fatGauge,
                ingredientsCZ = ingredientsCZ,
                ingredientsSK = ingredientsSK,
                isGlutenFree = isGlutenFree,
                isVegan = isVegan,
                isVegetarian = isVegetarian,
                instructionsCZ = instructionsCZ,
                instructionsSK = instructionsSK,
                basePrice = basePrice,
                category = category
            )
        }
    }

    private fun getCustomInstructionsCZ(title: String, category: String, ingredients: List<String>): String {
        val t = title.lowercase()
        val ingList = ingredients.joinToString(", ")
        return when {
            category == "obed" && (t.contains("polévka") || t.contains("vývar") || t.contains("kulajda") || t.contains("krém") || t.contains("zelňačka") || t.contains("kyselo") || t.contains("demikát")) -> {
                "1. Připravte si suroviny ($ingList). Očistěte kořenovou zeleninu a maso, nakrájejte na rovnoměrné kousky. Cibulku orestujte v hrnci na rozpuštěném másle po dobu 5 minut.\n" +
                "2. Přidejte ostatní suroviny a zalijte 1.5 litrem studené vody či připraveného vývaru. Přiveďte k varu, poté plamen okamžitě stáhněte na minimum.\n" +
                "3. Polévku pozvolna vařte (táhněte) při mírné teplotě cca 90-95 °C po dobu 40-50 minut (v případě poctivého hovězího/slepičího masového vývaru nechte táhnout na mírném ohni minimálně 2 až 3 hodiny).\n" +
                "4. Dochuťte solí, čerstvě mletým černým pepřem, prolisovaným česnekem, majoránkou nebo bylinkami. Pokud připravujete krém, rozmixujte na závěr tyčovým mixérem a zjemněte 100 ml sladké smetany (31%)."
            }
            t.contains("pečen") || t.contains("kachna") || t.contains("husa") || t.contains("žebra") || t.contains("koleno") || t.contains("bůček") || t.contains("sekaná") || t.contains("kuře") || t.contains("sekan") -> {
                "1. Připravte maso či zeleninu ($ingList). Maso očistěte, opláchněte a důkladně osušte papírovým ubrouskem. Vetřete sůl, mletý kmín a drcený česnek ze všech stran a vložte do pekáče s nakrájenou cibulí a kousky vepřového sádla.\n" +
                "2. Pekáč zakryjte víkem nebo alobalem a pečte v předem vyhřáté troubě při teplotě 180 °C po dobu 60 až 90 minut, dokud maso není dokonale křehké. Průběžně maso kontrolujte a podlévejte horkým výpekem.\n" +
                "3. Během pečení každých 20 minut propíchejte jehlicí a přelévejte lžící teplého výpeku. Posledních 15 minut sejměte víko, zvyšte teplotu trouby na 210 °C a dopečte dozlatova, aby kůže či povrch získaly neodolatelnou křupavou kůrčičku.\n" +
                "4. Hotové pečené maso nechte 5-10 minut odpočinout pod alobalem, nakrájejte ostrým nožem na plátky příčně přes vlákno a podávejte teplé s výpekem."
            }
            t.contains("smažen") || t.contains("řízek") || t.contains("karbanát") || t.contains("fašírk") || t.contains("hranolky") || t.contains("bramborák") || t.contains("sýr") || t.contains("stripsy") || t.contains("placky") -> {
                "1. Připravte si suroviny ($ingList). Řízky či zeleninu jemně naklepejte na tloušťku cca 1.5 cm. Osolte ze všech stran. Obalte postupně v hladké mouce, v rozšlehaných vajíčkách s kapkou mléka a nakonec v čerstvé strouhance.\n" +
                "2. V hluboké pánvi rozehřejte silnější vrstvu sádla nebo slunečnicového oleje (cca 2 cm) na teplotu 170-180 °C (správnou teplotu otestujete vhozením špetky strouhanky – musí hned začít bublat).\n" +
                "3. Smažte po dobu 4 až 6 minut z každé strany dozlatova, plamen udržujte stabilně střední. Kousky sýra smažte kratší dobu, cca 1.5 až 2 minuty při mírně vyšší teplotě, aby se sýr rozpustil, ale nevytekl.\n" +
                "4. Hotové smažené kousky vyjměte z pánve a nechte přebytečný tuk okapat na papírovém ubrousku. Podávejte horké s plátkem čerstvého citronu."
            }
            t.contains("guláš") || t.contains("omáčk") || t.contains("svíčkov") || t.contains("rajsk") || t.contains("koprov") || t.contains("segedín") || t.contains("tokáň") || t.contains("soté") || t.contains("ptáček") -> {
                "1. Připravte si základní suroviny ($ingList). Nakrájejte cibuli najemno. V hrnci rozehřejte lžíci sádla a cibuli restujte dozlatova (u guláše až do tmavohnědé barvy, cca 10 minut). Přidejte kostky masa, osolte, opepřete a nechte maso ze všech stran 5 minut zatáhnout.\n" +
                "2. Přisypte mletou sladkou papriku (případně další koření) a rychle promíchejte, aby nezhořkla. Ihned zalijte teplou vodou nebo vývarem tak, aby maso bylo téměř ponořené.\n" +
                "3. Zakryjte hrnec pokličkou a za občasného míchání duste na mírném ohni při teplotě cca 95 °C po dobu 50 až 75 minut, dokud není maso krásně křehké a měkké. Podle potřeby podlévejte teplým vývarem.\n" +
                "4. Omáčku zahustěte světlou jíškou (z másla a mouky) nebo hladkou moukou rozmíchanou v mléce či smetaně. Nechte na mírném plameni provařit ještě 10-15 minut. Na závěr dochuťte solí, špetkou cukru, citronovou šťávou či octem podle typu omáčky."
            }
            category == "dezert" && (t.contains("bábovka") || t.contains("závin") || t.contains("štrúdl") || t.contains("koláč") || t.contains("perník") || t.contains("buchty") || t.contains("vánočka") || t.contains("mák") || t.contains("linecký") || t.contains("dort") || t.contains("větrník") || t.contains("laskonky") || t.contains("beránek")) -> {
                "1. Připravte sypké a tekuté suroviny ($ingList). Ve větší míse vyšlehejte změklé máslo či žloutky s cukrem do světlé pěny. Postupně přimíchejte mouku rozmixovanou s kypřicím práškem (či droždí u kynutých těst) a vlijte vlažné mléko. Opatrně ručně zapracujte vyšlehaný sníh z vaječných bílků.\n" +
                "2. Těsto důkladně prohněťte, nebo vlijte do formy na bábovku/beránka (kterou předem vymažete máslem a vysypete hrubou moukou či krupicí) či rozprostřete na plech vyložený pečicím papírem.\n" +
                "3. Vložte do stabilně vyhřáté trouby na 170-180 °C. Pečte po dobu 30 až 45 minut (u kynutých těst a bábovek délku srovnejte na 45 minut, u závinů stačí cca 25 minut). Propečenost ověřte zapíchnutím dřevěné špejle do středu – po vytažení musí vyjít suchá.\n" +
                "4. Po upečení nechte moučník zcela vychladnout na mřížce. Před krájením na rovnoměrné porce jemně pocukrujte moučkovým cukrem s mletou vanilkou nebo přelijte čokoládovou polevou."
            }
            category == "dezert" || t.contains("palačink") || t.contains("kaše") || t.contains("knedlík") || t.contains("lívanc") || t.contains("vafle") || t.contains("vdolečky") || t.contains("buchtičky") || t.contains("žemlovka") || t.contains("kuba") || t.contains("nákyp") -> {
                "1. Připravte základní suroviny ($ingList). Do misky prosejte polohrubou mouku, přidejte špetku soli, cukr, vejce a vlažné mléko. Vyšlehejte hladké řídké těsto na palačinky nebo lívance (nechte 10 minut odpočinout v chladu), případně vypracujte pevné tvarohové či krupicové těsto.\n" +
                "2. Na rozpálenou pánev potřenou tenkou vrstvou sádla nebo oleje nalijte sběračku těsta. Palačinky smažte na prudším ohni přesně 1.5 minuty z jedné strany, poté otočte a smažte dalších 45-60 sekund dozlatova.\n" +
                "3. V případě ovocných/tvarohových knedlíků vložte vytvarované koule do vroucí mírně osolené vody. Vařte po dobu 8 až 10 minut na mírném ohni a občas jemně nadzvedněte dřevěnou vařečkou ode dna.\n" +
                "4. Hotové palačinky namažte džemem nebo tvarohem a srolujte. Knedlíky či sladké nákypy rozdělte na talíře, bohatě posypte mletým mákem, kakaem či strouhaným tvarohem, a přelijte rozpuštěným teplým máslem."
            }
            category == "vecere" || t.contains("salát") || t.contains("tatarák") || t.contains("pomazán") || t.contains("chléb") || t.contains("caprese") || t.contains("toast") || t.contains("utopenci") || t.contains("hermelín") || t.contains("chlebíčky") || t.contains("tlačenka") || t.contains("pomazánka") -> {
                "1. Očistěte čerstvé suroviny ($ingList) pod tekoucí studenou vodou a důkladně je osušte papírovým ubrouskem. Zeleninu, maso, uzeninu či sýry nakrájejte na stejně velké drobné kostičky, případně nastrouhejte najemno.\n" +
                "2. V čisté skleněné míse suroviny smíchejte se zbylými ingrediencemi (změklým máslem, lehkou majonézou, tatarkou nebo zakysanou smetanou). V případě tataráku hovězí maso pečlivě naškrábejte ostrým nožem a vymíchejte s kořením.\n" +
                "3. Směs promíchávejte lžící po dobu cca 2-3 minut tak, aby se všechny chutě rovnoměrně a harmonicky propojily. Přidejte sůl, pepř a případně pár kapek citronové šťávy.\n" +
                "4. Hotový salát nebo pomazánku přikryjte potravinovou fólií a nechte odležet v chladničce při stabilní teplotě 4-6 °C po dobu minimálně 30 až 60 minut před samotným podáváním. Servírujte s křupavým pečivem."
            }
            category == "piti" || t.contains("čaj") || t.contains("limonáda") || t.contains("džus") || t.contains("mošt") || t.contains("mojito") || t.contains("koktejl") || t.contains("smoothie") || t.contains("kakao") || t.contains("lassi") || t.contains("káva") -> {
                "1. Připravte suché i tekuté suroviny ($ingList). Citrusy a ovoce důkladně omyjte pod tekoucí vodou, odstraňte případné pecky a vymačkejte z nich čerstvou šťávu.\n" +
                "2. Pro studené drinky / smoothie vložte ingredience s hrstí drceného ledu do shakeru nebo mixéru a mixujte vysokou rychlostí po dobu 30-40 sekund, dokud nezískáte nadýchanou, dokonale hladkou konzistenci bez kousků.\n" +
                "3. Pro horké osvěžující nápoje (čaj, čokoládu, svařený mošt) zahřívejte suroviny s kořením v kastrůlku na mírném plameni na teplotu cca 80-85 °C po dobu 6 až 8 minut. Tekutina nesmí přejít v prudký var, aby se neodpařilo jemné aroma.\n" +
                "4. Nápoje rozlijte do vysokých sklenic nebo hřejivých hrnků, dozdobte čerstvě natrhaným lístkem máty, plátkem citronu nebo poprašte skořicí a ihned podávejte."
            }
            else -> {
                "1. Připravte si tyto čerstvé suroviny: $ingList a očistěte je pod tekoucí vodou.\n" +
                "2. Všechny ingredience očistěte, nakrájejte a postupně uvařte v hrnci, poduste na pánvi nebo upeče v troubě podle tradičního postupu po dobu 20-45 minut na středním ohni, dokud nebudou dokonale křehké, měkké a lahodné.\n" +
                "3. Dochuťte solí, pepřem, bylinkami a čerstvým kořením. Podávejte teplé a ozdobené podle vlastní chuti!"
            }
        }
    }

    private fun getCustomInstructionsSK(title: String, category: String, ingredients: List<String>): String {
        val t = title.lowercase()
        val ingList = ingredients.joinToString(", ")
        return when {
            category == "obed" && (t.contains("polievka") || t.contains("vývar") || t.contains("kulajda") || t.contains("krém") || t.contains("kapustnica") || t.contains("kyselo") || t.contains("demikát")) -> {
                "1. Pripravte si suroviny ($ingList). Očistite koreňovú zeleninu a mäso, nakrájajte na rovnomerné kúsky. Cibuľku orestujte v hrnci na rozpustenom masle po dobu 5 minút.\n" +
                "2. Pridajte ostatné suroviny a zalejte 1.5 litrom studenej vody či pripraveného vývaru. Priveďte k varu, potom plameň okamžite stiahnite na minimum.\n" +
                "3. Polievku pozvoľna varte (tiahnite) pri miernej teplote cca 90-95 °C po dobu 40-50 minút (v prípade poctivého hovädzieho/slepačieho masového vývaru nechajte tiahnuť na miernom ohni minimálne 2 až 3 hodiny).\n" +
                "4. Dochuťte soľou, čerstvo mletým čiernym korením, prelisovaným cesnakom, majoránom alebo bylinkami. Ak pripravujete krém, rozmixujte na záver tyčovým mixérom a zjemnite 100 ml sladkej smotany (31%)."
            }
            t.contains("pečen") || t.contains("bábovka") || t.contains("koláč") || t.contains("štrúdl") || t.contains("závin") || t.contains("sekan") || t.contains("kač") || t.contains("hus") || t.contains("rebier") || t.contains("perník") || t.contains("zapečen") || t.contains("šunkofleky") || t.contains("bôčik") || t.contains("koleno") || t.contains("sekaná") -> {
                "1. Pripravte mäso či zeleninu ($ingList). Mäso očistite, opláchnite a dôkladne osušte papierovým obrúskom. Votrite soľ, mletú rascu a roztlačený cesnak zo všetkých strán a vložte do pekáča s nakrájanou cibuľou a kúskami bravčovej masti.\n" +
                "2. Pekáč zakryte vekom alebo alobalom a pečte v vopred vyhriatej rúre pri teplote 180 °C po dobu 60 až 90 minút, kým mäso nie je dokonale krehké. Priebežne mäso kontrolujte a podlievajte horúcim výpekom.\n" +
                "3. Počas pečenia každých 20 minút prepichajte ihlicou a prelievajte lyžicou teplého výpeku. Posledných 15 minút zložte veko, zvýšte teplotu rúry na 210 °C a dopečte dozlatista, aby koža či povrch získali neodolateľnú chrumkavú kôrku.\n" +
                "4. Hotové pečené mäso nechajte 5-10 minút odpočinúť pod alobalom, nakrájajte ostrým nožom na plátky priečne cez vlákno a podávajte teplé s výpekom."
            }
            t.contains("vyprážan") || t.contains("rezeň") || t.contains("fašírk") || t.contains("hranolky") || t.contains("placky") || t.contains("syr") || t.contains("stripsy") || t.contains("placky") -> {
                "1. Pripravte si suroviny ($ingList). Rezne či zeleninu jemne naklepte na hrúbku cca 1.5 cm. Osoľte zo všetkých strán. Obaľte postupne v hladkej múke, v rozšľahaných vajíčkach s kvapkou mlieka a nakoniec v čerstvej strúhanke.\n" +
                "2. V hlbokej panvici rozohrejte hrubšiu vrstvu masti alebo slnečnicového oleja (cca 2 cm) na teplotu 170-180 °C (správnu teplotu otestujete vhodením štipky strúhanky – musí hneď začať bublať).\n" +
                "3. Smažte po dobu 4 až 6 minút z každej strany dozlatista, plameň udržujte stabilne stredný. Kúsky syra smažte kratšiu dobu, cca 1.5 až 2 minúty pri mierne vyššej teplote, aby sa syr rozpustil, ale nevytiekol.\n" +
                "4. Hotové vyprážané kúsky vyberte z panvice a nechajte prebytočný tuk odkvapkať na papierovom obrúsku. Podávajte teplé s plátkom čerstvého citróna."
            }
            t.contains("guláš") || t.contains("omáčk") || t.contains("sviečkov") || t.contains("rajsk") || t.contains("kôprov") || t.contains("segedín") || t.contains("tokáň") || t.contains("soté") || t.contains("vtáček") -> {
                "1. Pripravte si základné suroviny ($ingList). Nakrájajte cibuľu najjemno. V hrnci rozohrejte lyžicu masti a cibuľu restujte dozlatista (pri guláši až do tmavohnedej farby, cca 10 minút). Pridajte kocky mäsa, osoľte, okoreňte a nechajte mäso zo všetkých strán 5 minút zatiahnuť.\n" +
                "2. Prisypte mletú sladkú papriku (prípadne ďalšie korenie) a rýchlo premiešajte, aby nezhorkla. Ihneď zalejte teplou vodou alebo vývarom tak, aby mäso bolo takmer ponorené.\n" +
                "3. Zakryte hrniec pokrievkou a za občasného miešania duste na miernom plameni pri teplote cca 95 °C po dobu 50 až 75 minút, kým nie je mäso krásne krehké a mäkké. Podľa potreby podlievajte teplým vývarom.\n" +
                "4. Omáčku zahustite svetlou zápražkou (z masla a múky) alebo hladkou múkou rozmiešanou v mlieku či smotane. Nechajte na miernom plameni prevariť ešte 10-15 minút. Na záver dochuťte soľou, štipkou cukru, citrónovou šťavou či octom podľa typu omáčky."
            }
            category == "dezert" && (t.contains("bábovka") || t.contains("závin") || t.contains("štrúdl") || t.contains("koláč") || t.contains("perník") || t.contains("buchty") || t.contains("vánočka") || t.contains("mak") || t.contains("linecký") || t.contains("dort") || t.contains("větrník") || t.contains("laskonky") || t.contains("beránek") || t.contains("vianočka") || t.contains("baránok") || t.contains("recept")) -> {
                "1. Pripravte sypké a tekuté suroviny ($ingList). Vo väčšej mise vyšľahajte zmäknuté maslo či žĺtky s cukrom do svetlej peny. Postupnie primiešajte múku rozmixovanú s kypriacim práškom (či droždie u kysnutých ciest) a vlejte vlažné mlieko. Opatrne ručne zapracujte vyšľahaný sneh z vaječných bielkov.\n" +
                "2. Cesto dôkladne prehneťte, alebo vlejte do formy na bábovku/baránka (ktorú vopred vymažete maslom a vysypete hrubou múkou či krupicou) alebo rozprestrite na plech vyložený papierom na pečenie.\n" +
                "3. Vložte do stabilne vyhriatej rúre na 170-180 °C. Pečte po dobu 30 až 45 minút (u kysnutých ciest a báboviek dĺžku zarovnajte na 45 minút, u závinov stačí cca 25 minút). Propečenost overte zapichnutím drevenej špajle do stredu – po vytiahnutí musí vyjsť suchá.\n" +
                "4. Po upečení nechajte múčnik úplne vychladnúť na mriežke. Pred krájaním na rovnomerné porcie jemne pocukrujte práškovým cukrom s mletou vanilkou alebo prelejte čokoládovou polevou."
            }
            category == "dezert" || t.contains("palacink") || t.contains("kaša") || t.contains("knedle") || t.contains("dolky") || t.contains("wafle") || t.contains("krupic") || t.contains("žemľovka") || t.contains("kuba") || t.contains("nákyp") -> {
                "1. Pripravte základné suroviny ($ingList). Do misky preosejte polohrubú múku, pridajte štipku soli, cukor, vajce a vlažné mlieko. Vyšľahajte hladké riedke cesto na palacinky alebo lievance (nechajte 10 minút odpočinúť v chlade), prípadne vypracujte pevné tvarohové či krupicové cesto.\n" +
                "2. Na rozpálenú panvicu potretú tenkou vrstvou masti alebo oleja nalejte naberačku cesta. Palacinky smažte na prudšom ohni presne 1.5 minúty z jednej strany, potom otočte a smažte ďalších 45-60 sekúnd dozlatista.\n" +
                "3. V prípade ovocných/tvarohových knedlí vložte vytvarované gule do vriacej mierne osolenej vody. Varte po dobu 8 až 10 minút na miernom ohni a občas jemne nadvihnite drevenou varechou odo dňa.\n" +
                "4. Hotové palacinky namažte džemom alebo tvarohom a srolujte. Knedle či sladké nákypy rozdelte na taniere, bohato posypte mletým makom, kakaom či nastrúhaným tvarohom, a prelejte rozpusteným teplým maslom."
            }
            category == "vecere" || t.contains("šalát") || t.contains("tatar") || t.contains("nátierka") || t.contains("chlieb") || t.contains("caprese") || t.contains("toast") || t.contains("pomazánka") || t.contains("utopenci") || t.contains("hermelín") || t.contains("chlebíčky") || t.contains("tlačenka") -> {
                "1. Očistite čerstvé suroviny ($ingList) pod tečúcou studenou vodou a dôkladne ich osušte papierovým obrúskom. Zeleninu, mäso, údeninu či syry nakrájajte na rovnako veľké drobné kocky, prípadne nastrúhajte najjemno.\n" +
                "2. V čistej sklenenej mise suroviny zmiešajte so zvyšnými ingredienciami (zmäknutým maslom, ľahkou majonézou, tatárskou omáčkou alebo kyslou smotanou). V prípade tataráku hovädzie mäso starostlivo naškriabte ostrým nožom a vymiešajte s korením.\n" +
                "3. Zmes premiešavajte lyžicou po dobu cca 2-3 minút tak, aby sa všetky chute rovnomerne a harmonicky prepojili. Pridajte soľ, korenie a prípadne pár kvapiek citrónovej šťavy.\n" +
                "4. Hotový šalát alebo nátierku prikryte potravinovou fóliou a nechajte odležať v chladničke pri stabilnej teplote 4-6 °C po dobu minimálne 30 až 60 minút pred samotným podávaním. Servírujte s chrumkavým pečivom."
            }
            category == "piti" || t.contains("čaj") || t.contains("limonáda") || t.contains("džús") || t.contains("mušt") || t.contains("mojito") || t.contains("koktejl") || t.contains("smoothie") || t.contains("kakao") || t.contains("lassi") || t.contains("káva") -> {
                "1. Pripravte suché aj tekuté suroviny ($ingList). Citrusy a ovocie dôkladne umyte pod tečúcou vodou, odstráňte prípadné kôstky a vytlačte z nich čerstvú šťavu.\n" +
                "2. Pre studené drinky / smoothie vložte ingrediencie s hrsťou drveného ľadu do shakeru alebo mixéra a mixujte vysokou rýchlosťou po dobu 30-40 sekúnd, kým nezískate nadýchanú, dokonale hladkú konzistenciu bez kúskov.\n" +
                "3. Pre horúce osviežujúce nápoje (čaj, čokoládu, varený mušt) zahrievajte suroviny s korením v hrnci na miernom plameni na teplotu cca 80-85 °C po dobu 6 až 8 minút. Tekutina nesmie prejsť do prudkého varu, aby sa neodparila jemná aróma.\n" +
                "4. Nápoje rozlejte do vysokých pohárov alebo hrejivých hrnčekov, dozdobte čerstvo natrhnutým lístkom mäty, plátkom citróna alebo posypte škoricou a ihneď podávajte."
            }
            else -> {
                "1. Pripravte si tieto čerstvé suroviny: $ingList a očistite ich pod tečúcou vodou.\n" +
                "2. Všetky ingrediencie očistite, nakrájajte a postupne uvarte v hrnci, poduste na panvici alebo upečte v rúre podla tradičného postupu po dobu 20-45 minút na strednom ohni, kým nebudú dokonale krehké, mäkké a lahodné.\n" +
                "3. Dochuťte soľou, korením, bylinkami a čerstvým korením. Podávajte teplé a ozdobené podľa vlastnej chuti!"
            }
        }
    }

    private fun generateIngredientsCZ(title: String): List<String> {
        val t = title.lowercase()
        val list = mutableListOf<String>()
        if (t.contains("hovězí") || t.contains("svíčková") || t.contains("roštěná") || t.contains("tatarák") || t.contains("líčka")) list.add("hovězí maso")
        if (t.contains("vepřov") || t.contains("výpečky") || t.contains("kotlety") || t.contains("bůček") || t.contains("panenka") || t.contains("sekaná") || t.contains("karbanátky")) list.add("vepřové maso")
        if (t.contains("kuřec") || t.contains("kuře") || t.contains("stripsy")) list.add("kuřecí maso")
        if (t.contains("krůtí")) list.add("krůtí maso")
        if (t.contains("kachna") || t.contains("kachní")) list.add("kachní maso")
        if (t.contains("husa") || t.contains("husí")) list.add("husí maso")
        if (t.contains("ryb") || t.contains("kapr") || t.contains("pstruh") || t.contains("sumec") || t.contains("candát") || t.contains("filé")) list.add("ryba")
        if (t.contains("brambor") || t.contains("brambory") || t.contains("bramboráky") || t.contains("cmunda") || t.contains("šišky") || t.contains("lepenic")) list.add("brambory")
        if (t.contains("zelí") || t.contains("zelňačka") || t.contains("kapustnica")) list.add("zelí")
        if (t.contains("sýr") || t.contains("hermelín") || t.contains("tvarůžky") || t.contains("nivu")) list.add("sýr")
        if (t.contains("šunka") || t.contains("šunkofleky") || t.contains("závitek")) list.add("šunka")
        if (t.contains("slanina") || t.contains("výpečky")) list.add("slanina")
        if (t.contains("párek") || t.contains("párkem")) list.add("párek")
        if (t.contains("klobás")) list.add("klobása")
        if (t.contains("jater") || t.contains("játra") || t.contains("játrovými")) list.add("játra")
        if (t.contains("houb") || t.contains("hříbk") || t.contains("žampion") || t.contains("kulajda") || t.contains("kuba")) list.add("houby")
        if (t.contains("zelenina") || t.contains("zelenou") || t.contains("vývar") || t.contains("mrkvi") || t.contains("mrkev") || t.contains("brokolice") || t.contains("květák")) list.add("zelenina")
        if (t.contains("mák") || t.contains("mákem")) list.add("mák")
        if (t.contains("tvaroh") || t.contains("žemlovka")) list.add("tvaroh")
        if (t.contains("vejce") || t.contains("vajíčková") || t.contains("lečo") || t.contains("mozek")) list.add("vejce")
        if (t.contains("káva")) list.add("káva")
        if (t.contains("čaj") || t.contains("matcha") || t.contains("šípkový")) list.add("čaj")
        if (t.contains("limonáda") || t.contains("citronáda") || t.contains("džus") || t.contains("mošt") || t.contains("možito") || t.contains("mojito")) list.add("voda")
        if (t.contains("čokoláda") || t.contains("kakao")) list.add("mléko")
        if (t.contains("jablk") || t.contains("jablečný") || t.contains("závin") || t.contains("štrúdl") || t.contains("mošt") || t.contains("žemlovka")) list.add("jablka")
        if (t.contains("jahod") || t.contains("ostružin") || t.contains("broskv") || t.contains("mango") || t.contains("ovoc")) list.add("ovoce")
        if (list.size < 3) {
            list.add("cibule")
            list.add("máslo")
            list.add("sůl")
        }
        return list.distinct()
    }

    private fun generateIngredientsSK(title: String): List<String> {
        val t = title.lowercase()
        val list = mutableListOf<String>()
        if (t.contains("hovädz") || t.contains("sviečkov") || t.contains("rošten") || t.contains("tatar") || t.contains("líčka")) list.add("hovädzie mäso")
        if (t.contains("bravč") || t.contains("výpečky") || t.contains("kotlet") || t.contains("bôčik") || t.contains("panenka") || t.contains("sekan") || t.contains("fašír")) list.add("bravčové mäso")
        if (t.contains("kurac") || t.contains("kura") || t.contains("stripsy")) list.add("kuracie mäso")
        if (t.contains("morč")) list.add("morčacie mäso")
        if (t.contains("kač")) list.add("kačacie mäso")
        if (t.contains("hus")) list.add("husacie mäso")
        if (t.contains("ryb") || t.contains("kapor") || t.contains("pstruh") || t.contains("sumec") || t.contains("zubáč") || t.contains("filé")) list.add("ryba")
        if (t.contains("zemiak") || t.contains("placky") || t.contains("šišky") || t.contains("lepenic")) list.add("zemiaky")
        if (t.contains("kapust") || t.contains("kapustnica")) list.add("kapusta")
        if (t.contains("syr") || t.contains("hermelín") || t.contains("tvarôžk") || t.contains("nivou")) list.add("syr")
        if (t.contains("šunka") || t.contains("šunkofleky") || t.contains("závitok")) list.add("šunka")
        if (t.contains("slanina") || t.contains("výpečky")) list.add("slanina")
        if (t.contains("párok") || t.contains("párkom")) list.add("párok")
        if (t.contains("klobás")) list.add("klobása")
        if (t.contains("pečeň") || t.contains("játra") || t.contains("pečeňový")) list.add("pečeň")
        if (t.contains("hub") || t.contains("dubák") || t.contains("šampiňón") || t.contains("kulajda") || t.contains("kuba")) list.add("huby")
        if (t.contains("zelenina") || t.contains("vývar") || t.contains("mrkv") || t.contains("brokolic") || t.contains("karfiol")) list.add("zelenina")
        if (t.contains("mak") || t.contains("makom")) list.add("mak")
        if (t.contains("tvaroh") || t.contains("žemľovka")) list.add("tvaroh")
        if (t.contains("vajc") || t.contains("nátierka") || t.contains("lečo") || t.contains("mozog")) list.add("vajce")
        if (t.contains("káva")) list.add("káva")
        if (t.contains("čaj") || t.contains("matcha") || t.contains("šípkov")) list.add("čaj")
        if (t.contains("limonád") || t.contains("citrónád") || t.contains("džús") || t.contains("mušt") || t.contains("mojito")) list.add("voda")
        if (t.contains("čokolád") || t.contains("kakao")) list.add("mlieko")
        if (t.contains("jablk") || t.contains("závin") || t.contains("štrúdl") || t.contains("mušt") || t.contains("žemľovka")) list.add("jablká")
        if (t.contains("jahod") || t.contains("ostružin") || t.contains("brosky") || t.contains("mango") || t.contains("ovoc")) list.add("ovocie")
        if (list.size < 3) {
            list.add("cibuľa")
            list.add("maslo")
            list.add("soľ")
        }
        return list.distinct()
    }
}
