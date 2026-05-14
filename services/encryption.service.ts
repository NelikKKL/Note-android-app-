import * as Crypto from 'expo-crypto';

const ENCRYPTION_KEY = 'notes-app-encryption-key-2025';

/**
 * Генерирует SHA-256 хеш из строки
 */
async function generateHash(text: string): Promise<string> {
  const hash = await Crypto.digestStringAsync(
    Crypto.CryptoDigestAlgorithm.SHA256,
    text + ENCRYPTION_KEY
  );
  return hash;
}

/**
 * Простое шифрование текста (XOR cipher с хешированным ключом)
 */
export async function encryptText(text: string): Promise<string> {
  if (!text) return '';
  
  try {
    const keyHash = await generateHash(ENCRYPTION_KEY);
    const keyBytes = keyHash.split('').map(c => c.charCodeAt(0));
    
    let encrypted = '';
    for (let i = 0; i < text.length; i++) {
      const charCode = text.charCodeAt(i);
      const keyByte = keyBytes[i % keyBytes.length];
      const encryptedChar = charCode ^ keyByte;
      encrypted += String.fromCharCode(encryptedChar);
    }
    
    // Конвертируем в base64 для безопасного хранения
    return btoa(unescape(encodeURIComponent(encrypted)));
  } catch (error) {
    console.error('Ошибка шифрования:', error);
    return text;
  }
}

/**
 * Расшифровка текста
 */
export async function decryptText(encryptedText: string): Promise<string> {
  if (!encryptedText) return '';
  
  try {
    const keyHash = await generateHash(ENCRYPTION_KEY);
    const keyBytes = keyHash.split('').map(c => c.charCodeAt(0));
    
    // Декодируем из base64
    const encrypted = decodeURIComponent(escape(atob(encryptedText)));
    
    let decrypted = '';
    for (let i = 0; i < encrypted.length; i++) {
      const charCode = encrypted.charCodeAt(i);
      const keyByte = keyBytes[i % keyBytes.length];
      const decryptedChar = charCode ^ keyByte;
      decrypted += String.fromCharCode(decryptedChar);
    }
    
    return decrypted;
  } catch (error) {
    console.error('Ошибка расшифровки:', error);
    return encryptedText;
  }
}
