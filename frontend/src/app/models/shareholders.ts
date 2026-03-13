export type ParticipantType = 'permanent' | 'temporary';

export interface Shareholder {
  id?: string;
  name: string;
  email: string;
  participantType: ParticipantType;
  startDate: Date;
  endDate?: Date;
  isActive?: boolean;
  createdAt?: Date;
  updatedAt?: Date;
}

export interface CreateShareholderRequest {
  name: string;
  email: string;
  participantType: ParticipantType;
  startDate: Date;
  endDate?: Date;
}

export interface ShareholderBalance {
  shareholder: Shareholder;
  totalBalance: number;
  owes: number;
  isOwed: number;
}
