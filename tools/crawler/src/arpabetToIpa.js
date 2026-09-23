const CONSONANTS = {
  B: 'b', CH: 'tʃ', D: 'd', DH: 'ð', F: 'f', G: 'ɡ', HH: 'h', JH: 'dʒ', K: 'k',
  L: 'l', M: 'm', N: 'n', NG: 'ŋ', P: 'p', R: 'r', S: 's', SH: 'ʃ', T: 't',
  TH: 'θ', V: 'v', W: 'w', Y: 'j', Z: 'z', ZH: 'ʒ',
};

// AH0/ER0 la quy uoc CMUdict cho nguyen am giam (schwa); cac muc stress khac dung ky hieu day du.
const VOWELS = {
  AA: 'ɑ', AE: 'æ', AH: 'ʌ', AO: 'ɔ', AW: 'aʊ', AY: 'aɪ', EH: 'ɛ', ER: 'ɜr',
  EY: 'eɪ', IH: 'ɪ', IY: 'i', OW: 'oʊ', OY: 'ɔɪ', UH: 'ʊ', UW: 'u',
};
const REDUCED_VOWELS = { AH: 'ə', ER: 'ɚ' };

const STRESS_MARK = { 1: 'ˈ', 2: 'ˌ' };

/**
 * Chuyen mot chuoi phien am ARPAbet (CMUdict, vd tu Datamuse "pron:" tag) sang IPA gan dung.
 * Vi ARPAbet la day am vi phang, khong co ranh gioi am tiet, dau nhan duoc chen ngay truoc
 * nguyen am mang stress thay vi dau am tiet chuan - du dung de hien thi cho nguoi hoc.
 * Tra ve nguyen chuoi dau vao neu khong nhan dang duoc dinh dang ARPAbet (vd da la IPA).
 */
export function arpabetToIpa(phonetic) {
  if (!phonetic) return phonetic;
  const trimmed = phonetic.trim();
  const inner = trimmed.replace(/^\/|\/$/g, '');
  const tokens = inner.split(/\s+/).filter(Boolean);
  const isArpabet = tokens.length > 0 && tokens.every((t) => /^[A-Z]{1,3}[012]?$/.test(t));
  if (!isArpabet) return phonetic;

  // Gom phu am dung truoc vao cung am tiet voi nguyen am theo sau (onset), roi moi chen dau
  // nhan truoc CA am tiet - tranh dau nhan roi vao giua cum phu am nhu "/bˈʊk/" cho tu 1 am tiet.
  let result = '';
  let onset = '';
  for (const token of tokens) {
    const match = token.match(/^([A-Z]+)([012])?$/);
    const base = match[1];
    const stress = match[2];
    if (stress !== undefined) {
      const symbol = stress === '0' && REDUCED_VOWELS[base] ? REDUCED_VOWELS[base] : VOWELS[base];
      if (!symbol) return phonetic; // ky hieu la, khong chac chan -> giu nguyen ban goc
      result += (STRESS_MARK[stress] || '') + onset + symbol;
      onset = '';
    } else {
      const symbol = CONSONANTS[base];
      if (!symbol) return phonetic;
      onset += symbol;
    }
  }
  result += onset; // phu am cuoi tu (coda) khong di truoc nguyen am nao nua
  return `/${result}/`;
}
