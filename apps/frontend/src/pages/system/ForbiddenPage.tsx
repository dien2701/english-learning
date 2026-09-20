import React from 'react';
import SystemMessage from './SystemMessage';

const ForbiddenPage: React.FC = () => (
  <SystemMessage
    code="403"
    icon="lock"
    titleKey="system.forbiddenTitle"
    descriptionKey="system.forbiddenBody"
  />
);

export default ForbiddenPage;
