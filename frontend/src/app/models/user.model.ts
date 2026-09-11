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

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  roles: string[];
  avatar?: string;
}

export interface ProfileUpdateRequest {
  name: string;
  email: string;
  avatar?: string;
}
