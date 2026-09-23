// 20 chu de co dinh (10 chu de demo cu + 10 chu de moi). Gemini chi duoc gan
// topicSlug cho tu vung trong danh sach nay, khong duoc bia chu de moi.
export const TOPICS = [
  { slug: 'travel', nameVi: 'Du lịch', nameEn: 'Travel' },
  { slug: 'food-drink', nameVi: 'Ăn uống', nameEn: 'Food & Drink' },
  { slug: 'shopping', nameVi: 'Mua sắm', nameEn: 'Shopping' },
  { slug: 'family', nameVi: 'Gia đình', nameEn: 'Family' },
  { slug: 'health', nameVi: 'Sức khoẻ', nameEn: 'Health' },
  { slug: 'workplace', nameVi: 'Công sở', nameEn: 'Workplace' },
  { slug: 'it-tech', nameVi: 'Công nghệ thông tin', nameEn: 'IT & Technology' },
  { slug: 'exam-prep', nameVi: 'Luyện thi IELTS/TOEIC', nameEn: 'IELTS/TOEIC Preparation' },
  { slug: 'academic', nameVi: 'Học thuật', nameEn: 'Academic English' },
  { slug: 'daily-life', nameVi: 'Đời sống hằng ngày', nameEn: 'Daily Life' },
  { slug: 'nature-environment', nameVi: 'Thiên nhiên & môi trường', nameEn: 'Nature & Environment' },
  { slug: 'sports-fitness', nameVi: 'Thể thao & rèn luyện', nameEn: 'Sports & Fitness' },
  { slug: 'entertainment', nameVi: 'Giải trí', nameEn: 'Entertainment' },
  { slug: 'emotions-relationships', nameVi: 'Cảm xúc & các mối quan hệ', nameEn: 'Emotions & Relationships' },
  { slug: 'money-finance', nameVi: 'Tiền bạc & tài chính', nameEn: 'Money & Finance' },
  { slug: 'weather-seasons', nameVi: 'Thời tiết & mùa', nameEn: 'Weather & Seasons' },
  { slug: 'transportation', nameVi: 'Giao thông', nameEn: 'Transportation' },
  { slug: 'housing-home', nameVi: 'Nhà cửa', nameEn: 'Home & Housing' },
  { slug: 'education', nameVi: 'Giáo dục', nameEn: 'Education' },
  { slug: 'society-law', nameVi: 'Xã hội & pháp luật', nameEn: 'Society & Law' },
];

export const TOPIC_SLUGS = new Set(TOPICS.map((t) => t.slug));
