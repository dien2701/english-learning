import React from 'react';
import { Input } from 'antd';
import { useTranslation } from 'react-i18next';

import type { AnswerSubmission, Question } from '../../types/practice';

interface QuestionListProps {
  questions: Question[];
  answers: Record<string, AnswerSubmission>;
  onAnswer: (questionId: string, answer: AnswerSubmission) => void;
}

/**
 * Danh sách câu hỏi khi đang làm bài.
 *
 * Mỗi câu là một nhóm radio thật (`fieldset` + `legend`) để người dùng
 * bàn phím và trình đọc màn hình hiểu được quan hệ giữa câu hỏi và các
 * phương án.
 */
const QuestionList: React.FC<QuestionListProps> = ({
  questions,
  answers,
  onAnswer,
}) => {
  const { t } = useTranslation();

  return (
  <ol className="flex flex-col gap-4">
    {questions.map((question) => {
      const current = answers[question.id];

      return (
        <li
          key={question.id}
          // id để bài kiểm tra nhảy nhanh tới từng câu bằng liên kết neo.
          id={question.id}
          className="scroll-mt-[220px] rounded-lg border border-hairline bg-surface p-5 shadow-sm"
        >
          {question.kind === 'SINGLE_CHOICE' ? (
            <fieldset>
              <legend className="mb-3 flex gap-2 text-[15px] font-bold text-ink">
                <span className="shrink-0 text-accent">{question.order}.</span>
                <span>{question.text}</span>
              </legend>

              <div className="flex flex-col gap-2">
                {question.options?.map((option) => {
                  const isChosen = current?.optionId === option.id;

                  return (
                    <label
                      key={option.id}
                      className={`flex min-h-[44px] cursor-pointer items-center gap-3 rounded-md border px-3.5 transition-colors duration-200 ${
                        isChosen
                          ? 'border-action bg-accent-subtle'
                          : 'border-hairline hover:bg-surface-hover'
                      }`}
                    >
                      <input
                        type="radio"
                        name={question.id}
                        value={option.id}
                        checked={isChosen}
                        onChange={() =>
                          onAnswer(question.id, {
                            questionId: question.id,
                            optionId: option.id,
                          })
                        }
                        className="h-4 w-4 shrink-0 accent-brand-600"
                      />
                      <span className="text-[14px] text-ink">{option.text}</span>
                    </label>
                  );
                })}
              </div>
            </fieldset>
          ) : (
            <div>
              <label
                htmlFor={question.id}
                className="mb-3 flex gap-2 text-[15px] font-bold text-ink"
              >
                <span className="shrink-0 text-accent">{question.order}.</span>
                <span>{question.text}</span>
              </label>

              <Input
                id={question.id}
                size="large"
                value={current?.text ?? ''}
                onChange={(e) =>
                  onAnswer(question.id, {
                    questionId: question.id,
                    text: e.target.value,
                  })
                }
                placeholder={t('exam.answerPlaceholder')}
                className="max-w-md"
              />
            </div>
          )}
        </li>
      );
    })}
  </ol>
  );
};

export default QuestionList;
