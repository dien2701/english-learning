// Doc khung MP3 (Layer III) toi thieu de ghep nhieu file audio Tatoeba thanh mot bai nghe va
// do thoi luong, khong can ffmpeg. Ban rut gon cua apps/backend Mp3.java (khong can logic do-sync
// lai vi file nguon la audio thuc, khong phai TTS co the loi khung).
const BITRATES_KBPS = [
  [0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320],
  [0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160],
];
const SAMPLE_RATES = [
  [44100, 48000, 32000],
  [22050, 24000, 16000],
  [11025, 12000, 8000],
];
const ID3V1_LENGTH = 128;

function skipId3v2(buf) {
  if (buf.length >= 10 && buf[0] === 0x49 && buf[1] === 0x44 && buf[2] === 0x33) {
    const size = ((buf[6] & 0x7f) << 21) | ((buf[7] & 0x7f) << 14) | ((buf[8] & 0x7f) << 7) | (buf[9] & 0x7f);
    const footer = (buf[5] & 0x10) !== 0 ? 10 : 0;
    return Math.min(buf.length, 10 + size + footer);
  }
  return 0;
}

function readHeader(buf, pos, end) {
  if (pos + 4 > end || (buf[pos] & 0xff) !== 0xff || (buf[pos + 1] & 0xe0) !== 0xe0) return null;
  const version = (buf[pos + 1] >> 3) & 3; // 3 = MPEG-1, 2 = MPEG-2, 0 = MPEG-2.5, 1 = danh rieng
  const layer = (buf[pos + 1] >> 1) & 3; // 1 = Layer III
  const bitrateIndex = (buf[pos + 2] >> 4) & 0xf;
  const sampleRateIndex = (buf[pos + 2] >> 2) & 3;
  if (version === 1 || layer !== 1 || bitrateIndex === 0 || bitrateIndex === 15 || sampleRateIndex === 3) return null;
  const mpeg1 = version === 3;
  const bitrate = BITRATES_KBPS[mpeg1 ? 0 : 1][bitrateIndex] * 1000;
  const sampleRate = SAMPLE_RATES[version === 3 ? 0 : version === 2 ? 1 : 2][sampleRateIndex];
  const padding = (buf[pos + 2] >> 1) & 1;
  const length = Math.floor(((mpeg1 ? 144 : 72) * bitrate) / sampleRate) + padding;
  const mono = ((buf[pos + 3] >> 6) & 3) === 3;
  const crc = (buf[pos + 1] & 1) === 0;
  const sideInfo = mpeg1 ? (mono ? 17 : 32) : mono ? 9 : 17;
  return { length, samples: mpeg1 ? 1152 : 576, sampleRate, mono, infoOffset: 4 + (crc ? 2 : 0) + sideInfo };
}

function isInfoFrame(buf, pos, header) {
  return tagAt(buf, pos + header.infoOffset, 'Xing') || tagAt(buf, pos + header.infoOffset, 'Info') || tagAt(buf, pos + 36, 'VBRI');
}

function tagAt(buf, at, tag) {
  if (at + 4 > buf.length) return false;
  return buf.toString('latin1', at, at + 4) === tag;
}

/** @returns {{frames: Buffer, seconds: number, sampleRate: number, mono: boolean}} */
function parse(buf) {
  let pos = skipId3v2(buf);
  let end = buf.length;
  if (end - ID3V1_LENGTH >= pos && buf.toString('latin1', end - ID3V1_LENGTH, end - ID3V1_LENGTH + 3) === 'TAG') {
    end -= ID3V1_LENGTH;
  }
  const frames = [];
  let seconds = 0;
  let count = 0;
  let first = true;
  let firstHeader = null;
  while (pos + 4 <= end) {
    const h = readHeader(buf, pos, end);
    if (!h || pos + h.length > end) {
      pos++;
      continue;
    }
    if (first && isInfoFrame(buf, pos, h)) {
      first = false;
      pos += h.length;
      continue;
    }
    first = false;
    frames.push(buf.subarray(pos, pos + h.length));
    seconds += h.samples / h.sampleRate;
    if (!firstHeader) firstHeader = h;
    count++;
    pos += h.length;
  }
  if (count === 0) throw new Error('Khong co khung MP3 hop le');
  return { frames: Buffer.concat(frames), seconds, sampleRate: firstHeader.sampleRate, mono: firstHeader.mono };
}

/** Noi nhieu doan MP3 (bo qua doan khac tan so lay mau/so kenh voi doan dau). */
function join(buffers) {
  if (buffers.length === 0) throw new Error('Khong co doan nao de ghep');
  const parts = [];
  let seconds = 0;
  let first = null;
  for (const buf of buffers) {
    let parsed;
    try {
      parsed = parse(buf);
    } catch {
      continue; // bo qua file hong, khong chan ca bai
    }
    if (!first) {
      first = parsed;
    } else if (parsed.sampleRate !== first.sampleRate || parsed.mono !== first.mono) {
      continue; // khac dinh dang voi doan dau -> bo qua de tranh ram/nhieu
    }
    parts.push(parsed.frames);
    seconds += parsed.seconds;
  }
  if (parts.length === 0) throw new Error('Khong co doan MP3 hop le nao de ghep');
  return { data: Buffer.concat(parts), seconds };
}

export const Mp3 = { parse, join };
