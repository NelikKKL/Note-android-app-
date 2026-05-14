import AsyncStorage from '@react-native-async-storage/async-storage';
import { encryptText, decryptText } from './encryption.service';

const NOTES_STORAGE_KEY = '@notes_encrypted';

export interface Note {
  id: string;
  title: string;
  content: string;
  createdAt: number;
  updatedAt: number;
}

/**
 * Загрузить все заметки из хранилища
 */
export async function loadNotes(): Promise<Note[]> {
  try {
    const encryptedData = await AsyncStorage.getItem(NOTES_STORAGE_KEY);
    if (!encryptedData) {
      return [];
    }
    
    const decryptedData = await decryptText(encryptedData);
    return JSON.parse(decryptedData);
  } catch (error) {
    console.error('Ошибка загрузки заметок:', error);
    return [];
  }
}

/**
 * Сохранить все заметки в хранилище
 */
export async function saveNotes(notes: Note[]): Promise<void> {
  try {
    const jsonData = JSON.stringify(notes);
    const encryptedData = await encryptText(jsonData);
    await AsyncStorage.setItem(NOTES_STORAGE_KEY, encryptedData);
  } catch (error) {
    console.error('Ошибка сохранения заметок:', error);
    throw error;
  }
}

/**
 * Создать новую заметку
 */
export async function createNote(title: string, content: string): Promise<Note> {
  const note: Note = {
    id: Date.now().toString(),
    title: title || 'Без названия',
    content,
    createdAt: Date.now(),
    updatedAt: Date.now(),
  };
  
  const notes = await loadNotes();
  notes.unshift(note);
  await saveNotes(notes);
  
  return note;
}

/**
 * Обновить существующую заметку
 */
export async function updateNote(id: string, title: string, content: string): Promise<void> {
  const notes = await loadNotes();
  const index = notes.findIndex(n => n.id === id);
  
  if (index !== -1) {
    notes[index] = {
      ...notes[index],
      title: title || 'Без названия',
      content,
      updatedAt: Date.now(),
    };
    await saveNotes(notes);
  }
}

/**
 * Удалить заметку
 */
export async function deleteNote(id: string): Promise<void> {
  const notes = await loadNotes();
  const filtered = notes.filter(n => n.id !== id);
  await saveNotes(filtered);
}
