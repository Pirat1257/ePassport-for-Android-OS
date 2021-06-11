import socket

from Cryptodome.PublicKey import RSA
from Cryptodome.Cipher import AES
from Cryptodome.Cipher import  PKCS1_OAEP
from Cryptodome import Random
from Cryptodome.Hash import SHA256, SHA1
from Cryptodome.Signature import pss

from base64 import b64decode
from base64 import b64encode

import hashlib

ip = "192.168.0.106"
#ip = "127.0.0.1"
port = 8060

# RSA
# Генерация ключей RSA
def generate_RSA(bits):
    new_key = RSA.generate(bits)
    public_key = new_key.publickey().exportKey("PEM")
    private_key = new_key.exportKey("PEM")
    return private_key, public_key
# Шифрование RSA
def encryptRSA(key, plaintext):
    pubkey = RSA.importKey(b64decode(key))
    cipher = PKCS1_OAEP.new(pubkey, hashAlgo=SHA256)
    encrypted = cipher.encrypt(plaintext)
    return b64encode(encrypted)
# Расшифрование RSA
def decryptRSA(private_key, ciphertext):
    rsa_key = RSA.importKey(private_key)
    cipher = PKCS1_OAEP.new(rsa_key, hashAlgo=SHA256)
    decrypted = cipher.decrypt(b64decode(ciphertext))
    return decrypted

# AES
bs = 16
key = hashlib.sha256("Hello".encode()).digest()

def _pad(s):
    return s + (bs - len(s) % bs) * chr(bs - len(s) % bs)

def _unpad(s):
    return s[:-ord(s[len(s)-1:])]

# Зашифрование AES
def encryptAES(message):
    message = _pad(message)
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(key, AES.MODE_CBC, iv)
    return b64encode(iv + cipher.encrypt(message.encode()))
# Расшифрование AES
def decryptAES(enc):
    enc = b64decode(enc)
    iv = enc[:AES.block_size]
    cipher = AES.new(key, AES.MODE_CBC, iv)
    return _unpad(cipher.decrypt(enc[AES.block_size:])).decode('utf-8')

# Генерация ключ-пары
private_key, public_key = generate_RSA(bits = 2048)

pubKeyStr = public_key.decode("utf-8")
pubKeyStr = pubKeyStr.replace("-----BEGIN PUBLIC KEY-----\n", "")
pubKeyStr = pubKeyStr.replace("-----END PUBLIC KEY-----", "")

# Активируем прослушку
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind((ip, port))
sock.listen(10)
print('Server:', ip, port)

while (True):
    # Подключение клиента
    conn, addr = sock.accept()
    print('Connected: ', addr)
    # Получение от клиента запроса GiveKey
    giveKey = conn.recv(1000)
    print(giveKey)
    # Отправка клиенту публичного ключа
    conn.send(pubKeyStr.encode())
    print("PublicKey sended")
    # Получение от клиента сеансового ключа
    encryptedSessionKey = conn.recv(60000)
    session_key = decryptRSA(private_key, encryptedSessionKey)
    # Ставим новый сеансовый ключ
    key = hashlib.sha256(session_key).digest()
    # Отправка зашифрованного подтверждения
    encOk = encryptAES("ok")
    conn.send(encOk)
    print("ok sended")

    # Ожидание второго подключения клиента (вообще можно сохранять ip, чтобы 
    # дифференцировать имеешь ли ты с этим клиентом ключ или нет)
    conn, addr = sock.accept()
    # Получение от клиента запроса на сверку данных - GetInfo
    encGetInfo = conn.recv(1000)
    getInfo = decryptAES(encGetInfo)
    print(getInfo)
    # Отправка клиенту фотографии
    in_file = open("photo.jpg", "rb")
    data = in_file.read()
    in_file.close()
    size = len(data)
    strSize = str(size) + "\n"
    conn.sendall(strSize.encode('utf-8'))
    conn.sendall(data)
    # Получение подтверждения
    encOk = conn.recv(1000)
    ok = decryptAES(encOk)
    print(ok)
    # Отправка остальной информации
    chipId = "98732"
    documentType = "P"
    issuingState = "UK"
    documentNumber = "987654321"
    surname = "IVANOV"
    name = "IVAN"
    nationality = "UNITED KINGDOM"
    dateOfBirth = "15.09.1988"
    sex = "W"
    authority = "MVD 12345"
    dateOfExpiryOrValidUntilDate = "08.07.2028"
    sign1 = "sign1"
    sign2 = "sign2"
    allInfo = chipId + "\n" + documentType + "\n" + issuingState + "\n" + documentNumber + "\n" + surname + "\n" + name + "\n" + nationality + "\n" + dateOfBirth + "\n" + sex + "\n" + authority + "\n" + dateOfExpiryOrValidUntilDate + "\n" + sign1 + "\n" + sign2 + "\n"
    encAllInfo = encryptAES(allInfo)
    conn.sendall(encAllInfo)
    print("Complete")
