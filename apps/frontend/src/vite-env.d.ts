/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 'false' để gọi backend thật; mọi giá trị khác đều bật mock. */
  readonly VITE_USE_MOCK?: string;
  /** Tiền tố đường dẫn API, mặc định '/api'. */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
