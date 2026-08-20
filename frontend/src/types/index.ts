export interface User {
  id: string;
  email: string;
  name: string;
  role: 'CITIZEN' | 'VOLUNTEER' | 'ADMIN';
}

export interface UserProfile {
  userId: string;
  name: string;
  phone: string;
  medicalInfo: string;
}

export interface EmergencyContact {
  id: string;
  userId: string;
  name: string;
  phone: string;
  relationship: string;
  isPrimary: boolean;
  createdAt: string;
}

export interface VolunteerResponder {
  volunteerId: string;
  volunteerName: string;
  volunteerPhone: string;
  responseType: 'ACCEPTED' | 'DECLINED';
  estimatedArrival?: string;
  message?: string;
  createdAt: string;
}

export interface SOSEvent {
  id: string;
  userId: string;
  userName?: string;
  userPhone?: string;
  type: 'Health' | 'Fire' | 'Threat/Theft' | 'Other';
  description?: string;
  latitude: number;
  longitude: number;
  address?: string;
  status: 'ACTIVE' | 'NOTIFYING' | 'ACCEPTED' | 'RESPONDING' | 'ARRIVED' | 'RESOLVED' | 'CANCELLED' | 'EXPIRED';
  createdAt: string;
  resolvedAt?: string;
  responders?: VolunteerResponder[];
}

export interface VolunteerProfile {
  userId: string;
  isAvailable: boolean;
  maxRangeMeters: number;
  skills: string[];
  latitude: number;
  longitude: number;
}

export interface NotificationItem {
  id: string;
  userId: string;
  title: string;
  message: string;
  type: string;
  referenceId?: string;
  isRead: boolean;
  createdAt: string;
}

export interface AdminStats {
  totalUsers: number;
  totalCitizens: number;
  totalVolunteers: number;
  availableVolunteers: number;
  totalSOSRequests: number;
  activeSOSRequests: number;
  resolvedSOSRequests: number;
  cancelledSOSRequests: number;
  totalResponses: number;
  averageResponseTimeMinutes: number;
  emergencyTypeDistribution: Record<string, number>;
}