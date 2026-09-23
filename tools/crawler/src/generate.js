import { generateExercises } from './generateExercises.js';
import { generateLessons } from './generateLessons.js';

/**
 * Sinh de (13.3): doc words.json/topics.json da co (13.2) de tao bai tap tu vung
 * (khong goi AI), roi goi Gemini tao de Viet, bai Noi, bai Nghe theo tung chu de.
 */
async function main() {
  const startedAt = Date.now();
  const exercises = await generateExercises();
  console.log(`[exercises] ${exercises.length} de, ${exercises.reduce((s, e) => s + e.questions.length, 0)} cau hoi.`);

  const { writing, speaking, listening } = await generateLessons();

  const elapsedSec = Math.round((Date.now() - startedAt) / 1000);
  console.log(
    `\nXong trong ${elapsedSec}s. exercises=${exercises.length}, writing=${writing.length}, ` +
      `speaking=${speaking.length}, listening=${listening.length}.`,
  );
}

main().catch((err) => {
  console.error('\n[loi]', err.message);
  process.exitCode = 1;
});
