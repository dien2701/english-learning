import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
// index.css nạp reset của Ant Design vào lớp `antd` rồi mới tới Tailwind.
import "./index.css";
import "./i18n/config";
import App from "./App.tsx";
import { MOCK_MODULES, USE_MOCK } from "./shared/api/client";

// Còn module mock thì nạp trước khi render, để request đầu tiên đã có handler.
if (USE_MOCK) {
  const { announceMocks } = await import("./mocks");
  announceMocks(MOCK_MODULES);
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
