import type { DeckDetail, Flashcard } from '../../types/flashcard';
import type { Level } from '../../types/common';
import type { L10n } from '../../types/l10n';
import { topicName } from './topics';

/* Ảnh lấy từ Unsplash, cắt sẵn kích thước nhỏ để tải nhanh. Ảnh hỏng thì
   giao diện hiện khối dự phòng, không để trống chỗ. */
const photo = (id: string, w = 600) =>
  `https://images.unsplash.com/${id}?w=${w}&q=70&auto=format&fit=crop`;

/** Từ loại dùng lại nhiều lần, khai báo sẵn cho gọn. */
const POS = {
  noun: { vi: 'danh từ', en: 'noun' },
  verb: { vi: 'động từ', en: 'verb' },
  adj: { vi: 'tính từ', en: 'adjective' },
  adv: { vi: 'trạng từ', en: 'adverb' },
  nounVerb: { vi: 'danh từ, động từ', en: 'noun, verb' },
} satisfies Record<string, L10n>;

interface CardSeed {
  word: string;
  phonetic: string;
  /** vi: nghĩa tiếng Việt — en: định nghĩa bằng tiếng Anh. */
  meaning: L10n;
  pos: L10n;
  example: string;
  exampleVi: string;
  /** Mã ảnh Unsplash minh hoạ đúng nghĩa của từ. */
  photo: string;
}

function buildCards(deckId: string, seeds: CardSeed[]): Flashcard[] {
  return seeds.map((s, i) => ({
    id: `${deckId}-c${i + 1}`,
    word: s.word,
    phonetic: s.phonetic,
    meaning: s.meaning,
    partOfSpeech: s.pos,
    example: s.example,
    exampleMeaning: s.exampleVi,
    imageUrl: photo(s.photo, 480),
    audioUrl: null,
    recallLevel: null,
  }));
}

const OFFICE: CardSeed[] = [
  {
    word: 'invoice',
    phonetic: '/ˈɪn.vɔɪs/',
    meaning: { vi: 'hoá đơn', en: 'a document listing goods sold and the price owed' },
    pos: POS.noun,
    example: 'Please send the invoice before Friday.',
    exampleVi: 'Vui lòng gửi hoá đơn trước thứ Sáu.',
    photo: 'photo-1554224155-6726b3ff858f',
  },
  {
    word: 'deadline',
    phonetic: '/ˈded.laɪn/',
    meaning: { vi: 'hạn chót', en: 'the latest time by which something must be finished' },
    pos: POS.noun,
    example: 'We missed the deadline by two days.',
    exampleVi: 'Chúng tôi trễ hạn chót hai ngày.',
    photo: 'photo-1501139083538-0139583c060f',
  },
  {
    word: 'schedule',
    phonetic: '/ˈskedʒ.uːl/',
    meaning: { vi: 'lịch trình; lên lịch', en: 'a plan of when things will happen' },
    pos: POS.nounVerb,
    example: 'Let me schedule a meeting for next Monday.',
    exampleVi: 'Để tôi sắp lịch họp vào thứ Hai tới.',
    photo: 'photo-1506784983877-45594efa4cbe',
  },
  {
    word: 'negotiate',
    phonetic: '/nəˈɡoʊ.ʃi.eɪt/',
    meaning: { vi: 'đàm phán, thương lượng', en: 'to discuss in order to reach an agreement' },
    pos: POS.verb,
    example: 'They negotiated a better price with the supplier.',
    exampleVi: 'Họ đã thương lượng được giá tốt hơn với nhà cung cấp.',
    photo: 'photo-1521791136064-7986c2920216',
  },
  {
    word: 'reimbursement',
    phonetic: '/ˌriː.ɪmˈbɜːs.mənt/',
    meaning: { vi: 'sự hoàn tiền', en: 'money paid back for what you spent' },
    pos: POS.noun,
    example: 'Submit your receipts to claim reimbursement.',
    exampleVi: 'Nộp hoá đơn để yêu cầu hoàn tiền.',
    photo: 'photo-1554224154-26032ffc0d07',
  },
  {
    word: 'agenda',
    phonetic: '/əˈdʒen.də/',
    meaning: { vi: 'chương trình nghị sự', en: 'the list of items to discuss in a meeting' },
    pos: POS.noun,
    example: 'The agenda for the meeting has three items.',
    exampleVi: 'Chương trình cuộc họp có ba mục.',
    photo: 'photo-1454165804606-c3d57bc86b40',
  },
  {
    word: 'colleague',
    phonetic: '/ˈkɒl.iːɡ/',
    meaning: { vi: 'đồng nghiệp', en: 'a person you work with' },
    pos: POS.noun,
    example: 'My colleague will cover for me tomorrow.',
    exampleVi: 'Đồng nghiệp của tôi sẽ làm thay tôi ngày mai.',
    photo: 'photo-1522071820081-009f0129c71c',
  },
  {
    word: 'shipment',
    phonetic: '/ˈʃɪp.mənt/',
    meaning: { vi: 'lô hàng, chuyến hàng', en: 'a load of goods sent somewhere' },
    pos: POS.noun,
    example: 'The shipment arrives at the warehouse on Tuesday.',
    exampleVi: 'Lô hàng sẽ tới kho vào thứ Ba.',
    photo: 'photo-1566576912321-d58ddd7a6088',
  },
];

const FOOD: CardSeed[] = [
  {
    word: 'cuisine',
    phonetic: '/kwɪˈziːn/',
    meaning: { vi: 'ẩm thực, phong cách nấu ăn', en: 'a style of cooking from a place' },
    pos: POS.noun,
    example: 'Vietnamese cuisine is famous for its fresh herbs.',
    exampleVi: 'Ẩm thực Việt Nam nổi tiếng với rau thơm tươi.',
    photo: 'photo-1504674900247-0877df9cc836',
  },
  {
    word: 'savoury',
    phonetic: '/ˈseɪ.vər.i/',
    meaning: { vi: 'mặn, đậm đà', en: 'salty or spicy rather than sweet' },
    pos: POS.adj,
    example: 'I prefer savoury dishes to sweet ones.',
    exampleVi: 'Tôi thích món mặn hơn món ngọt.',
    photo: 'photo-1565299624946-b28f40a0ae38',
  },
  {
    word: 'nutritious',
    phonetic: '/njuːˈtrɪʃ.əs/',
    meaning: { vi: 'bổ dưỡng', en: 'containing substances the body needs to stay healthy' },
    pos: POS.adj,
    example: 'Beans are cheap and highly nutritious.',
    exampleVi: 'Các loại đậu vừa rẻ vừa rất bổ dưỡng.',
    photo: 'photo-1512621776951-a57141f2eefd',
  },
  {
    word: 'marinate',
    phonetic: '/ˈmær.ɪ.neɪt/',
    meaning: { vi: 'ướp (thịt, cá)', en: 'to soak food in a sauce before cooking' },
    pos: POS.verb,
    example: 'Marinate the chicken for at least an hour.',
    exampleVi: 'Ướp thịt gà ít nhất một tiếng.',
    photo: 'photo-1432139555190-58524dae6a55',
  },
  {
    word: 'leftovers',
    phonetic: '/ˈleft.oʊ.vərz/',
    meaning: { vi: 'đồ ăn thừa', en: 'food remaining after a meal' },
    pos: POS.noun,
    example: 'We had leftovers for lunch the next day.',
    exampleVi: 'Hôm sau chúng tôi ăn trưa bằng đồ ăn thừa.',
    photo: 'photo-1526470608268-f674ce90ebd4',
  },
  {
    word: 'appetite',
    phonetic: '/ˈæp.ɪ.taɪt/',
    meaning: { vi: 'sự thèm ăn, khẩu vị', en: 'the feeling of wanting to eat' },
    pos: POS.noun,
    example: 'Walking in the cold gave me a huge appetite.',
    exampleVi: 'Đi bộ trong trời lạnh làm tôi đói cồn cào.',
    photo: 'photo-1466637574441-749b8f19452f',
  },
];

const BASICS: CardSeed[] = [
  {
    word: 'borrow',
    phonetic: '/ˈbɒr.oʊ/',
    meaning: { vi: 'mượn', en: 'to take something and give it back later' },
    pos: POS.verb,
    example: 'Can I borrow your pen for a minute?',
    exampleVi: 'Tôi mượn bút của bạn một lát được không?',
    photo: 'photo-1455390582262-044cdead277a',
  },
  {
    word: 'neighbour',
    phonetic: '/ˈneɪ.bər/',
    meaning: { vi: 'hàng xóm', en: 'a person living next to or near you' },
    pos: POS.noun,
    example: 'Our neighbour looks after the cat when we travel.',
    exampleVi: 'Hàng xóm trông mèo giúp khi chúng tôi đi xa.',
    photo: 'photo-1570129477492-45c003edd2be',
  },
  {
    word: 'weather',
    phonetic: '/ˈweð.ər/',
    meaning: { vi: 'thời tiết', en: 'the state of the air: sun, rain, wind and so on' },
    pos: POS.noun,
    example: 'The weather is getting colder this week.',
    exampleVi: 'Thời tiết tuần này đang lạnh dần.',
    photo: 'photo-1504608524841-42fe6f032b4b',
  },
  {
    word: 'arrive',
    phonetic: '/əˈraɪv/',
    meaning: { vi: 'đến, tới nơi', en: 'to reach a place' },
    pos: POS.verb,
    example: 'The train arrives at half past six.',
    exampleVi: 'Tàu đến lúc sáu giờ rưỡi.',
    photo: 'photo-1474487548417-781cb71495f3',
  },
  {
    word: 'busy',
    phonetic: '/ˈbɪz.i/',
    meaning: { vi: 'bận rộn', en: 'having a lot to do' },
    pos: POS.adj,
    example: 'She is busy preparing for the exam.',
    exampleVi: 'Cô ấy đang bận ôn thi.',
    photo: 'photo-1484480974693-6ca0a78fb36b',
  },
];

const ACADEMIC: CardSeed[] = [
  {
    word: 'significant',
    phonetic: '/sɪɡˈnɪf.ɪ.kənt/',
    meaning: { vi: 'đáng kể, quan trọng', en: 'large or important enough to matter' },
    pos: POS.adj,
    example: 'There was a significant rise in car ownership.',
    exampleVi: 'Số lượng người sở hữu ô tô tăng đáng kể.',
    photo: 'photo-1551288049-bebda4e38f71',
  },
  {
    word: 'fluctuate',
    phonetic: '/ˈflʌk.tʃu.eɪt/',
    meaning: { vi: 'dao động, lên xuống thất thường', en: 'to rise and fall irregularly' },
    pos: POS.verb,
    example: 'Prices fluctuated sharply during the period.',
    exampleVi: 'Giá dao động mạnh trong giai đoạn đó.',
    photo: 'photo-1611974789855-9c2a0a7236a3',
  },
  {
    word: 'substantial',
    phonetic: '/səbˈstæn.ʃəl/',
    meaning: { vi: 'lớn, đáng kể', en: 'large in amount or degree' },
    pos: POS.adj,
    example: 'A substantial number of students chose science.',
    exampleVi: 'Một số lượng lớn sinh viên chọn ngành khoa học.',
    photo: 'photo-1454165833767-1a3b8a1a1a1a',
  },
  {
    word: 'proportion',
    phonetic: '/prəˈpɔːr.ʃən/',
    meaning: { vi: 'tỉ lệ', en: 'a part of a whole, seen against the total' },
    pos: POS.noun,
    example: 'The proportion of remote workers doubled.',
    exampleVi: 'Tỉ lệ người làm việc từ xa tăng gấp đôi.',
    photo: 'photo-1543286386-713bdd548da4',
  },
  {
    word: 'consequently',
    phonetic: '/ˈkɒn.sɪ.kwənt.li/',
    meaning: { vi: 'do đó, kết quả là', en: 'as a result of what came before' },
    pos: POS.adv,
    example: 'Fuel costs rose; consequently, ticket prices went up.',
    exampleVi: 'Giá nhiên liệu tăng, do đó giá vé cũng tăng theo.',
    photo: 'photo-1450101499163-c8848c66ca85',
  },
  {
    word: 'hypothesis',
    phonetic: '/haɪˈpɒθ.ə.sɪs/',
    meaning: { vi: 'giả thuyết', en: 'an idea put forward to be tested' },
    pos: POS.noun,
    example: 'The data supports our original hypothesis.',
    exampleVi: 'Dữ liệu ủng hộ giả thuyết ban đầu của chúng tôi.',
    photo: 'photo-1532094349884-543bc11b234d',
  },
  {
    word: 'evaluate',
    phonetic: '/ɪˈvæl.ju.eɪt/',
    meaning: { vi: 'đánh giá', en: 'to judge how good or useful something is' },
    pos: POS.verb,
    example: 'Researchers evaluate the results carefully.',
    exampleVi: 'Các nhà nghiên cứu đánh giá kết quả một cách cẩn thận.',
    photo: 'photo-1516321318423-f06f85e504b3',
  },
];

const INTERVIEW: CardSeed[] = [
  {
    word: 'strength',
    phonetic: '/streŋθ/',
    meaning: { vi: 'điểm mạnh', en: 'something you are good at' },
    pos: POS.noun,
    example: 'My greatest strength is attention to detail.',
    exampleVi: 'Điểm mạnh nhất của tôi là sự tỉ mỉ.',
    photo: 'photo-1526506118085-60ce8714f8c5',
  },
  {
    word: 'qualification',
    phonetic: '/ˌkwɒl.ɪ.fɪˈkeɪ.ʃən/',
    meaning: { vi: 'bằng cấp, trình độ chuyên môn', en: 'training or a degree that fits you for a job' },
    pos: POS.noun,
    example: 'The role requires a relevant qualification.',
    exampleVi: 'Vị trí này đòi hỏi bằng cấp liên quan.',
    photo: 'photo-1523050854058-8df90110c9f1',
  },
  {
    word: 'responsibility',
    phonetic: '/rɪˌspɒn.sɪˈbɪl.ə.ti/',
    meaning: { vi: 'trách nhiệm', en: 'a duty you are expected to carry out' },
    pos: POS.noun,
    example: 'My main responsibility was managing the budget.',
    exampleVi: 'Trách nhiệm chính của tôi là quản lý ngân sách.',
    photo: 'photo-1454165804606-c3d57bc86b40',
  },
  {
    word: 'achievement',
    phonetic: '/əˈtʃiːv.mənt/',
    meaning: { vi: 'thành tựu', en: 'something done successfully after effort' },
    pos: POS.noun,
    example: 'Launching the product was my proudest achievement.',
    exampleVi: 'Ra mắt sản phẩm là thành tựu tôi tự hào nhất.',
    photo: 'photo-1552664730-d307ca884978',
  },
  {
    word: 'adaptable',
    phonetic: '/əˈdæp.tə.bəl/',
    meaning: { vi: 'dễ thích nghi', en: 'able to change to suit new conditions' },
    pos: POS.adj,
    example: 'You need to be adaptable in a start-up.',
    exampleVi: 'Bạn cần dễ thích nghi khi làm ở công ty khởi nghiệp.',
    photo: 'photo-1517245386807-bb43f82c33c4',
  },
];

const TRAVEL: CardSeed[] = [
  {
    word: 'itinerary',
    phonetic: '/aɪˈtɪn.ər.er.i/',
    meaning: { vi: 'lịch trình chuyến đi', en: 'a plan of a journey, stop by stop' },
    pos: POS.noun,
    example: 'Our itinerary includes three cities in five days.',
    exampleVi: 'Lịch trình của chúng tôi gồm ba thành phố trong năm ngày.',
    photo: 'photo-1488646953014-85cb44e25828',
  },
  {
    word: 'boarding pass',
    phonetic: '/ˈbɔːr.dɪŋ pæs/',
    meaning: { vi: 'thẻ lên máy bay', en: 'the card that lets you board a plane' },
    pos: POS.noun,
    example: 'Please have your boarding pass ready.',
    exampleVi: 'Vui lòng chuẩn bị sẵn thẻ lên máy bay.',
    photo: 'photo-1436491865332-7a61a109cc05',
  },
  {
    word: 'accommodation',
    phonetic: '/əˌkɒm.əˈdeɪ.ʃən/',
    meaning: { vi: 'chỗ ở', en: 'a place to stay, such as a hotel room' },
    pos: POS.noun,
    example: 'Accommodation near the beach is expensive.',
    exampleVi: 'Chỗ ở gần biển khá đắt.',
    photo: 'photo-1566073771259-6a8506099945',
  },
  {
    word: 'delay',
    phonetic: '/dɪˈleɪ/',
    meaning: { vi: 'sự trì hoãn; trì hoãn', en: 'a period of waiting beyond the planned time' },
    pos: POS.nounVerb,
    example: 'The flight was delayed by two hours.',
    exampleVi: 'Chuyến bay bị hoãn hai tiếng.',
    photo: 'photo-1542296332-2e4473faf563',
  },
  {
    word: 'landmark',
    phonetic: '/ˈlænd.mɑːrk/',
    meaning: { vi: 'địa danh nổi tiếng', en: 'a well-known building or feature you navigate by' },
    pos: POS.noun,
    example: 'The bridge is the city’s best-known landmark.',
    exampleVi: 'Cây cầu là địa danh nổi tiếng nhất thành phố.',
    photo: 'photo-1502602898657-3e91760cbb34',
  },
];

interface DeckSeed {
  id: string;
  title: L10n;
  description: L10n;
  topicId: string;
  level: Level;
  cover: string;
  learned: number;
  cards: CardSeed[];
}

const DECK_SEEDS: DeckSeed[] = [
  {
    id: 'deck-toeic-600',
    title: { vi: 'TOEIC 600 — Chủ đề Văn phòng', en: 'TOEIC 600 — Office Topics' },
    description: {
      vi: 'Từ vựng thường gặp trong email, hợp đồng và họp hành.',
      en: 'Words that keep coming up in emails, contracts and meetings.',
    },
    topicId: 'tp-toeic',
    level: 'INTERMEDIATE',
    cover: 'photo-1497032628192-86f99bcd76bc',
    learned: 3,
    cards: OFFICE,
  },
  {
    id: 'deck-ielts-food',
    title: { vi: 'IELTS — Chủ đề Ẩm thực', en: 'IELTS — Food and Cooking' },
    description: {
      vi: 'Từ vựng mô tả món ăn và thói quen ăn uống cho phần Speaking.',
      en: 'Vocabulary for describing dishes and eating habits in the Speaking test.',
    },
    topicId: 'tp-ielts',
    level: 'INTERMEDIATE',
    cover: 'photo-1504674900247-0877df9cc836',
    learned: 4,
    cards: FOOD,
  },
  {
    id: 'deck-basics',
    title: { vi: 'Nền tảng — 500 từ đầu tiên', en: 'Foundation — Your First 500 Words' },
    description: {
      vi: 'Những từ cơ bản nhất dùng hằng ngày, hợp cho người mới bắt đầu.',
      en: 'The most common everyday words, made for complete beginners.',
    },
    topicId: 'tp-foundation',
    level: 'BEGINNER',
    cover: 'photo-1503676260728-1c00da094a0b',
    learned: 0,
    cards: BASICS,
  },
  {
    id: 'deck-ielts-academic',
    title: { vi: 'IELTS — Từ vựng học thuật', en: 'IELTS — Academic Vocabulary' },
    description: {
      vi: 'Từ mô tả xu hướng và số liệu, dùng nhiều trong Writing Task 1.',
      en: 'Words for describing trends and figures, heavily used in Writing Task 1.',
    },
    topicId: 'tp-ielts',
    level: 'ADVANCED',
    cover: 'photo-1454165804606-c3d57bc86b40',
    learned: 0,
    cards: ACADEMIC,
  },
  {
    id: 'deck-interview',
    title: { vi: 'Công việc — Phỏng vấn xin việc', en: 'Work — Job Interviews' },
    description: {
      vi: 'Từ và cụm từ cần thiết để trả lời phỏng vấn tự tin.',
      en: 'The words and phrases you need to answer interview questions with confidence.',
    },
    topicId: 'tp-work',
    level: 'INTERMEDIATE',
    cover: 'photo-1521737711867-e3b97375f902',
    learned: 2,
    cards: INTERVIEW,
  },
  {
    id: 'deck-travel',
    title: { vi: 'Du lịch — Sân bay và khách sạn', en: 'Travel — Airports and Hotels' },
    description: {
      vi: 'Từ vựng cần khi đặt phòng, làm thủ tục bay và hỏi đường.',
      en: 'Vocabulary for booking a room, checking in for a flight and asking directions.',
    },
    topicId: 'tp-travel',
    level: 'BEGINNER',
    cover: 'photo-1436491865332-7a61a109cc05',
    learned: 0,
    cards: TRAVEL,
  },
];

export const decks: DeckDetail[] = DECK_SEEDS.map((seed) => {
  const cards = buildCards(seed.id, seed.cards);
  const total = cards.length;
  const learned = Math.min(seed.learned, total);

  return {
    id: seed.id,
    title: seed.title,
    description: seed.description,
    coverImageUrl: photo(seed.cover),
    topicId: seed.topicId,
    topicName: topicName(seed.topicId),
    level: seed.level,
    totalCards: total,
    learnedCards: learned,
    progressPercent: total === 0 ? 0 : Math.round((learned / total) * 100),
    status:
      learned === 0 ? 'NOT_STARTED' : learned >= total ? 'COMPLETED' : 'IN_PROGRESS',
    cards,
  };
});

export function findDeck(id: string): DeckDetail | undefined {
  return decks.find((d) => d.id === id);
}
