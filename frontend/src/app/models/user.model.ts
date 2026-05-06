export interface AuthResponse {
  accessToken: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  role?: 'CLIENT' | 'SELLER';
}

export interface LoginRequest {
  email: string;
  password: string;
}
