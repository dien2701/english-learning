import React from 'react';
import { Pagination as AntPagination } from 'antd';

interface PaginationProps {
  page: number;
  pageSize: number;
  total: number;
  onChange: (page: number) => void;
}

/** Phân trang dùng chung cho các trang danh sách; tự ẩn khi chỉ có một trang. */
const Pagination: React.FC<PaginationProps> = ({ page, pageSize, total, onChange }) => (
  <div className="mt-6 flex justify-center">
    <AntPagination
      current={page}
      pageSize={pageSize}
      total={total}
      onChange={onChange}
      showSizeChanger={false}
      hideOnSinglePage
    />
  </div>
);

export default Pagination;
