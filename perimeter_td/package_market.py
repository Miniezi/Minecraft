import io,json,sys
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import defense,upgrade,Spore_Perimeter,package_td
upgrade.install()
import arsenal_campaign
arsenal_campaign.install()
import market
market.install()
Spore_Perimeter.EXTRA.update({1174200:6066208,1356005:8497753})
base=io.BytesIO()
package_td.assemble(Path('inf.zip'),base)
with ZipFile(base) as src, ZipFile('Perimeter-Arsenal-0.5.zip','w',ZIP_DEFLATED) as dest:
    m=json.loads(src.read('manifest.json'))
    m.update(name='Периметр — Осада',version='0.5.0')
    folder=m.get('overrides','overrides')+'/mods/'
    dest.writestr('manifest.json',json.dumps(m,ensure_ascii=False))
    for entry in src.infolist():
        if entry.filename!='manifest.json':dest.writestr(entry,src.read(entry))
    dest.write(sys.argv[1],folder+'Perimeter-Siege-0.5.0.jar')
