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
    # Получение от клиента запроса на сверку данных - CheckInfo
    encCheckInfo = conn.recv(1000)
    сheckInfo = decryptAES(encCheckInfo)
    print(сheckInfo)
    # Уведомление клиента о получении запроса
    encOk = encryptAES("ok")
    conn.send(encOk)
    print("ok sended")
    # Получение от клиента номера чипа
    encChipId = conn.recv(1000)
    chipId = decryptAES(encChipId)
    print("CHIP:")
    print(chipId)
    # Отправка клиенту хеша картинки
    imgHash = b"bd1a1bf8d4180dff69e5e9f72980f16b72ac3e6c62f4ceb6e6f24a24e8242ef2"
    encImgHash = encryptAES(imgHash.decode('utf-8'))
    conn.send(encImgHash)
    # Получение от клиента остальной инфы
    encAllInfo = conn.recv(60000)
    allInfo = decryptAES(encAllInfo)
    print("REST INFO:")
    print(allInfo)
