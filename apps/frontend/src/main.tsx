import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
// index.css nạp reset của Ant Design vào lớp `antd` rồi mới tới Tailwind.
import "./index.css";
import "./i18n/config";
import App from "./App.tsx";

// Data router: cần cho useBlocker (xác nhận rời bài đang làm).
const router = createBrowserRouter([{ path: "*", element: <App /> }]);

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <RouterProvider router={router} />
  </StrictMode>
);
