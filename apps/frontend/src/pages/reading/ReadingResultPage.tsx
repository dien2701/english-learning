import React from 'react';
import PracticeResultPage from '../practice/PracticeResultPage';

const ReadingResultPage: React.FC = () => (
  <PracticeResultPage
    titleKey="reading.resultTitle"
    backLabelKey="reading.allLessons"
    backTo="/reading"
    retryPath={(lessonId) => `/reading/${lessonId}`}
  />
);

export default ReadingResultPage;
