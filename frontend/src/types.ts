export interface User {
  id?: number | string;
  name: string;
  address: string;
  email: string;
  telephone: string;
}

export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number; // current page index (0-based)
}