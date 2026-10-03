# Feudal Sovereign (suveren) — Minecraft 1.20.1 / Forge 47.x

Srednjeveski simulacijski mod: postanes kralj, si s krono, oznacujes ozemlje s
grbovnimi zastavami, naboras zveste viteze in kmete, dolocis zitnice ter
orozarne, poveljujes cetam prek vojne karte, in prezivis razbojniske napade.

## Namestitev (za razvijalca)

1. Prenesi **Forge 1.20.1 MDK** (npr. 1.20.1-47.2.0) s https://files.minecraftforge.net
2. Vsebino te mape (`src/`, `build.gradle`, `settings.gradle`) skopiraj v MDK projekt.
3. V terminalu: `gradlew genIntellijRuns` nato `gradlew runClient` (zahteva JDK 17).
4. Za server: `gradlew build` -> jar v `build/libs`.

## Kako se igra

1. **Kraljeva krona** (zlati grudice + rdece barvilo) — ZAKON moda:
   brez krone na glavi te podlozniki in vascani jemljejo za navadnega kmeta.
2. **Grbovna zastava** — postavitev ZAHTEVA chunk za svoje kraljestvo.
   V svojih dezelah se ponoci ne pojavljajo razbojniki.
3. **Kraljevo zezlo**:
   - desni klik na vascana -> novicen v VITEZA (ostane ti zvest za vedno),
   - desni klik na viteza -> menjava driznega nacina
     (sledi / straza / ostani / patrolja),
   - squat + desni klik na blok -> najblizji vitez dobi STRAZNO TOCKO.
4. **Listini** (papir + psenica / papir + zelezni mec): desni klik na SKRINJO
   jo doloci za ZITNICO ali OROZARNO. Vitezi tam jedo in si jemljejo mece,
   kmetje tam oddajajo davek (8 psenic) in jedo.
5. **Pogodba za kmeta** (papir + semena + zlat grudic): desni klik na vascana
   -> KMET: obira in saje psenico, ponoce spi doma, v ozki situaciji bezi.
6. **Vojna karta** (kompas + deske): desni klik v zrak -> pregled vseh tvojih
   vitezov in ukazi na daljavo (gumbi: Sledi / Straza / Ostani / Patrolja).
7. **Razbojniki** se ponoci zbirajo v neoznacenih dezelah in napadajo
   kmete, viteze in tebe. Njihov vodja nosi sekiro in zlato.

## Ze vgrajene 'pametne' stvari

- vitez, ki mu pristopis s krono, ti odpre vrata / vrata ograje / padalo v blizini,
  in jih zapre, ko odjes,
- ce je cilj dalec (>24 blokov), vitez sam zajame konja v okolici in ga jase,
  pred bojem pa sestopi,
- vitez sam odide po hrano v zitnico, ko je lačen, in v orozarno po mec,
  ko mu orozje peša,
- vitez brani svojega kralja: napade tistega, ki napada tebe,
- kmet obira le ZRELO psenico, saje le obdelano zemljo in oddaja davek,
- vse zveste entitete se nikoli ne despawnajo (setPersistenceRequired).

## Cestni zemljevid (faze 2+)

Cete s kapitani in enotnimi manevri, dvig mosta z rocico, samostreli,
grbovna delavnica (lova), cehi in stoppnje znanja, tuja kraljestva +
diplomacija (zaveznistva, dajatve), letni casi, revolte pri previsokih davkih.
