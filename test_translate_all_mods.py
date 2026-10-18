import unittest
from unittest.mock import patch
from translate_all_mods import protected_tokens, complex_valid, translate_safe, translate_resilient
import time

class ComplexTranslationTests(unittest.TestCase):
    def test_format_tokens(self):
        original="Hello %1$s, §a{player}! <bold>Victory</bold>\\n"
        result="Привет %1$s, §a{player}! <bold>Победа</bold>\\n"
        self.assertEqual(protected_tokens(original),protected_tokens(result))
        self.assertTrue(complex_valid(original,result))
        self.assertFalse(complex_valid(original,"Привет %s, §a{player}!"))

    def test_masked_translation(self):
        source="Welcome, {name}! You've earned %1$d hearts."
        def fake(x):
            return x.replace("Welcome", "Добро пожаловать").replace("You've earned", "Ты получил").replace("hearts","сердец")
        with patch("translate_all_mods.translated",side_effect=fake):
            self.assertIn("{name}",translate_safe(source))
            self.assertIn("%1$d",translate_safe(source))

    def test_separator_failure_falls_back(self):
        entries=[("a","Village"),("b","Blacksmith")]
        def fake(x):
            if "🟩" in x:return "сломанный разделитель"
            return {"Village":"Деревня","Blacksmith":"Кузнец"}.get(x,x)
        counter={"batch_attempts":0,"batch_failures":0,"individual_attempts":0,"individual_failures":0}
        with patch("translate_all_mods.translated",side_effect=fake):
            result=translate_resilient(entries,time.monotonic()+10,counter)
        self.assertEqual(result,{"a":"Деревня","b":"Кузнец"})
        self.assertGreater(counter["individual_attempts"],0)

    def test_preserve_chipped_exclusion(self):
        from translate_all_mods import SKIP_NAMESPACES,SKIP_JARS
        self.assertIn("chipped",SKIP_NAMESPACES)
        self.assertTrue(any("chipped-3.0.7.jar".startswith(x) for x in SKIP_JARS))

if __name__=="__main__":unittest.main()
