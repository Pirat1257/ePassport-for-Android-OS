# ePassport application
 Cryptographic methods of information protection

Приложение, представляющее собой замену обычного электронного загран. паспорта.

Особенности:
- Аутентификация проверяющего проводится без раскрытия информациии (за исключением id чипа, который по легенде не даст злоумышленнику никакой важной информации);
- Между клиентом и сервером производится установка сеансового ключа, при помощи которого производится шифрование всей передаваемой информации;
- Аутентификация производится при помощи зашифрованного хеша фотографии;
- Производится 1 этап доказательства, вообще можно дать право клиенту генерировать запросы на получение хеша определенных рандомных полей;
- Вся информация хранится в базе данных SQlite в зашифрованном виде;
- Приложение запрашивает пароль, при помощи которого производтся шифрование и расшифрование информации, хранимой в базе данных.

Сетевое взаимодействие при проверке соединения:
1) Потом...

Полезные ссылки:
- https://www.javaer101.com/en/article/2650249.html;
- http://developer.alexanderklimov.ru/android/theory/asynctask.php;
- https://metanit.com/java/tutorial/6.1.php;
- https://www.youtube.com/watch?v=X7T0g5kBYJk;
- https://www.youtube.com/watch?v=LWFSGs4CG6I;
- https://vshivam.wordpress.com/2015/06/09/android-javascript-and-python-compatible-rsa-encryption/;
- https://stackoverflow.com/questions/29013414/encrypt-and-decrypt-by-aes-algorithm-in-both-python-and-android;
