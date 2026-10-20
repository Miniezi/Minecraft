# Переводчик сборок — автоматический русификатор Minecraft

Ветка minecraft. Независимый инструмент для создания ресурс-паков ru_ru из ZIP
с JAR-модами или Modrinth .mrpack (Minecraft 1.20–1.20.1).

Возможности: читает en_us.json и встроенный ru_ru.json из модов; сохраняет
существующий русский; переводит английский при наличии OPENAI_API_KEY;
использует контекст названия мода и ключа; кэширует результат с SHA256
английской строки; проверяет переменные %s, {player}, §a, URL и цвета;
записывает CSV-аудит; собирает проверенный ZIP ресурс-пака.
Chipped исключён по умолчанию.

ПРИМЕР: python "переводчик сборок/translate_modpack.py" --input mods.zip
С .mrpack: python "переводчик сборок/translate_modpack.py" --input pack.mrpack --download-mods
Без API: python "переводчик сборок/translate_modpack.py" --input mods.zip --audit-only
Тесты: python -m unittest discover -s "переводчик сборок" -p "test_*.py"

GitHub Actions: Translate Minecraft Modpack -> Run workflow (branch minecraft).
source_url: прямая публичная HTTPS-ссылка на ZIP или .mrpack.
download_mods: true для .mrpack, загружает JAR и сверяет SHA512.
Секрет OPENAI_API_KEY задаётся в Settings -> Secrets and variables -> Actions.

Файлы результата: build/modpack-ru.zip, build/audit.csv, build/summary.json,
translations/cache.json. Скачайте артефакт Actions, распакуйте, поместите
modpack-ru.zip в папку resourcepacks и активируйте его в игре.

ВНИМАНИЕ: язык в текстах вне assets/<modid>/lang/*.json не обрабатывается:
содержимое книг Patchouli, KubeJS, datapack, исходники программных диалогов и
текст, зашитый в Java-код, требуют отдельного сканирования и адаптеров.
Технические исключения — эвристика; их нужно просматривать вручную.
Успешная сборка ресурс-пака не гарантирует 100% перевода или игрового теста.
