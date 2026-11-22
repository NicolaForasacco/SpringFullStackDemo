export interface AuthResponse {
  token: string;
  email: string;
  fullName: string;
  role: string;
}

export interface User {
  email: string;
  fullName: string;
  role: string;
}
