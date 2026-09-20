import React from 'react';
import PracticeResultPage from '../practice/PracticeResultPage';

const ListeningResultPage: React.FC = () => (
  <PracticeResultPage
    titleKey="listening.resultTitle"
    backLabelKey="listening.allLessons"
    backTo="/listening"
    retryPath={(lessonId) => `/listening/${lessonId}`}
  />
);

export default ListeningResultPage;
