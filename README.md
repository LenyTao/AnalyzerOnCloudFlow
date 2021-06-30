Запуск прроекта:

1) Вводим sbt в консоле чтобы перейти в режим управления
2) Вводим avroScalaGenerateSpecific - чтобы сгенерировать кейс класс для передачи сообщений. 
3) После генерации заходим в папку datamodel -> target -> scala-2.12 -> scala_avro. На папке scala_avro нажимаем правой кнопокй мыши 
и выбираем Mark Directory as и выбираем Generated Sources Root. После чего идея видит классы.
4) Вводим verifyBlueprint для проверки корректности схемы blueprint.
5) Вводим runLocal - для полного запуска пайплайна.
6) Ищем логи в пути: C:\Users\userName\AppData\Local\Temp\cloudflow-local-run123...
