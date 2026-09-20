import React from 'react';
import SystemMessage from './SystemMessage';

const ServerErrorPage: React.FC = () => (
  <SystemMessage
    code="500"
    icon="cloud_off"
    titleKey="system.serverErrorTitle"
    descriptionKey="system.serverErrorBody"
  />
);

export default ServerErrorPage;
