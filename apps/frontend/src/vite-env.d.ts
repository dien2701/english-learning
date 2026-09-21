/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Tiền tố API còn dùng mock, cách nhau bằng dấu phẩy. Rỗng = gọi backend thật hết. */
  readonly VITE_MOCK_MODULES?: string;
  /** Tiền tố đường dẫn API, mặc định '/api'. */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
