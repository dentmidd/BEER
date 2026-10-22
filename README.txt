Ведмедик спить — Android-застосунок

ВАРІАНТ 1: онлайн-збірка через GitHub (без Android Studio)
1. Створіть акаунт на github.com і новий репозиторій (New repository).
2. Натисніть "uploading an existing file" і перетягніть ВМІСТ папки BearSleep
   (app, gradle, .github, build.gradle.kts тощо), а не саму папку.
   Папка .github має лежати в корені репозиторію.
3. Натисніть Commit changes.
4. Відкрийте вкладку Actions -> Build APK -> дочекайтесь зеленої галочки (2-5 хв).
5. Відкрийте завершений запуск -> внизу Artifacts -> BearSleep-apk. Розпакуйте, там app-debug.apk.

ВАРІАНТ 2: Android Studio
File -> Open -> BearSleep -> Build -> Build APK(s).

Встановлення на планшет: скопіюйте app-debug.apk, дозвольте встановлення з невідомих джерел.
