import React from 'react';
import SystemMessage from './SystemMessage';

const NotFoundPage: React.FC = () => (
  <SystemMessage
    code="404"
    icon="search_off"
    titleKey="system.notFoundTitle"
    descriptionKey="system.notFoundBody"
  />
);

export default NotFoundPage;
