Pro android >= 5.0
Nemusi byt rooted device.

Pro android < 5.0 
Root device, jiny script.

apk ... apk soubory potrebne pro provedeni monkey testu
jar ... vygenerovana jar aplikace, ktera spousti a zastavuje nahravani
record ... zdrojove soubory aplikace Record
src ... zdrojove soubory - monkey testy

logs ... logovaci soubry testu - vysledky + video


v record/ 
	$ ant build (vytvori jar soubor record.jar v record/bin/)
v record/ 
	$ant install (nahraje soubor do zarizeni)

v korenovem adresari projektu
	$ sh install.sh (nainstaluje aplikaci pro nahravani videa a BeeeOn aplikaci do zarizeni, pushne jar soubor aplikace Record do zarizeni)
	$ sh src/monkey_d_small.sh [seed]

v logs/ se objevi logovaci soubory a video
