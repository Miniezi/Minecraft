import importlib.util
import io
import json
from pathlib import Path
import tempfile
import unittest
import zipfile

p=Path(__file__).with_name("translate_modpack.py")
spec=importlib.util.spec_from_file_location("translator",p)
t=importlib.util.module_from_spec(spec)
spec.loader.exec_module(t)

class TranslationTests(unittest.TestCase):
    def test_mask(self):
        s="Hello %1$s! {player} §a"
        masked,tokens=t.mask(s)
        self.assertEqual(t.unmask(masked,tokens),s)
        with self.assertRaises(ValueError):
            t.unmask("broken",tokens)

    def test_technical(self):
        self.assertTrue(t.is_technical("id","minecraft:stone")[0])
        self.assertFalse(t.is_technical("tip","Hello, adventurous villager!")[0])

    def test_zip(self):
        jar=io.BytesIO()
        with zipfile.ZipFile(jar,"w") as z:
            z.writestr("assets/testmod/lang/en_us.json",json.dumps({"item.test":"Test Sword"}))
            z.writestr("assets/testmod/lang/ru_ru.json",json.dumps({"item.test":"Тестовый меч"}))
        pack=io.BytesIO()
        with zipfile.ZipFile(pack,"w") as z:z.writestr("mods/test.jar",jar.getvalue())
        registry={}; names=set()
        t.collect_zip(pack.getvalue(),registry,names)
        self.assertEqual(registry["testmod"]["ru_ru"]["item.test"],"Тестовый меч")
        with tempfile.TemporaryDirectory() as d:
            output=Path(d)/"out.zip"
            t.write_pack(output,{"testmod":{"item.test":"Тестовый меч"}})
            self.assertTrue(zipfile.is_zipfile(output))

if __name__=="__main__":
    unittest.main()
